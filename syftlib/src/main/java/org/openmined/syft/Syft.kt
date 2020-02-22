package org.openmined.syft

import android.util.Log
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import org.openmined.syft.Processes.SyftJob
import org.openmined.syft.networking.clients.NetworkMessage
import org.openmined.syft.networking.clients.SocketSignallingClient
import org.openmined.syft.networking.datamodels.AuthenticationSuccess
import org.openmined.syft.networking.datamodels.CycleResponseData
import org.openmined.syft.networking.datamodels.ReportStatus
import org.openmined.syft.networking.datamodels.SocketResponse
import org.openmined.syft.networking.requests.CommunicationDataFactory
import org.openmined.syft.threading.ProcessSchedulers
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

private const val TAG = "Syft"

@ExperimentalUnsignedTypes
class Syft private constructor(
    val socketSignallingClient: SocketSignallingClient,
    private val schedulers: ProcessSchedulers
) {
    companion object {
        @Volatile
        private var INSTANCE: Syft? = null

        fun getInstance(
            socketSignallingClient: SocketSignallingClient,
            schedulers: ProcessSchedulers
        ): Syft =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: Syft(
                        socketSignallingClient,
                        schedulers
                    ).also { INSTANCE = it }
                }
    }

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val workerJobs = ConcurrentHashMap<SyftJob.JobID, SyftJob>()
    private val pendingJobs = ConcurrentLinkedQueue<SyftJob>()

    //todo decide if this can be changed by pygrid or will remain same irrespective of the requests we make
    @Volatile
    lateinit var workerId: AtomicReference<String>
    @Volatile
    private var socketClientSubscribed = AtomicBoolean(false)

    fun newJob(model: String, version: String? = null): SyftJob {
        val job = SyftJob(this, model, version)
        workerJobs[SyftJob.JobID(model, version)] = job
        return job
    }

    fun requestCycle(job: SyftJob) {
        initiateSocketIfEmpty()
        if (this::workerId.isInitialized)
            socketSignallingClient.send(
                CommunicationDataFactory.requestCycle(
                    workerId.get(),
                    job,
                    getPing(),
                    getDownloadSpeed(),
                    getUploadSpeed()
                )
            )
        else {
            socketSignallingClient.send(CommunicationDataFactory.authenticate())
            pendingJobs.add(job)
        }
    }

    private fun initiateSocketIfEmpty() {
        if (socketClientSubscribed.get())
            return

        compositeDisposable.add(socketSignallingClient.start()
                .map {
                    when (it) {
                        is NetworkMessage.SocketOpen -> {
                            socketSignallingClient.send(CommunicationDataFactory.authenticate())
                        }
                        is NetworkMessage.SocketClosed -> Log.d(
                            TAG,
                            "Socket was closed successfully"
                        )
                        is NetworkMessage.SocketError -> Log.e(
                            TAG,
                            "socket error",
                            it.throwable
                        )
                        is NetworkMessage.MessageReceived -> handleResponse(
                            CommunicationDataFactory.deserializeSocket(
                                it.message
                            )
                        )
                        is NetworkMessage.MessageSent -> println("Message sent successfully")
                    }
                }
                .subscribeOn(schedulers.computeThreadScheduler)
                .observeOn(schedulers.calleeThreadScheduler)
                .subscribe())
        socketClientSubscribed.set(true)
    }

    private fun getPing() = ""
    private fun getDownloadSpeed() = ""
    private fun getUploadSpeed() = ""

    private fun handleResponse(response: SocketResponse) {
        when (response.data) {
            is AuthenticationSuccess -> {
                this.workerId.set(response.data.workerId)
                //empty pending jobs first
                emptyPendingJobs()

            }
            is CycleResponseData -> handleCycleResponse(response.data)
            is ReportStatus -> Log.i(TAG, response.data.status)
        }
    }

    private fun handleCycleResponse(responseData: CycleResponseData) {
        when (responseData) {
            is CycleResponseData.CycleAccept -> {
                var jobId = SyftJob.JobID(responseData.modelName, responseData.version)
                val job = workerJobs.getOrElse(jobId, {
                    jobId = SyftJob.JobID(responseData.modelName)
                    workerJobs.getValue(jobId)
                })
                job.setRequestKey(responseData)
                job.downloadData()
            }
            is CycleResponseData.CycleReject -> {
                var jobId = SyftJob.JobID(responseData.modelName, responseData.version)
                val job = workerJobs.getOrElse(jobId, {
                    jobId = SyftJob.JobID(responseData.modelName)
                    workerJobs.getValue(jobId)
                })
                compositeDisposable.add(
                    Completable
                            .timer(responseData.timeout.toLong(), TimeUnit.MILLISECONDS)
                            .subscribeOn(schedulers.computeThreadScheduler)
                            .observeOn(schedulers.calleeThreadScheduler)
                            .subscribe { job.start() }
                )
            }
        }
    }

    private fun emptyPendingJobs() {
        while (pendingJobs.isNotEmpty())
            requestCycle(pendingJobs.remove())
    }

}
