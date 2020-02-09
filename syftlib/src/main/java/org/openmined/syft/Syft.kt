package org.openmined.syft

import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import kotlinx.serialization.json.json
import org.openmined.syft.network.CommunicationDataFactory
import org.openmined.syft.network.NetworkMessage
import org.openmined.syft.network.REQUEST
import org.openmined.syft.network.SignallingClient
import org.openmined.syft.threading.ProcessSchedulers

private const val TAG = "Syft"

@ExperimentalUnsignedTypes
class Syft private constructor(
    private val signallingClient: SignallingClient,
    private val schedulers: ProcessSchedulers
) {
    private lateinit var syftWorker: Syft
    private lateinit var workerId: String

    private val workerJobs = mutableListOf<SyftJob>()
    private val compositeDisposable = CompositeDisposable().add(
        signallingClient.start()
                .map {
                    when (it) {
                        is NetworkMessage.SocketOpen -> {
                            signallingClient.send(REQUEST.AUTHENTICATION, json {})
                        }
                        is NetworkMessage.SocketClosed -> Log.d(
                            TAG,
                            "Socket was closed successfully"
                        )
                        is NetworkMessage.SocketError -> Log.e(TAG,"socket error",it.throwable)
                        is NetworkMessage.MessageReceived -> println(it)
                        is NetworkMessage.MessageSent -> println("Message sent successfully")
                    }
                }
                .subscribeOn(schedulers.computeThreadScheduler)
                .observeOn(schedulers.calleeThreadScheduler)
                .subscribe()
    )

    //TODO decide if we go by dependency injection who will initiate module
    fun getSyftWorker(signallingClient: SignallingClient, schedulers: ProcessSchedulers): Syft {
        if (!::syftWorker.isInitialized)
            syftWorker = Syft(signallingClient, schedulers)

        return syftWorker
    }

    fun newJob(modelName: String, version: String): SyftJob {
        val job = SyftJob(modelName, version)
        signallingClient.send(
            REQUEST.CYCLE,
            CommunicationDataFactory.requestCycle(workerId, job, "", "", "")
        )
        workerJobs.add(job)
        return job
    }
}
