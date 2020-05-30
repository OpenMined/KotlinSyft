package org.openmined.syft

import android.accounts.NetworkErrorException
import android.util.Log
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.execution.JobStatusMessage
import org.openmined.syft.execution.JobStatusSubscriber
import org.openmined.syft.execution.SyftJob
import org.openmined.syft.monitor.DeviceMonitor
import org.openmined.syft.networking.datamodels.syft.AuthenticationRequest
import org.openmined.syft.networking.datamodels.syft.AuthenticationResponse
import org.openmined.syft.networking.datamodels.syft.CycleRequest
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import java.util.concurrent.ConcurrentHashMap


private const val TAG = "Syft"

@ExperimentalUnsignedTypes
class Syft internal constructor(
    private val authToken: String,
    private val syftConfig: SyftConfiguration,
    private val deviceMonitor: DeviceMonitor
) {
    companion object {
        @Volatile
        private var INSTANCE: Syft? = null

        fun getInstance(
            authToken: String,
            syftConfiguration: SyftConfiguration
        ): Syft =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: Syft(
                        authToken,
                        syftConfiguration,
                        DeviceMonitor(syftConfiguration)
                    ).also { INSTANCE = it }
                }
    }

    private val workerJobs = ConcurrentHashMap<SyftJob.JobID, SyftJob>()
    private val jobStatusProcessors =
            ConcurrentHashMap<SyftJob.JobID, PublishProcessor<JobStatusMessage>>()
    private val compositeDisposable = CompositeDisposable()

    @Volatile
    private var workerId: String? = null

    fun newJob(
        model: String,
        version: String? = null
    ): SyftJob {
        val publishProcessor = PublishProcessor.create<JobStatusMessage>()
        val job = SyftJob(
            model,
            version,
            this,
            publishProcessor,
            syftConfig
        )
        jobStatusProcessors[job.jobId] = publishProcessor
        workerJobs[job.jobId] = job
        job.subscribe(object : JobStatusSubscriber() {
            override fun onComplete() {
                workerJobs.remove(job.jobId)
            }

            override fun onError(throwable: Throwable) {
                workerJobs.remove(job.jobId)
            }
        }, syftConfig.networkingSchedulers)

        return job
    }

    fun getSyftWorkerId() = workerId

    fun executeCycleRequest(job: SyftJob) {
        if (returnJobErrorIfStateInvalid(job)) return
        workerId?.let { id ->
            compositeDisposable.add(
                deviceMonitor.getNetworkStatus(id)
                        .flatMap { networkState ->
                            requestCycle(
                                id,
                                job,
                                networkState.ping,
                                networkState.downloadSpeed,
                                networkState.uploadspeed
                            )
                        }
                        .compose(syftConfig.networkingSchedulers.applySingleSchedulers())
                        .subscribe(
                            { response: CycleResponseData ->
                                when (response) {
                                    is CycleResponseData.CycleAccept -> handleCycleAccept(response)
                                    is CycleResponseData.CycleReject -> handleCycleReject(response)
                                }
                            },
                            { errorMsg: Throwable ->
                                jobStatusProcessors[job.jobId]?.onError(errorMsg)
                            })
            )
        } ?: executeAuthentication(job)
    }

    fun dispose() {
        compositeDisposable.clear()
        workerJobs.forEach { (_, job) -> job.dispose() }
    }

    fun returnJobErrorIfStateInvalid(job: SyftJob): Boolean {
        when {
            !deviceMonitor.isNetworkStateValid() -> {
                jobStatusProcessors[job.jobId]?.onError(
                    IllegalStateException("network connection broken")
                )
                return true
            }
            !deviceMonitor.isActivityStateValid() -> {
                jobStatusProcessors[job.jobId]?.onError(
                    IllegalStateException("user activity detected")
                )
                return true
            }
            !deviceMonitor.isBatteryStateValid() -> {
                jobStatusProcessors[job.jobId]?.onError(
                    IllegalStateException("user activity detected")
                )
                return true
            }
            else ->
                return false
        }
    }

    private fun requestCycle(
        id: String,
        job: SyftJob,
        ping: String?,
        downloadSpeed: String?,
        uploadSpeed: String?
    ): Single<CycleResponseData> {
        return when {
            ping == null ->
                Single.error(NetworkErrorException("unable to get ping"))
            downloadSpeed == null ->
                Single.error(NetworkErrorException("unable to verify download speed"))
            uploadSpeed == null ->
                Single.error(NetworkErrorException("unable to verify upload speed"))
            else -> syftConfig.getSignallingClient().getCycle(
                CycleRequest(
                    id, job.jobId.modelName,
                    job.jobId.version,
                    ping,
                    downloadSpeed,
                    uploadSpeed
                )
            )
        }
    }

    private fun handleCycleReject(responseData: CycleResponseData.CycleReject) {
        val job = workerJobs.getValue(
            SyftJob.JobID(
                responseData.modelName,
                responseData.version
            )
        )
        job.cycleRejected(responseData)
    }

    private fun handleCycleAccept(responseData: CycleResponseData.CycleAccept) {
        val job = workerJobs.getValue(
            SyftJob.JobID(
                responseData.modelName,
                responseData.version
            )
        )
        job.setJobArguments(responseData)
        if (returnJobErrorIfStateInvalid(job))
            return
        workerId?.let {
            job.downloadData(it)
        } ?: throw IllegalStateException("workerId is not initialised")

    }

    private fun executeAuthentication(job: SyftJob) {
        compositeDisposable.add(
            syftConfig.getSignallingClient().authenticate(AuthenticationRequest(authToken))
                    .compose(syftConfig.networkingSchedulers.applySingleSchedulers())
                    .subscribe({ t: AuthenticationResponse ->
                        when (t) {
                            is AuthenticationResponse.AuthenticationSuccess -> {
                                if (workerId == null)
                                    setSyftWorkerId(t.workerId)
                                executeCycleRequest(job)
                            }
                            is AuthenticationResponse.AuthenticationError ->
                                Log.d(TAG, t.errorMessage)
                        }
                    }, {
                        jobStatusProcessors[job.jobId]?.onError(it)
                    })
        )
    }

    @Synchronized
    private fun setSyftWorkerId(workerId: String) {
        if (this.workerId == null)
            this.workerId = workerId
        else if (workerJobs.isEmpty())
            this.workerId = workerId
    }

}
