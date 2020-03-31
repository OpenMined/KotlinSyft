package org.openmined.syft.execution

import android.util.Log
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import okhttp3.ResponseBody
import org.openmined.syft.Syft
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.datamodels.syft.ReportRequest
import org.openmined.syft.networking.datamodels.syft.ReportResponse
import org.openmined.syft.proto.State
import org.openmined.syft.proto.SyftModel
import org.openmined.syft.threading.ProcessSchedulers
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

private const val TAG = "SyftJob"

/**
 * @param worker : The syft worker handling this job
 * @param computeSchedulers : The threads on which networking and file saving occurs
 * @param modelName : The model being trained or used in inference
 * @param version : The version of the model with name modelName
 */
@ExperimentalUnsignedTypes
class SyftJob(
    private val worker: Syft,
    //todo change this to read from syft configuration
    private val computeSchedulers: ProcessSchedulers,
    //todo change this to read from syft configuration
    private val networkingSchedulers: ProcessSchedulers,
    modelName: String,
    version: String? = null
) {

    val jobId = JobID(modelName, version)

    private var cycleStatus = AtomicReference(CycleStatus.APPLY)
    private var trainingParamsStatus = AtomicReference(DownloadStatus.NOT_STARTED)
    private var requestKey: String? = null
    private var clientConfig: ClientConfig? = null

    //todo need to filled based on the destination directory defined by syft configuration class
    private val destinationDir = "/data/data/org.openmined.syft.demo/files"
    private val modelFileLocation = "$destinationDir/model/"
    private val plans = ConcurrentHashMap<String, Plan>()
    private val protocols = ConcurrentHashMap<String, Protocol>()
    private val model = SyftModel(modelName, version)
    private val jobStatusProcessor: PublishProcessor<JobStatusMessage> = PublishProcessor.create()
    private val compositeDisposable = CompositeDisposable()

    /**
     * create a worker job
     */
    fun start(subscriber: JobStatusSubscriber = JobStatusSubscriber()) {
        //todo all this in syft.kt
        //todo check for connection if doesn't exist establish one
        //todo before calling this function syft should have checked the bandwidth etc requirements
        if (cycleStatus.get() == CycleStatus.REJECT) {
            Log.d(TAG, "job awaiting timer completion to resend the Cycle Request")
            return
        }
        worker.requestCycle(this)
        subscribe(subscriber, computeSchedulers)
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
    fun downloadData() {
        if (trainingParamsStatus.get() != DownloadStatus.NOT_STARTED) {
            Log.d(TAG, "download already running")
            return
        }
        Log.d(TAG, "beginning download")
        trainingParamsStatus.set(DownloadStatus.RUNNING)
        val downloadList = mutableListOf<Single<String>>()

        plans.forEach { (_, plan) ->
            //todo instead of hardcoding this will be defined by configuration class method and by plan class
            downloadList.add(planDownloader("$destinationDir/plans", plan))
        }
        protocols.forEach { (protocolId, protocol) ->
            //todo instead of hardcoding this will be defined by configuration class method and by protocol class
            protocol.protocolFileLocation = "$destinationDir/protocols"
            downloadList.add(protocolDownloader(protocol.protocolFileLocation, protocolId))
        }
        downloadList.add(modelDownloader())

        compositeDisposable.add(Single.zip(downloadList) { successMessages ->
            successMessages.joinToString(
                ",",
                prefix = "files ",
                postfix = " downloaded successfully"
            )
        }
                .compose(networkingSchedulers.applySingleSchedulers())
                .subscribe(
                    { successMsg: String ->
                        Log.d(TAG, successMsg)
                        trainingParamsStatus.set(DownloadStatus.COMPLETE)
                        jobStatusProcessor.offer(JobStatusMessage.JobReady(model, plans, clientConfig))
                    },
                    { e -> jobStatusProcessor.onError(e) }
                )
        )
    }


    /**
     * report the results back to PyGrid
     */
    fun report(diff: State) {
        val requestKey = requestKey
        val workerId = worker.getSyftWorkerId()
        if (requestKey != null && workerId != null)
            compositeDisposable.add(
                worker.getSignallingClient()
                        .report(
                            ReportRequest(
                                workerId,
                                requestKey,
                                //todo this should be sent via post call on http client
                                //todo Not yet yet decided on pygrid
                                diff.serialize().toString()
                            )
                        )
                        .compose(networkingSchedulers.applySingleSchedulers())
                        .subscribe { reportResponse: ReportResponse ->
                            Log.i(TAG, reportResponse.status)
                        })
    }

    //We might want to make these public if needed later
    private fun modelDownloader(): Single<String> {
        val requestKey = requestKey
        val workerId = worker.getSyftWorkerId()
        val modelId = model.pyGridModelId
        return if (requestKey == null || modelId == null || workerId == null)
            Single.error(IllegalStateException("request Key,workerId or modelId has not been set"))
        else
            worker.getDownloader()
                    .downloadModel(workerId, requestKey, modelId)
                    .compose(
                        computeSchedulers.applySingleSchedulers()
                    ).flatMap { response ->
                        saveFile(response.body(), modelFileLocation, modelId)
                    }.flatMap { modelFile ->
                        Single.create<String> { emitter ->
                            model.loadModelState(modelFile)
                            emitter.onSuccess(modelFile)
                        }
                    }
    }

    private fun planDownloader(destinationDir: String, plan: Plan): Single<String> {
        val workerId = worker.getSyftWorkerId()
        val requestKey = requestKey
        return if (workerId == null || requestKey == null)
            Single.error(IllegalStateException("workerId or request not initialised yet"))
        else
            worker.getDownloader().downloadPlan(
                workerId,
                requestKey,
                plan.planId,
                "torchscript"
            ).compose(computeSchedulers.applySingleSchedulers())
                    .flatMap { response ->
                        saveFile(response.body(), destinationDir, plan.planId)
                    }.flatMap { filepath ->
                        Single.create<String> { emitter ->
                            plan.generateScriptModule(destinationDir, filepath)
                            emitter.onSuccess(filepath)
                        }
                    }
    }

    private fun protocolDownloader(destinationDir: String, protocolId: String): Single<String> {
        val workerId = worker.getSyftWorkerId()
        val requestKey = requestKey
        return if (workerId == null || requestKey == null)
            Single.error(IllegalStateException("workerId or request not initialised yet"))
        else
            worker.getDownloader().downloadProtocol(
                workerId, requestKey, protocolId
            ).compose(computeSchedulers.applySingleSchedulers())
                    .flatMap { response ->
                        saveFile(response.body(), destinationDir, protocolId)
                    }
    }

    private fun saveFile(
        input: ResponseBody?,
        destinationDir: String,
        fileName: String
    ): Single<String> {
        val destination = File(destinationDir)
        if (!destination.mkdirs())
            Log.d(TAG, "directory already exists")

        return Single.create { emitter ->
            input?.byteStream().use { inputStream ->
                val file = File(destination, "$fileName.pb")
                file.outputStream().use { outputFile ->
                    inputStream?.copyTo(outputFile)
                    ?: emitter.onError(FileSystemException(file))
                }
                Log.d(TAG, "file written at ${file.absolutePath}")
                emitter.onSuccess(file.absolutePath)
            }
        }
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
