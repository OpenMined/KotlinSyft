package org.openmined.syft.demo

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.ImplicitReflectionSerializer
import org.openmined.syft.Syft
import org.openmined.syft.threading.ProcessSchedulers

@ImplicitReflectionSerializer
fun main() {
    val syft = Syft(
        "myWorker",
        2000,
        "ws://echo.websocket.org",
        object : ProcessSchedulers {
            override val computeThreadScheduler: Scheduler
                get() = Schedulers.computation()
            override val calleeThreadScheduler: Scheduler
                get() = Schedulers.single()
        }
    )

    syft?.start() ?: println("Something went wrong can couldn't start Syft")
}
