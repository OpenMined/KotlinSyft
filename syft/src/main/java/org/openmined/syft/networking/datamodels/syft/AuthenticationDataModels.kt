package org.openmined.syft.networking.datamodels.syft

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonPrimitive
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
    val version: String? = null,
) : NetworkModels()

@ExperimentalSerializationApi
@Serializable(with = AuthenticationResponseSerializer::class)
internal sealed class AuthenticationResponse : NetworkModels() {

    @SerialName(AUTH_SUCCESS)
    @Serializable
    data class AuthenticationSuccess(
        @SerialName("worker_id")
        val workerId: String,
        @SerialName("requires_speed_test")
        val requiresSpeedTest: Boolean = true,
    ) : AuthenticationResponse()

    @SerialName(AUTH_FAILURE)
    @Serializable
    data class AuthenticationError(
        @SerialName("error")
        val errorMessage: String,
    ) : AuthenticationResponse()
}

@ExperimentalSerializationApi
@Serializer(forClass = AuthenticationResponse::class)
internal class AuthenticationResponseSerializer : KSerializer<AuthenticationResponse> {

    override val descriptor: SerialDescriptor = AuthenticationResponse.serializer().descriptor

    override fun deserialize(decoder: Decoder): AuthenticationResponse {
        require(decoder is JsonDecoder) { throw SerializationException("This class can be loaded only by Json") }

        val response = decoder.decodeJsonElement() as? JsonObject
                       ?: throw SerializationException("Expected JsonObject")

        val data = buildJsonArray {
            response.forEach { key, value ->
                if (key != "status")
                    key to value
            }
        }
        return if (response["status"]?.jsonPrimitive?.content == AUTH_SUCCESS)
            Json.decodeFromString(
                AuthenticationResponse.AuthenticationSuccess.serializer(),
                data.toString()
            )
        else
            Json.decodeFromString(AuthenticationResponse.AuthenticationError.serializer(),
                data.toString())
    }

    override fun serialize(encoder: Encoder, obj: AuthenticationResponse) {
        require(encoder is JsonEncoder) { throw SerializationException("This class can only be saved by Json encoder") }

        when (obj) {
            is AuthenticationResponse.AuthenticationSuccess -> encoder.encodeJsonElement(
                Json.encodeToJsonElement(
                    AuthenticationResponse.AuthenticationSuccess.serializer(),
                    obj
                )
            )
            is AuthenticationResponse.AuthenticationError ->
                encoder.encodeJsonElement(
                    Json.encodeToJsonElement(
                        AuthenticationResponse.AuthenticationError.serializer(),
                        obj
                    )
                )
        }
    }
}
