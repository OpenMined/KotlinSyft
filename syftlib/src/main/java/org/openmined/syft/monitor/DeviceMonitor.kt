package org.openmined.syft.monitor

import io.reactivex.processors.PublishProcessor
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.monitor.battery.BatteryStatusRepository
import org.openmined.syft.monitor.network.NetworkStatusRepository

@ExperimentalUnsignedTypes
class DeviceMonitor(
    syftConfig: SyftConfiguration
) {

    private val networkStatusRepository = NetworkStatusRepository.initialize(syftConfig)
    private val batteryStatusRepository = BatteryStatusRepository.initialize(syftConfig)
    private val statusProcessor = PublishProcessor.create<StateChangeMessage>()

    fun getNetworkStatus(workerId: String) = networkStatusRepository.getNetworkStatus(workerId)

    fun getBatteryStatus() = batteryStatusRepository.getBatteryState()

    fun getStatusProcessor() = statusProcessor

    //TODO("add appropriate register/deregister broadcasts here")
}