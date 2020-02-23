package org.openmined.syft.demo

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.openmined.syft.Syft
import org.openmined.syft.networking.clients.SyftWebSocket
import org.openmined.syft.networking.requests.Protocol
import org.openmined.syft.threading.ProcessSchedulers

@ExperimentalUnsignedTypes
fun main() {
    val syft = Syft.getInstance(SyftWebSocket(
        Protocol.WSS,
        "echo.websocket.org",
        2000u
    ), object : ProcessSchedulers {
        override val computeThreadScheduler: Scheduler
            get() = Schedulers.computation()
        override val calleeThreadScheduler: Scheduler
            get() = Schedulers.single()
    }
    )
}
