package org.openmined.syft.networking.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val AUTH_TYPE = "federated/authenticate"
const val CYCLE_TYPE = "federated/cycle-request"
const val CYCLE_ACCEPT = "accepted"
const val CYCLE_REJECT = "rejected"
const val REPORT_TYPE = "federated/report"

@Serializable
sealed class SerializableClasses {

    @SerialName(AUTH_TYPE)
    @Serializable
    data class AuthenticationSuccess(
        @SerialName("worker_id")
        val workerId: String
    ) : SerializableClasses()

    @SerialName(CYCLE_TYPE)
    @Serializable
    sealed class CycleResponseData : SerializableClasses() {

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
        ) : SerializableClasses.CycleResponseData()

        @SerialName(CYCLE_REJECT)
        @Serializable
        data class CycleReject(
            val status: String,
            val timeout: Int
        ) : SerializableClasses.CycleResponseData()

    }

    @SerialName(REPORT_TYPE)
    @Serializable
    data class ReportStatus(
        val status: String
    ) : SerializableClasses()
}

