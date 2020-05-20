package org.openmined.syft.monitor.network

import kotlin.Float.Companion.POSITIVE_INFINITY

data class NetworkStatusModel(
    var ping: String? = null,
    var downloadSpeed: String? = null,
    var uploadspeed: String? = null,
    var networkValidity: Boolean = false,
    var cacheTimeStamp: Long = POSITIVE_INFINITY.toLong()
)