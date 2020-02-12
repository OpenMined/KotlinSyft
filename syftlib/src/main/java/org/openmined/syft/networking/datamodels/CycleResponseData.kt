package org.openmined.syft.networking.datamodels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration


@Serializable
sealed class CycleResponseData : NetworkModels() {

    @SerialName(CYCLE_ACCEPT)
    @Serializable
    data class CycleAccept(
        val status: String,
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
        val status: String,
        val timeout: Int
    ) : CycleResponseData()

    companion object {
        val jsonParser = Json(JsonConfiguration.Stable.copy(classDiscriminator = "status"))
    }

}
