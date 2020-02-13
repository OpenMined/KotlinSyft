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
        @SerialName("request_key")
        val requestKey: String,
        @SerialName("training_plan")
        val trainingPlanID: String,
        @SerialName("model_config")
        val modelConfig: ModelConfig,
        @SerialName("protocol")
        val protocolID: String,
        @SerialName("model")
        val modelId: String
    ) : CycleResponseData()

    @SerialName(CYCLE_REJECT)
    @Serializable
    data class CycleReject(
        val timeout: Int
    ) : CycleResponseData()
}
