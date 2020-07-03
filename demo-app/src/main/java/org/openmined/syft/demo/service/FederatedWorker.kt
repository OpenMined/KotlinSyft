package org.openmined.syft.demo.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import org.openmined.syft.demo.federated.datasource.LocalMNISTDataDataSource
import org.openmined.syft.demo.federated.domain.MNISTDataRepository
import org.openmined.syft.demo.federated.domain.TrainingTask
import org.openmined.syft.demo.federated.ui.AUTH_TOKEN
import org.openmined.syft.demo.federated.ui.BASE_URL
import org.openmined.syft.demo.federated.ui.ContentState
import org.openmined.syft.demo.federated.ui.logging.MnistLogger
import org.openmined.syft.domain.SyftConfiguration
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

const val LOSS_LIST = "loss"
const val STATUS = "status"
const val EPOCH = "epoch"
const val LOG = "log"

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class FederatedWorker(
    private val serviceContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(serviceContext, workerParameters) {

    override suspend fun doWork(): Result {
        val authToken = inputData.getString(AUTH_TOKEN) ?: return Result.failure()
        val baseUrl = inputData.getString(BASE_URL) ?: return Result.failure()

        val config = SyftConfiguration.builder(serviceContext, baseUrl)
                .enableBackgroundServiceExecution()
                .setCacheTimeout(0L)
                .build()
        val localMNISTDataDataSource = LocalMNISTDataDataSource(serviceContext.resources)
        val dataRepository = MNISTDataRepository(localMNISTDataDataSource)
        return awaitTask(
            TrainingTask(
                config,
                authToken,
                dataRepository
            )
        )
    }

    private suspend fun awaitTask(task: TrainingTask) = suspendCoroutine<Result> { continuation ->
        task.runTask(WorkLogger()).subscribe { result: Result -> continuation.resume(result) }
    }

    inner class WorkLogger : MnistLogger() {
        private val state = mutableMapOf<String, Any>()

        override fun postState(status: ContentState) {
            state[STATUS] = status.toString()
            setProgressAsync(getWorkData())
        }

        override fun postData(result: List<Float>) {
            state[LOSS_LIST] = result.toFloatArray()
            setProgressAsync(getWorkData())
        }

        override fun postEpoch(epoch: Int) {
            state[EPOCH] = epoch
            setProgressAsync(getWorkData())
        }

        override fun postLog(message: String) {
            state[LOG] = message
            setProgressAsync(getWorkData())
        }

        private fun getWorkData(): Data {
            return workDataOf(
                *(state.map { (key, value) ->
                    key to value
                }.toTypedArray())
            )
        }
    }
}