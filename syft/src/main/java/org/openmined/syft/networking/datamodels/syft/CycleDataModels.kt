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
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.networking.datamodels.NetworkModels

internal const val CYCLE_TYPE = "model-centric/cycle-request"
internal const val CYCLE_ACCEPT = "accepted"
internal const val CYCLE_REJECT = "rejected"

@Serializable(with = CycleResponseSerializer::class)
internal sealed class CycleResponseData : NetworkModels() {
    @SerialName(CYCLE_ACCEPT)
    @Serializable
    data class CycleAccept(
        @SerialName("model")
        val modelName: String,
        val version: String,
        @SerialName("request_key")
        val requestKey: String,
        val plans: HashMap<String, String>,
        @SerialName("client_config")
        val clientConfig: ClientConfig,
        val protocols: HashMap<String, String>,
        @SerialName("model_id")
        val modelId: String,
    ) : CycleResponseData()

    @SerialName(CYCLE_REJECT)
    @Serializable
    data class CycleReject(
        val timeout: String = "",
    ) : CycleResponseData()
}

@Serializable
internal data class CycleRequest(
    @SerialName("worker_id")
    val workerId: String,
    @SerialName("model")
    val modelName: String,
    val version: String? = null,
    val ping: Int,
    @SerialName("download")
    val downloadSpeed: Float,
    @SerialName("upload")
    val uploadSpeed: Float,
) : NetworkModels()


@ExperimentalSerializationApi
@Serializer(forClass = CycleResponseData::class)
internal class CycleResponseSerializer : KSerializer<CycleResponseData> {
    override val descriptor: SerialDescriptor = CycleResponseData.serializer().descriptor

    override fun deserialize(decoder: Decoder): CycleResponseData {
        require(decoder is JsonDecoder) { throw SerializationException("This class can be loaded only by Json") }

        val response = decoder.decodeJsonElement() as? JsonObject
                       ?: throw SerializationException("Expected JsonObject")

        val data = buildJsonArray {
            response.forEach { key, value ->
                if (key != "status")
                    key to value
            }
        }
        return if (response["status"]?.jsonPrimitive?.content == CYCLE_ACCEPT)
            Json.decodeFromString(
                CycleResponseData.CycleAccept.serializer(),
                data.toString()
            )
        else
            Json.decodeFromString(CycleResponseData.CycleReject.serializer(), data.toString())
    }

    override fun serialize(encoder: Encoder, obj: CycleResponseData) {
        require(encoder is JsonEncoder) { throw SerializationException("This class can only be saved by Json encoder") }

        when (obj) {
            is CycleResponseData.CycleAccept -> encoder.encodeJsonElement(
                Json.encodeToJsonElement(
                    CycleResponseData.CycleAccept.serializer(),
                    obj
                )
            )
            is CycleResponseData.CycleReject ->
                encoder.encodeJsonElement(
                    Json.encodeToJsonElement(
                        CycleResponseData.CycleReject.serializer(),
                        obj
                    )
                )
        }
    }
}
