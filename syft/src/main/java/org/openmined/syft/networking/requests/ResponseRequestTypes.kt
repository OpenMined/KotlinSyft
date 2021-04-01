package org.openmined.syft.networking.requests

import kotlinx.serialization.SerializationException
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

internal sealed class REQUESTS(override val value: String) : ResponseMessageTypes() {
    companion object {
        fun getObjectFromString(value: String): REQUESTS {
            return when (value) {
                AUTH_TYPE -> AUTHENTICATION
                CYCLE_TYPE -> CYCLE_REQUEST
                REPORT_TYPE -> REPORT
                WEBRTC_INTERNAL_TYPE -> WEBRTC_INTERNAL
                NEW_PEER_TYPE -> WEBRTC_PEER
                else -> throw SerializationException("unknown type REQUESTS $value")
            }
        }
    }

    object AUTHENTICATION : REQUESTS(AUTH_TYPE) {
        override fun parseJson(jsonString: String): NetworkModels =
                jsonParser.parse(AuthenticationResponse.serializer(), jsonString)

        override fun serialize(obj: NetworkModels) =
                jsonParser.toJson(
                    AuthenticationRequest.serializer(),
                    obj as AuthenticationRequest
                )
    }

    object CYCLE_REQUEST : REQUESTS(CYCLE_TYPE) {
        override fun parseJson(jsonString: String): NetworkModels =
                jsonParser.parse(CycleResponseData.serializer(), jsonString)

        override fun serialize(obj: NetworkModels) =
                jsonParser.toJson(CycleRequest.serializer(), obj as CycleRequest)
    }

    object REPORT : REQUESTS(REPORT_TYPE) {
        override fun parseJson(jsonString: String): NetworkModels =
                jsonParser.parse(ReportResponse.serializer(), jsonString)

        override fun serialize(obj: NetworkModels) =
                jsonParser.toJson(ReportRequest.serializer(), obj as ReportRequest)
    }

    object WEBRTC_INTERNAL : REQUESTS(WEBRTC_INTERNAL_TYPE) {
        override fun parseJson(jsonString: String): NetworkModels =
                jsonParser.parse(InternalMessageResponse.serializer(), jsonString)

        override fun serialize(obj: NetworkModels): JsonElement =
                jsonParser.toJson(
                    InternalMessageRequest.serializer(),
                    obj as InternalMessageRequest
                )
    }

    object WEBRTC_PEER : REQUESTS(NEW_PEER_TYPE) {
        override fun parseJson(jsonString: String): NetworkModels =
                jsonParser.parse(NewPeer.serializer(), jsonString)

        override fun serialize(obj: NetworkModels): JsonElement =
                jsonParser.toJson(
                    InternalMessageRequest.serializer(),
                    obj as InternalMessageRequest
                )
    }
}
