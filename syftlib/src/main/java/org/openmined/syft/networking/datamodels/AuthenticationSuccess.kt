package org.openmined.syft.networking.datamodels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val AUTH_TYPE = "federated/authenticate"

@Serializable
data class AuthenticationSuccess(
    @SerialName("worker_id")
    val workerId: String
) : NetworkModels()