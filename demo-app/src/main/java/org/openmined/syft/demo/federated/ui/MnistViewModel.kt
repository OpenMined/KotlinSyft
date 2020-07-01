package org.openmined.syft.demo.federated.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.openmined.syft.Syft
import org.openmined.syft.demo.federated.domain.MNISTDataRepository
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.execution.JobStatusSubscriber
import org.openmined.syft.execution.Plan
import org.openmined.syft.execution.SyftJob
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "FederatedCycleViewModel"

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class MnistViewModel(
    private val authToken: String,
    private val configuration: SyftConfiguration,
    private val mnistDataRepository: MNISTDataRepository
) : ViewModel() {

    private lateinit var syftWorker: Syft
    private lateinit var mnistJob: SyftJob

    val logger
        get() = _logger
    private val _logger = MutableLiveData<String>()

    val steps
        get() = _steps
    private val _steps = MutableLiveData<String>()

    val processState
        get() = _processState
    private val _processState = MutableLiveData<ContentState>()

    val processData
        get() = _processData
    private val _processData = MutableLiveData<ProcessData>()

    private val result = mutableListOf<Float>()

    fun startCycle() {
        syftWorker = Syft.getInstance(configuration, authToken)
        mnistJob = syftWorker.newJob("mnist", "1.0.0")
        postLog("MNIST job started \n\nChecking for download and upload speeds")
        postState(ContentState.Loading)
        val jobStatusSubscriber = object : JobStatusSubscriber() {

            override fun onReady(
                model: SyftModel,
                plans: ConcurrentHashMap<String, Plan>,
                clientConfig: ClientConfig
            ) {
                postLog("Model ${model.modelName} received.\n\nStarting training process")
                trainingProcess(model, plans, clientConfig)
            }

            override fun onComplete() {
                syftWorker.dispose()
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

    fun disposeTraining(){
        syftWorker.dispose()
    }

    private fun trainingProcess(
        model: SyftModel,
        plans: ConcurrentHashMap<String, Plan>,
        clientConfig: ClientConfig
    ) {

        plans.values.first().let { plan ->
            repeat(clientConfig.maxUpdates) { step ->
                postEpoch(step + 1)
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
                    postLog("the model returned empty array due to invalid device state")
                    return
                }
                postState(ContentState.Training)
                postData(result)

            }
            postLog("Training done!\n reporting diff")
            val diff = mnistJob.createDiff()
            mnistJob.report(diff)
            postLog("reported the model to PyGrid")

        }

    }

    private fun postState(state: ContentState) {
        _processState.postValue(state)
    }

    private fun postData(result: List<Float>) {
        _processData.postValue(
            ProcessData(
                result
            )
        )
    }

    private fun postEpoch(epoch: Int) {
        _steps.postValue("Step : $epoch")
    }

    private fun postLog(message: String) {
        _logger.postValue("${_logger.value ?: ""}\n\n$message")
    }
}
