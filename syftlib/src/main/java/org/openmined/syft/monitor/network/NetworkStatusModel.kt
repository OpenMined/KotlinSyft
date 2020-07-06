package org.openmined.syft.monitor.network

import kotlin.Float.Companion.POSITIVE_INFINITY

internal data class NetworkStatusModel(
    var ping: String? = null,
    var downloadSpeed: String? = null,
    var uploadSpeed: String? = null,
    var networkValidity: Boolean = false,
    var cacheTimeStamp: Long = POSITIVE_INFINITY.toLong()
)