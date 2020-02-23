package org.openmined.syft.Processes

import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import org.openmined.syft.networking.datamodels.CycleResponseData
import org.openmined.syft.networking.clients.SocketClient
import org.openmined.syft.Syft
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

@ExperimentalUnsignedTypes
class SyftJob(private val worker: Syft, val modelName: String, val version: String? = null) {

    var cycleStatus = CycleStatus.APPLY
    var trainingParamsStatus = AtomicReference<DownloadStatus>()

    private lateinit var requestKey: String
    private val plans = ConcurrentHashMap<String, Plan>()
    private val protocols = ConcurrentHashMap<String, Protocol>()
    private val jobStatusProcessor: PublishProcessor<JobStatusMessage> =
            PublishProcessor.create<JobStatusMessage>()

    /**
     * create a worker job
     */
    fun start(): Flowable<JobStatusMessage> {
        //todo all this in syft.kt
        //todo check for connection if doesn't exist establish one
        //todo before calling this function syft should have checked the bandwidth etc requirements
        trainingParamsStatus.set(DownloadStatus.NOT_STARTED)
        worker.requestCycle(this)
        return jobStatusProcessor.onBackpressureBuffer()
    }

    @Synchronized
    fun setRequestKey(responseData: CycleResponseData.CycleAccept) {
        requestKey = responseData.requestKey
        cycleStatus = CycleStatus.ACCEPTED
        jobStatusProcessor.offer(JobStatusMessage.JobCycleAccepted)
    }

    fun downloadData() {
        //todo send jobReady on jobStatusProcessor after it completes
    }


    /**
     * report the results back to PyGrid
     */
    private fun report(diff: String) {
        worker.socketClient.send(
            SocketClient.report(worker.workerId.get(), requestKey, diff)
        )
    }

    data class JobID(val modelName: String, val version: String? = null)

    enum class DownloadStatus {
        NOT_STARTED, INCOMPLETE, COMPLETE
    }

    enum class CycleStatus {
        APPLY, REJECTED, ACCEPTED
    }

}