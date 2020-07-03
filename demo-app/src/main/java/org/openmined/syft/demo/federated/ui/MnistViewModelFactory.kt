package org.openmined.syft.demo.federated.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.openmined.syft.demo.federated.domain.MNISTDataRepository
import org.openmined.syft.domain.SyftConfiguration

@Suppress("UNCHECKED_CAST")
@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class MnistViewModelFactory(
    private val application: Application,
    private val authToken: String,
    private val baseURL: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MnistViewModel::class.java))
            return MnistViewModel(
                application,
                authToken,
                baseURL
            ) as T
        throw IllegalArgumentException("unknown view model class")
    }
}
