package org.openmined.syft.demo.federated.ui.work

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.work.WorkInfo
import org.openmined.syft.demo.federated.logging.MnistLogger
import org.openmined.syft.demo.federated.service.EPOCH
import org.openmined.syft.demo.federated.service.LOG
import org.openmined.syft.demo.federated.service.LOSS_LIST
import org.openmined.syft.demo.federated.service.STATUS
import org.openmined.syft.demo.federated.service.WorkerRepository
import org.openmined.syft.domain.ContentState
import org.openmined.syft.domain.ProcessData

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class WorkInfoViewModel(private val workerRepository: WorkerRepository) : MnistLogger, ViewModel() {

    override val logText
        get() = logTextInternal
    private val logTextInternal = MutableLiveData<String>()

    override val steps
        get() = stepsInternal
    private val stepsInternal = MutableLiveData<String>()

    override val processState
        get() = processStateInternal
    private val processStateInternal = MutableLiveData<ContentState>()

    override val processData
        get() = processDataInternal
    private val processDataInternal = MutableLiveData<ProcessData>()

    override fun postState(status: ContentState) {
        processStateInternal.postValue(status)
    }

    override fun postData(result: Float) {
        processDataInternal.postValue(
            ProcessData(
                (processDataInternal.value?.data ?: emptyList()) + result
            )
        )
    }

    override fun postEpoch(epoch: Int) {
        stepsInternal.postValue("Step : $epoch")
    }

    override fun postLog(message: String) {
        logTextInternal.postValue("${logTextInternal.value ?: ""}\n\n$message")
    }

    fun getRunningWorkInfo() = workerRepository.getRunningWorkStatus()?.let {
        workerRepository.getWorkInfo(it)
    }

    fun getWorkInfoObserver() = Observer { workInfo: WorkInfo? ->
        if (workInfo != null) {
            val progress = workInfo.progress
            progress.getFloat(LOSS_LIST, -2.0f).takeIf { it > -1 }?.let {
                postData(it)
            }
            progress.getInt(EPOCH, -2).takeIf { it > -1 }?.let {
                postEpoch(it)
            }
            progress.getString(LOG)?.let {
                postLog(it)
            }
            postState(
                ContentState.getObjectFromString(
                    progress.getString(STATUS)
                ) ?: ContentState.Training
            )
        }
    }

}