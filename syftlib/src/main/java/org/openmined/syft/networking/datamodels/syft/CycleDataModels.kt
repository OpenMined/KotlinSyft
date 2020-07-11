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
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.networking.datamodels.NetworkModels

internal const val CYCLE_TYPE = "federated/cycle-request"
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
        val modelId: String
    ) : CycleResponseData()

    @SerialName(CYCLE_REJECT)
    @Serializable
    data class CycleReject(
        val timeout: String
    ) : CycleResponseData()
}

@Serializable
internal data class CycleRequest(
    @SerialName("worker_id")
    val workerId: String,
    @SerialName("model")
    val modelName: String,
    val version: String? = null,
    val ping: String,
    @SerialName("download")
    val downloadSpeed: String,
    @SerialName("upload")
    val uploadSpeed: String
) : NetworkModels()


@Serializer(forClass = CycleResponseData::class)
internal class CycleResponseSerializer : KSerializer<CycleResponseData> {
    private val json = Json(JsonConfiguration.Stable)
    override val descriptor: SerialDescriptor
        get() = SerialClassDescImpl("AuthResponseSerializer")

    override fun deserialize(decoder: Decoder): CycleResponseData {
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
        return if (response.getPrimitive("status").content == CYCLE_ACCEPT)
            json.parse(
                CycleResponseData.CycleAccept.serializer(),
                data.toString()
            )
        else
            json.parse(CycleResponseData.CycleReject.serializer(), data.toString())
    }

    override fun serialize(encoder: Encoder, obj: CycleResponseData) {
        val output = encoder as? JsonOutput
                     ?: throw SerializationException("This class can be saved only by Json")
        when (obj) {
            is CycleResponseData.CycleAccept -> output.encodeJson(
                json.toJson(
                    CycleResponseData.CycleAccept.serializer(),
                    obj
                )
            )
            is CycleResponseData.CycleReject ->
                output.encodeJson(
                    json.toJson(
                        CycleResponseData.CycleReject.serializer(),
                        obj
                    )
                )
        }
    }
}
