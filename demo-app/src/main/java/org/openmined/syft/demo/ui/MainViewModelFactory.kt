package org.openmined.syft.demo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.openmined.syft.demo.domain.MNISTModuleRepository
import org.openmined.syft.demo.domain.MNISTDataRepository
import org.openmined.syft.demo.domain.MNISTTrainer
import org.openmined.syft.threading.ProcessSchedulers

class MainViewModelFactory(
    private val schedulerProvider: ProcessSchedulers,
    private val moduleRepository: MNISTModuleRepository,
    private val dataRepository: MNISTDataRepository,
    private val trainer: MNISTTrainer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(
            schedulerProvider,
            moduleRepository,
            dataRepository,
            trainer
        ) as T
    }
}
