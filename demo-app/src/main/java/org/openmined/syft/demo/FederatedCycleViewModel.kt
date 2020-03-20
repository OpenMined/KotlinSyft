package org.openmined.syft.demo

import android.util.Log
import androidx.lifecycle.ViewModel
import org.openmined.syft.Syft
import org.openmined.syft.networking.clients.HttpClient
import org.openmined.syft.networking.clients.SocketClient
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
    private val mnistJob = syftWorker.newJob("mnist")

    fun startCycle() {
        Log.d(TAG,"mnist job started")
        mnistJob.start()
        Log.d(TAG,"mnist job finished")
    }
}