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
        @SerialName("model_name")
        val modelName :String,
        @SerialName("version")
        val version: String,
        @SerialName("request_key")
        val requestKey: String,
        @SerialName("training_plan")
        val trainingPlanID: String,
        @SerialName("model_config")
        val clientConfig: ClientConfig,
        @SerialName("protocol")
        val protocolID: String,
        @SerialName("model")
        val modelId: String
    ) : CycleResponseData()

    @SerialName(CYCLE_REJECT)
    @Serializable
    data class CycleReject(
        @SerialName("model_name")
        val modelName :String,
        @SerialName("version")
        val version: String,
        val timeout: Int
    ) : CycleResponseData()
}
