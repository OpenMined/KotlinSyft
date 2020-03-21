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
    
    abstract val modelName: String
    abstract val version: String?

    @SerialName(CYCLE_ACCEPT)
    @Serializable
    data class CycleAccept(
        @SerialName("model")
        override val modelName: String,
        override val version: String? = null,
        @SerialName("request_key")
        val requestKey: String,
        val plans: HashMap<String, String>,
        @SerialName("client_config")
        val clientConfig: ClientConfig,
        val protocols: HashMap<String, String>,
        @SerialName("model_id")
        val modelId: String
    ) : CycleResponseData()

    @SerialName(CYCLE_REJECT)
    @Serializable
    data class CycleReject(
        @SerialName("model")
        //todo making this optional till pygrid makes this compulsory
        override val modelName: String = "",
        override val version: String? = null,
        val timeout: String
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
