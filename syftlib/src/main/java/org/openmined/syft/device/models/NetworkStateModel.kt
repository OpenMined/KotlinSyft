package org.openmined.syft.device.models

data class NetworkStateModel(
    var ping: String? = null,
    var downloadSpeed: String? = null,
    var uploadspeed: String? = null,
    var networkValidity: Boolean = false,
    override var cacheTimeStamp: Long = 0
) : DeviceStatus