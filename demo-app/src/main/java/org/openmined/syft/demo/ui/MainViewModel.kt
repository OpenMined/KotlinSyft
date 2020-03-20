package org.openmined.syft.demo.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Completable
import org.openmined.syft.demo.domain.MNISTDataRepository
import org.openmined.syft.demo.domain.MNISTModuleRepository
import org.openmined.syft.demo.domain.MNISTTrainer
import org.openmined.syft.threading.ProcessSchedulers
import org.pytorch.IValue

class MainViewModel(
    private val schedulerProvider: ProcessSchedulers,
    private val moduleRepository: MNISTModuleRepository,
    private val dataRepository: MNISTDataRepository,
    private val trainer: MNISTTrainer
) : ViewModel() {

    val trainingState
        get() = _trainingState

    private val _trainingState: MutableLiveData<IValue> by lazy {
        MutableLiveData<IValue>()
    }

    fun process(): Completable {
        return Completable.fromAction {
            val trainingSet = dataRepository.loadData()
            val script = moduleRepository.loadModule()
            _trainingState.postValue(trainer.train(script, trainingSet))
        }.subscribeOn(schedulerProvider.computeThreadScheduler)
                .observeOn(schedulerProvider.calleeThreadScheduler)
    }
}
