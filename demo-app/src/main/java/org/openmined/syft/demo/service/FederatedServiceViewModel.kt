package org.openmined.syft.demo.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

const val AUTH_TOKEN = "authToken"
const val BASE_URL = "baseUrl"

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
class FederatedServiceViewModel(private val context: Context, private val authToken: String) {
    fun launchFederatedService() {
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresCharging(true).setRequiresDeviceIdle(true)
                .build()

        val oneTimeRequest = OneTimeWorkRequestBuilder<FederatedWorker>()
                .setInputData(workDataOf(AUTH_TOKEN to authToken))
                .setConstraints(constraints)
                .addTag("trainer")
                .build()
        WorkManager.getInstance(context).enqueue(oneTimeRequest)
    }
}