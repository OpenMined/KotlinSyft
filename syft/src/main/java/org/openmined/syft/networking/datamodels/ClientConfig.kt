package org.openmined.syft.networking.datamodels

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

private const val NAME = "name"
private const val VERSION = "version"
private const val MAX_UPDATES = "max_updates"

/**
 * Client properties specific to the job description
 * @property modelName The name of the model or [SyftJob][org.openmined.syft.execution.SyftJob]
 * @property modelVersion The version of the model or [SyftJob][org.openmined.syft.execution.SyftJob]
 * @property maxUpdates The number of training iterations per cycle
 */
@Serializable
data class ClientProperties(
    @SerialName(NAME)
    val modelName: String,
    @SerialName(VERSION)
    val modelVersion: String,
    @SerialName(MAX_UPDATES)
    val maxUpdates: Int
)

/**
 * All the user defined parameters will be serialised and sent by the PyGrid in the form of [ClientConfig]
 * @property properties Contains job specific descriptions. See [ClientProperties]
 * @property planArgs A [Map] containing the keys and values of the hyper parameters of the model. All the values are serialized as string and the user must deserialize them at runtime.
 */
@Serializable(with = ClientConfigSerializer::class)
data class ClientConfig(
    val properties: ClientProperties,
    val planArgs: Map<String, String>
)

@ExperimentalSerializationApi
@Suppress("UNCHECKED_CAST")
@Serializer(forClass = ClientConfig::class)
internal class ClientConfigSerializer : KSerializer<ClientConfig> {
    private val propertiesList = listOf(NAME, VERSION, MAX_UPDATES)

    override val descriptor: SerialDescriptor = ClientConfig.serializer().descriptor

    override fun deserialize(decoder: Decoder): ClientConfig {
        val input = decoder as? JsonDecoder
                    ?: throw SerializationException("This class can be loaded only by Json")
        val response = input.decodeJsonElement() as? JsonObject
                       ?: throw SerializationException("Expected JsonObject")
        val properties = getClientProperties(response)
        val map = mutableMapOf<String, String>()
        response.entries.filterNot { it.key in propertiesList }.forEach { (key, value) ->
            map[key] = value.toString()
        }
        return ClientConfig(properties, map)
    }

    override fun serialize(encoder: Encoder, obj: ClientConfig) {
        val output = encoder as? JsonEncoder
                     ?: throw SerializationException("This class can be saved only by Json")

        output.encodeJsonElement(buildJsonObject {
            NAME to obj.properties.modelName
            VERSION to obj.properties.modelVersion
            MAX_UPDATES to obj.properties.maxUpdates
            obj.planArgs.forEach{ (key, value) ->
                key to value
            }
        })
    }

    private fun getClientProperties(response: JsonObject): ClientProperties {
        val modelName = response["name"].toString()
        val modelVersion = response["version"].toString()
        if (modelName.isEmpty() || modelVersion.isEmpty())
            throw SerializationException("incomplete client properties keys")

        val maxUpdates = response["max_updates"]?.toString()?.toIntOrNull()
                         ?: throw SerializationException("key max_updates not present")

        return ClientProperties(modelName, modelVersion, maxUpdates)
    }
}