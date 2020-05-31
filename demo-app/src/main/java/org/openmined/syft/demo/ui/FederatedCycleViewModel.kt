package org.openmined.syft.demo.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.openmined.syft.Syft
import org.openmined.syft.demo.domain.MNISTDataRepository
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.execution.JobStatusSubscriber
import org.openmined.syft.execution.Plan
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "FederatedCycleViewModel"

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class FederatedCycleViewModel(
    authToken: String,
    configuration: SyftConfiguration,
    private val mnistDataRepository: MNISTDataRepository
) : ViewModel() {
    private val syftWorker = Syft.getInstance(authToken, configuration)
    private val mnistJob = syftWorker.newJob("mnist", "1.0.0")

    val logger
        get() = _logger
    private val _logger = MutableLiveData<String>()

    val steps
        get() = _steps
    private val _steps = MutableLiveData<String>()

    val processState
        get() = _processState
    private val _processState = MutableLiveData<ProcessState>()

    val processData
        get() = _processData
    private val _processData = MutableLiveData<ProcessData>()

    fun startCycle() {
        postLog("MNIST job started")
        postState(ProcessState.Loading)
        val jobStatusSubscriber = object : JobStatusSubscriber() {

            override fun onReady(
                model: SyftModel,
                plans: ConcurrentHashMap<String, Plan>,
                clientConfig: ClientConfig
            ) {
                postLog("Model ${model.modelName} received.\nStarting training process")
                trainingProcess(model, plans, clientConfig)
            }

            override fun onRejected(timeout: String) {
                postLog("We've been rejected $timeout")
            }

            override fun onError(throwable: Throwable) {
                postLog("There was an error $throwable")
            }
        }
        mnistJob.start(jobStatusSubscriber)
    }

    private fun trainingProcess(
        model: SyftModel,
        plans: ConcurrentHashMap<String, Plan>,
        clientConfig: ClientConfig
    ) {

        plans.values.first().let { plan ->
            val result = mutableListOf<Float>()
            repeat(clientConfig.maxUpdates) { step ->
                postEpoch(step + 1)
                val batchData = mnistDataRepository.loadDataBatch(clientConfig.batchSize.toInt())
                val output = plan.execute(
                    model,
                    batchData,
                    clientConfig
                )?.toTuple()
                output?.let { outputResult ->
                    val paramSize = model.modelState!!.syftTensors.size
                    val beginIndex = outputResult.size - paramSize
                    val updatedParams =
                            outputResult.slice(beginIndex until outputResult.size - 1)
                    model.updateModel(updatedParams.map { it.toTensor() })
                    result.add(outputResult[1].toTensor().dataAsFloatArray.last())
                } ?: run {
                    postLog("the model returned empty array due to invalid device state")
                    Thread.sleep(100000)
                }
                postState(ProcessState.Hidden)
                postData(result)

            }
            postLog("Training done!")

        }

    }

    private fun postState(state: ProcessState) {
        _processState.postValue(state)
    }

    private fun postData(result: List<Float>) {
        _processData.postValue(ProcessData(result))
    }

    private fun postEpoch(epoch: Int) {
        _steps.postValue("Step : $epoch")
    }

    private fun postLog(message: String) {
        _logger.postValue("${_logger.value ?: ""}\n$message")
    }
}
