package org.openmined.syft.demo

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.openmined.syft.Syft
import org.openmined.syft.networking.clients.HttpClient
import org.openmined.syft.networking.clients.SocketClient
import org.openmined.syft.threading.ProcessSchedulers

@ExperimentalUnsignedTypes
fun main() {
    val schedulers = object : ProcessSchedulers {
        override val computeThreadScheduler: Scheduler
            get() = Schedulers.computation()
        override val calleeThreadScheduler: Scheduler
            get() = Schedulers.single()
    }
    val syft = Syft.getInstance(
        SocketClient(
            "echo.websocket.org",
            2000u
            , schedulers
        ), HttpClient("echo.websocket.org"), schedulers
    )
}
