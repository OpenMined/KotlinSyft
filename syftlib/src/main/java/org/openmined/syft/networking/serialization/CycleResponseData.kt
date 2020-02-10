package org.openmined.syft.networking.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CycleResponseData(
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
) : RequestResponseBody