package org.openmined.syft

import io.reactivex.disposables.CompositeDisposable
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.openmined.syft.network.NetworkMessage
import org.openmined.syft.network.SignallingClient
import org.openmined.syft.threading.ProcessSchedulers

class Syft private constructor(
    private val workerId: String,
    private val keepAliveTimeout: Int = 20000,
    private val url: String,
    private val schedulers: ProcessSchedulers
) {
    private lateinit var signallingClient: SignallingClient
    private val compositeDisposable = CompositeDisposable()

    companion object {
        operator fun invoke(
            workerId: String,
            keepAliveTimeout: Int = 20000,
            url: String,
            schedulers: ProcessSchedulers
        ): Syft? =
                Syft(workerId, keepAliveTimeout, url, schedulers).takeIf { isValid(it) }

        private fun isValid(syft: Syft): Boolean {
            return checkProtocol(syft.url) &&
                   checkKeepAliveTimeout(syft.keepAliveTimeout)
        }

        private fun checkProtocol(url: String) =
                if (!url.startsWith("wss", true)) {
                    println("Protocol must be wss")
                    false
                } else true

        private fun checkKeepAliveTimeout(timeout: Int) =
                if (timeout < 0) {
                    println("keep alive timeout must be equals or greater than 0")
                    false
                } else true
    }

    @ImplicitReflectionSerializer
    fun start() {
        signallingClient = SignallingClient(
            workerId,
            url,
            keepAliveTimeout
        )

        val disposable = signallingClient.start()
                .map {
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
    fun send(message: String) = signallingClient.send(
        "Personal", JsonObject(
            mapOf(
                "data" to JsonPrimitive(message),
                "workerId" to JsonPrimitive("1234")
            )
        )
    )
}
