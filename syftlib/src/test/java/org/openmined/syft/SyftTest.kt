package org.openmined.syft

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.junit.jupiter.api.Test
import org.openmined.syft.execution.SyftJob
import org.openmined.syft.networking.clients.HttpClient
import org.openmined.syft.networking.clients.SocketClient
import org.openmined.syft.networking.datamodels.syft.AuthenticationResponse
import org.openmined.syft.threading.ProcessSchedulers

internal class SyftTest {

    @Test
    @ExperimentalUnsignedTypes
    fun `Given a syft object when requestCycle is invoked the socket client calls authenticate api`() {
        val socketClient = mock<SocketClient> {
            on { authenticate() }.thenReturn(
                Single.just(
                    AuthenticationResponse.AuthenticationSuccess(
                        "test id"
                    )
                )
            )
        }
        val httpClient = mock<HttpClient>()
        val schedulers = object : ProcessSchedulers {
            override val computeThreadScheduler: Scheduler
                get() = Schedulers.io()
            override val calleeThreadScheduler: Scheduler
                get() = AndroidSchedulers.mainThread()
        }

        val workerTest = spy(
            Syft("auth token", socketClient, httpClient, schedulers, schedulers)
        )
        val syftJob = SyftJob(workerTest, schedulers, schedulers, "test model")

        workerTest.requestCycle(syftJob)
        verify(socketClient).authenticate()
    }
}