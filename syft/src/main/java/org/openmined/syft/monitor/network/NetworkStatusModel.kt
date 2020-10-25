package org.openmined.syft.monitor.network

import kotlin.Float.Companion.POSITIVE_INFINITY

internal data class NetworkStatusModel(
    val ping: Int? = null,
    val downloadSpeed: Float? = null,
    val uploadSpeed: Float? = null,
    val networkValidity: Boolean = false,
    val cacheTimeStamp: Long = POSITIVE_INFINITY.toLong()
)