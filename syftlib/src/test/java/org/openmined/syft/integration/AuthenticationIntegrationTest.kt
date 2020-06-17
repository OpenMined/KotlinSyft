package org.openmined.syft.integration

import android.net.NetworkCapabilities
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.openmined.syft.Syft
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.execution.JobStatusSubscriber
import org.openmined.syft.integration.clients.HttpClientMock
import org.openmined.syft.integration.clients.SocketClientMock
import org.openmined.syft.integration.execution.ShadowPlan
import org.robolectric.annotation.Config

@ExperimentalUnsignedTypes
class AuthenticationIntegrationTest : AbstractSyftWorkerTest() {

    @Test
    @Config(shadows = [ShadowPlan::class])
    fun `Test success workflow`() {
        val socketClient = SocketClientMock(
            authenticateSuccess = true,
            cycleSuccess = true
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
            listOf(),
            NetworkCapabilities.TRANSPORT_WIFI,
            0,
            socketClient.getMockedClient(),
            httpClient.getMockedClient(),
            1,
            SyftConfiguration.NetworkingClients.SOCKET
        )

        val syftWorker = Syft.getInstance(syftConfiguration, "test token")
        val job = syftWorker.newJob("test", "1")
        val jobStatusSubscriber = spy<JobStatusSubscriber>()
        job.start(jobStatusSubscriber)
        verify(jobStatusSubscriber).onReady(any(), any(), any())
        syftWorker.dispose()
        verify(jobStatusSubscriber).onComplete()
    }

    @Test
    @Config(shadows = [ShadowPlan::class])
    fun `Test workflow with no auth token`() {
        val socketClient = SocketClientMock(
            authenticateSuccess = true,
            cycleSuccess = true
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
            listOf(),
            NetworkCapabilities.TRANSPORT_WIFI,
            0,
            socketClient.getMockedClient(),
            httpClient.getMockedClient(),
            1,
            SyftConfiguration.NetworkingClients.SOCKET
        )

        val syftWorker = Syft.getInstance(syftConfiguration)
        val job = syftWorker.newJob("test", "1")
        val jobStatusSubscriber = spy<JobStatusSubscriber>()
        job.start(jobStatusSubscriber)
        verify(jobStatusSubscriber).onReady(any(), any(), any())
        syftWorker.dispose()
        verify(jobStatusSubscriber).onComplete()
    }

    @Test
    @Config(shadows = [ShadowPlan::class])
    fun `Test workflow with authentication failure`() {
        val socketClient = SocketClientMock(
            authenticateSuccess = false,
            cycleSuccess = true
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
            listOf(),
            NetworkCapabilities.TRANSPORT_WIFI,
            0,
            socketClient.getMockedClient(),
            httpClient.getMockedClient(),
            1,
            SyftConfiguration.NetworkingClients.SOCKET
        )

        val syftWorker = Syft.getInstance(syftConfiguration)
        val job = syftWorker.newJob("test", "1")
        val jobStatusSubscriber = spy<JobStatusSubscriber>()
        job.start(jobStatusSubscriber)
        verify(jobStatusSubscriber).onError(any())
        syftWorker.dispose()
        verify(jobStatusSubscriber, never()).onComplete()
    }

}