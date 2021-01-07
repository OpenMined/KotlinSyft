package org.openmined.syft.unit

import android.net.NetworkCapabilities
import com.nhaarman.mockitokotlin2.mock
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.openmined.syft.Syft
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.monitor.DeviceMonitor
import org.openmined.syft.monitor.network.NetworkStatusModel
import org.openmined.syft.networking.clients.HttpClient
import org.openmined.syft.networking.clients.SocketClient
import org.openmined.syft.networking.datamodels.syft.AuthenticationResponse
import org.openmined.syft.networking.datamodels.syft.AuthenticationRequest
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.datamodels.syft.CycleRequest
import org.openmined.syft.threading.ProcessSchedulers

@ExperimentalCoroutinesApi
internal class SyftTest {

    @Test
    @ExperimentalUnsignedTypes
    fun `Given a syft object when requestCycle is invoked then socket client calls authenticate api`() {
        val workerId = "test id"
        val socketClient = mockk<SocketClient> {
            coEvery {
                authenticate(
                    AuthenticationRequest(
                        "auth token",
                        "model name",
                        "1.0.0"
                    )
                )
            } returns
                    AuthenticationResponse.AuthenticationSuccess(
                        workerId,
                        true
                    )
            coEvery { getCycle(any()) } returns mockk<CycleResponseData.CycleAccept>()
        }
        val httpClient = mockk<HttpClient>() {
            every { apiClient } returns mock()
        }
        val schedulers = object : ProcessSchedulers {
            override val computeThreadScheduler: Scheduler
                get() = Schedulers.io()
            override val calleeThreadScheduler: Scheduler
                get() = AndroidSchedulers.mainThread()
        }

        val networkStatusModel = NetworkStatusModel(12, 12.0f, 12.0f, true)
        val deviceMonitor = mockk<DeviceMonitor> {
            every { isActivityStateValid() } returns true
            every { isNetworkStateValid() } returns true
            every { isBatteryStateValid() } returns true
            coEvery { getNetworkStatus(workerId, true) } returns networkStatusModel
        }

        val config = SyftConfiguration(
            mockk(),
            schedulers,
            schedulers,
            mockk(),
            true,
            batteryCheckEnabled = true,
            networkConstraints = listOf(),
            transportMedium = NetworkCapabilities.TRANSPORT_WIFI,
            cacheTimeOut = 0L,
            maxConcurrentJobs = 1,
            socketClient = socketClient,
            httpClient = httpClient,
            messagingClient = SyftConfiguration.NetworkingClients.SOCKET
        )
        val workerTest = spyk(
            Syft(config, deviceMonitor, "auth token")
        )
        val syftJob = workerTest.newJob(
            "model name",
            "1.0.0"
        )

        runBlocking {
            workerTest.executeCycleRequest(syftJob)
        }

        coVerify {
            socketClient.authenticate(
                AuthenticationRequest(
                    "auth token",
                    "model name",
                    "1.0.0"
                )
            )
        }
    }

    @Test
    @ExperimentalUnsignedTypes
    fun `Given a syft object when requestCycle is invoked and speed test is not enabled then network status returns an empty result`() {
        val workerId = "test id"
        val socketClient = mockk<SocketClient> {
            coEvery {
                authenticate(
                    AuthenticationRequest(
                        "auth token",
                        "model name",
                        "1.0.0"
                    )
                )
            } returns
                    AuthenticationResponse.AuthenticationSuccess(
                        workerId,
                        false
                    )
            coEvery { getCycle(any()) } returns mockk<CycleResponseData.CycleAccept>()
        }
        val httpClient = mockk<HttpClient>() {
            every { apiClient } returns mock()
        }
        val schedulers = object : ProcessSchedulers {
            override val computeThreadScheduler: Scheduler
                get() = Schedulers.io()
            override val calleeThreadScheduler: Scheduler
                get() = AndroidSchedulers.mainThread()
        }

        val networkStatusModel = NetworkStatusModel(-1, 0.0f, 0.0f)
        val deviceMonitor = mockk<DeviceMonitor> {
            every { isActivityStateValid() } returns true
            every { isNetworkStateValid() } returns true
            every { isBatteryStateValid() } returns true
            coEvery { getNetworkStatus(workerId, false) } returns networkStatusModel
        }

        val config = SyftConfiguration(
            mockk(),
            schedulers,
            schedulers,
            mockk(),
            true,
            batteryCheckEnabled = true,
            networkConstraints = listOf(),
            transportMedium = NetworkCapabilities.TRANSPORT_WIFI,
            cacheTimeOut = 0L,
            maxConcurrentJobs = 1,
            socketClient = socketClient,
            httpClient = httpClient,
            messagingClient = SyftConfiguration.NetworkingClients.SOCKET
        )
        val workerTest = spyk(
            Syft(config, deviceMonitor, "auth token")
        )
        val syftJob = workerTest.newJob(
            "model name",
            "1.0.0"
        )

        runBlocking {
            workerTest.executeCycleRequest(syftJob)
        }

        coVerify {
            socketClient.authenticate(
                AuthenticationRequest(
                    "auth token",
                    "model name",
                    "1.0.0"
                )
            )
        }
    }
}
