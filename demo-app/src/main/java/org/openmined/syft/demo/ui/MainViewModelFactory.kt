package org.openmined.syft.demo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.openmined.syft.demo.domain.MNISTDataRepository
import org.openmined.syft.domain.SyftConfiguration

@Suppress("UNCHECKED_CAST")
@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class MainViewModelFactory(
    private val authToken: String,
    private val config: SyftConfiguration,
    private val mnistDataRepository: MNISTDataRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FederatedCycleViewModel(
            authToken,
            config,
            mnistDataRepository
        ) as T
    }
}
