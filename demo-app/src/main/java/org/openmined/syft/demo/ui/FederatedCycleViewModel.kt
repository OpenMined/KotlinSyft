package org.openmined.syft.demo.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.openmined.syft.Syft
import org.openmined.syft.demo.domain.MNISTDataRepository
import org.openmined.syft.domain.LocalConfiguration
import org.openmined.syft.execution.JobStatusSubscriber
import org.openmined.syft.execution.Plan
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel
import org.openmined.syft.threading.ProcessSchedulers
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "FederatedCycleViewModel"
private const val EPOCHS = 8000

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class FederatedCycleViewModel(
    baseUrl: String,
    authToken: String,
    private val mnistDataRepository: MNISTDataRepository,
    networkSchedulers: ProcessSchedulers,
    computeSchedulers: ProcessSchedulers,
    private val localConfiguration: LocalConfiguration
) : ViewModel() {
    private val syftWorker = Syft.getInstance(
        baseUrl, authToken,
        networkSchedulers, computeSchedulers
    )
    private val mnistJob = syftWorker.newJob("mnist", "1.0.0")

    val logger
        get() = _logger
    private val _logger = MutableLiveData<String>()

    val epoch
        get() = _epoch
    private val _epoch = MutableLiveData<String>()

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
            val loadData = mnistDataRepository.loadData(clientConfig.batchSize.toInt())
            val zipData = loadData.first zip loadData.second
            repeat(EPOCHS) {epoch->
                postLog("Starting epoch ${epoch + 1}")
                val tempRe = mutableListOf<Float>()
                zipData.forEach { trainingBatch ->
                    val output = plan.execute(
                        model,
                        trainingBatch,
                        clientConfig
                    )?.toTuple()
                    output?.let { outputResult ->
                        val paramSize = model.modelState!!.syftTensors.size
                        val beginIndex = outputResult.size - paramSize
                        val updatedParams =
                                outputResult.slice(beginIndex until outputResult.size - 1)
                        model.updateModel(updatedParams.map { it.toTensor() })
                        tempRe.add(outputResult[1].toTensor().dataAsFloatArray.last())
                    } ?: postLog("the model returned empty array")
                }
                postState(ProcessState.Hidden)
                if (epoch % 10 == 0) {
                    result.add(tempRe.sum())
                    postData(result)
                }
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
        _epoch.postValue("EPOCH : $epoch")
    }

    private fun postLog(message: String) {
        _logger.postValue("${_logger.value ?: ""}\n$message")
    }
}
