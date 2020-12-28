package org.openmined.syft.demo.federated.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import org.openmined.syft.demo.federated.datasource.MNISTDataset
import org.openmined.syft.demo.federated.domain.TrainingTask
import org.openmined.syft.demo.federated.logging.MnistLogger
import org.openmined.syft.demo.federated.ui.main.AUTH_TOKEN
import org.openmined.syft.demo.federated.ui.main.BASE_URL
import org.openmined.syft.demo.federated.ui.ContentState
import org.openmined.syft.demo.federated.ui.ProcessData
import org.openmined.syft.demo.federated.ui.work.WorkInfoActivity
import org.openmined.syft.domain.SyftConfiguration
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

const val LOSS_LIST = "loss"
const val STATUS = "status"
const val EPOCH = "epoch"
const val LOG = "log"
const val NOTIFICATION_ID = 1
private const val CHANNEL_ID = "worker"

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
        val mnistDataset = MNISTDataset(serviceContext.resources)
        setForegroundAsync(createForegroundInfo(0))
        return awaitTask(
            TrainingTask(
                config,
                authToken,
                mnistDataset
            )
        )
    }

    private suspend fun awaitTask(task: TrainingTask) = suspendCoroutine<Result> { continuation ->
        task.runTask(WorkLogger()).subscribe { result: Result -> continuation.resume(result) }
    }

    private fun createForegroundInfo(progress: Int): ForegroundInfo {
        val intent = WorkManager.getInstance(applicationContext)
                .createCancelPendingIntent(id)
        val notifyIntent = Intent(serviceContext, WorkInfoActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val notifyPendingIntent = PendingIntent.getActivity(
            serviceContext, 0, notifyIntent, PendingIntent.FLAG_ONE_SHOT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notification = NotificationCompat.Builder(applicationContext,
            CHANNEL_ID
        )
                .setContentTitle("Federated Trainer")
                .setProgress(100, progress, false)
                .setTicker("Federated Trainer")
                .setContentIntent(notifyPendingIntent)
                .setContentText("running epoch $progress")
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_delete, "cancel training", intent)
                .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val name = "testing"
        val descriptionText = "testing"
        val importance = NotificationManager.IMPORTANCE_LOW
        val mChannel = NotificationChannel("worker", name, importance)
        mChannel.description = descriptionText
        val notificationManager =
                serviceContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    inner class WorkLogger : MnistLogger {

        override val logText
            get() = logTextInternal
        private val logTextInternal = MutableLiveData<String>()

        override val steps
            get() = stepsInternal
        private val stepsInternal = MutableLiveData<String>()

        override val processState
            get() = processStateInternal
        private val processStateInternal = MutableLiveData<ContentState>()

        override val processData
            get() = processDataInternal
        private val processDataInternal = MutableLiveData<ProcessData>()

        private val workManager = WorkManager.getInstance(serviceContext)

        override fun postState(status: ContentState) {
            publish(STATUS to status.toString())
        }

        override fun postData(result: Float) {
            publish(LOSS_LIST to result)
        }

        override fun postEpoch(epoch: Int) {
            if (epoch % 10 == 0 && getState() == WorkInfo.State.RUNNING)
                setForegroundAsync(createForegroundInfo(epoch))
            publish(EPOCH to epoch)
        }

        override fun postLog(message: String) {
            publish(LOG to message)
        }

        private fun getState() = workManager.getWorkInfoById(id).get().state

        private fun publish(pair: Pair<String, Any>) {
            if (getState() == WorkInfo.State.RUNNING)
                setProgressAsync(workDataOf(pair))
        }

    }
}