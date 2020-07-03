package org.openmined.syft.demo.federated.ui

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import io.reactivex.disposables.CompositeDisposable
import org.openmined.syft.demo.federated.datasource.LocalMNISTDataDataSource
import org.openmined.syft.demo.federated.domain.MNISTDataRepository
import org.openmined.syft.demo.federated.domain.TrainingTask
import org.openmined.syft.demo.federated.ui.logging.ActivityLogger
import org.openmined.syft.demo.service.EPOCH
import org.openmined.syft.demo.service.FederatedWorker
import org.openmined.syft.demo.service.LOG
import org.openmined.syft.demo.service.LOSS_LIST
import org.openmined.syft.demo.service.STATUS
import org.openmined.syft.domain.SyftConfiguration

private const val TAG = "FederatedCycleViewModel"

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class MnistViewModel(
    private val application: Application,
    private val baseURL: String,
    private val authToken: String
) : ViewModel() {

    val logger = ActivityLogger.getInstance()
    private val workManager = WorkManager.getInstance(application)
    private val compositeDisposable = CompositeDisposable()
    private var trainingTask: TrainingTask? = null

    fun launchBackgroundCycle() {
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresCharging(true).setRequiresDeviceIdle(false)
                .build()

        val oneTimeRequest = OneTimeWorkRequestBuilder<FederatedWorker>()
                .setInputData(workDataOf(AUTH_TOKEN to authToken, BASE_URL to baseURL))
                .setConstraints(constraints)
                .addTag("trainer")
                .build()
        workManager.enqueue(oneTimeRequest)
        attachMnistLogger(oneTimeRequest)
    }

    fun launchForegroundCycle() {
        val config = SyftConfiguration.builder(application, baseURL)
                .setCacheTimeout(0L)
                .build()
        val localMNISTDataDataSource = LocalMNISTDataDataSource(application.resources)
        val dataRepository = MNISTDataRepository(localMNISTDataDataSource)

        trainingTask = TrainingTask(
            config,
            authToken,
            dataRepository
        )
        compositeDisposable.add(trainingTask!!.runTask(logger).subscribe())
    }

    fun disposeTraining() {
        workManager.cancelAllWorkByTag("trainer")
        compositeDisposable.clear()
        trainingTask?.disposeTraining()
    }

    private fun attachMnistLogger(workRequest: WorkRequest) {
        workManager.getWorkInfoByIdLiveData(workRequest.id)
                .observe(application.applicationContext as LifecycleOwner, Observer { workInfo: WorkInfo? ->
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
                            ) ?: ContentState.Loading
                        )
                    }
                })
    }
}
