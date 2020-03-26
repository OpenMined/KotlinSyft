package org.openmined.syft.demo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.openmined.syft.networking.clients.HttpClient
import org.openmined.syft.networking.clients.SocketClient
import org.openmined.syft.threading.ProcessSchedulers

class MainViewModelFactory(
    private val socketClient: SocketClient,
    private val httpClient: HttpClient,
    private val networkSchedulers: ProcessSchedulers,
    private val computeSchedulers: ProcessSchedulers
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FederatedCycleViewModel(
            socketClient,
            httpClient,
            networkSchedulers,
            computeSchedulers
        ) as T
    }
}
