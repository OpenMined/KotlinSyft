package org.openmined.syft.execution

import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import org.openmined.syft.Syft
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.datamodels.syft.ReportRequest
import org.openmined.syft.networking.datamodels.syft.ReportResponse
import org.openmined.syft.proto.State
import org.openmined.syft.proto.SyftModel
import org.openmined.syft.threading.ProcessSchedulers
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

private const val TAG = "SyftJob"

/**
 * @param worker : The syft worker handling this job
 * @param config : The configuration class for schedulers and clients
 * @param modelName : The model being trained or used in inference
 * @param version : The version of the model with name modelName
 */
@ExperimentalUnsignedTypes
class SyftJob internal constructor(
    modelName: String,
    version: String? = null,
    private val worker: Syft,
    private val config: SyftConfiguration,
    private val jobDownloader: JobDownloader
) : Disposable {

    companion object {
        fun create(
            modelName: String,
            version: String? = null,
            worker: Syft,
            config: SyftConfiguration
        ): SyftJob {
            return SyftJob(
                modelName,
                version,
                worker,
                config,
                JobDownloader()
            )
        }
    }

    val jobId = JobID(modelName, version)
    private val jobStatusProcessor = PublishProcessor.create<JobStatusMessage>()
    private val isDisposed = AtomicBoolean(false)
    private var cycleStatus = AtomicReference(CycleStatus.APPLY)

    private val plans = ConcurrentHashMap<String, Plan>()
    private val protocols = ConcurrentHashMap<String, Protocol>()
    private val model = SyftModel(modelName, version)
    private val networkDisposable = CompositeDisposable()
    private val statusDisposable = CompositeDisposable()
    private var requestKey = ""

    /**
     * create a worker job
     */
    fun start(subscriber: JobStatusSubscriber = JobStatusSubscriber()) {
        if (cycleStatus.get() == CycleStatus.REJECT) {
            Log.d(TAG, "job awaiting timer completion to resend the Cycle Request")
            return
        }
        if (isDisposed.get()) {
            Log.e(TAG, "cannot start a disposed job")
            subscriber.onError(IllegalThreadStateException("Job has already been disposed"))
            return
        }
        subscribe(subscriber, config.computeSchedulers)
        worker.executeCycleRequest(this)
    }

    fun subscribe(
        subscriber: JobStatusSubscriber,
        schedulers: ProcessSchedulers
    ) {
        statusDisposable.add(
            jobStatusProcessor.onBackpressureBuffer()
                    .compose(schedulers.applyFlowableSchedulers())
                    .subscribe(
                        { message -> subscriber.onJobStatusMessage(message) },
                        { error -> subscriber.onError(error) },
                        { subscriber.onComplete() }
                    )
        )
    }

    @Synchronized
    fun cycleAccepted(responseData: CycleResponseData.CycleAccept) {
        Log.d(TAG, "setting Request Key")
        responseData.plans.forEach { (_, planId) -> plans[planId] = Plan(this, planId) }
        responseData.protocols.forEach { (_, protocolId) ->
            protocols[protocolId] = Protocol(protocolId)
        }
        requestKey = responseData.requestKey
        model.pyGridModelId = responseData.modelId
        cycleStatus.set(CycleStatus.ACCEPTED)
    }

    fun cycleRejected(responseData: CycleResponseData.CycleReject) {
        cycleStatus.set(CycleStatus.REJECT)
        jobStatusProcessor.offer(JobStatusMessage.JobCycleRejected(responseData.timeout))
    }

    fun downloadData(
        workerId: String,
        responseData: CycleResponseData.CycleAccept
    ) {
        if (cycleStatus.get() != CycleStatus.ACCEPTED) {
            throwError(IllegalStateException("Cycle not accepted. Download cannot start"))
            return
        }
        if (jobDownloader.status == DownloadStatus.NOT_STARTED) {
            jobDownloader.downloadData(
                workerId,
                config,
                responseData.requestKey,
                networkDisposable,
                jobStatusProcessor,
                responseData.clientConfig,
                plans,
                model,
                protocols
            )
        }
    }

    /**
     * report the results back to PyGrid
     */
    fun report(diff: State) {
        val workerId = worker.getSyftWorkerId()
        if (throwErrorIfNetworkInvalid() ||
            throwErrorIfDeviceActive() ||
            throwErrorIfBatteryInvalid()
        ) return

        if (!workerId.isNullOrEmpty() && requestKey.isNotEmpty())
            networkDisposable.add(
                config.getSignallingClient()
                        .report(
                            ReportRequest(
                                workerId,
                                requestKey,
                                diff.serialize().toString()
                            )
                        )
                        .compose(config.networkingSchedulers.applySingleSchedulers())
                        .subscribe { reportResponse: ReportResponse ->
                            Log.i(TAG, reportResponse.status)
                        })
    }

    fun throwErrorIfNetworkInvalid(): Boolean {
        if (worker.jobErrorIfNetworkInvalid(this)) {
            //todo save model to a file here
            return true
        }
        return false
    }

    fun throwErrorIfBatteryInvalid(): Boolean {
        if (worker.jobErrorIfBatteryInvalid(this)) {
            //todo save model to a file here
            return true
        }
        return false
    }

    fun throwErrorIfDeviceActive(): Boolean {
        if (worker.jobErrorIfDeviceActive(this)) {
            //todo save model to a file here
            return true
        }
        return false
    }

    fun throwError(throwable: Throwable) {
        jobStatusProcessor.onError(throwable)
        networkDisposable.clear()
        isDisposed.set(true)
    }

    override fun isDisposed() = isDisposed.get()

    override fun dispose() {
        if (!isDisposed()) {
            jobStatusProcessor.onComplete()
            networkDisposable.clear()
            isDisposed.set(true)
            Log.d(TAG, "job $jobId disposed")
        } else
            Log.d(TAG, "job $jobId already disposed")
    }


    data class JobID(val modelName: String, val version: String? = null) {
        fun matchWithResponse(modelName: String, version: String? = null) =
                if (version.isNullOrEmpty() || this.version.isNullOrEmpty())
                    this.modelName == modelName
                else
                    (this.modelName == modelName) && (this.version == version)
    }

    enum class CycleStatus {
        APPLY, REJECT, ACCEPTED
    }
}
