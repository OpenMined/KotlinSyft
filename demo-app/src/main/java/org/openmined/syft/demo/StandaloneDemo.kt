package org.openmined.syft.demo

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.openmined.syft.Syft
import org.openmined.syft.networking.clients.HttpClient
import org.openmined.syft.networking.clients.SocketClient
import org.openmined.syft.threading.ProcessSchedulers

@ExperimentalUnsignedTypes
fun main() {
    val networkingSchedulers = object : ProcessSchedulers {
        override val computeThreadScheduler: Scheduler
            get() = Schedulers.io()
        override val calleeThreadScheduler: Scheduler
            get() = AndroidSchedulers.mainThread()
    }
    val computeSchedulers = object : ProcessSchedulers {
        override val computeThreadScheduler: Scheduler
            get() = Schedulers.computation()
        override val calleeThreadScheduler: Scheduler
            get() = Schedulers.single()
    }
    val syft = Syft.getInstance(
        SocketClient(
            "echo.websocket.org",
            2000u
            , computeSchedulers
        ), HttpClient("echo.websocket.org"), computeSchedulers, networkingSchedulers
    )
}
