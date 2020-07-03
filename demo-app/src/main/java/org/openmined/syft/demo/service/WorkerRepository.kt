package org.openmined.syft.demo.service

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import org.openmined.syft.demo.federated.ui.AUTH_TOKEN
import org.openmined.syft.demo.federated.ui.BASE_URL
import java.util.UUID

private const val TAG = "WorkerRepository"

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class WorkerRepository(private val workManager: WorkManager) {

    constructor(activity: AppCompatActivity) : this(WorkManager.getInstance(activity))

    fun submitJob(
        authToken: String,
        baseURL: String
    ): UUID {
        Log.d(TAG, "starting new work")
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
        return oneTimeRequest.id
    }

    fun getRunningWorkStatus(): UUID? {
        val prevWork = workManager.getWorkInfosByTag("trainer").get()
        if (prevWork.size > 1)
            prevWork.subList(1, prevWork.size).forEach {
                workManager.cancelWorkById(it.id)
                Log.d(TAG, "background worker id ${it.id} status ${it.state}")
            }
        return if (prevWork.size > 0 && prevWork[0].state == WorkInfo.State.RUNNING)
            prevWork[0].id
        else null
    }

    fun getWorkObserver(workRequestId: UUID) = workManager.getWorkInfoByIdLiveData(workRequestId)

    fun cancelAllWork() {
        workManager.cancelAllWork()
    }
}