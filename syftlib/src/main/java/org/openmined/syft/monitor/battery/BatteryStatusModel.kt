package org.openmined.syft.monitor.battery

data class BatteryStatusModel(
    var isCharging: Boolean,
    var batteryLevel: Float?,
    var chargePlug: CHARGE_TYPE?,
    var cacheTimeStamp: Long = 0
)

enum class CHARGE_TYPE {
    USB, AC
}