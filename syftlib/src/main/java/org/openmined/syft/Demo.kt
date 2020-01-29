package org.openmined.syft

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.ImplicitReflectionSerializer
import org.openmined.syft.interfaces.ProcessSchedulers
import org.openmined.syft.interfaces.SignallingInterface

@ImplicitReflectionSerializer
fun main() {
    val signallingInterface = object : SignallingInterface {

        override fun onMessage(message: String) {
            println("I have received this message $message")
        }

        override fun onClose() {
            println("Good bye")
        }

        override fun onOpen() {
            println("We are live!")
        }
    }
    val syft = Syft(
        signallingInterface,
        "myWorker",
        2000,
        "ws://echo.websocket.org",
        23,
        object : ProcessSchedulers {
            override val computeThreadScheduler: Scheduler
                get() = Schedulers.computation()
            override val calleeThreadScheduler: Scheduler
                get() = Schedulers.single()
        }
    )

    syft.start()
}