package org.openmined.syft.demo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.openmined.syft.threading.ProcessSchedulers

@ExperimentalUnsignedTypes
class MainViewModelFactory(
    private val baseUrl: String,
    private val authToken: String,
    private val networkSchedulers: ProcessSchedulers,
    private val computeSchedulers: ProcessSchedulers
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FederatedCycleViewModel(
            baseUrl,
            authToken,
            networkSchedulers,
            computeSchedulers
        ) as T
    }
}
