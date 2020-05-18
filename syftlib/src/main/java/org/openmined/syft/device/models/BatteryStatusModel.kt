package org.openmined.syft.device.models

data class BatteryStatusModel(
    var isCharging: Boolean,
    var batteryLevel: Float?,
    var chargePlug: CHARGE_TYPE?,
    override var cacheTimeStamp: Long = 0
) : DeviceStatus

enum class CHARGE_TYPE {
    USB, AC
}