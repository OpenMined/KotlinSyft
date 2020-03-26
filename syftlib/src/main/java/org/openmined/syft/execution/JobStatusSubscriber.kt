package org.openmined.syft.execution

import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel

open class JobStatusSubscriber {
    open fun onReady(model: SyftModel, clientConfig: ClientConfig) {}
    open fun onComplete() {}
    open fun onRejected(timeout: String) {}
    open fun onError(throwable: Throwable) {}

    fun onJobStatusMessage(jobStatusMessage: JobStatusMessage) {
        when (jobStatusMessage) {
            is JobStatusMessage.JobReady -> {
                if (jobStatusMessage.clientConfig != null)
                    onReady(
                        jobStatusMessage.model,
                        jobStatusMessage.clientConfig
                    )
                else
                    onError(IllegalStateException("Client config not avialable yet"))
            }
            is JobStatusMessage.JobCycleRejected -> onRejected(jobStatusMessage.timeout)
            //add all the other messages as and when needed
        }
    }
}