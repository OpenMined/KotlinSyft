package org.openmined.syft

import android.util.Log
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.execution.JobErrorThrowable
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

/**
 * This is the main syft worker handling creation and deletion of jobs. This class is also responsible for monitoring device resources via DeviceMonitor
 */
@ExperimentalUnsignedTypes
class Syft internal constructor(
    private val syftConfig: SyftConfiguration,
    private val deviceMonitor: DeviceMonitor,
    private val authToken: String?
) : Disposable {
    companion object {
        @Volatile
        private var INSTANCE: Syft? = null

        /**
         * Only a single worker must be instantiated across an app lifecycle.
         * The [getInstance] ensures creation of the singleton object if needed or returns the already created worker.
         * This method is thread safe so getInstance calls across threads do not suffer
         * @param syftConfiguration The SyftConfiguration object specifying the mutable properties of syft worker
         * @param authToken (Optional) The JWT token to be passed by the user
         * @return Syft instance
         */
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
                    authToken = authToken
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

    /**
     * Create a new job for the worker.
     * @param model specifies the model name by which the parameters are hosted on the PyGrid server
     * @param version the version of the model on PyGrid
     * @return [SyftJob]
     */
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
        if (workerJob != null)
            throw IndexOutOfBoundsException("maximum number of allowed jobs reached")

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
        if (job.throwErrorIfBatteryInvalid() || job.throwErrorIfNetworkInvalid())
            return

        workerId?.let { id ->
            compositeDisposable.add(
                deviceMonitor.getNetworkStatus(id, job.requiresSpeedTest.get())
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
                                job.publishError(
                                    JobErrorThrowable.ExternalException(
                                        errorMsg.message,
                                        errorMsg.cause
                                    )
                                )
                            })
            )
        } ?: executeAuthentication(job)
    }

    /**
     * Check if the syft worker has been disposed
     * @return True/False
     */
    override fun isDisposed() = isDisposed.get()

    /**
     * Explicitly dispose off the worker. All the jobs running in the worker will be disposed off as well.
     * Clears the current singleton worker instance so the immediately next [getInstance] call creates a new syft worker
     */
    override fun dispose() {
        Log.d(TAG, "disposing syft worker")
        deviceMonitor.dispose()
        compositeDisposable.clear()
        workerJob?.dispose()
        INSTANCE = null
    }

    internal fun isNetworkValid() = deviceMonitor.isNetworkStateValid()
    internal fun isBatteryValid() = deviceMonitor.isBatteryStateValid()

    private fun requestCycle(
        id: String,
        job: SyftJob,
        ping: Int?,
        downloadSpeed: Float?,
        uploadSpeed: Float?
    ): Single<CycleResponseData> {

        return when (val check = checkConditions(ping, downloadSpeed, uploadSpeed)) {
            is Either.Left -> Single.error(JobErrorThrowable.NetworkUnreachable(check.a))
            is Either.Right -> syftConfig.getSignallingClient().getCycle(
                CycleRequest(
                    id,
                    job.jobId.modelName,
                    job.jobId.version,
                    ping ?: -1,
                    downloadSpeed ?: 0.0f,
                    uploadSpeed ?: 0.0f
                )
            )
        }
    }

    private fun checkConditions(
        ping: Int?,
        downloadSpeed: Float?,
        uploadSpeed: Float?
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
        if (job.throwErrorIfBatteryInvalid() ||
            job.throwErrorIfNetworkInvalid()
        )
            return

        workerId?.let {
            job.downloadData(it, responseData)
        } ?: job.publishError(JobErrorThrowable.UninitializedWorkerError)

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
                                }
                                //todo eventually requires_speed test will be migrated to it's own endpoint
                                job.requiresSpeedTest.set(response.requiresSpeedTest)
                                executeCycleRequest(job)
                            }
                            is AuthenticationResponse.AuthenticationError -> {
                                job.publishError(JobErrorThrowable.AuthenticationFailure(response.errorMessage))
                                Log.d(TAG, response.errorMessage)
                            }
                        }
                    }, {
                        job.publishError(JobErrorThrowable.ExternalException(it.message, it.cause))
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
