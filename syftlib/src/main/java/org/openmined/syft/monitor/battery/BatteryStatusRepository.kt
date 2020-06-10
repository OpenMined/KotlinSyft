package org.openmined.syft.monitor.battery

import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.monitor.BroadCastListener

@ExperimentalUnsignedTypes
class BatteryStatusRepository internal constructor(
    private val batteryStatusDataSource: BatteryStatusDataSource
) : BroadCastListener{
    companion object {
        fun initialize(configuration: SyftConfiguration): BatteryStatusRepository {
            return BatteryStatusRepository(BatteryStatusDataSource.initialize(configuration))
        }
    }

    fun getBatteryState() = BatteryStatusModel(
        batteryStatusDataSource.checkIfCharging(),
        batteryStatusDataSource.getBatteryLevel(),
        System.currentTimeMillis()
    )

    override fun subscribeStateChange() = batteryStatusDataSource.subscribeStateChange()

    override fun unsubscribeStateChange() {
        batteryStatusDataSource.unsubscribeStateChange()
    }

}