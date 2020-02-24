package org.openmined.syft.networking.datamodels.syft

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openmined.syft.networking.datamodels.NetworkModels

const val AUTH_TYPE = "federated/authenticate"

@Serializable
data class AuthenticationSuccess(
    @SerialName("worker_id")
    val workerId: String
) : NetworkModels()