package org.openmined.syft

import Either
import android.accounts.NetworkErrorException
import android.util.Log
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.execution.JobStatusSubscriber
import org.openmined.syft.execution.SyftJob
import org.openmined.syft.monitor.DeviceMonitor
import org.openmined.syft.networking.datamodels.syft.AuthenticationRequest
import org.openmined.syft.networking.datamodels.syft.AuthenticationResponse
import org.openmined.syft.networking.datamodels.syft.CycleRequest
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "Syft"

@ExperimentalUnsignedTypes
class Syft internal constructor(
    private val syftConfig: SyftConfiguration,
    private val deviceMonitor: DeviceMonitor,
    private val authToken: String?
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
                    syftConfiguration,
                    DeviceMonitor.construct(syftConfiguration),
                    authToken
                ).also { INSTANCE = it }
            }
        }
    }

    private val workerJobs = ConcurrentHashMap<SyftJob.JobID, SyftJob>()
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
        if (syftConfig.maxConcurrentJobs == workerJobs.size)
            throw IndexOutOfBoundsException("maximum number of allowed jobs reached")

        workerJobs[job.jobId] = job
        job.subscribe(object : JobStatusSubscriber() {
            override fun onComplete() {
                workerJobs.remove(job.jobId)
            }

            override fun onError(throwable: Throwable) {
                Log.e(TAG, throwable.message.toString())
                workerJobs.remove(job.jobId)
            }
        }, syftConfig.networkingSchedulers)

        return job
    }

    internal fun getSyftWorkerId() = workerId

    internal fun executeCycleRequest(job: SyftJob) {
        if (jobErrorIfBatteryInvalid(job) || jobErrorIfNetworkInvalid(job))
            return

        val isRequiresSpeedTestEnabled = BuildConfig.REQUIRE_SPEED_TESTS_ENABLED and requiresSpeedTest
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
        workerJobs.forEach { (_, job) -> job.dispose() }
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
                    "10",
                    "10",
                    "10"
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
            syftConfig.getSignallingClient().authenticate(AuthenticationRequest(authToken))
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
        else if (workerJobs.isEmpty())
            this.workerId = workerId
    }

    private fun disposeSocketClient() {
        syftConfig.getWebRTCSignallingClient().dispose()
    }

}
