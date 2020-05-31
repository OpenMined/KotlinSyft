package org.openmined.syft.monitor

import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.monitor.battery.BatteryStatusRepository
import org.openmined.syft.monitor.battery.CHARGE_TYPE
import org.openmined.syft.monitor.network.NetworkStatusRepository
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "device monitor"

@ExperimentalUnsignedTypes
class DeviceMonitor(
    syftConfig: SyftConfiguration
) : Disposable {

    private val networkStatusRepository = NetworkStatusRepository.initialize(syftConfig)
    private val batteryStatusRepository = BatteryStatusRepository.initialize(syftConfig)

    private val isDisposed = AtomicBoolean(false)

    private val networkValidity = AtomicBoolean(true)
    private val batteryValidity = AtomicBoolean(true)
    private val userValidity = AtomicBoolean(true)
    private val compositeDisposable = CompositeDisposable()

    init {
        subscribe()
    }

    fun isNetworkStateValid(): Boolean {
        if (isDisposed.get())
            subscribe()
        return networkValidity.get()
    }

    fun isBatteryStateValid(): Boolean {
        if (isDisposed.get())
            subscribe()
        return batteryValidity.get()
    }

    fun isActivityStateValid(): Boolean {
        if (isDisposed.get())
            subscribe()
        return userValidity.get()
    }

    fun getNetworkStatus(workerId: String) = networkStatusRepository.getNetworkStatus(workerId)
    fun getBatteryStatus() = batteryStatusRepository.getBatteryState()

    private fun subscribe() {
        compositeDisposable.add(networkStatusRepository.subscribeStateChange()
                //todo add other processors here when written
                .subscribe {
                    when (it) {
                        is StateChangeMessage.Charging ->
                            if (it.chargingState == CHARGE_TYPE.AC)
                                Log.d(TAG, "charging to plug")
                            else
                                Log.d(TAG, "Charging with USB")

                        is StateChangeMessage.NetworkStatus ->
                            if (it.connected) {
                                networkValidity.set(true)
                                Log.d(TAG, "connected to the required network")
                            } else {
                                networkValidity.set(false)
                                Log.d(TAG, "disconnected to valid network")
                            }

                        is StateChangeMessage.Activity ->
                            Log.d(TAG, "user activity started")
                    }
                }
        )

        networkValidity.set(networkStatusRepository.getNetworkValidity())
        //todo set battery validity here as well
        isDisposed.set(false)
    }

    override fun isDisposed() = isDisposed.get()

    override fun dispose() {
        compositeDisposable.clear()
        networkStatusRepository.unsubscribeStateChange()
        isDisposed.set(true)
    }
}