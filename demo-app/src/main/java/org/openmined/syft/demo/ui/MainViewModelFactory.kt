package org.openmined.syft.demo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.openmined.syft.demo.domain.MNISTDataRepository
import org.openmined.syft.domain.LocalConfiguration
import org.openmined.syft.threading.ProcessSchedulers

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class MainViewModelFactory(
    private val baseUrl: String,
    private val authToken: String,
    private val mnistDataRepository: MNISTDataRepository,
    private val networkSchedulers: ProcessSchedulers,
    private val computeSchedulers: ProcessSchedulers,
    private val localConfiguration: LocalConfiguration
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FederatedCycleViewModel(
            baseUrl,
            authToken,
            mnistDataRepository,
            networkSchedulers,
            computeSchedulers,
            localConfiguration
        ) as T
    }
}
