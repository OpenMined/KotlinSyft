package org.openmined.syft.device.models

data class NetworkStateModel(
    var ping: String? = null,
    var downloadSpeed: String? = null,
    var uploadspeed: String? = null,
    var wifiStatus: Boolean? = null
)