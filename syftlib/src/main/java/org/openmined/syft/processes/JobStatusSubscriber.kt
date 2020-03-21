package org.openmined.syft.processes

import org.openmined.syft.networking.datamodels.ClientConfig

open class JobStatusSubscriber {
    open fun onReady(model: String, clientConfig: ClientConfig) {}
    open fun onComplete() {}
    open fun onRejected(timeout: String) {}
    open fun onError(throwable: Throwable) {}

    fun onJobStatusMessage(jobStatusMessage: JobStatusMessage) {
        when (jobStatusMessage) {
            is JobStatusMessage.JobReady -> onReady(
                jobStatusMessage.model,
                jobStatusMessage.clientConfig
            )
            is JobStatusMessage.JobCycleRejected -> onRejected(jobStatusMessage.timeout)
            //add all the other messages as and when needed
        }
    }
}