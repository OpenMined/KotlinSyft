package org.openmined.syft.monitor.battery

data class BatteryStatusModel(
    var isCharging: Boolean,
    var batteryLevel: Float?,
    var cacheTimeStamp: Long = 0
)