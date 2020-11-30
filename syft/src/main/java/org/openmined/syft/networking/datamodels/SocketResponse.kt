package org.openmined.syft.networking.datamodels

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.openmined.syft.networking.requests.REQUESTS
import org.openmined.syft.networking.requests.ResponseMessageTypes

private const val TAG = "SocketSerializer"

@Serializable(with = SocketSerializer::class)
internal data class SocketResponse(
    @SerialName("type")
    val typesResponse: ResponseMessageTypes,
    val data: NetworkModels
)

@ExperimentalSerializationApi
@Suppress("UNCHECKED_CAST")
@Serializer(forClass = SocketResponse::class)
internal class SocketSerializer : KSerializer<SocketResponse> {
    override val descriptor: SerialDescriptor = SocketResponse.serializer().descriptor

    override fun deserialize(decoder: Decoder): SocketResponse {
        require(decoder is JsonDecoder) { throw SerializationException("This class can be loaded only by Json")}

        val response = decoder.decodeJsonElement() as? JsonObject
                       ?: throw SerializationException("Expected JsonObject")
        val typeValue = response["type"]?.jsonPrimitive?.content ?: ""
        // TODO This will throw a Serialization exception in REQUESTS.getObjectFromString. It should be done here
        val type = REQUESTS.getObjectFromString(typeValue)
        val data = type.parseJson(response["data"].toString())
        return SocketResponse(type, data)
    }

    override fun serialize(encoder: Encoder, obj: SocketResponse) {
        require(encoder is JsonEncoder) { throw SerializationException("This class can only be saved by Json encoder") }

        buildJsonObject {
            "type" to obj.typesResponse.value
            "data" to obj.typesResponse.serialize(obj.data)
        }

//        encoder.encodeJsonElement(json {
//            "type" to obj.typesResponse.value
//            "data" to obj.typesResponse.serialize(obj.data)
//        })
    }

}