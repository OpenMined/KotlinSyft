package org.openmined.syft.integration

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import androidx.test.core.app.ApplicationProvider
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.runner.RunWith
import org.openmined.syft.threading.ProcessSchedulers
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.LooperMode.Mode.PAUSED
import org.robolectric.annotation.LooperMode
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowConnectivityManager
import org.robolectric.shadows.ShadowNetworkCapabilities

@ExperimentalUnsignedTypes
@RunWith(RobolectricTestRunner::class)
@LooperMode(PAUSED)
abstract class AbstractSyftWorkerTest {

    protected val context: Context = ApplicationProvider.getApplicationContext()
    protected val networkConstraints = listOf(
        NetworkCapabilities.NET_CAPABILITY_INTERNET,
        NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED,
        NetworkCapabilities.NET_CAPABILITY_NOT_METERED
    )
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
        val networkManager = getConnectivityManager()
        val networkCapability = ShadowNetworkCapabilities.newInstance()
        val shadowNC = Shadows.shadowOf(networkCapability)
        shadowNC.addTransportType(ConnectivityManager.TYPE_WIFI)
        networkConstraints.forEach { shadowNC.addCapability(it) }
        getShadowConnectivityManager()
                .setNetworkCapabilities(networkManager.activeNetwork, networkCapability)

        val batteryStatus = Shadow.newInstanceOf(Intent::class.java)
        batteryStatus.action = Intent.ACTION_BATTERY_CHANGED
        batteryStatus.putExtra(BatteryManager.EXTRA_LEVEL, 1000)
        batteryStatus.putExtra(BatteryManager.EXTRA_SCALE, 4000)
        batteryStatus.putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_CHARGING)
        context.sendStickyBroadcast(batteryStatus)
    }

    fun getConnectivityManager(): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun getShadowConnectivityManager(): ShadowConnectivityManager {
        return Shadows.shadowOf(getConnectivityManager())
    }
}