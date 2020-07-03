package org.openmined.syft.demo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import org.openmined.syft.demo.R
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
        setForegroundAsync(createForegroundInfo(0))
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

    private fun createForegroundInfo(progress: Int): ForegroundInfo {
        val id = "worker"
        val title = "trainer"
        val cancel = "cancel training"
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext)
                .createCancelPendingIntent(getId())

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notification = NotificationCompat.Builder(applicationContext, id)
                .setContentTitle(title)
                .setProgress(100, progress, false)
                .setTicker(title)
                .setContentText("running epoch $progress")
                .setSmallIcon(R.mipmap.pysyft_android)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_delete, cancel, intent)
                .build()

        return ForegroundInfo(1, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val name = "testing"
        val descriptionText = "testing"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel("worker", name, importance)
        mChannel.description = descriptionText
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager =
                serviceContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
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
            if (epoch % 10 == 0)
                setForegroundAsync(createForegroundInfo(epoch))
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