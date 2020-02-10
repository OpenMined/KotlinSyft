package org.openmined.syft.networking.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationSuccess(
    @SerialName("worker_id")
    val workerId: String
) : RequestResponseBody