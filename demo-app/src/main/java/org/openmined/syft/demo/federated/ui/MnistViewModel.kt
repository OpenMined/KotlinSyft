package org.openmined.syft.demo.federated.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import io.reactivex.disposables.CompositeDisposable
import org.openmined.syft.demo.federated.datasource.LocalMNISTDataDataSource
import org.openmined.syft.demo.federated.domain.MNISTDataRepository
import org.openmined.syft.demo.service.FederatedWorker
import org.openmined.syft.demo.service.TrainingTask
import org.openmined.syft.domain.SyftConfiguration

private const val TAG = "FederatedCycleViewModel"

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class MnistViewModel(
    private val application: Application,
    private val baseURL: String,
    private val authToken: String
) : ViewModel() {

    val logger = Logger.getInstance()
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
    }

    fun launchForegroundCycle() {
        val config = SyftConfiguration.builder(application, baseURL)
                .enableBackgroundServiceExecution()
                .setCacheTimeout(0L)
                .build()
        val localMNISTDataDataSource = LocalMNISTDataDataSource(application.resources)
        val dataRepository = MNISTDataRepository(localMNISTDataDataSource)

        trainingTask = TrainingTask(config, authToken, dataRepository)
        compositeDisposable.add(trainingTask!!.runTask().subscribe())
    }

    fun disposeTraining() {
        workManager.cancelAllWorkByTag("trainer")
        compositeDisposable.clear()
        trainingTask?.disposeTraining()
    }
}
