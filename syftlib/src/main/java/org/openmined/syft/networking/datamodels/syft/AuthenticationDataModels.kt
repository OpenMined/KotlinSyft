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
import kotlinx.serialization.json.JsonInput
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonOutput
import kotlinx.serialization.json.json
import org.openmined.syft.networking.datamodels.NetworkModels

const val AUTH_TYPE = "federated/authenticate"
private const val TAG = "Authentication"

@Serializable
data class AuthenticationRequest(
    @SerialName("auth_token")
    val authToken: String = ""
) : NetworkModels()

@Serializable(with = AuthSerializer::class)
sealed class AuthenticationResponse : NetworkModels() {
    @Serializable
    data class AuthenticationSuccess(
        @SerialName("worker_id")
        val workerId: String
    ) : AuthenticationResponse()

    @Serializable
    data class AuthenticationError(
        @SerialName("error")
        val errorMessage: String
    ) : AuthenticationResponse()
}

@Serializer(forClass = AuthenticationResponse::class)
class AuthSerializer : KSerializer<AuthenticationResponse> {
    override val descriptor: SerialDescriptor
        get() = SerialClassDescImpl("AuthSerializer")

    override fun deserialize(decoder: Decoder): AuthenticationResponse {
        val input = decoder as? JsonInput
                    ?: throw SerializationException("This class can be loaded only by Json")
        val response = input.decodeJson() as? JsonObject
                       ?: throw SerializationException("Expected JsonObject")
        return if (response.containsKey("worker_id"))
            AuthenticationResponse.AuthenticationSuccess(
                response.getPrimitive("worker_id").content
            )
        else
            AuthenticationResponse.AuthenticationError(
                response.getPrimitive("error").content
            )
    }

    override fun serialize(encoder: Encoder, obj: AuthenticationResponse) {
        val output = encoder as? JsonOutput
                     ?: throw SerializationException("This class can be saved only by Json")
        val data = when (obj) {
            is AuthenticationResponse.AuthenticationError -> json { "error" to obj.errorMessage }
            is AuthenticationResponse.AuthenticationSuccess -> json { "worKer_id" to obj.workerId }
        }
        output.encodeJson(data)
    }

}
