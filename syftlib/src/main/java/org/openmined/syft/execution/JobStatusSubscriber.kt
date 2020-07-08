package org.openmined.syft.execution

import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel
import java.io.InvalidObjectException
import java.util.concurrent.ConcurrentHashMap

@ExperimentalUnsignedTypes
open class JobStatusSubscriber {
    /**
     * This method is called when KotlinSyft has downloaded all the plans and protocols from PyGrid and it is ready to train the model.
     * @param model stores the model weights given by PyGrid
     * @param plans is a HashMap of all the planIDs and their plans.
     * @param clientConfig has hyper parameters like batchsize, learning rate, number of steps, etc
     */
    open fun onReady(
        model: SyftModel,
        plans: ConcurrentHashMap<String, Plan>,
        clientConfig: ClientConfig
    ) {
    }

    /**
     * This method is called when the job cycle finishes successfully. Override this method to clear the worker and the jobs
     */
    open fun onComplete() {}

    /**
     * This method is called when the worker has been rejected from the cycle by the PyGrid
     * @param timeout is the timestamp indicating the time after which the worker should retry joining into the cycle
     */
    open fun onRejected(timeout: String) {}

    /**
     * This method is called when the job throws an error
     * @param throwable contains the error message
     */
    open fun onError(throwable: Throwable) {}

    /**
     * Calls the respective user callbacks upon receiving a [JobStatusMessage]
     */
    internal fun onJobStatusMessage(jobStatusMessage: JobStatusMessage) {
        when (jobStatusMessage) {
            is JobStatusMessage.JobReady -> {
                if (jobStatusMessage.clientConfig != null)
                    onReady(
                        jobStatusMessage.model,
                        jobStatusMessage.plans,
                        jobStatusMessage.clientConfig
                    )
                else
                    onError(InvalidObjectException("Client config not available yet"))
            }
            is JobStatusMessage.JobCycleRejected -> onRejected(jobStatusMessage.timeout)
            //add all the other messages as and when needed
        }
    }
}