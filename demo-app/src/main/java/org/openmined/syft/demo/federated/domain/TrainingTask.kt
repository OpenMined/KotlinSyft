package org.openmined.syft.demo.federated.domain

import androidx.work.ListenableWorker.Result
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import org.openmined.syft.Syft
import org.openmined.syft.demo.federated.ui.ContentState
import org.openmined.syft.demo.federated.ui.MnistLogger
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.execution.JobStatusSubscriber
import org.openmined.syft.execution.Plan
import org.openmined.syft.execution.SyftJob
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel
import java.util.concurrent.ConcurrentHashMap

const val LOSS_LIST = "loss"

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class TrainingTask(
    private val configuration: SyftConfiguration,
    private val authToken: String,
    private val mnistDataRepository: MNISTDataRepository
) {
    private var syftWorker : Syft? = null
    private val logger = MnistLogger.getInstance()

    fun runTask(): Single<Result> {
        syftWorker = Syft.getInstance(configuration, authToken)
        val mnistJob = syftWorker!!.newJob("mnist", "1.0.0")
        val result = mutableListOf<Float>()
        val statusPublisher = PublishProcessor.create<Result>()

        logger.postLog("MNIST job started \n\nChecking for download and upload speeds")
        logger.postState(ContentState.Loading)
        val jobStatusSubscriber = object : JobStatusSubscriber() {
            override fun onReady(
                model: SyftModel,
                plans: ConcurrentHashMap<String, Plan>,
                clientConfig: ClientConfig
            ) {
                logger.postLog("Model ${model.modelName} received.\n\nStarting training process")
                trainingProcess(mnistJob, model, plans, clientConfig, result)
            }

            override fun onComplete() {
                syftWorker?.dispose()
                statusPublisher.offer(Result.success())
            }

            override fun onRejected(timeout: String) {
                logger.postLog("We've been rejected $timeout")
                statusPublisher.offer(Result.retry())
            }

            override fun onError(throwable: Throwable) {
                logger.postLog("There was an error $throwable")
            }
        }
        mnistJob.start(jobStatusSubscriber)
        return statusPublisher.onBackpressureBuffer().firstOrError()
    }

    fun disposeTraining(){
        syftWorker?.dispose()
    }

    private fun trainingProcess(
        mnistJob: SyftJob,
        model: SyftModel,
        plans: ConcurrentHashMap<String, Plan>,
        clientConfig: ClientConfig,
        result: MutableList<Float>
    ) {

        plans.values.first().let { plan ->
            repeat(clientConfig.maxUpdates) { step ->
                logger.postEpoch(step + 1)
                val batchData = mnistDataRepository.loadDataBatch(clientConfig.batchSize.toInt())
                val output = plan.execute(
                    model,
                    batchData,
                    clientConfig
                )?.toTuple()
                output?.let { outputResult ->
                    val paramSize = model.modelSyftState!!.syftTensors.size
                    val beginIndex = outputResult.size - paramSize
                    val updatedParams =
                            outputResult.slice(beginIndex until outputResult.size)
                    model.updateModel(updatedParams.map { it.toTensor() })
                    result.add(outputResult[1].toTensor().dataAsFloatArray.last())
                } ?: run {
                    logger.postLog("the model returned empty array due to invalid device state")
                    return
                }
                logger.postState(ContentState.Training)
                logger.postData(result)
            }
            logger.postLog("Training done!\n reporting diff")
            val diff = mnistJob.createDiff()
            mnistJob.report(diff)
            logger.postLog("reported the model to PyGrid")
        }
    }
}