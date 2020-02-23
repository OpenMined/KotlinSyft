package org.openmined.syft.networking.requests

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonElement
import org.openmined.syft.networking.datamodels.AUTH_TYPE
import org.openmined.syft.networking.datamodels.AuthenticationSuccess
import org.openmined.syft.networking.datamodels.CYCLE_TYPE
import org.openmined.syft.networking.datamodels.CycleResponseData
import org.openmined.syft.networking.datamodels.NEW_PEER_TYPE
import org.openmined.syft.networking.datamodels.NetworkModels
import org.openmined.syft.networking.datamodels.REPORT_TYPE
import org.openmined.syft.networking.datamodels.ReportResponse
import org.openmined.syft.networking.datamodels.WEBRTC_INTERNAL_TYPE
import org.openmined.syft.networking.datamodels.WebRTCInternalMessage
import org.openmined.syft.networking.datamodels.WebRTCNewPeer

enum class REQUESTS(override val value: String) : ResponseMessageTypes {

    AUTHENTICATION(AUTH_TYPE) {
        override val jsonParser = Json(JsonConfiguration.Stable)
        override fun parseJson(jsonString: String): NetworkModels =
                jsonParser.parse(AuthenticationSuccess.serializer(), jsonString)

        override fun serialize(obj: NetworkModels) =
                jsonParser.toJson(AuthenticationSuccess.serializer(), obj as AuthenticationSuccess)
    },

    CYCLE_REQUEST(CYCLE_TYPE) {
        override val jsonParser = Json(
            JsonConfiguration.Stable.copy(classDiscriminator = "status")
        )

        override fun parseJson(jsonString: String): NetworkModels =
                jsonParser.parse(CycleResponseData.serializer(), jsonString)

        override fun serialize(obj: NetworkModels) =
                jsonParser.toJson(CycleResponseData.serializer(), obj as CycleResponseData)
    },
    REPORT(REPORT_TYPE) {
        override val jsonParser = Json(JsonConfiguration.Stable)
        override fun parseJson(jsonString: String): NetworkModels =
                jsonParser.parse(ReportResponse.serializer(), jsonString)

        override fun serialize(obj: NetworkModels) =
                jsonParser.toJson(ReportResponse.serializer(), obj as ReportResponse)
    },
    WEBRTC_INTERNAL(WEBRTC_INTERNAL_TYPE) {
        override val jsonParser: Json
            get() = Json(JsonConfiguration.Stable)

        override fun parseJson(jsonString: String): NetworkModels =
                jsonParser.parse(WebRTCInternalMessage.serializer(), jsonString)

        override fun serialize(obj: NetworkModels): JsonElement =
                jsonParser.toJson(WebRTCInternalMessage.serializer(), obj as WebRTCInternalMessage)
    },
    WEBRTC_PEER(NEW_PEER_TYPE) {
        override val jsonParser: Json
            get() = Json(JsonConfiguration.Stable)

        override fun parseJson(jsonString: String): NetworkModels =
                jsonParser.parse(WebRTCNewPeer.serializer(), jsonString)

        override fun serialize(obj: NetworkModels): JsonElement =
                jsonParser.toJson(WebRTCInternalMessage.serializer(), obj as WebRTCInternalMessage)

    }

}
