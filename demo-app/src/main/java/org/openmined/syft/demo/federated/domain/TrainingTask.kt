package org.openmined.syft.demo.federated.domain

import android.util.Log
import androidx.work.ListenableWorker.Result
import io.reactivex.processors.PublishProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import org.openmined.syft.Syft
import org.openmined.syft.demo.federated.logging.MnistLogger
import org.openmined.syft.demo.federated.ui.ContentState
import org.openmined.syft.domain.InputParamType
import org.openmined.syft.domain.PlanInputSpec
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.domain.TrainingParameters
import org.openmined.syft.execution.JobStatusMessage
import org.openmined.syft.execution.SyftJob
import org.openmined.syft.execution.TrainingState

@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class TrainingTask(
    configuration: SyftConfiguration,
    authToken: String,
    private val mnistDataRepository: MNISTDataRepository,
    private val modelName: String,
    private val modelVersion: String
) {
    private val syftWorker = Syft.getInstance(configuration, authToken)

    suspend fun runTask(logger: MnistLogger) {
        val mnistJob = syftWorker.newJob(modelName, modelVersion)

        val statusPublisher = PublishProcessor.create<Result>()

        logger.postLog("MNIST job started \n\nChecking for download and upload speeds")
        logger.postState(ContentState.Loading)

        when (val requestResult = mnistJob.request()) {
            is JobStatusMessage.JobReady -> {
                executeTraining(logger, mnistJob, requestResult)
            }

            JobStatusMessage.JobInit -> logger.postLog("Job initialised")

            is JobStatusMessage.JobCycleRejected -> {
                logger.postLog("We've been rejected for the time $requestResult.timeout")
                statusPublisher.offer(Result.retry())
            }

            JobStatusMessage.Complete -> {
                syftWorker.dispose()
                statusPublisher.offer(Result.success())
            }

            is JobStatusMessage.Error -> {
                logger.postLog("There was an error $requestResult.throwable")
                statusPublisher.offer(Result.failure())
            }

            is JobStatusMessage.UnexpectedDownloadStatus -> {
                logger.postLog("Job was in an unexpected status ${requestResult.downloadStatus}")
                statusPublisher.offer(Result.failure())
            }

            JobStatusMessage.ConditionsNotMet -> {
                // TODO Offer more info why conditions were not met
                logger.postLog("Battery/network conditions were not met. Stopping process")
                statusPublisher.offer(Result.failure())
            }
        }
    }

    private suspend fun executeTraining(
        logger: MnistLogger,
        mnistJob: SyftJob,
        requestResult: JobStatusMessage.JobReady
    ) {
        val startTime = System.currentTimeMillis()
        logger.postLog("Starting training!")
        logger.postState(ContentState.Training)

        mnistJob.train(requestResult.plans,
            requestResult.clientConfig!!,
            mnistDataRepository,
            generateTrainingParameters()
        ).collect {
            // collect happens in IO Dispatcher. Change context to process the training state.
            withContext(Dispatchers.Main) {
                processTrainingState(it, logger)
            }
        }

        logger.postLog("Training Finished in ${System.currentTimeMillis() - startTime} ms")
    }

    // Observe training state flow changes
    // Note that this flow must be launched in the Main scope to interact with the UI
    private fun processTrainingState(trainingState: TrainingState, logger: MnistLogger) {
        Log.d("TrainingTask", "Processing $trainingState")
        when (trainingState) {
            is TrainingState.Message -> {
                logger.postLog(trainingState.message)
            }
            is TrainingState.Epoch -> {
                logger.postEpoch(trainingState.epoch)
            }
            is TrainingState.Data -> {
                logger.postData(trainingState.result)
            }
            is TrainingState.Error -> {
                logger.postState(ContentState.Error)
            }
            is TrainingState.Complete -> {
                logger.postLog("Training completed!")
            }
        }
    }

    fun disposeTraining() {
        syftWorker.dispose()
    }

    companion object MnistTrainingParameters {
        //      outputs: [
        //        new PlanOutputSpec(PlanOutputSpec.TYPE_LOSS),
        //        new PlanOutputSpec(PlanOutputSpec.TYPE_METRIC, 'accuracy'),
        //        new PlanOutputSpec(PlanOutputSpec.TYPE_MODEL_PARAM, 'W1', 0),
        //        new PlanOutputSpec(PlanOutputSpec.TYPE_MODEL_PARAM, 'b1', 1),
        //        new PlanOutputSpec(PlanOutputSpec.TYPE_MODEL_PARAM, 'W2', 2),
        //        new PlanOutputSpec(PlanOutputSpec.TYPE_MODEL_PARAM, 'b2', 3),
        //      ],
        fun generateTrainingParameters(): TrainingParameters {
            val planInputSpec = listOf(
                PlanInputSpec(InputParamType.Data),
                PlanInputSpec(InputParamType.Target),
                PlanInputSpec(InputParamType.BatchSize),
                PlanInputSpec(InputParamType.Value, name = "lr"),
                PlanInputSpec(InputParamType.ModelParameter)
//                PlanInputSpec(InputParamType.ModelParameter, name = "b1", index = 1),
//                PlanInputSpec(InputParamType.ModelParameter, name = "W2", index = 2),
//                PlanInputSpec(InputParamType.ModelParameter, name = "b1", index = 3)
            )
            return TrainingParameters(planInputSpec)
        }
    }
}