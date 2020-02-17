package org.openmined.syft

import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import kotlinx.serialization.json.JsonObject
import org.openmined.syft.networking.clients.NetworkMessage
import org.openmined.syft.networking.clients.SignallingClient
import org.openmined.syft.networking.datamodels.AuthenticationSuccess
import org.openmined.syft.networking.datamodels.CycleResponseData
import org.openmined.syft.networking.datamodels.SocketResponse
import org.openmined.syft.networking.requests.CommunicationDataFactory
import org.openmined.syft.networking.requests.MessageTypes
import org.openmined.syft.networking.requests.REQUESTS
import org.openmined.syft.threading.ProcessSchedulers
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "Syft"

@ExperimentalUnsignedTypes
class Syft private constructor(
    val signallingClient: SignallingClient,
    val schedulers: ProcessSchedulers
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

    lateinit var workerId: String
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val workerJobs = ConcurrentHashMap<String, SyftJob>()

    fun newJob(modelName: String, version: String): SyftJob {
        val job = SyftJob(this, modelName, version)
        workerJobs[job.modelName] = job
        return job
    }

    private fun initiateSocket() {
        if (compositeDisposable.size() == 0)
            compositeDisposable.add(signallingClient.start()
                    .map {
                        when (it) {
                            is NetworkMessage.SocketOpen ->
                                signallingClient.send(REQUESTS.AUTHENTICATION)

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

    }

    private fun handleResponse(response: SocketResponse) {
        when (response.data) {
            is AuthenticationSuccess ->
                this.workerId = response.data.workerId
            is CycleResponseData -> (response.data)

        }
    }


}
