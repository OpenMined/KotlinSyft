package org.openmined.syft.networking

import kotlinx.serialization.json.json
import org.junit.jupiter.api.Test
import org.openmined.syft.networking.requests.CommunicationDataFactory
import org.openmined.syft.networking.datamodels.NetworkModels

class RequestBuilderTest {

    private val authenticationSuccessJson = json {
        "type" to "federated/authenticate"
        "worker_id" to "Test worker ID"
    }.toString()

    val cycleResponseReject = json {
        "type" to "federated/cycle-request"
        "status" to "rejected"
        "timeout" to 2700
    }.toString()

    private val cycleResponseAccept = json {
        "type" to "federated/cycle-request"
        "status" to "accepted"
        "request_key" to "LONG HASH VALUE"
        "training_plan" to "TRAINING ID"
        "model_config" to json { "modelName" to "model test" }
        "protocol" to "PROTOCOL ID"
        "model" to "model ID"
    }.toString()

    private val reportStatus = json {
        "type" to "federated/report"
        "status" to "success"
    }.toString()

    @Test
    fun `given authentication json is parsed into AuthenticationSuccess class`() {

        val deserializeObject = CommunicationDataFactory.deserializeSocket(authenticationSuccessJson)
        assert(
            deserializeObject == NetworkModels.AuthenticationSuccess(
                "Test worker ID"
            )
        )
    }

    @Test
    fun `given cycle response as reject parse into CycleReject`(){
        val deserializeObject = CommunicationDataFactory.deserializeSocket(cycleResponseReject)
        val trueObject = NetworkModels.CycleResponseData.CycleReject("rejected",2700)
        assert(deserializeObject == trueObject)
    }


}