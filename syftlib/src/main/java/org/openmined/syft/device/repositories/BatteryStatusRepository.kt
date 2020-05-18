package org.openmined.syft.device.repositories

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import org.openmined.syft.device.models.CHARGE_TYPE

class BatteryStatusRepository(private val context: Context)  {

    private val batteryStatus = context.registerReceiver(
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    )

    fun getBatteryLevel(): Float? {
        return batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale.toFloat()
        }
    }

    fun checkIfCharging(): Boolean {
        val charge = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return charge == BatteryManager.BATTERY_STATUS_CHARGING
               || charge == BatteryManager.BATTERY_STATUS_FULL
    }

    fun chargeType(): CHARGE_TYPE? {
        return when (batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1) {
            BatteryManager.BATTERY_PLUGGED_USB ->
                CHARGE_TYPE.USB
            BatteryManager.BATTERY_PLUGGED_AC ->
                CHARGE_TYPE.AC
            else ->
                null
        }
    }
}