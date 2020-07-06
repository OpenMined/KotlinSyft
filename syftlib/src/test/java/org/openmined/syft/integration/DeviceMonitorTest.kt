package org.openmined.syft.integration

import android.content.Intent
import android.net.NetworkCapabilities
import android.os.Looper.getMainLooper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
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
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow

@ExperimentalUnsignedTypes
class DeviceMonitorTest : AbstractSyftWorkerTest() {
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
        batteryCheckEnabled = true,
        networkConstraints = networkConstraints,
        transportMedium = NetworkCapabilities.TRANSPORT_WIFI,
        cacheTimeOut = 0,
        maxConcurrentJobs = 1,
        socketClient = socketClient.getMockedClient(),
        httpClient = httpClient.getMockedClient(),
        messagingClient = SyftConfiguration.NetworkingClients.SOCKET
    )

    @Test
    @Config(shadows = [ShadowPlan::class])
    fun `Test job error when charging disconnects`() {
        val syftWorker = Syft.getInstance(syftConfiguration)
        val job = syftWorker.newJob("test", "1")

        val jobStatusSubscriber = spy<JobStatusSubscriber>()

        val chargingStatus = Shadow.newInstanceOf(Intent::class.java)
        chargingStatus.action = Intent.ACTION_POWER_DISCONNECTED
        // registered receiver updates battery status
        context.sendBroadcast(chargingStatus)
        shadowOf(getMainLooper()).idle();

        job.start(jobStatusSubscriber)
        argumentCaptor<Throwable>().apply {
            verify(jobStatusSubscriber).onError(capture())
            assert(firstValue is IllegalStateException)
            assert(firstValue.message == "Battery constraints failed")
        }
        syftWorker.dispose()
        verify(jobStatusSubscriber, never()).onComplete()
    }

    @Test
    @Config(shadows = [ShadowPlan::class])
    fun `Test job success when charging reconnects`() {
        val syftWorker = Syft.getInstance(syftConfiguration)
        val job = syftWorker.newJob("test", "1")

        val jobStatusSubscriber = spy<JobStatusSubscriber>()

        // registered receiver updates battery status
        val chargingStatus = Shadow.newInstanceOf(Intent::class.java)
        chargingStatus.action = Intent.ACTION_POWER_DISCONNECTED
        context.sendBroadcast(chargingStatus)

        // registered receiver updates battery status again
        val laterCharge = Shadow.newInstanceOf(Intent::class.java)
        chargingStatus.action = Intent.ACTION_POWER_CONNECTED
        context.sendBroadcast(laterCharge)
        shadowOf(getMainLooper()).idle();

        job.start(jobStatusSubscriber)
        verify(jobStatusSubscriber).onReady(any(), any(), any())
        syftWorker.dispose()
        verify(jobStatusSubscriber).onComplete()
    }

    @Test
    @Config(shadows = [ShadowPlan::class])
    fun `Test job error when network disconnects`() {
        val syftWorker = Syft.getInstance(syftConfiguration)
        val job = syftWorker.newJob("test", "1")
        val jobStatusSubscriber = spy<JobStatusSubscriber>()

        val shadowNetworkManager = getShadowConnectivityManager()
        shadowNetworkManager.networkCallbacks.forEach {
            it.onLost(getConnectivityManager().activeNetwork!!)
        }
        shadowOf(getMainLooper()).idle();
        job.start(jobStatusSubscriber)
        argumentCaptor<Throwable>().apply {
            verify(jobStatusSubscriber).onError(capture())
            assert(firstValue is IllegalStateException)
            assert(firstValue.message == "network constraints failed")
        }
        syftWorker.dispose()
        verify(jobStatusSubscriber, never()).onComplete()
    }

    @Test
    @Config(shadows = [ShadowPlan::class])
    fun `Test job success when network reconnects`() {
        val syftWorker = Syft.getInstance(syftConfiguration)
        val job = syftWorker.newJob("test", "1")
        val jobStatusSubscriber = spy<JobStatusSubscriber>()

        val shadowNetworkManager = getShadowConnectivityManager()
        val networkManger = getConnectivityManager()

        shadowNetworkManager.networkCallbacks.forEach {
            it.onLost(networkManger.activeNetwork!!)
        }

        shadowNetworkManager.networkCallbacks.forEach {
            it.onAvailable(networkManger.activeNetwork!!)
        }
        shadowOf(getMainLooper()).idle();

        job.start(jobStatusSubscriber)
        verify(jobStatusSubscriber).onReady(any(), any(), any())
        syftWorker.dispose()
        verify(jobStatusSubscriber).onComplete()
    }
}