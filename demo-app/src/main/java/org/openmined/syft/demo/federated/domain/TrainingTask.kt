package org.openmined.syft.demo.federated.domain

import androidx.work.ListenableWorker.Result
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import org.openmined.syft.Syft
import org.openmined.syft.demo.federated.logging.MnistLogger
import org.openmined.syft.demo.federated.ui.ContentState
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.execution.JobStatusSubscriber
import org.openmined.syft.execution.Plan
import org.openmined.syft.execution.SyftJob
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel
import org.pytorch.IValue
import org.pytorch.Tensor
import java.util.concurrent.ConcurrentHashMap

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class TrainingTask(
    private val configuration: SyftConfiguration,
    private val authToken: String,
    private val mnistDataRepository: MNISTDataRepository
) {
    private var syftWorker: Syft? = null

    fun runTask(logger: MnistLogger): Single<Result> {
        syftWorker = Syft.getInstance(configuration, authToken)
        val mnistJob = syftWorker!!.newJob("mnist", "1.0.0")
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
                trainingProcess(mnistJob, model, plans, clientConfig, logger)
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
                statusPublisher.offer(Result.failure())
            }
        }
        mnistJob.start(jobStatusSubscriber)
        return statusPublisher.onBackpressureBuffer().firstOrError()
    }

    fun disposeTraining() {
        syftWorker?.dispose()
    }

    private fun trainingProcess(
        mnistJob: SyftJob,
        model: SyftModel,
        plans: ConcurrentHashMap<String, Plan>,
        clientConfig: ClientConfig,
        logger: MnistLogger
    ) {
        var result = -0.0f
        plans.values.first().let { plan ->
            repeat(clientConfig.properties.maxUpdates) { step ->
                logger.postEpoch(step + 1)
                val batchSize = (clientConfig.planArgs["batch_size"]
                                 ?: error("batch_size doesn't exist")).toInt()
                val batchIValue = IValue.from(
                    Tensor.fromBlob(longArrayOf(batchSize.toLong()), longArrayOf(1))
                )
                val lr = IValue.from(
                    Tensor.fromBlob(
                        floatArrayOf(
                            (clientConfig.planArgs["lr"] ?: error("lr doesn't exist")).toFloat()
                        ),
                        longArrayOf(1)
                    )
                )
                val batchData =
                        mnistDataRepository.loadDataBatch(batchSize)
                val modelParams = model.getParamArray() ?: return
                val output = plan.execute(
                    modelParams,
                    batchData.first,
                    batchData.second,
                    batchIValue,
                    lr
                )?.toTuple()
                output?.let { outputResult ->
                    val paramSize = model.modelSyftState!!.syftTensors.size
                    val beginIndex = outputResult.size - paramSize
                    val updatedParams =
                            outputResult.slice(beginIndex until outputResult.size)
                    model.updateModel(updatedParams.map { it.toTensor() })
                    result = outputResult[1].toTensor().dataAsFloatArray.last()
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