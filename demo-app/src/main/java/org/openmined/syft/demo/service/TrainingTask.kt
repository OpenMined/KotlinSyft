package org.openmined.syft.demo.service

import androidx.work.ListenableWorker.Result
import androidx.work.workDataOf
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import org.openmined.syft.Syft
import org.openmined.syft.demo.federated.domain.MNISTDataRepository
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

    fun runTask(): Single<Result> {
        val syftWorker = Syft.getInstance(configuration, authToken)
        val mnistJob = syftWorker.newJob("mnist", "1.0.0")
        val result = mutableListOf<Float>()
        val statusPublisher = PublishProcessor.create<Result>()
        val jobStatusSubscriber = object : JobStatusSubscriber() {

            override fun onReady(
                model: SyftModel,
                plans: ConcurrentHashMap<String, Plan>,
                clientConfig: ClientConfig
            ) {
                trainingProcess(mnistJob, model, plans, clientConfig, result)
            }

            override fun onComplete() {
                syftWorker.dispose()
                val outputData = workDataOf(LOSS_LIST to result)
                statusPublisher.offer(Result.success(outputData))
            }

            override fun onRejected(timeout: String) {
                statusPublisher.offer(Result.retry())
            }

            override fun onError(throwable: Throwable) {
                statusPublisher.onError(throwable)
            }
        }
        mnistJob.start(jobStatusSubscriber)
        return statusPublisher.onBackpressureBuffer().firstOrError()
    }

    fun trainingProcess(
        mnistJob: SyftJob,
        model: SyftModel,
        plans: ConcurrentHashMap<String, Plan>,
        clientConfig: ClientConfig,
        result: MutableList<Float>
    ) {

        plans.values.first().let { plan ->
            repeat(clientConfig.maxUpdates) { step ->
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
                    return
                }
            }
            val diff = mnistJob.createDiff()
            mnistJob.report(diff)
        }
    }
}