package org.openmined.syft.monitor

import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.monitor.battery.BatteryStatusRepository
import org.openmined.syft.monitor.network.NetworkStatusRepository

private const val TAG = "device monitor"

@ExperimentalUnsignedTypes
class DeviceMonitor(
    syftConfig: SyftConfiguration
) {

    private val networkStatusRepository = NetworkStatusRepository.initialize(syftConfig)
    private val batteryStatusRepository = BatteryStatusRepository.initialize(syftConfig)
    private val statusProcessor = networkStatusRepository.subscribeStateChange()
            .mergeWith(batteryStatusRepository.subscribeStateChange())


    fun getNetworkStatus(workerId: String) = networkStatusRepository.getNetworkStatus(workerId)

    fun getBatteryStatus() = batteryStatusRepository.getBatteryState()

    fun getStatusProcessor() = statusProcessor

}