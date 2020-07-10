package org.openmined.syft.networking.requests

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonElement
import org.openmined.syft.networking.datamodels.NetworkModels

internal interface MessageTypes {
    val value: String
}

internal abstract class ResponseMessageTypes() : MessageTypes {
    val jsonParser: Json = Json(JsonConfiguration.Stable)
    abstract fun parseJson(jsonString: String): NetworkModels
    abstract fun serialize(obj: NetworkModels): JsonElement
}

internal enum class DOWNLOAD(
    override val value: String
) : MessageTypes {
    TRAININGPLAN("federated/get-training-plan"),
    PROTOCOL("federated/get-protocol"),
    MODEL("federated/get-model")
}

internal enum class WebRTCMessageTypes(override val value: String) : MessageTypes {
    CANDIDATE("candidate"),
    OFFER("offer"),
    ANSWER("answer"),
    WEBRTC_JOIN_ROOM("webrtc: join-room"),
    WEBRTC_INTERNAL_MESSAGE("webrtc: internal-message")
}
