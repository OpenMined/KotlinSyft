package org.openmined.syft.demo.federated.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.work.WorkInfo
import io.reactivex.disposables.CompositeDisposable
import org.openmined.syft.demo.federated.datasource.LocalMNISTDataDataSource
import org.openmined.syft.demo.federated.domain.MNISTDataRepository
import org.openmined.syft.demo.federated.domain.TrainingTask
import org.openmined.syft.demo.federated.logging.ActivityLogger
import org.openmined.syft.demo.service.EPOCH
import org.openmined.syft.demo.service.LOG
import org.openmined.syft.demo.service.LOSS_LIST
import org.openmined.syft.demo.service.STATUS
import org.openmined.syft.demo.service.WorkerRepository
import org.openmined.syft.domain.SyftConfiguration
import java.util.UUID

private const val TAG = "FederatedCycleViewModel"

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class MnistViewModel(
    private val activity: AppCompatActivity,
    private val baseURL: String,
    private val authToken: String
) : ViewModel() {

    val logger = ActivityLogger.getInstance()
    val workerRepository = WorkerRepository(activity)
    private val compositeDisposable = CompositeDisposable()
    private var trainingTask: TrainingTask? = null

    fun launchBackgroundCycle() {
        val requestId = workerRepository.getRunningWorkStatus()
                        ?: workerRepository.submitJob(authToken, baseURL)
        attachMnistLogger(requestId)
    }

    fun launchForegroundCycle() {
        val config = SyftConfiguration.builder(activity, baseURL)
                .setCacheTimeout(0L)
                .build()
        val localMNISTDataDataSource = LocalMNISTDataDataSource(activity.resources)
        val dataRepository = MNISTDataRepository(localMNISTDataDataSource)

        trainingTask = TrainingTask(
            config,
            authToken,
            dataRepository
        )
        compositeDisposable.add(trainingTask!!.runTask(logger).subscribe())
    }

    fun disposeTraining() {
        compositeDisposable.clear()
        trainingTask?.disposeTraining()
    }

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
