package org.openmined.syft.demo.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import org.openmined.syft.demo.federated.datasource.LocalMNISTDataDataSource
import org.openmined.syft.demo.federated.domain.MNISTDataRepository
import org.openmined.syft.demo.federated.ui.AUTH_TOKEN
import org.openmined.syft.demo.federated.ui.BASE_URL
import org.openmined.syft.domain.SyftConfiguration
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class FederatedWorker(
    private val serviceContext: Context,
    workerParameters: WorkerParameters
) : Worker(serviceContext, workerParameters) {

    override fun doWork(): Result {
        var status: Result
        val authToken = inputData.getString(AUTH_TOKEN) ?: return Result.failure()
        val baseUrl = inputData.getString(BASE_URL) ?: return Result.failure()

        val config = SyftConfiguration.builder(serviceContext, baseUrl)
                .enableBackgroundServiceExecution()
                .setCacheTimeout(0L)
                .build()
        val localMNISTDataDataSource = LocalMNISTDataDataSource(serviceContext.resources)
        val dataRepository = MNISTDataRepository(localMNISTDataDataSource)

        runBlocking {
            status = awaitTask(
                TrainingTask(
                    config,
                    authToken,
                    dataRepository
                )
            )
        }
        return status
    }

    private suspend fun awaitTask(task: TrainingTask) =
            suspendCoroutine { continuation: Continuation<Result> ->
                task.runTask().subscribe(
                    { continuation.resume(it) },
                    { continuation.resumeWithException(it) })
            }
}