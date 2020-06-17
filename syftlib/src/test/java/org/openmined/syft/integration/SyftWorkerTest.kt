package org.openmined.syft.integration

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.openmined.syft.Syft
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.execution.JobStatusSubscriber
import org.openmined.syft.integration.clients.HttpClientMock
import org.openmined.syft.integration.clients.SocketClientMock
import org.openmined.syft.integration.execution.ShadowPlan
import org.openmined.syft.monitor.DeviceMonitor
import org.openmined.syft.threading.ProcessSchedulers
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowNetworkCapabilities

@ExperimentalUnsignedTypes
@RunWith(RobolectricTestRunner::class)
class SyftWorkerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val networkingSchedulers = object : ProcessSchedulers {
        override val computeThreadScheduler: Scheduler
            get() = Schedulers.trampoline()
        override val calleeThreadScheduler: Scheduler
            get() = Schedulers.trampoline()
    }
    private val computeSchedulers = object : ProcessSchedulers {
        override val computeThreadScheduler: Scheduler
            get() = Schedulers.trampoline()
        override val calleeThreadScheduler: Scheduler
            get() = Schedulers.trampoline()
    }

    @Before
    fun `initialise context`() {
        val networkManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapability = ShadowNetworkCapabilities.newInstance()
        Shadows.shadowOf(networkCapability).addTransportType(ConnectivityManager.TYPE_WIFI)
        Shadows.shadowOf(networkManager)
                .setNetworkCapabilities(networkManager.activeNetwork, networkCapability)

        val batteryStatus = Shadow.newInstanceOf(Intent::class.java)
        batteryStatus.action = Intent.ACTION_BATTERY_CHANGED
        batteryStatus.putExtra(BatteryManager.EXTRA_LEVEL, 1000)
        batteryStatus.putExtra(BatteryManager.EXTRA_SCALE, 4000)
        batteryStatus.putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_CHARGING)
        context.sendStickyBroadcast(batteryStatus)
    }

    @Test
    @Config(shadows = [ShadowPlan::class])
    fun `Test success workflow`() {
        val socketClient =
                SocketClientMock(
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

        val deviceMonitor = DeviceMonitor.construct(syftConfiguration)
        val syftWorker = Syft(syftConfiguration, deviceMonitor,"test token")
        val job = syftWorker.newJob("test", "1")
        val jobStatusSubscriber = spy<JobStatusSubscriber>()
        job.start(jobStatusSubscriber)
        verify(jobStatusSubscriber).onReady(any(), any(), any())
        job.dispose()
        verify(jobStatusSubscriber).onComplete()
    }

    @Test
    @Config(shadows = [ShadowPlan::class])
    fun `Test workflow with no auth token`() {
        val socketClient =
                SocketClientMock(
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

        val syftWorker = Syft.getInstance(syftConfiguration,null)
        val job = syftWorker.newJob("test", "1")
        val jobStatusSubscriber = spy<JobStatusSubscriber>()
        job.start(jobStatusSubscriber)
        verify(jobStatusSubscriber).onReady(any(), any(), any())
        syftWorker.dispose()
        verify(jobStatusSubscriber).onComplete()
    }

}