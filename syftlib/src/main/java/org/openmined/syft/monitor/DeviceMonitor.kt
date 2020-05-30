package org.openmined.syft.monitor

import android.util.Log
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.monitor.battery.BatteryStatusRepository
import org.openmined.syft.monitor.battery.CHARGE_TYPE
import org.openmined.syft.monitor.network.NetworkStatusRepository
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "device monitor"

@ExperimentalUnsignedTypes
class DeviceMonitor(
    syftConfig: SyftConfiguration
) {

    private val networkStatusRepository = NetworkStatusRepository.initialize(syftConfig)
    private val batteryStatusRepository = BatteryStatusRepository.initialize(syftConfig)

    //todo this should be initiated when subscribe is called by start job
    private val statusProcessor = networkStatusRepository.subscribeStateChange()
            .mergeWith(batteryStatusRepository.subscribeStateChange())

    private val networkValidity = AtomicBoolean(false)
    private val batteryValidity = AtomicBoolean(true)
    private val userValidity = AtomicBoolean(true)

    private val disposable = statusProcessor
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

    fun isNetworkStateValid() = networkValidity.get()
    fun isBatteryStateValid() = batteryValidity.get()
    fun isActivityStateValid() = userValidity.get()

    fun getNetworkStatus(workerId: String) = networkStatusRepository.getNetworkStatus(workerId)
    fun getBatteryStatus() = batteryStatusRepository.getBatteryState()
    fun getStatusProcessor() = statusProcessor
}