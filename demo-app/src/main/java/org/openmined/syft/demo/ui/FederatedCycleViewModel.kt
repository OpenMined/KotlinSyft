package org.openmined.syft.demo.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import org.openmined.syft.Syft
import org.openmined.syft.execution.JobStatusSubscriber
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel
import org.openmined.syft.threading.ProcessSchedulers

private const val TAG = "FederatedCycleViewModel"

@ExperimentalUnsignedTypes
class FederatedCycleViewModel(
    baseUrl: String,
    authToken: String,
    networkSchedulers: ProcessSchedulers,
    computeSchedulers: ProcessSchedulers
) : ViewModel() {
    private val syftWorker = Syft.getInstance(
        baseUrl, authToken,
        networkSchedulers, computeSchedulers
    )
    private val mnistJob = syftWorker.newJob("mnist", "1.0.0")

    fun startCycle() {
        Log.d(TAG, "mnist job started")
        val jobStatusSubscriber = object : JobStatusSubscriber() {
            override fun onReady(model: SyftModel, clientConfig: ClientConfig) {
                //todo training code goes here
            }

            override fun onRejected(timeout: String) {
                //TODO retry after timeout
            }

            override fun onError(throwable: Throwable) {
                //todo error functionality
            }
        }
        mnistJob.start(jobStatusSubscriber)
    }
}