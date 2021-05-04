package org.openmined.syft.execution.checkpoint

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONObject
import org.openmined.syft.execution.JobModel
import org.openmined.syft.execution.SyftJob
import org.openmined.syft.networking.datamodels.ClientConfig
import org.pytorch.Tensor

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
data class CheckPoint(
    var steps: Int,
    var currentStep: Int,
    var batchSize: Int,
    var clientConfig: ClientConfig? = null,
    var jobModel: JobModel? = null,
    var modelParams: Array<Tensor>? = null
) {

    companion object {
        fun fromJob(job: SyftJob): CheckPoint {
            val batchSize = job.clientConfig.planArgs["batch_size"]?.toInt() ?: 0

            return CheckPoint(
                steps = job.clientConfig.properties.maxUpdates,
                currentStep = job.currentStep,
                batchSize = batchSize,
                clientConfig = job.clientConfig,
                jobModel = job.jobModel,
                modelParams = job.model.paramArray
            )
        }
    }
}
