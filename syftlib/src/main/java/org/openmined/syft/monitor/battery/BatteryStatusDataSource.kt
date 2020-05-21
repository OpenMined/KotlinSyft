package org.openmined.syft.monitor.battery

import android.content.Intent
import android.os.BatteryManager

class BatteryStatusDataSource(private val batteryStatusIntent: Intent?) {

    fun getBatteryLevel(): Float? {
        return batteryStatusIntent?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale.toFloat()
        }
    }

    fun checkIfCharging(): Boolean {
        val charge = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return charge == BatteryManager.BATTERY_STATUS_CHARGING
               || charge == BatteryManager.BATTERY_STATUS_FULL
    }

    fun chargeType(): CHARGE_TYPE? {
        return when (batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1) {
            BatteryManager.BATTERY_PLUGGED_USB ->
                CHARGE_TYPE.USB
            BatteryManager.BATTERY_PLUGGED_AC ->
                CHARGE_TYPE.AC
            else ->
                null
        }
    }
}