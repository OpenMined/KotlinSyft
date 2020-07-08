package org.openmined.syft.networking.datamodels.syft

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openmined.syft.networking.datamodels.NetworkModels

internal const val AUTH_TYPE = "federated/authenticate"
internal const val AUTH_SUCCESS = "success"
internal const val AUTH_FAILURE = "rejected"

@Serializable
internal data class AuthenticationRequest(
    @SerialName("auth_token")
    val authToken: String? = null
) : NetworkModels()

@Serializable
internal sealed class AuthenticationResponse : NetworkModels() {

    @SerialName(AUTH_SUCCESS)
    @Serializable
    data class AuthenticationSuccess(
        @SerialName("worker_id")
        val workerId: String,
        @SerialName("requires_speed_test")
        val requiresSpeedTest: Boolean = true
    ) : AuthenticationResponse()

    @SerialName(AUTH_FAILURE)
    @Serializable
    data class AuthenticationError(
        @SerialName("error")
        val errorMessage: String
    ) : AuthenticationResponse()
}
