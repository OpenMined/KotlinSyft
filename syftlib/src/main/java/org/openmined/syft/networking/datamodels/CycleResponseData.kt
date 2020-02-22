package org.openmined.syft.networking.datamodels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val CYCLE_TYPE = "federated/cycle-request"
const val CYCLE_ACCEPT = "accepted"
const val CYCLE_REJECT = "rejected"

@Serializable
sealed class CycleResponseData : NetworkModels() {

    @SerialName(CYCLE_ACCEPT)
    @Serializable
    data class CycleAccept(
        @SerialName("model")
        val modelName :String,
        @SerialName("version")
        val version: String,
        @SerialName("request_key")
        val requestKey: String,
        @SerialName("training_plan")
        val trainingPlanID: String,
        @SerialName("client_config")
        val clientConfig: ClientConfig,
        @SerialName("protocols")
        val protocolID: String,
        @SerialName("model_id")
        val modelId: String
    ) : CycleResponseData()

    @SerialName(CYCLE_REJECT)
    @Serializable
    data class CycleReject(
        @SerialName("model")
        val modelName :String,
        @SerialName("version")
        val version: String,
        val timeout: Int
    ) : CycleResponseData()
}
