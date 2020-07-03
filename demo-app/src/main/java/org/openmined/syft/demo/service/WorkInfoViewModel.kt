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
                            logger.postData(
                                progress.getFloatArray(LOSS_LIST)?.toList() ?: emptyList()
                            )
                            logger.postEpoch(progress.getInt(EPOCH, -1))
                            logger.postLog(progress.getString(LOG) ?: "empty log")
                            logger.postState(
                                ContentState.getObjectFromString(
                                    progress.getString(STATUS)
                                ) ?: ContentState.Training
                            )
                        }
                    })
    }
}