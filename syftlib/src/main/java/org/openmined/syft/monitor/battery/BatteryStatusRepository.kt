package org.openmined.syft.monitor.battery

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import org.openmined.syft.domain.SyftConfiguration

@ExperimentalUnsignedTypes
class BatteryStatusRepository internal constructor(
    private val batteryStatusDataSource: BatteryStatusDataSource
) {
    companion object {
        fun initialize(
            configuration: SyftConfiguration,
            receiver: BroadcastReceiver? = null
        ): BatteryStatusRepository {

            val batteryStatus = configuration.context.registerReceiver(
                receiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val batteryStatusDataSource = BatteryStatusDataSource(batteryStatus)
            return BatteryStatusRepository(batteryStatusDataSource)
        }
    }

    fun getBatteryState() = BatteryStatusModel(
        batteryStatusDataSource.checkIfCharging(),
        batteryStatusDataSource.getBatteryLevel(),
        batteryStatusDataSource.chargeType(),
        System.currentTimeMillis()
    )

}