package org.openmined.syft.networking.datamodels.syft

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.networking.datamodels.NetworkModels

const val CYCLE_TYPE = "federated/cycle-request"
const val CYCLE_ACCEPT = "accepted"
const val CYCLE_REJECT = "rejected"

@Serializable
sealed class CycleResponseData : NetworkModels() {

    @SerialName("model")
    abstract val modelName: String
    @SerialName("version")
    abstract val version: String

    @SerialName(CYCLE_ACCEPT)
    @Serializable
    data class CycleAccept(
        override val modelName: String,
        override val version: String,
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
        override val modelName: String,
        override val version: String,
        val timeout: Int
    ) : CycleResponseData()
}

@Serializable
data class CycleRequest(
    @SerialName("worker_id")
    val workerId: String,
    @SerialName("model")
    val modelName: String,
    val version: String? = null,
    val ping: String,
    @SerialName("download")
    val downloadSpeed: String,
    @SerialName("upload")
    val uploadSpeed: String
) : NetworkModels()
