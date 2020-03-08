package org.openmined.syft.processes

open class JobStatusSubscriber {
    open fun onReady() {}
    open fun onComplete() {}
    open fun onError(throwable: Throwable) {}

    fun onJobStatusMessage(jobStatusMessage: JobStatusMessage) {
        when (jobStatusMessage) {
            is JobStatusMessage.JobReady -> onReady()
            //add all the other messages as and when needed
        }
    }
}