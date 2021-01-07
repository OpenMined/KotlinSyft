package org.openmined.syft.demo.federated.service

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.openmined.syft.demo.federated.ui.main.AUTH_TOKEN
import org.openmined.syft.demo.federated.ui.main.BASE_URL
import org.openmined.syft.demo.federated.ui.main.MODEL_NAME
import org.openmined.syft.demo.federated.ui.main.MODEL_VERSION
import java.util.UUID

private const val TAG = "WorkerRepository"

@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class WorkerRepository(
    private val workManager: WorkManager,
    private val modelName: String,
    private val modelVersion: String) {

    constructor(activity: AppCompatActivity, modelName: String, modelVersion: String) : this(WorkManager.getInstance(activity), modelName, modelVersion)

    fun submitJob(
        authToken: String,
        baseURL: String
    ): UUID {
        Log.d(TAG, "starting new work")
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresCharging(true).setRequiresDeviceIdle(false)
                .build()

        val inputData = workDataOf(
            AUTH_TOKEN to authToken,
            BASE_URL to baseURL,
            MODEL_NAME to modelName,
            MODEL_VERSION to modelVersion
        )

        val oneTimeRequest = OneTimeWorkRequestBuilder<FederatedWorker>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .addTag("trainer")
                .build()
        workManager.enqueue(oneTimeRequest)
        return oneTimeRequest.id
    }

    fun getRunningWorkStatus(): UUID? {
        val prevWork = workManager.getWorkInfosByTag("trainer").get()
                .filter { it.state == WorkInfo.State.RUNNING }
        if (prevWork.size > 1)
            prevWork.subList(1, prevWork.size)
                    .forEach {
                        workManager.cancelWorkById(it.id)
                        Log.d(TAG, "background worker id ${it.id} status ${it.state}")
                    }
        return if (prevWork.isNotEmpty())
            prevWork[0].id
        else null
    }

    fun getWorkInfo(workRequestId: UUID) = workManager.getWorkInfoByIdLiveData(workRequestId)

    fun cancelAllWork() {
        workManager.cancelAllWork()
    }
}