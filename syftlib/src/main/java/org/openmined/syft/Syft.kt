package org.openmined.syft

import io.reactivex.disposables.CompositeDisposable
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.openmined.syft.network.SignallingClient
import org.openmined.syft.threading.ProcessSchedulers

class Syft(
    private val workerId: String,
    private val keepAliveTimeout: Int = 20000,
    private val url: String,
    private val port: Int,
    private val schedulers: ProcessSchedulers
) {

    private lateinit var signallingClient: SignallingClient
    private val compositeDisposable = CompositeDisposable()

    @ImplicitReflectionSerializer
    fun start() {
        // Create signalling client and execute it in background thread
        signallingClient = SignallingClient(
            workerId,
            keepAliveTimeout,
            url,
            port
        )

        val disposable = signallingClient.start()
            .map {
                // TODO Please excuse this terrible piece of code
                if (it == "We are open for business!") {
                    send("And now that we are opened, I send a message")
                }
                println(it)
            }
            .subscribeOn(schedulers.computeThreadScheduler)
            .observeOn(schedulers.calleeThreadScheduler)
            .subscribe()

        compositeDisposable.add(disposable)
    }

    @ImplicitReflectionSerializer
    fun send(message: String) {
        signallingClient.send("Personal", JsonObject(
            mapOf(
                "data" to Json.plain.toJson(message),
                "workerId" to Json.plain.toJson("1234")
            )
        ))
    }
}
