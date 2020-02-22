package org.openmined.syft.networking

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.json
import org.junit.jupiter.api.Test
import org.openmined.syft.networking.datamodels.AuthenticationSuccess
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.networking.datamodels.CycleResponseData
import org.openmined.syft.networking.datamodels.ReportStatus
import org.openmined.syft.networking.datamodels.WebRTCInternalMessage
import org.openmined.syft.networking.datamodels.WebRTCNewPeer
import org.openmined.syft.networking.requests.CommunicationDataFactory
import org.openmined.syft.networking.requests.REQUESTS

class RequestBuilderTest {

    private val authenticationSuccess =
            appendType(
                "federated/authenticate",
                json { "worker_id" to "Test worker ID" }).toString()

    private val cycleResponseReject =
            appendType("federated/cycle-request",
                json {
                    "status" to "rejected"
                    "model" to "my-federated-model"
                    "version" to "0.1.0"
                    "timeout" to 2700
                }).toString()

    private val cycleResponseAccept =
            appendType("federated/cycle-request",
                json {
                    "status" to "accepted"
                    "model" to "my-federated-model"
                    "version" to "0.1.0"
                    "request_key" to "LONG HASH VALUE"
                    "training_plan" to "TRAINING ID"
                    "client_config" to json { "modelName" to "model test" }
                    "protocol" to "PROTOCOL ID"
                    "model" to "model ID"
                }).toString()

    private val reportStatus =
            appendType("federated/report", json { "status" to "success" }).toString()

    private val webRTCInternal =
            appendType("webrtc_internal", json {
                "type" to "candidate"
                "worker_id" to "testing new worker"
                "sdp_string" to "SDP"
            }).toString()

    private val newPeer =
            appendType("peer", json { "worker_id" to "new ID" }).toString()

    @Test
    fun `given authentication json is parsed into AuthenticationSuccess class`() {

        val deserializeObject =
                CommunicationDataFactory.deserializeSocket(authenticationSuccess)
        assert(
            deserializeObject.data == AuthenticationSuccess(
                "Test worker ID"
            )
        )
    }

    @Test
    fun `given cycle response as reject parse into CycleReject`() {
        val deserializeObject = CommunicationDataFactory.deserializeSocket(cycleResponseReject)
        val trueObject = CycleResponseData.CycleReject(
            "my-federated-model",
            "0.1.0", 2700
        )
        assert(deserializeObject.data == trueObject)
        assert(deserializeObject.typesResponse == REQUESTS.CYCLE_REQUEST)
    }

    @Test
    fun `given cycle response as accept parse into CycleAccept`() {
        val deserializeObject = CommunicationDataFactory.deserializeSocket(cycleResponseAccept)
        val trueObject = CycleResponseData.CycleAccept(
            "my-federated-model",
            "0.1.0",
            "LONG HASH VALUE",
            "TRAINING ID",
            ClientConfig("model test"),
            "PROTOCOL ID",
            "model ID"
        )
        assert(deserializeObject.data == trueObject)
        assert(deserializeObject.typesResponse == REQUESTS.CYCLE_REQUEST)
    }

    @Test
    fun `check report status`() {
        val deserializeObject = CommunicationDataFactory.deserializeSocket(reportStatus)
        val trueObject = ReportStatus("success")
        assert(deserializeObject.data == trueObject)
        assert(deserializeObject.typesResponse == REQUESTS.REPORT)
    }

    @Test
    fun `check webRTC internal message deserialization`() {
        val deserializeObject = CommunicationDataFactory.deserializeSocket(webRTCInternal)
        val trueObject = WebRTCInternalMessage("candidate", "testing new worker", "SDP")
        assert(deserializeObject.data == trueObject)
        assert(deserializeObject.typesResponse == REQUESTS.WEBRTC_INTERNAL)
    }

    @Test
    fun `check webRTC new peer message deserialization`() {
        val deserializeObject = CommunicationDataFactory.deserializeSocket(newPeer)
        val trueObject = WebRTCNewPeer("new ID")
        assert(deserializeObject.data == trueObject)
        assert(deserializeObject.typesResponse == REQUESTS.WEBRTC_PEER)
    }


    private fun appendType(type: String, obj: JsonElement): JsonElement {
        return json {
            "type" to type
            "data" to obj
        }
    }

}