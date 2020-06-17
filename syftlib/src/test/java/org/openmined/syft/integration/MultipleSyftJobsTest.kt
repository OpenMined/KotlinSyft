package org.openmined.syft.integration

import android.net.NetworkCapabilities
import com.nhaarman.mockitokotlin2.any
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
class MultipleSyftJobsTest : AbstractSyftWorkerTest() {
    private val socketClient = SocketClientMock(
        authenticateSuccess = true,
        cycleSuccess = true
    )
    private val httpClient = HttpClientMock(
        pingSuccess = true, downloadSpeedSuccess = true,
        uploadSuccess = true, downloadPlanSuccess = true, downloadModelSuccess = true
    )
    private val syftConfiguration = SyftConfiguration(
        context,
        networkingSchedulers,
        computeSchedulers,
        context.filesDir,
        true,
        networkConstraints,
        NetworkCapabilities.TRANSPORT_WIFI,
        0,
        2,
        socketClient.getMockedClient(),
        httpClient.getMockedClient(),
        SyftConfiguration.NetworkingClients.SOCKET
    )

    @Test
    @Config(shadows = [ShadowPlan::class])
    fun `Test successful execution of multiple jobs`() {
        val syftWorker = Syft.getInstance(syftConfiguration)
        val job1 = syftWorker.newJob("test", "1")
        val job2 = syftWorker.newJob("test2", "1")
        val jobStatusSubscriber1 = spy<JobStatusSubscriber>()
        val jobStatusSubscriber2 = spy<JobStatusSubscriber>()

        job1.start(jobStatusSubscriber1)
        job2.start(jobStatusSubscriber2)
        verify(jobStatusSubscriber1).onReady(any(), any(), any())
        verify(jobStatusSubscriber2).onReady(any(), any(), any())
        syftWorker.dispose()
        verify(jobStatusSubscriber1).onComplete()
        verify(jobStatusSubscriber2).onComplete()
    }

    @Test
    @Config(shadows = [ShadowPlan::class])
    fun `throw error on exceeding job limit`() {
        val syftWorker = Syft.getInstance(syftConfiguration)
        val job1 = syftWorker.newJob("test", "1")
        val job2 = syftWorker.newJob("test2", "1")
        try {
            val job3 = syftWorker.newJob("should fail", "1")
            assert(false)
        } catch (e: IndexOutOfBoundsException) {
            assert(true)
        }
    }

}