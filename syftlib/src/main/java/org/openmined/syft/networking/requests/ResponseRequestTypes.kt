package org.openmined.syft.networking.requests

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonElement
import org.openmined.syft.networking.datamodels.NetworkModels
import org.openmined.syft.networking.datamodels.syft.AUTH_TYPE
import org.openmined.syft.networking.datamodels.syft.AuthenticationRequest
import org.openmined.syft.networking.datamodels.syft.AuthenticationResponse
import org.openmined.syft.networking.datamodels.syft.CYCLE_TYPE
import org.openmined.syft.networking.datamodels.syft.CycleRequest
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.datamodels.syft.REPORT_TYPE
import org.openmined.syft.networking.datamodels.syft.ReportRequest
import org.openmined.syft.networking.datamodels.syft.ReportResponse
import org.openmined.syft.networking.datamodels.webRTC.InternalMessageRequest
import org.openmined.syft.networking.datamodels.webRTC.InternalMessageResponse
import org.openmined.syft.networking.datamodels.webRTC.NEW_PEER_TYPE
import org.openmined.syft.networking.datamodels.webRTC.NewPeer
import org.openmined.syft.networking.datamodels.webRTC.WEBRTC_INTERNAL_TYPE

enum class REQUESTS(override val value: String) : ResponseMessageTypes {

    AUTHENTICATION(AUTH_TYPE) {
        override val jsonParser = Json(JsonConfiguration.Stable.copy(classDiscriminator = "status"))
        override fun parseJson(jsonString: String): NetworkModels =
                jsonParser.parse(AuthenticationResponse.serializer(), jsonString)

        override fun serialize(obj: NetworkModels) =
                jsonParser.toJson(
                    AuthenticationRequest.serializer(),
                    obj as AuthenticationRequest
                )
    },

    CYCLE_REQUEST(CYCLE_TYPE) {
        override val jsonParser = Json(
            JsonConfiguration.Stable.copy(classDiscriminator = "status")
        )

        override fun parseJson(jsonString: String): NetworkModels =
                jsonParser.parse(CycleResponseData.serializer(), jsonString)

        override fun serialize(obj: NetworkModels) =
                jsonParser.toJson(CycleRequest.serializer(), obj as CycleRequest)
    },
    REPORT(REPORT_TYPE) {
        override val jsonParser = Json(JsonConfiguration.Stable)
        override fun parseJson(jsonString: String): NetworkModels =
                jsonParser.parse(ReportResponse.serializer(), jsonString)

        override fun serialize(obj: NetworkModels) =
                jsonParser.toJson(ReportRequest.serializer(), obj as ReportRequest)
    },
    WEBRTC_INTERNAL(WEBRTC_INTERNAL_TYPE) {
        override val jsonParser: Json
            get() = Json(JsonConfiguration.Stable)

        override fun parseJson(jsonString: String): NetworkModels =
                jsonParser.parse(InternalMessageResponse.serializer(), jsonString)

        override fun serialize(obj: NetworkModels): JsonElement =
                jsonParser.toJson(
                    InternalMessageRequest.serializer(),
                    obj as InternalMessageRequest
                )
    },
    WEBRTC_PEER(NEW_PEER_TYPE) {
        override val jsonParser: Json
            get() = Json(JsonConfiguration.Stable)

        override fun parseJson(jsonString: String): NetworkModels =
                jsonParser.parse(NewPeer.serializer(), jsonString)

        override fun serialize(obj: NetworkModels): JsonElement =
                jsonParser.toJson(
                    InternalMessageRequest.serializer(),
                    obj as InternalMessageRequest
                )

    }

}
