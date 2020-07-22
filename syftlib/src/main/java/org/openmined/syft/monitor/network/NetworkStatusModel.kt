package org.openmined.syft.monitor.network

import kotlin.Float.Companion.POSITIVE_INFINITY

internal data class NetworkStatusModel(
    var ping: Int? = null,
    var downloadSpeed: Float? = null,
    var uploadSpeed: Float? = null,
    var networkValidity: Boolean = false,
    var cacheTimeStamp: Long = POSITIVE_INFINITY.toLong()
)