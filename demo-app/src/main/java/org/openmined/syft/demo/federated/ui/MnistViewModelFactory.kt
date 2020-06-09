package org.openmined.syft.demo.federated.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.openmined.syft.demo.federated.domain.MNISTDataRepository
import org.openmined.syft.domain.SyftConfiguration
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class MnistViewModelFactory(
    private val authToken: String,
    private val config: SyftConfiguration,
    private val mnistDataRepository: MNISTDataRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MnistViewModel::class.java))
            return MnistViewModel(
                authToken,
                config,
                mnistDataRepository
            ) as T
        throw IllegalArgumentException("unknown view model class")
    }
}
