package org.openmined.syft.demo

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.ImplicitReflectionSerializer
import org.openmined.syft.Syft
import org.openmined.syft.domain.Protocol
import org.openmined.syft.network.SignallingClient
import org.openmined.syft.threading.ProcessSchedulers

@ExperimentalUnsignedTypes
@ImplicitReflectionSerializer
fun main() {
    val syft = Syft(
        SignallingClient(
            "myWorker",
            Protocol.WSS,
            "echo.websocket.org",
            2000u
        ),
        object : ProcessSchedulers {
            override val computeThreadScheduler: Scheduler
                get() = Schedulers.computation()
            override val calleeThreadScheduler: Scheduler
                get() = Schedulers.single()
        }
    )
    syft.start()
}
