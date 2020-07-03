package org.openmined.syft.demo.federated.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class MnistViewModelFactory(
    private val application: Application,
    private val baseURL: String,
    private val authToken: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MnistViewModel::class.java))
            return MnistViewModel(
                application,
                baseURL,
                authToken
            ) as T
        throw IllegalArgumentException("unknown view model class")
    }
}
