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
    val modelName: String,
    val version: String? = null
) {


    var cycleStatus = AtomicReference(CycleStatus.APPLY)
    private var trainingParamsStatus = AtomicReference(DownloadStatus.NOT_STARTED)

    private lateinit var requestKey: String

    //todo need to filled based on the destination directory defined by syft configuration class
    private val destinationDir = "/data/data/org.openmined.syft.demo/files"
    private val modelFileLocation = "$destinationDir/model/"
    private val plans = ConcurrentHashMap<String, Plan>()
    private val protocols = ConcurrentHashMap<String, Protocol>()
    private lateinit var modelID: String
    private lateinit var clientConfig: ClientConfig
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

    /**
     * report the results back to PyGrid
     */
    fun report(diff: String) {
        compositeDisposable.add(
            worker.getSignallingClient().report(ReportRequest(worker.workerId, requestKey, diff))
                    .compose(networkingSchedulers.applySingleSchedulers())
                    .subscribe { reportResponse: ReportResponse ->
                        Log.i(TAG, reportResponse.status)
                    })
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
        modelID = responseData.modelId
        responseData.plans.forEach { (_, planId) -> plans[planId] = Plan(planId) }
        responseData.protocols.forEach { (_, protocolId) ->
            protocols[protocolId] = Protocol(protocolId)
        }
        clientConfig = responseData.clientConfig
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

        plans.forEach { (planId, plan) ->
            //todo instead of hardcoding this will be defined by configuration class method and by plan class
            plan.planFileLocation = "$destinationDir/plans"
            downloadList.add(planDownloader(plan.planFileLocation, planId))
        }
        protocols.forEach { (protocolId, protocol) ->
            //todo instead of hardcoding this will be defined by configuration class method and by protocol class
            protocol.protocolFileLocation = "$destinationDir/protocols"
            downloadList.add(protocolDownloader(protocol.protocolFileLocation, protocolId))
        }
        downloadList.add(modelDownloader(modelID))

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
                        jobStatusProcessor.offer(
                            JobStatusMessage.JobReady(
                                modelName,
                                clientConfig
                            )
                        )
                    },
                    { e -> jobStatusProcessor.onError(e) }
                )
        )
    }

    //We might want to make these public if needed later
    private fun modelDownloader(modelId: String) =
            worker.getDownloader().downloadModel(worker.workerId, requestKey, modelId).compose(
                computeSchedulers.applySingleSchedulers()
            ).flatMap { response ->
                saveFile(response.body(), modelFileLocation, modelName)
            }


    private fun planDownloader(destinationDir: String, planId: String) =
            worker.getDownloader().downloadPlan(
                worker.workerId,
                requestKey,
                planId,
                "torchscript"
            ).compose(computeSchedulers.applySingleSchedulers())
                    .flatMap { response ->
                        saveFile(response.body(), destinationDir, planId)
                    }

    private fun protocolDownloader(destinationDir: String, protocolId: String) =
            worker.getDownloader().downloadProtocol(
                worker.workerId,
                requestKey,
                protocolId
            ).compose(computeSchedulers.applySingleSchedulers())
                    .flatMap { response ->
                        saveFile(response.body(), destinationDir, protocolId)
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
                    ?: emitter.onError(Exception("invalid input stream"))
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
