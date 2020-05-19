package org.openmined.syft.monitor.battery

import android.content.Context

class BatteryStatusRepository(context: Context) {
    private val batteryStatusDataSource =
            BatteryStatusDataSource(context)

    fun getBatteryState() = BatteryStatusModel(
        batteryStatusDataSource.checkIfCharging(),
        batteryStatusDataSource.getBatteryLevel(),
        batteryStatusDataSource.chargeType(),
        System.currentTimeMillis()
    )

    //todo add battery state change listener
}