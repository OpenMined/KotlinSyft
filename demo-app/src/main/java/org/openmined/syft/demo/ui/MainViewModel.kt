package org.openmined.syft.demo.ui

import androidx.lifecycle.ViewModel
import io.reactivex.Completable
import org.openmined.syft.demo.domain.MNISTDataRepository
import org.openmined.syft.demo.domain.MNISTModuleRepository
import org.openmined.syft.demo.domain.MNISTTrainer
import org.openmined.syft.threading.ProcessSchedulers

class MainViewModel(
    private val schedulerProvider: ProcessSchedulers,
    private val moduleRepository: MNISTModuleRepository,
    private val dataRepository: MNISTDataRepository,
    private val trainer: MNISTTrainer
) : ViewModel() {

    fun process(): Completable {
        return Completable.fromAction {
            val trainingSet = dataRepository.loadData()
            val script = moduleRepository.loadModule()
            trainer.train(script, trainingSet)
        }.subscribeOn(schedulerProvider.computeThreadScheduler)
                .observeOn(schedulerProvider.calleeThreadScheduler)
    }
}
