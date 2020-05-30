package org.openmined.syft.execution

import android.util.Log
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.openmined.syft.Syft
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.datamodels.syft.ReportRequest
import org.openmined.syft.networking.datamodels.syft.ReportResponse
import org.openmined.syft.proto.State
import org.openmined.syft.proto.SyftModel
import org.openmined.syft.threading.ProcessSchedulers
import org.openmined.syft.utilities.FileWriter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

private const val TAG = "SyftJob"

/**
 * @param worker : The syft worker handling this job
 * @param config : The configuration class for schedulers and clients
 * @param modelName : The model being trained or used in inference
 * @param version : The version of the model with name modelName
 */
@ExperimentalUnsignedTypes
class SyftJob(
    modelName: String,
    version: String? = null,
    private val worker: Syft,
    private val jobStatusProcessor: PublishProcessor<JobStatusMessage>,
    private val config: SyftConfiguration
) {

    val jobId = JobID(modelName, version)

    private var cycleStatus = AtomicReference(CycleStatus.APPLY)
    private var trainingParamsStatus = AtomicReference(DownloadStatus.NOT_STARTED)
    private var requestKey: String? = null
    private var clientConfig: ClientConfig? = null

    private val plans = ConcurrentHashMap<String, Plan>()
    private val protocols = ConcurrentHashMap<String, Protocol>()
    private val model = SyftModel(modelName, version)
    private val compositeDisposable = CompositeDisposable()

    /**
     * create a worker job
     */
    fun start(subscriber: JobStatusSubscriber = JobStatusSubscriber()) {
        if (cycleStatus.get() == CycleStatus.REJECT) {
            Log.d(TAG, "job awaiting timer completion to resend the Cycle Request")
            return
        }
        worker.executeCycleRequest(this)
        subscribe(subscriber, config.computeSchedulers)
    }

    fun subscribe(
        subscriber: JobStatusSubscriber,
        schedulers: ProcessSchedulers
    ) {
        compositeDisposable.add(
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
    fun setJobArguments(responseData: CycleResponseData.CycleAccept) {
        Log.d(TAG, "setting Request Key")
        requestKey = responseData.requestKey
        clientConfig = responseData.clientConfig
        responseData.plans.forEach { (_, planId) -> plans[planId] = Plan(planId) }
        responseData.protocols.forEach { (_, protocolId) ->
            protocols[protocolId] = Protocol(protocolId)
        }
        model.pyGridModelId = responseData.modelId
        cycleStatus.set(CycleStatus.ACCEPTED)
    }

    fun cycleRejected(responseData: CycleResponseData.CycleReject) {
        cycleStatus.set(CycleStatus.REJECT)
        jobStatusProcessor.offer(JobStatusMessage.JobCycleRejected(responseData.timeout))
    }

    //todo before downloading check for wifi connection again
    fun downloadData(workerId: String) {
        if (trainingParamsStatus.get() != DownloadStatus.NOT_STARTED) {
            Log.d(TAG, "download already running")
            return
        }
        Log.d(TAG, "beginning download")
        trainingParamsStatus.set(DownloadStatus.RUNNING)

        requestKey?.let { request ->

            compositeDisposable.add(Single.zip(
                getDownloadables(
                    workerId,
                    request
                )
            ) { successMessages ->
                successMessages.joinToString(
                    ",",
                    prefix = "files ",
                    postfix = " downloaded successfully"
                )
            }
                    .compose(config.networkingSchedulers.applySingleSchedulers())
                    .subscribe(
                        { successMsg: String ->
                            Log.d(TAG, successMsg)
                            trainingParamsStatus.set(DownloadStatus.COMPLETE)
                            jobStatusProcessor.offer(
                                JobStatusMessage.JobReady(
                                    model,
                                    plans,
                                    clientConfig
                                )
                            )
                        },
                        { e -> jobStatusProcessor.onError(e) }
                    )
            )
        } ?: throw IllegalStateException("request Key has not been set")
    }


    /**
     * report the results back to PyGrid
     */
    fun report(diff: State) {
        val requestKey = requestKey
        val workerId = worker.getSyftWorkerId()
        if (requestKey != null && workerId != null)
            compositeDisposable.add(
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

    private fun getDownloadables(workerId: String, request: String): List<Single<String>> {
        val downloadList = mutableListOf<Single<String>>()
        plans.forEach { (_, plan) ->
            downloadList.add(
                planDownloader(
                    workerId,
                    request,
                    "${config.filesDir}/plans",
                    plan
                )
            )
        }
        protocols.forEach { (protocolId, protocol) ->
            protocol.protocolFileLocation = "${config.filesDir}/protocols"
            downloadList.add(
                protocolDownloader(
                    workerId,
                    request,
                    protocol.protocolFileLocation,
                    protocolId
                )
            )
        }

        model.pyGridModelId?.let {
            downloadList.add(modelDownloader(workerId, request, it))
        } ?: throw IllegalStateException("model id has not been set")

        return downloadList
    }

    //We might want to make these public if needed later
    private fun modelDownloader(
        workerId: String,
        requestKey: String,
        modelId: String
    ): Single<String> {
        return config.getDownloader()
                .downloadModel(workerId, requestKey, modelId)
                .flatMap { response ->
                    FileWriter("${config.filesDir}/models", "$modelId.pb")
                            .writeFromNetwork(response.body())
                }.flatMap { modelFile ->
                    Single.create<String> { emitter ->
                        model.loadModelState(modelFile)
                        emitter.onSuccess(modelFile)
                    }
                }
                .compose(config.networkingSchedulers.applySingleSchedulers())
    }

    private fun planDownloader(
        workerId: String,
        requestKey: String,
        destinationDir: String,
        plan: Plan
    ): Single<String> {
        return config.getDownloader().downloadPlan(
                workerId,
                requestKey,
                plan.planId,
                "torchscript"
            )
                    .flatMap { response ->
                        FileWriter(destinationDir, plan.planId + ".pb")
                                .writeFromNetwork(response.body())
                    }.flatMap { filepath ->
                        Single.create<String> { emitter ->
                            plan.generateScriptModule(destinationDir, filepath)
                            emitter.onSuccess(filepath)
                        }
                    }
                .compose(config.networkingSchedulers.applySingleSchedulers())

    }

    private fun protocolDownloader(
        workerId: String,
        requestKey: String,
        destinationDir: String,
        protocolId: String
    ): Single<String> {
        return config.getDownloader().downloadProtocol(
                workerId, requestKey, protocolId
            )
                    .flatMap { response ->
                        FileWriter(destinationDir, "$protocolId.pb")
                                .writeFromNetwork(response.body())
                    }
                .compose(config.networkingSchedulers.applySingleSchedulers())
    }

    data class JobID(val modelName: String, val version: String? = null) {
        fun matchWithResponse(modelName: String, version: String? = null) =
                if (version.isNullOrEmpty() || this.version.isNullOrEmpty())
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
