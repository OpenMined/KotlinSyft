package org.openmined.syft.networking.datamodels

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
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.json
import org.pytorch.IValue

private const val NAME = "name"
private const val VERSION = "version"
private const val MAX_UPDATES = "max_updates"

@Serializable
data class ClientProperties(
    @SerialName(NAME)
    val modelName: String,
    @SerialName(VERSION)
    val modelVersion: String,
    @SerialName(MAX_UPDATES)
    val maxUpdates: Int
)

@Serializable(with = ClientConfigSerializer::class)
data class ClientConfig(
    val properties: ClientProperties,
    val planArgs: Map<String, IValue>
)

@Suppress("UNCHECKED_CAST")
@Serializer(forClass = ClientConfig::class)
internal class ClientConfigSerializer : KSerializer<ClientConfig> {
    private val propertiesList = listOf(NAME, VERSION, MAX_UPDATES)

    override val descriptor: SerialDescriptor
        get() = SerialClassDescImpl("SocketSerializer")

    override fun deserialize(decoder: Decoder): ClientConfig {
        val input = decoder as? JsonInput
                    ?: throw SerializationException("This class can be loaded only by Json")
        val response = input.decodeJson() as? JsonObject
                       ?: throw SerializationException("Expected JsonObject")
        val properties = getClientProperties(response)
        val map = mutableMapOf<String, IValue>()
        response.content.filterNot { it.key in propertiesList }.forEach { (key, value) ->
            map[key] = createIValue(value.toString())
        }
        return ClientConfig(properties, map)
    }

    override fun serialize(encoder: Encoder, obj: ClientConfig) {
        val output = encoder as? JsonOutput
                     ?: throw SerializationException("This class can be saved only by Json")

        output.encodeJson(json {
            NAME to obj.properties.modelName
            VERSION to obj.properties.modelVersion
            MAX_UPDATES to obj.properties.maxUpdates
            obj.planArgs.forEach{ (key, value) ->
                key to value.stringify()
            }
        })
    }

    private fun createIValue(value: String): IValue {
        return when {
            value.toLongOrNull() != null -> IValue.from(value.toLong())
            value.toDoubleOrNull() != null -> IValue.from(value.toDouble())
            else -> throw SerializationException("Illegal IValue supplied $value")
        }
    }

    private fun getClientProperties(response: JsonObject): ClientProperties {
        val modelName = response["name"].toString()
        val modelVersion = response["version"].toString()
        if (modelName.isEmpty() || modelVersion.isEmpty())
            throw SerializationException("incomplete client properties keys")

        val maxUpdates = response["max_updates"]?.intOrNull
                         ?: throw SerializationException("key max_updates not present")

        return ClientProperties(modelName, modelVersion, maxUpdates)
    }
}

private fun IValue.stringify() : String {
    return when{
        this.isLong -> this.toLong().toString()
        this.isDouble -> this.toDouble().toString()
        else -> throw SerializationException("unable to serialize IValue")
    }
}