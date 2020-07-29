package org.openmined.syft.networking.datamodels.syft

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonInput
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonOutput
import kotlinx.serialization.json.json
import org.openmined.syft.networking.datamodels.NetworkModels

internal const val AUTH_TYPE = "model-centric/authenticate"
internal const val AUTH_SUCCESS = "success"
internal const val AUTH_FAILURE = "rejected"

@Serializable
internal data class AuthenticationRequest(
    @SerialName("auth_token")
    val authToken: String? = null,
    @SerialName("model_name")
    val modelName: String,
    @SerialName("model_version")
    val version: String? = null
) : NetworkModels()

@Serializable(with = AuthenticationResponseSerializer::class)
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

@Serializer(forClass = AuthenticationResponse::class)
internal class AuthenticationResponseSerializer : KSerializer<AuthenticationResponse> {
    private val json = Json(JsonConfiguration.Stable)
    override val descriptor: SerialDescriptor
        get() = SerialClassDescImpl("AuthResponseSerializer")

    override fun deserialize(decoder: Decoder): AuthenticationResponse {
        val input = decoder as? JsonInput
                    ?: throw SerializationException("This class can be loaded only by Json")
        val response = input.decodeJson() as? JsonObject
                       ?: throw SerializationException("Expected JsonObject")
        val data = json {
            response.forEach { key, value ->
                if (key != "status")
                    key to value
            }
        }
        return if (response.getPrimitive("status").content == AUTH_SUCCESS)
            json.parse(
                AuthenticationResponse.AuthenticationSuccess.serializer(),
                data.toString()
            )
        else
            json.parse(AuthenticationResponse.AuthenticationError.serializer(), data.toString())
    }

    override fun serialize(encoder: Encoder, obj: AuthenticationResponse) {
        val output = encoder as? JsonOutput
                     ?: throw SerializationException("This class can be saved only by Json")
        when (obj) {
            is AuthenticationResponse.AuthenticationSuccess -> output.encodeJson(
                json.toJson(
                    AuthenticationResponse.AuthenticationSuccess.serializer(),
                    obj
                )
            )
            is AuthenticationResponse.AuthenticationError ->
                output.encodeJson(
                    json.toJson(
                        AuthenticationResponse.AuthenticationError.serializer(),
                        obj
                    )
                )
        }
    }
}
