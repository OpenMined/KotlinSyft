package org.openmined.syft.demo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.openmined.syft.demo.domain.MNISTDataRepository
import org.openmined.syft.demo.domain.MNISTTrainer
import org.openmined.syft.domain.LocalConfiguration
import org.openmined.syft.domain.ModelRepository
import org.openmined.syft.networking.clients.HttpClient
import org.openmined.syft.networking.clients.SocketClient
import org.openmined.syft.threading.ProcessSchedulers

@ExperimentalUnsignedTypes
class MainViewModelFactory(
    private val baseUrl: String,
    private val authToken: String,
    private val mnistDataRepository: MNISTDataRepository,
    private val mnistTrainer: MNISTTrainer,
    private val modelRepository: ModelRepository,
    private val networkSchedulers: ProcessSchedulers,
    private val computeSchedulers: ProcessSchedulers,
    private val localConfiguration: LocalConfiguration
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FederatedCycleViewModel(
            mnistDataRepository,
            mnistTrainer,
            modelRepository,
            baseUrl,
            authToken,
            networkSchedulers,
            computeSchedulers
        ) as T
    }
}
