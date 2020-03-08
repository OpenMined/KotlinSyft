package org.openmined.syft.processes

import android.util.Log
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.openmined.syft.Syft
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.datamodels.syft.ReportRequest
import org.openmined.syft.networking.datamodels.syft.ReportResponse
import org.openmined.syft.threading.ProcessSchedulers
import java.io.File
import java.io.InputStream
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
    private val destinationDir = ""
    private val modelFileLocation = "$destinationDir/model/$modelName"
    private val plans = ConcurrentHashMap<String, Plan>()
    private val protocols = ConcurrentHashMap<String, Protocol>()
    private val jobStatusProcessor: PublishProcessor<JobStatusMessage> = PublishProcessor.create()
    private val compositeDisposable = CompositeDisposable()

    /**
     * create a worker job
     */
    fun start(subscriber: JobStatusSubscriber = JobStatusSubscriber()) {
        //todo all this in syft.kt
        //todo check for connection if doesn't exist establish one
        //todo before calling this function syft should have checked the bandwidth etc requirements
        if (cycleStatus.get() == CycleStatus.APPLY) {
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
    fun setRequestKey(responseData: CycleResponseData.CycleAccept) {
        requestKey = responseData.requestKey
        cycleStatus.set(CycleStatus.ACCEPTED)
        jobStatusProcessor.offer(JobStatusMessage.JobCycleAccepted)
    }

    //todo before downloading check for wifi connection again
    fun downloadData() {
        if (trainingParamsStatus.get() != DownloadStatus.NOT_STARTED) {
            Log.d(TAG, "download already running")
            return
        }
        trainingParamsStatus.set(DownloadStatus.RUNNING)
        val downloadList = mutableListOf<Single<String>>()

        plans.forEach { (planId, plan) ->
            //todo instead of hardcoding this will be defined by configuration class method and by plan class
            plan.torchScriptLocation = "$destinationDir/plans/$planId"
            downloadList.add(planDownloader(plan.torchScriptLocation, planId))
        }
        protocols.forEach { (protocolId, protocol) ->
            //todo instead of hardcoding this will be defined by configuration class method and by protocol class
            protocol.protocolFileLocation = "$destinationDir/plans/$protocolId"
            downloadList.add(protocolDownloader(protocol.protocolFileLocation, protocolId))
        }
        downloadList.add(modelDownloader(modelName))

        compositeDisposable.add(Single.zip(downloadList) { successMessages ->
                    successMessages.joinToString(
                        ",",
                        prefix = "files ",
                        postfix = "downloaded successfully"
                    )
                }
                .compose(networkingSchedulers.applySingleSchedulers())
                .subscribe(
                    { successMsg: String ->
                        Log.d(TAG, successMsg)
                        trainingParamsStatus.set(DownloadStatus.COMPLETE)
                        jobStatusProcessor.offer(JobStatusMessage.JobReady)
                    },
                    { e -> jobStatusProcessor.onError(e) }
                )
        )
    }

    //We might want to make these public if needed later
    private fun modelDownloader(modelName: String) =
            worker.getDownloader().downloadModel(worker.workerId, requestKey, modelName).compose(
                computeSchedulers.applySingleSchedulers()
            ).flatMap { response ->
                saveFile(response.body()?.byteStream(), modelFileLocation, modelName)
            }


    private fun planDownloader(destinationDir: String, planId: String) =
            worker.getDownloader().downloadPlan(
                        worker.workerId,
                        requestKey,
                        planId,
                        "torchscript"
                    ).compose(computeSchedulers.applySingleSchedulers())
                    .flatMap { response ->
                        saveFile(response.body()?.byteStream(), destinationDir, planId)
                    }

    private fun protocolDownloader(destinationDir: String, protocolId: String) =
            worker.getDownloader().downloadProtocol(
                        worker.workerId,
                        requestKey,
                        protocolId
                    ).compose(computeSchedulers.applySingleSchedulers())
                    .flatMap { response ->
                        saveFile(response.body()?.byteStream(), destinationDir, protocolId)
                    }

    private fun saveFile(
        input: InputStream?,
        destinationDir: String,
        fileName: String
    ): Single<String> {
        val destination = File(destinationDir)
        if (!destination.exists())
            destination.mkdirs()
        return Single.create { emitter ->
            input?.let {
                val file = File(destination,fileName)
                file.outputStream().use { outputFile ->
                    input.copyTo(outputFile)
                }
                emitter.onSuccess(file.absolutePath)
            } ?: emitter.onError(Exception("invalid response stream for downloaded file"))
        }
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
