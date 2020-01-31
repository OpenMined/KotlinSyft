package org.openmined.syft

import io.reactivex.disposables.CompositeDisposable
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.openmined.syft.network.NetworkMessage
import org.openmined.syft.network.SignallingClient
import org.openmined.syft.threading.ProcessSchedulers

class Syft(
    private val workerId: String,
    private val keepAliveTimeout: Int = 20000,
    private val url: String,
    private val schedulers: ProcessSchedulers
) {

    private lateinit var signallingClient: SignallingClient
    private val compositeDisposable = CompositeDisposable()

    @ImplicitReflectionSerializer
    fun start() {
        // Create signalling client and execute it in background thread
        signallingClient = SignallingClient(
            workerId,
            url,
            keepAliveTimeout
        )

        val disposable = signallingClient.start()
                .map {
                    // TODO Please excuse this terrible piece of code
                    when (it) {
                        is NetworkMessage.SocketOpen -> {
                            println("Socket open")
                            send("And now that we are opened, I send a message")
                        }
                        is NetworkMessage.SocketClosed -> println("Socket was closed successfully")
                        is NetworkMessage.SocketError -> println(it.throwable.message)
                        is NetworkMessage.MessageReceived -> println(it)
                        is NetworkMessage.MessageSent -> println("Message sent successfully")
                    }
                }
                .subscribeOn(schedulers.computeThreadScheduler)
                .observeOn(schedulers.calleeThreadScheduler)
                .subscribe()

        compositeDisposable.add(disposable)
    }

    @ImplicitReflectionSerializer
    fun send(message: String) {
        signallingClient.send(
            "Personal", JsonObject(
                mapOf(
                    "data" to JsonPrimitive(message),
                    "workerId" to JsonPrimitive("1234")
                )
            )
        )
    }
}
