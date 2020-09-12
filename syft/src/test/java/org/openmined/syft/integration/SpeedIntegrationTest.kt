package org.openmined.syft.integration

import android.net.NetworkCapabilities
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.openmined.syft.Syft
import org.openmined.syft.common.AbstractSyftWorkerTest
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.execution.JobStatusSubscriber
import org.openmined.syft.integration.clients.HttpClientMock
import org.openmined.syft.integration.clients.SocketClientMock
import org.openmined.syft.integration.execution.ShadowPlan
import org.robolectric.annotation.Config

@ExperimentalUnsignedTypes
class SpeedIntegrationTest : AbstractSyftWorkerTest() {

    @Test
    @Config(shadows = [ShadowPlan::class])
    fun `Test workflow with cycle rejected`() {
        val socketClient = SocketClientMock(
            authenticateSuccess = true,
            cycleSuccess = false
        )
        val httpClient = HttpClientMock(
            pingSuccess = true, downloadSpeedSuccess = true,
            uploadSuccess = true, downloadPlanSuccess = true, downloadModelSuccess = true
        )

        val syftConfiguration = SyftConfiguration(
            context,
            networkingSchedulers,
            computeSchedulers,
            context.filesDir,
            true,
            batteryCheckEnabled = true,
            networkConstraints = networkConstraints,
            transportMedium = NetworkCapabilities.TRANSPORT_WIFI,
            cacheTimeOut = 0,
            maxConcurrentJobs = 1,
            socketClient = socketClient.getMockedClient(),
            httpClient = httpClient.getMockedClient(),
            messagingClient = SyftConfiguration.NetworkingClients.SOCKET
        )

        val syftWorker = Syft.getInstance(syftConfiguration)
        val job = syftWorker.newJob("test", "1")
        val jobStatusSubscriber = spy<JobStatusSubscriber>()
        job.request(jobStatusSubscriber)
        verify(jobStatusSubscriber).onRejected(any())
        syftWorker.dispose()
        verify(jobStatusSubscriber).onComplete()
    }

    @Test
    @Config(shadows = [ShadowPlan::class])
    fun `Test workflow with ping error`() {
        val socketClient = SocketClientMock(
            authenticateSuccess = true,
            cycleSuccess = true
        )
        val httpClient = HttpClientMock(
            pingSuccess = false, downloadSpeedSuccess = true,
            uploadSuccess = true, downloadPlanSuccess = true, downloadModelSuccess = true
        )

        val syftConfiguration = SyftConfiguration(
            context,
            networkingSchedulers,
            computeSchedulers,
            context.filesDir,
            true,
            batteryCheckEnabled = true,
            networkConstraints = networkConstraints,
            transportMedium = NetworkCapabilities.TRANSPORT_WIFI,
            cacheTimeOut = 0,
            maxConcurrentJobs = 1,
            socketClient = socketClient.getMockedClient(),
            httpClient = httpClient.getMockedClient(),
            messagingClient = SyftConfiguration.NetworkingClients.SOCKET
        )

        val syftWorker = Syft.getInstance(syftConfiguration)
        val job = syftWorker.newJob("test", "1")
        val jobStatusSubscriber = spy<JobStatusSubscriber>()
        job.request(jobStatusSubscriber)
        verify(jobStatusSubscriber).onError(any())
        syftWorker.dispose()
        verify(jobStatusSubscriber, never()).onComplete()
    }

    @Test
    @Config(shadows = [ShadowPlan::class])
    fun `Test workflow with download speed test error`() {
        val socketClient = SocketClientMock(
            authenticateSuccess = true,
            cycleSuccess = true
        )
        val httpClient = HttpClientMock(
            pingSuccess = true, downloadSpeedSuccess = false,
            uploadSuccess = true, downloadPlanSuccess = true, downloadModelSuccess = true
        )

        val syftConfiguration = SyftConfiguration(
            context,
            networkingSchedulers,
            computeSchedulers,
            context.filesDir,
            true,
            batteryCheckEnabled = true,
            networkConstraints = networkConstraints,
            transportMedium = NetworkCapabilities.TRANSPORT_WIFI,
            cacheTimeOut = 0,
            maxConcurrentJobs = 1,
            socketClient = socketClient.getMockedClient(),
            httpClient = httpClient.getMockedClient(),
            messagingClient = SyftConfiguration.NetworkingClients.SOCKET
        )

        val syftWorker = Syft.getInstance(syftConfiguration)
        val job = syftWorker.newJob("test", "1")
        val jobStatusSubscriber = spy<JobStatusSubscriber>()
        job.request(jobStatusSubscriber)
        verify(jobStatusSubscriber).onError(any())
        syftWorker.dispose()
        verify(jobStatusSubscriber, never()).onComplete()
    }

    @Test
    @Config(shadows = [ShadowPlan::class])
    fun `Test workflow with upload speed test error`() {
        val socketClient = SocketClientMock(
            authenticateSuccess = true,
            cycleSuccess = true
        )
        val httpClient = HttpClientMock(
            pingSuccess = true, downloadSpeedSuccess = true,
            uploadSuccess = false, downloadPlanSuccess = true, downloadModelSuccess = true
        )

        val syftConfiguration = SyftConfiguration(
            context,
            networkingSchedulers,
            computeSchedulers,
            context.filesDir,
            true,
            batteryCheckEnabled = true,
            networkConstraints = networkConstraints,
            transportMedium = NetworkCapabilities.TRANSPORT_WIFI,
            cacheTimeOut = 0,
            maxConcurrentJobs = 1,
            socketClient = socketClient.getMockedClient(),
            httpClient = httpClient.getMockedClient(),
            messagingClient = SyftConfiguration.NetworkingClients.SOCKET
        )

        val syftWorker = Syft.getInstance(syftConfiguration)
        val job = syftWorker.newJob("test", "1")
        val jobStatusSubscriber = spy<JobStatusSubscriber>()
        job.request(jobStatusSubscriber)
        verify(jobStatusSubscriber).onError(any())
        syftWorker.dispose()
        verify(jobStatusSubscriber, never()).onComplete()
    }

}