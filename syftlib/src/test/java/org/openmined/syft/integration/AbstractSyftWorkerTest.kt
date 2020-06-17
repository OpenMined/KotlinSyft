package org.openmined.syft.integration

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.BatteryManager
import androidx.test.core.app.ApplicationProvider
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.runner.RunWith
import org.openmined.syft.threading.ProcessSchedulers
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowNetworkCapabilities

@ExperimentalUnsignedTypes
@RunWith(RobolectricTestRunner::class)
abstract class AbstractSyftWorkerTest {

    protected val context: Context = ApplicationProvider.getApplicationContext()
    protected val networkingSchedulers = object : ProcessSchedulers {
        override val computeThreadScheduler: Scheduler
            get() = Schedulers.trampoline()
        override val calleeThreadScheduler: Scheduler
            get() = Schedulers.trampoline()
    }
    protected val computeSchedulers = object : ProcessSchedulers {
        override val computeThreadScheduler: Scheduler
            get() = Schedulers.trampoline()
        override val calleeThreadScheduler: Scheduler
            get() = Schedulers.trampoline()
    }

    @Before
    open fun initialiseContext() {
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
}