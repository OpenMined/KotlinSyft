package org.openmined.syft.demo.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import org.openmined.syft.Syft
import org.openmined.syft.networking.clients.HttpClient
import org.openmined.syft.networking.clients.SocketClient
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.processes.JobStatusSubscriber
import org.openmined.syft.threading.ProcessSchedulers

private const val TAG = "FederatedCycleViewModel"

@ExperimentalUnsignedTypes
class FederatedCycleViewModel(
    socketClient: SocketClient,
    httpClient: HttpClient,
    networkSchedulers: ProcessSchedulers,
    computeSchedulers: ProcessSchedulers
) : ViewModel() {
    private val syftWorker = Syft.getInstance(
        socketClient, httpClient,
        networkSchedulers, computeSchedulers
    )
    private val mnistJob = syftWorker.newJob("mnist", "1.0.0")

    fun startCycle() {
        Log.d(TAG, "mnist job started")
        val jobStatusSubscriber = object : JobStatusSubscriber() {
            override fun onReady(model: String, clientConfig: ClientConfig) {
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