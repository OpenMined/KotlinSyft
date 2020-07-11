package org.openmined.syft

import android.accounts.NetworkErrorException
import android.util.Log
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.execution.JobStatusSubscriber
import org.openmined.syft.execution.SyftJob
import org.openmined.syft.fp.Either
import org.openmined.syft.monitor.DeviceMonitor
import org.openmined.syft.networking.datamodels.syft.AuthenticationRequest
import org.openmined.syft.networking.datamodels.syft.AuthenticationResponse
import org.openmined.syft.networking.datamodels.syft.CycleRequest
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "Syft"

@ExperimentalUnsignedTypes
class Syft internal constructor(
    private val syftConfig: SyftConfiguration,
    private val deviceMonitor: DeviceMonitor,
    private val authToken: String?,
    private val isSpeedTestEnable: Boolean
) : Disposable {
    companion object {
        @Volatile
        private var INSTANCE: Syft? = null

        fun getInstance(
            syftConfiguration: SyftConfiguration,
            authToken: String? = null
        ): Syft {
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let {
                    if (it.syftConfig == syftConfiguration && it.authToken == authToken) it
                    else throw ExceptionInInitializerError("syft worker initialised with different parameters. Dispose previous worker")
                } ?: Syft(
                    syftConfig = syftConfiguration,
                    deviceMonitor = DeviceMonitor.construct(syftConfiguration),
                    authToken = authToken,
                    isSpeedTestEnable = true
                ).also { INSTANCE = it }
            }
        }
    }

    //todo single job for now but eventually worker should support multiple jobs
    private var workerJob: SyftJob? = null
    private val compositeDisposable = CompositeDisposable()
    private val isDisposed = AtomicBoolean(false)

    @Volatile
    private var workerId: String? = null

    private var requiresSpeedTest: Boolean = true

    fun newJob(
        model: String,
        version: String? = null
    ): SyftJob {
        val job = SyftJob.create(
            model,
            version,
            this,
            syftConfig
        )
//        if (syftConfig.maxConcurrentJobs == workerJob.size)
//            throw IndexOutOfBoundsException("maximum number of allowed jobs reached")

        workerJob = job
        job.subscribe(object : JobStatusSubscriber() {
            override fun onComplete() {
                workerJob = null
            }

            override fun onError(throwable: Throwable) {
                Log.e(TAG, throwable.message.toString())
                workerJob = null
            }
        }, syftConfig.networkingSchedulers)

        return job
    }

    internal fun getSyftWorkerId() = workerId

    internal fun executeCycleRequest(job: SyftJob) {
        if (jobErrorIfBatteryInvalid(job) || jobErrorIfNetworkInvalid(job))
            return

        val isRequiresSpeedTestEnabled = isSpeedTestEnable and requiresSpeedTest
        Log.d(TAG, "isRequiresSpeedTestEnabled $isRequiresSpeedTestEnabled")

        workerId?.let { id ->
            compositeDisposable.add(
                deviceMonitor.getNetworkStatus(id, isRequiresSpeedTestEnabled)
                        .flatMap { networkState ->
                            requestCycle(
                                id,
                                job,
                                networkState.ping,
                                networkState.downloadSpeed,
                                networkState.uploadSpeed
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
                                job.throwError(errorMsg)
                            })
            )
        } ?: executeAuthentication(job)
    }

    override fun isDisposed() = isDisposed.get()

    override fun dispose() {
        Log.d(TAG, "disposing syft worker")
        deviceMonitor.dispose()
        compositeDisposable.clear()
        workerJob?.dispose()
        INSTANCE = null
    }

    internal fun jobErrorIfNetworkInvalid(job: SyftJob): Boolean {
        if (!deviceMonitor.isNetworkStateValid()) {
            job.throwError(IllegalStateException("network constraints failed"))
            disposeSocketClient()
            return true
        }
        return false
    }

    internal fun jobErrorIfBatteryInvalid(job: SyftJob): Boolean {
        if (!deviceMonitor.isBatteryStateValid()) {
            job.throwError(IllegalStateException("Battery constraints failed"))
            return true
        }
        return false
    }

    private fun requestCycle(
        id: String,
        job: SyftJob,
        ping: String?,
        downloadSpeed: String?,
        uploadSpeed: String?
    ): Single<CycleResponseData> {

        return when (val check = checkConditions(ping, downloadSpeed, uploadSpeed)) {
            is Either.Left -> Single.error(NetworkErrorException(check.a))
            is Either.Right -> syftConfig.getSignallingClient().getCycle(
                CycleRequest(
                    id,
                    job.jobId.modelName,
                    job.jobId.version,
                    if (ping.isNullOrEmpty()) "10" else ping,
                    if (downloadSpeed.isNullOrEmpty()) "10" else downloadSpeed,
                    if (uploadSpeed.isNullOrEmpty()) "10" else uploadSpeed
                )
            )
        }
    }

    private fun checkConditions(
        ping: String?,
        downloadSpeed: String?,
        uploadSpeed: String?
    ): Either<String, Boolean> {
        return when {
            ping == null ->
                Either.Left("unable to get ping")
            downloadSpeed == null ->
                Either.Left("unable to verify download speed")
            uploadSpeed == null ->
                Either.Left("unable to verify upload speed")
            else -> Either.Right(true)
        }
    }

    private fun handleCycleReject(responseData: CycleResponseData.CycleReject) {
        workerJob?.cycleRejected(responseData)
    }

    private fun handleCycleAccept(responseData: CycleResponseData.CycleAccept) {
        val job = workerJob ?: throw IllegalStateException("job deleted and accessed")
        job.cycleAccepted(responseData)
        if (jobErrorIfBatteryInvalid(job) ||
            jobErrorIfNetworkInvalid(job)
        )
            return

        workerId?.let {
            job.downloadData(it, responseData)
        } ?: throw IllegalStateException("workerId is not initialised")

    }

    private fun executeAuthentication(job: SyftJob) {
        compositeDisposable.add(
            syftConfig.getSignallingClient().authenticate(
                AuthenticationRequest(
                    authToken,
                    job.jobId.modelName,
                    job.jobId.version
                )
            )
                    .compose(syftConfig.networkingSchedulers.applySingleSchedulers())
                    .subscribe({ response: AuthenticationResponse ->
                        when (response) {
                            is AuthenticationResponse.AuthenticationSuccess -> {
                                if (workerId == null) {
                                    setSyftWorkerId(response.workerId)
                                    requiresSpeedTest = response.requiresSpeedTest
                                }
                                executeCycleRequest(job)
                            }
                            is AuthenticationResponse.AuthenticationError -> {
                                job.throwError(SecurityException(response.errorMessage))
                                Log.d(TAG, response.errorMessage)
                            }
                        }
                    }, {
                        job.throwError(it)
                    })
        )
    }

    @Synchronized
    private fun setSyftWorkerId(workerId: String) {
        if (this.workerId == null)
            this.workerId = workerId
        else if (workerJob == null)
            this.workerId = workerId
    }

    private fun disposeSocketClient() {
        syftConfig.getWebRTCSignallingClient().dispose()
    }

}
