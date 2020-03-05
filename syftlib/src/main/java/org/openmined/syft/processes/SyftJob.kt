package org.openmined.syft.processes

import android.util.Log
import io.reactivex.Flowable
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

@ExperimentalUnsignedTypes
class SyftJob(
    private val worker: Syft,
    private val schedulers: ProcessSchedulers,
    val modelName: String,
    val version: String? = null
) {

    var cycleStatus = AtomicReference<CycleStatus>(CycleStatus.APPLY)
    private var trainingParamsStatus = AtomicReference<DownloadStatus>(DownloadStatus.NOT_STARTED)

    private lateinit var requestKey: String
    private lateinit var modelFile: String
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
                        jobStatusProcessor.onComplete()
                    })
    }

    fun getStatusProcessor(): Flowable<JobStatusMessage> = jobStatusProcessor.onBackpressureBuffer()

    @Synchronized
    fun setRequestKey(responseData: CycleResponseData.CycleAccept) {
        requestKey = responseData.requestKey
        cycleStatus.set(CycleStatus.ACCEPTED)
        jobStatusProcessor.offer(JobStatusMessage.JobCycleAccepted)
    }

    fun downloadData(destinationDir: String) {
        trainingParamsStatus.set(DownloadStatus.RUNNING)
        plans.forEach { (planId, plan) -> downloadPlanFile("$destinationDir/plans", planId, plan) }

        protocols.forEach { (protocolId, protocol) ->
            downloadProtocolFile(
                "$destinationDir/protocols",
                protocolId,
                protocol
            )
        }
        downloadModelFile(destinationDir, modelName)
        trainingParamsStatus.set(DownloadStatus.COMPLETE)
    }

    //We might want to make these public if needed later
    private fun downloadModelFile(destinationDir: String, modelName: String) {
        compositeDisposable.add(
            worker.getDownloader().downloadModel(worker.workerId, requestKey, modelName).compose(
                schedulers.applySingleSchedulers()
            ).flatMap { response ->
                saveFile(
                    response.body()?.byteStream(),
                    destinationDir,
                    modelName
                )
            }.subscribe(
                { fileLocation: String -> modelFile = fileLocation },
                { e -> jobStatusProcessor.onError(e) })
        )
    }

    private fun downloadPlanFile(destinationDir: String, planId: String, plan: Plan) {
        compositeDisposable.add(
            worker.getDownloader().downloadPlan(
                worker.workerId,
                requestKey,
                planId,
                "torchscript"
            ).compose(schedulers.applySingleSchedulers())
                    .flatMap { response ->
                        saveFile(
                            response.body()?.byteStream(),
                            destinationDir,
                            planId
                        )
                    }.subscribe(
                        { fileLocation: String -> plan.torchScriptLocation = fileLocation },
                        { e -> jobStatusProcessor.onError(e) })
        )
    }

    private fun downloadProtocolFile(
        destinationDir: String,
        protocolId: String,
        protocol: Protocol
    ) {
        compositeDisposable.add(worker.getDownloader().downloadProtocol(
            worker.workerId,
            requestKey,
            protocolId
        ).compose(schedulers.applySingleSchedulers())
                .flatMap { response ->
                    saveFile(
                        response.body()?.byteStream(),
                        destinationDir,
                        protocolId
                    )
                }.subscribe({ fileLocation: String ->
                    protocol.protocolFileLocation = fileLocation
                }, { e -> jobStatusProcessor.onError(e) })
        )
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
                val file = File(destination, fileName)
                file.outputStream()
                        .use { fileName -> input.copyTo(fileName) }
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
