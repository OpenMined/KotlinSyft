package org.openmined.syft.networking.datamodels

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl
import kotlinx.serialization.json.JsonInput
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonOutput
import kotlinx.serialization.json.json
import org.openmined.syft.networking.requests.REQUESTS
import org.openmined.syft.networking.requests.ResponseMessageTypes

@Serializable(with = SocketSerializer::class)
data class SocketResponse<T : NetworkModels>(
    val typesResponse: ResponseMessageTypes,
    val data: T
)

@Suppress("UNCHECKED_CAST")
@Serializer(forClass = SocketResponse::class)
class SocketSerializer<T : NetworkModels> : KSerializer<SocketResponse<T>> {
    override val descriptor: SerialDescriptor
        get() = SerialClassDescImpl("SocketSerializer")

    override fun deserialize(decoder: Decoder): SocketResponse<T> {
        val input = decoder as? JsonInput
                    ?: throw SerializationException("This class can be loaded only by Json")
        val response = input.decodeJson() as? JsonObject
                       ?: throw SerializationException("Expected JsonObject")
        val type = enumValueOf<REQUESTS>(response.getPrimitive("type").content)
        val data = type.parseJson(response["data"].toString()) as T
        return SocketResponse(type, data)
    }

    override fun serialize(encoder: Encoder, obj: SocketResponse<T>) {
        val output = encoder as? JsonOutput
                     ?: throw SerializationException("This class can be saved only by Json")

        output.encodeJson(json {
            "type" to obj.typesResponse.value
            "data" to obj.typesResponse.serialize(obj.data)
        })
    }

}