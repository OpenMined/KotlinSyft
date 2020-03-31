package org.openmined.syft.demo.ui

import android.util.Log
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


    fun startCycle() {
        Log.d(TAG, "mnist job started")
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

    private fun postLog(message: String) {
        _logger.postValue("${_logger.value ?: ""}\n$message")
    }

    private fun trainingProcess(
        model: SyftModel,
        plans: ConcurrentHashMap<String, Plan>,
        clientConfig: ClientConfig
    ) {
        val destinationDir = "/data/data/org.openmined.syft.demo/files/plans"

        val plan = plans.toList().first().second
        plan.generateScriptModule(localConfiguration.plansLocation, "$destinationDir/${plan.planId}")
        val loadData = mnistDataRepository.loadData()
        plan.execute(model, loadData, clientConfig)
    }
}