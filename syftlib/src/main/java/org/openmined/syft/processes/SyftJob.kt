package org.openmined.syft.processes

import android.util.Log
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.openmined.syft.Syft
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.datamodels.syft.ReportRequest
import org.openmined.syft.networking.datamodels.syft.ReportResponse
import org.openmined.syft.threading.ProcessSchedulers
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

private const val TAG = "SyftJob"

@ExperimentalUnsignedTypes
class SyftJob(
    private val worker: Syft,
    private val schedulers: ProcessSchedulers,
    val modelName: String,
    val version: String? = null
) {

    var cycleStatus = AtomicReference<CycleStatus>(CycleStatus.APPLY)
    var trainingParamsStatus = AtomicReference<DownloadStatus>(DownloadStatus.NOT_STARTED)

    private lateinit var requestKey: String
    private val plans = ConcurrentHashMap<String, Plan>()
    private val protocols = ConcurrentHashMap<String, Protocol>()
    private val jobStatusProcessor: PublishProcessor<JobStatusMessage> =
            PublishProcessor.create<JobStatusMessage>()
    private val compositeDisposable = CompositeDisposable()

    /**
     * create a worker job
     */
    fun start(): Flowable<JobStatusMessage> {
        //todo all this in syft.kt
        //todo check for connection if doesn't exist establish one
        //todo before calling this function syft should have checked the bandwidth etc requirements
        if (cycleStatus.get() == CycleStatus.APPLY) {
            Log.d(TAG, "job awaiting timer completion to resend the Cycle Request")
            return getStatusProcessor()
        }
        worker.requestCycle(this)
        return jobStatusProcessor.onBackpressureBuffer()
    }

    /**
     * report the results back to PyGrid
     */
    fun report(diff: String) {
        compositeDisposable.add(
            worker.getSignallingClient().report(ReportRequest(worker.workerId, requestKey, diff))
                    .compose(schedulers.applySingleSchedulers())
                    .subscribe { reportResponse: ReportResponse ->
                        Log.i(TAG, reportResponse.status)
                    })
    }

    fun getStatusProcessor(): Flowable<JobStatusMessage> = jobStatusProcessor.onBackpressureBuffer()

    @Synchronized
    fun setRequestKey(responseData: CycleResponseData.CycleAccept) {
        requestKey = responseData.requestKey
        cycleStatus.set(CycleStatus.ACCEPTED)
        jobStatusProcessor.offer(JobStatusMessage.JobCycleAccepted)
    }

    fun downloadData() {
        trainingParamsStatus.set(DownloadStatus.RUNNING)
        plans.forEach { (planId, _) ->
            worker.getDownloader().downloadPlan(
                worker.workerId,
                requestKey,
                planId,
                "torchscript"
            ).compose(schedulers.applySingleSchedulers()).subscribe()
        }
        trainingParamsStatus.set(DownloadStatus.COMPLETE)
    }


    data class JobID(val modelName: String, val version: String? = null) {
        fun matchWithResponse(modelName: String, version: String) =
                if (this.version.isNullOrEmpty())
                    this.modelName == modelName
                else
                    (this.modelName == modelName) && (this.version == version)
    }

    enum class DownloadStatus {
        NOT_STARTED, RUNNING, COMPLETE
    }

    enum class CycleStatus {
        APPLY, REJECT, ACCEPTED
    }

}