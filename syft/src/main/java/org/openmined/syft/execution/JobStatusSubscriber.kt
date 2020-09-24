package org.openmined.syft.execution

import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel
import java.util.concurrent.ConcurrentHashMap

/**
 * This is passed as argument to [SyftJob.start] giving the overridden callbacks for different phases of the job cycle.
 * ```kotlin
 * val jobStatusSubscriber = object : JobStatusSubscriber() {
 *      override fun onReady(
 *      model: SyftModel,
 *      plans: ConcurrentHashMap<String, Plan>,
 *      clientConfig: ClientConfig
 *      ) {
 *      }
 *
 *      override fun onRejected(timeout: String) {
 *      }
 *
 *      override fun onError(throwable: Throwable) {
 *      }
 * }
 *
 * job.start(jobStatusSubscriber)
 * ```
 */
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
     * @param timeout is the timestamp indicating the time after which the worker should retry joining into the cycle. This will be empty if it is the last cycle.
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
                    onError(JobErrorThrowable.DownloadIncomplete("Client config not available yet"))
            }
            is JobStatusMessage.JobCycleRejected -> onRejected(jobStatusMessage.timeout)
            //add all the other messages as and when needed
        }
    }
}