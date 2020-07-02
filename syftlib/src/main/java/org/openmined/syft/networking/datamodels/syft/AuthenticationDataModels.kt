package org.openmined.syft.networking.datamodels.syft

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openmined.syft.networking.datamodels.NetworkModels

const val AUTH_TYPE = "federated/authenticate"
const val AUTH_SUCCESS = "success"
const val AUTH_FAILURE = "rejected"

@Serializable
data class AuthenticationRequest(
    @SerialName("auth_token")
    val authToken: String? = null
) : NetworkModels()

@Serializable
sealed class AuthenticationResponse : NetworkModels() {

    @SerialName(AUTH_SUCCESS)
    @Serializable
    data class AuthenticationSuccess(
        @SerialName("worker_id")
        val workerId: String,
        @SerialName("requires_speed_test")
        val requiresSpeedTest: Boolean
    ) : AuthenticationResponse()

    @SerialName(AUTH_FAILURE)
    @Serializable
    data class AuthenticationError(
        @SerialName("error")
        val errorMessage: String
    ) : AuthenticationResponse()
}
