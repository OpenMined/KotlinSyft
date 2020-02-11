package org.openmined.syft

import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import org.openmined.syft.networking.clients.NetworkMessage
import org.openmined.syft.networking.clients.SignallingClient
import org.openmined.syft.networking.requests.CommunicationDataFactory
import org.openmined.syft.networking.requests.REQUESTS
import org.openmined.syft.networking.serialization.SerializableClasses
import org.openmined.syft.threading.ProcessSchedulers

private const val TAG = "Syft"

@ExperimentalUnsignedTypes
class Syft private constructor(
    private val signallingClient: SignallingClient,
    private val schedulers: ProcessSchedulers
) {
    companion object {
        @Volatile
        private var INSTANCE: Syft? = null

        fun getInstance(signallingClient: SignallingClient, schedulers: ProcessSchedulers): Syft =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: Syft(
                        signallingClient,
                        schedulers
                    ).also { INSTANCE = it }
                }
    }

    private lateinit var workerId: String

    private val workerJobs = mutableListOf<SyftJob>()
    private val compositeDisposable = CompositeDisposable().add(
        signallingClient.start()
                .map {
                    when (it) {
                        is NetworkMessage.SocketOpen ->
                            signallingClient.send(REQUESTS.AUTHENTICATION)

                        is NetworkMessage.SocketClosed -> Log.d(
                            TAG,
                            "Socket was closed successfully"
                        )
                        is NetworkMessage.SocketError -> Log.e(TAG, "socket error", it.throwable)
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
                .subscribe()
    )

    fun newJob(modelName: String, version: String): SyftJob {
        val job = SyftJob(modelName, version)
        signallingClient.send(
            REQUESTS.CYCLE,
            CommunicationDataFactory.requestCycle(workerId, job, "", "", "")
        )
        workerJobs.add(job)
        return job
    }

    private fun handleResponse(response: SerializableClasses) {
        when (response) {
            is SerializableClasses.AuthenticationSuccess ->
                this.workerId = response.workerId
            is SerializableClasses.CycleResponseData -> {
                when (response) {
                    is SerializableClasses.CycleResponseData.CycleAccept -> "accept here"
                    is SerializableClasses.CycleResponseData.CycleReject -> "set timeout for job"
                }
            }

        }
    }
}
