package org.openmined.syft.demo.service

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.work.WorkInfo
import org.openmined.syft.demo.federated.logging.ActivityLogger
import org.openmined.syft.demo.federated.ui.ContentState
import java.util.UUID

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class WorkInfoViewModel(private val activity: AppCompatActivity) : ViewModel() {
    val logger = ActivityLogger.getInstance()
    val workerRepository = WorkerRepository(activity)

    fun attachMnistLogger(workRequestId: UUID) {
        workerRepository.getWorkObserver(workRequestId)
                .observe(
                    activity,
                    Observer { workInfo: WorkInfo? ->
                        if (workInfo != null) {
                            val progress = workInfo.progress
                            progress.getFloat(LOSS_LIST, -2.0f).takeIf { it > -1 }?.let {
                                logger.postData(it)
                            }
                            progress.getInt(EPOCH, -2).takeIf { it > -1 }?.let {
                                logger.postEpoch(it)
                            }
                            progress.getString(LOG)?.let {
                                logger.postLog(it)
                            }
                            logger.postState(
                                ContentState.getObjectFromString(
                                    progress.getString(STATUS)
                                ) ?: ContentState.Training
                            )
                        }
                    })
    }
}