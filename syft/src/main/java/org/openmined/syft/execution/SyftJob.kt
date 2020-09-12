package org.openmined.syft.execution

import android.util.Base64
import android.util.Log
import androidx.annotation.VisibleForTesting
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import org.openmined.syft.Syft
import org.openmined.syft.datasource.DIFF_SCRIPT_NAME
import org.openmined.syft.datasource.JobLocalDataSource
import org.openmined.syft.datasource.JobRemoteDataSource
import org.openmined.syft.domain.ContentState
import org.openmined.syft.domain.SyftDataRepository
import org.openmined.syft.domain.DownloadStatus
import org.openmined.syft.domain.JobRepository
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.domain.SyftLogger
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.datamodels.syft.ReportRequest
import org.openmined.syft.networking.datamodels.syft.ReportResponse
import org.openmined.syft.proto.SyftModel
import org.openmined.syft.proto.SyftState
import org.openmined.syft.threading.ProcessSchedulers
import org.pytorch.IValue
import org.pytorch.Tensor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

private const val TAG = "SyftJob"

/**
 * @param modelName : The model being trained or used in inference
 * @param version : The version of the model with name modelName
 * @property worker : The syft worker handling this job
 * @property config : The configuration class for schedulers and clients
 * @property jobRepository : The repository dealing data downloading and file writing of job
 */
@ExperimentalUnsignedTypes
class SyftJob internal constructor(
    modelName: String,
    version: String? = null,
    private val worker: Syft,
    private val config: SyftConfiguration,
    private val jobRepository: JobRepository
) : Disposable {

    companion object {

        /**
         * Creates a new Syft Job
         *
         * @param modelName : The model being trained or used in inference
         * @param version : The version of the model with name modelName
         * @param worker : The syft worker handling this job
         * @param config : The configuration class for schedulers and clients
         * @sample org.openmined.syft.Syft.newJob
         */
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
                JobRepository(
                    JobLocalDataSource(),
                    JobRemoteDataSource(config.getDownloader())
                )
            )
        }
    }

    val jobId = JobID(modelName, version)
    internal var cycleStatus = AtomicReference(CycleStatus.APPLY)
    internal val requiresSpeedTest = AtomicBoolean(true)
    private val jobStatusProcessor = PublishProcessor.create<JobStatusMessage>()
    private val isDisposed = AtomicBoolean(false)

    private val plans = ConcurrentHashMap<String, Plan>()
    private val protocols = ConcurrentHashMap<String, Protocol>()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val model = SyftModel(modelName, version)

    private val networkDisposable = CompositeDisposable()
    private var statusDisposable: Disposable? = null
    private val computeDisposable = CompositeDisposable()
    private var requestKey = ""


    /**
     * Starts the job by asking syft worker to request for cycle.
     * Initialises Socket connection if not initialised already.
     * @param subscriber (Optional) Contains the methods overridden by the user to be called upon job success/error
     * @see org.openmined.syft.execution.JobStatusSubscriber for available methods
     *
     * ```kotlin
     * job.request()
     * // OR
     * val jobStatusSubscriber = object : JobStatusSubscriber() {
     *      override fun onReady(
     *      model: SyftModel,
     *      plans: ConcurrentHashMap<String, Plan>,
     *      clientConfig: ClientConfig
     *      ) {
     *      }
     *
     *      override fun onRejected(timeout: String) {
     *      }
     *
     *      override fun onError(throwable: Throwable) {
     *      }
     * }
     *
     * job.request(jobStatusSubscriber)
     * ```
     */
    fun request(subscriber: JobStatusSubscriber = JobStatusSubscriber()) {
        if (cycleStatus.get() == CycleStatus.REJECT) {
            Log.d(TAG, "job awaiting timer completion to resend the Cycle Request")
            return
        }
        if (isDisposed.get()) {
            Log.e(TAG, "cannot start a disposed job")
            subscriber.onError(JobErrorThrowable.RunningDisposedJob)
            return
        }
        subscribe(subscriber, config.computeSchedulers)
        worker.executeCycleRequest(this)
    }

    @ExperimentalStdlibApi
    fun train(
        model: SyftModel,
        plans: ConcurrentHashMap<String, Plan>,
        syftDataRepository: SyftDataRepository,
        clientConfig: ClientConfig,
        logger: SyftLogger
    ) {
        var result = -0.0f
        plans["training_plan"]?.let { plan ->
            repeat(clientConfig.properties.maxUpdates) { step ->
                logger.postEpoch(step + 1)
                val batchSize = (clientConfig.planArgs["batch_size"]
                                 ?: error("batch_size doesn't exist")).toInt()
                val batchIValue = IValue.from(
                    Tensor.fromBlob(longArrayOf(batchSize.toLong()), longArrayOf(1))
                )
                val lr = IValue.from(
                    Tensor.fromBlob(
                        floatArrayOf(
                            (clientConfig.planArgs["lr"] ?: error("lr doesn't exist")).toFloat()
                        ),
                        longArrayOf(1)
                    )
                )
                val batchData = syftDataRepository.loadDataBatch(batchSize)
                val modelParams = model.paramArray ?: return
                val paramIValue = IValue.listFrom(*modelParams)
                val output = plan.execute(
                    batchData.first,
                    batchData.second,
                    batchIValue,
                    lr,
                    paramIValue
                )?.toTuple()

                output?.let { outputResult ->
                    val paramSize = model.stateTensorSize!!
                    val beginIndex = outputResult.size - paramSize
                    val updatedParams =
                            outputResult.slice(beginIndex until outputResult.size)
                    model.updateModel(updatedParams)
                    result = outputResult[0].toTensor().dataAsFloatArray.last()
                }
                logger.postState(ContentState.Training)
                logger.postData(result)
            }
            logger.postLog("Training done!\n reporting diff")
            val diff = createDiff()
            report(diff)
            logger.postLog("reported the model to PyGrid")
        }
    }

    /**
     * This method can be called when the user needs to attach a listener to the job but do not wish to start it
     * @param subscriber (Optional) Contains the methods overridden by the user to be called upon job success/error
     * @see org.openmined.syft.execution.JobStatusSubscriber for available methods
     * @sample org.openmined.syft.Syft.newJob
     */
    fun subscribe(
        subscriber: JobStatusSubscriber,
        schedulers: ProcessSchedulers
    ) {
        statusDisposable = jobStatusProcessor.onBackpressureBuffer()
                .subscribeOn(schedulers.calleeThreadScheduler)
                .subscribe(
                    { message ->
                        computeDisposable.add(Completable.create {
                            subscriber.onJobStatusMessage(message)
                            it.onComplete()
                        }.subscribeOn(schedulers.computeThreadScheduler).subscribe({}, {
                            subscriber.onError(it)
                        }))
                    },
                    { error ->
                        subscriber.onError(error)
                        computeDisposable.clear()
                        computeDisposable.dispose()
                    },
                    { subscriber.onComplete() }
                )

    }

    /**
     * This method is called by [Syft Worker][org.openmined.syft.Syft] on being accepted by PyGrid into a cycle
     * @param responseData The training parameters and requestKey returned by PyGrid
     */
    @Synchronized
    internal fun cycleAccepted(responseData: CycleResponseData.CycleAccept) {
        Log.d(TAG, "setting Request Key")
        responseData.plans.forEach { (planName, planId) ->
            plans[planName] = Plan(this, planId, planName)
        }
        responseData.protocols.forEach { (protocolName, protocolId) ->
            protocols[protocolName] = Protocol(protocolId)
        }
        requestKey = responseData.requestKey
        model.pyGridModelId = responseData.modelId
        cycleStatus.set(CycleStatus.ACCEPTED)
    }

    /**
     * This method is called by [Syft Worker][org.openmined.syft.Syft] on being rejected by PyGrid into a cycle
     * @param responseData The timeout returned by PyGrid after which the worker should retry
     */
    internal fun cycleRejected(responseData: CycleResponseData.CycleReject) {
        cycleStatus.set(CycleStatus.REJECT)
        jobStatusProcessor.offer(JobStatusMessage.JobCycleRejected(responseData.timeout))
    }

    /**
     * Downloads all the plans, protocols and the model weights from PyGrid
     * @param workerId The unique id assigned to the syft worker by PyGrid
     * @param responseData contains the cycle accept request key and training parameters
     */
    internal fun downloadData(
        workerId: String,
        responseData: CycleResponseData.CycleAccept
    ) {
        if (cycleStatus.get() != CycleStatus.ACCEPTED) {
            publishError(JobErrorThrowable.CycleNotAccepted("Cycle not accepted. Download cannot start"))
            return
        }
        if (jobRepository.status == DownloadStatus.NOT_STARTED) {
            jobRepository.downloadData(
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
     * Create a diff between the model parameters downloaded from the PyGrid with the current state of model parameters
     * The diff is sent to [report] for sending it to PyGrid
     */
    fun createDiff(): SyftState {
        val modulePath = jobRepository.persistToLocalStorage(
            jobRepository.getDiffScript(config),
            config.filesDir.toString(),
            DIFF_SCRIPT_NAME
        )
        val oldState =
                SyftState.loadSyftState("${config.filesDir}/models/${model.pyGridModelId}.pb")
        return model.createDiff(oldState, modulePath)
    }

    /**
     * Once training is finished submit the new model weights to PyGrid to complete the cycle
     * @param diff the difference of the new and old model weights serialised into [State][org.openmined.syft.proto.SyftState]
     */
    fun report(diff: SyftState) {
        val workerId = worker.getSyftWorkerId()
        if (throwErrorIfNetworkInvalid() ||
            throwErrorIfBatteryInvalid()
        ) return

        if (!workerId.isNullOrEmpty() && requestKey.isNotEmpty())
            networkDisposable.add(
                config.getSignallingClient()
                        .report(
                            ReportRequest(
                                workerId,
                                requestKey,
                                Base64.encodeToString(
                                    diff.serialize().toByteArray(),
                                    Base64.DEFAULT
                                )
                            )
                        )
                        .compose(config.networkingSchedulers.applySingleSchedulers())
                        .subscribe { reportResponse: ReportResponse ->
                            if (reportResponse.error != null)
                                publishError(JobErrorThrowable.NetworkResponseFailure(reportResponse.error))
                            if (reportResponse.status != null) {
                                Log.d(TAG, "report status ${reportResponse.status}")
                                jobStatusProcessor.onComplete()
                            }
                        })
    }

    /**
     * Throw an error when network constraints fail.
     * @param publish when false the error is thrown for the error handler otherwise caught and published on the status processor
     * @return true if error is thrown otherwise false
     */
    internal fun throwErrorIfNetworkInvalid(publish: Boolean = true): Boolean {
        val validity = worker.isNetworkValid()
        if (publish && !validity)
            publishError(JobErrorThrowable.NetworkConstraintsFailure)
        else if (!validity)
            throwError(JobErrorThrowable.NetworkConstraintsFailure)
        return !validity
    }

    /**
     * Throw an error when battery constraints fail
     * @param publish when false the error is thrown for the error handler otherwise caught and published on the status processor
     * @return true if error is thrown otherwise false
     */
    internal fun throwErrorIfBatteryInvalid(publish: Boolean = true): Boolean {
        val validity = worker.isBatteryValid()
        if (publish && !validity)
            publishError(JobErrorThrowable.BatteryConstraintsFailure)
        else if (!validity)
            throwError(JobErrorThrowable.BatteryConstraintsFailure)
        return !validity
    }

    /**
     * Notify all the listeners about the error and dispose the job
     */
    internal fun publishError(throwable: JobErrorThrowable) {
        jobStatusProcessor.onError(throwable)
        networkDisposable.clear()
        isDisposed.set(true)
    }

    /**
     * Throw the error to be caught by error handlers
     */
    private fun throwError(throwable: JobErrorThrowable) {
        networkDisposable.clear()
        isDisposed.set(true)
        throw throwable
    }

    /**
     * Identifies if the job is already disposed
     */
    override fun isDisposed() = isDisposed.get()

    /**
     * Dispose the job. Once disposed, a job cannot be resumed again.
     */
    override fun dispose() {
        if (!isDisposed()) {
            jobStatusProcessor.onComplete()
            networkDisposable.clear()
            isDisposed.set(true)
            Log.d(TAG, "job $jobId disposed")
        } else
            Log.d(TAG, "job $jobId already disposed")
    }

    /**
     * A uniquer identifier class for the job
     * @property modelName The name of the model used in the job while querying PyGrid
     * @property version The model version in PyGrid
     */
    data class JobID(val modelName: String, val version: String? = null) {
        /**
         * Check if two [JobID] are same. Matches both model names and version if [version] is not null for param and current jobId.
         * @param modelName the modelName of the jobId which has to be compared with the current object
         * @param version the version of the jobID which ahs to be compared with the current jobId
         * @return true if JobId match
         * @return false otherwise
         */
        fun matchWithResponse(modelName: String, version: String? = null) =
                if (version.isNullOrEmpty() || this.version.isNullOrEmpty())
                    this.modelName == modelName
                else
                    (this.modelName == modelName) && (this.version == version)
    }

    internal enum class CycleStatus {
        APPLY, REJECT, ACCEPTED
    }
}
