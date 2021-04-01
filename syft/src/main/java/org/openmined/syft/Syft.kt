package org.openmined.syft

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.execution.JobErrorThrowable
import org.openmined.syft.execution.JobStatusMessage
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
@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
class Syft internal constructor(
    private val syftConfig: SyftConfiguration,
    private val deviceMonitor: DeviceMonitor,
    private val authToken: String?
) {
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

        fun getCurrentInstance() = INSTANCE?.let {
            INSTANCE
        } ?: throw java.lang.IllegalStateException("Syft Worker was not initiliased. Use getInstance(syftConfiguration, authToken)")
    }

    //todo single job for now but eventually worker should support multiple jobs
    private var workerJob: SyftJob? = null
    private val isDisposed = AtomicBoolean(false)

    // https://github.com/Kotlin/kotlinx.coroutines/issues/1003
    private val scope = CoroutineScope(context = Dispatchers.Default)

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
        return job
    }

    internal fun getSyftWorkerId() = workerId

    internal suspend fun executeCycleRequest(job: SyftJob): JobStatusMessage {
        if (job.throwErrorIfBatteryInvalid() || job.throwErrorIfNetworkInvalid())
            return JobStatusMessage.ConditionsNotMet

        when (val response = executeAuthentication(job)) {
            is AuthenticationResponse.AuthenticationSuccess -> {
                if (workerId == null) {
                    setSyftWorkerId(response.workerId)
                }
                //todo eventually requires_speed test will be migrated to it's own endpoint
                job.jobModel.requiresSpeedTest.set(response.requiresSpeedTest)
            }
            is AuthenticationResponse.AuthenticationError -> {
                job.publishError(JobErrorThrowable.AuthenticationFailure(response.errorMessage))
                Log.d(TAG, response.errorMessage)
                return JobStatusMessage.Error(JobErrorThrowable.AuthenticationFailure(response.errorMessage))
            }
            is AuthenticationResponse.AlreadyAuthenticated -> {}
            is AuthenticationResponse.UnknownError -> {
                job.publishError(JobErrorThrowable.ExternalException(response.exception.message, response.exception.cause))
                return JobStatusMessage.Error(JobErrorThrowable.ExternalException(response.exception.message, response.exception.cause))
            }
        }

        return try {
            // TODO Fix this workerId!!
            val networkStatus = deviceMonitor.getNetworkStatus(workerId!!, job.jobModel.requiresSpeedTest.get())
            val cycleResponse = requestCycle(
                workerId!!,
                job,
                networkStatus.ping,
                networkStatus.downloadSpeed,
                networkStatus.uploadSpeed
            )
            when (cycleResponse) {
                is CycleResponseData.CycleAccept -> handleCycleAccept(cycleResponse)
                is CycleResponseData.CycleReject -> handleCycleReject(cycleResponse)
            }
        } catch (e: Exception) {
            job.publishError(JobErrorThrowable.ExternalException(e.message, e.cause))
            JobStatusMessage.Error(JobErrorThrowable.ExternalException(e.message, e.cause))
        }
    }

    /**
     * Explicitly dispose off the worker. All the jobs running in the worker will be disposed off as well.
     * Clears the current singleton worker instance so the immediately next [getInstance] call creates a new syft worker
     */
    fun dispose() {
        Log.d(TAG, "disposing syft worker")
        deviceMonitor.dispose()
        workerJob?.dispose()
        INSTANCE = null
    }

    internal fun isNetworkValid() = deviceMonitor.isNetworkStateValid()
    internal fun isBatteryValid() = deviceMonitor.isBatteryStateValid()

    private suspend fun requestCycle(
        id: String,
        job: SyftJob,
        ping: Int?,
        downloadSpeed: Float?,
        uploadSpeed: Float?
    ): CycleResponseData {

        return when (val check = checkConditions(ping, downloadSpeed, uploadSpeed)) {
            is Either.Left -> throw(JobErrorThrowable.NetworkUnreachable(check.a))
            is Either.Right -> {
                val cycleRequest = CycleRequest(
                    id,
                    job.jobModel.modelName,
                    job.jobModel.version,
                    ping ?: -1,
                    downloadSpeed ?: 0.0f,
                    uploadSpeed ?: 0.0f
                )
                syftConfig.getSignallingClient().getCycle(cycleRequest)
            }
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

    private fun handleCycleReject(responseData: CycleResponseData.CycleReject): JobStatusMessage {
        return workerJob?.cycleRejected(responseData) ?: JobStatusMessage.Error(JobErrorThrowable.UninitializedWorkerError)
    }

    private suspend fun handleCycleAccept(responseData: CycleResponseData.CycleAccept): JobStatusMessage {
        val job = workerJob ?: return JobStatusMessage.Error(JobErrorThrowable.IllegalJobState(IllegalStateException("job deleted and accessed")))

        job.cycleAccepted(responseData)

        if (job.throwErrorIfBatteryInvalid() || job.throwErrorIfNetworkInvalid())
            return JobStatusMessage.ConditionsNotMet

        return workerId?.let {
            try {
                job.downloadData(it, responseData)
            } catch (e: JobErrorThrowable) {
                job.publishError(e)
                JobStatusMessage.Error(e)
            }
        } ?: onNoWorkerReady(job)
    }

    private fun onNoWorkerReady(job: SyftJob): JobStatusMessage.Error {
        job.publishError(JobErrorThrowable.UninitializedWorkerError)
        return JobStatusMessage.Error(JobErrorThrowable.UninitializedWorkerError)
    }

    private suspend fun executeAuthentication(job: SyftJob): AuthenticationResponse {
        return if (workerId != null) {
            AuthenticationResponse.AlreadyAuthenticated
        } else {
            try {
                val authRequest = AuthenticationRequest(
                    authToken,
                    job.jobModel.modelName,
                    job.jobModel.version
                )
                syftConfig.getSignallingClient().authenticate(authRequest)
            } catch (e: Exception) {
                AuthenticationResponse.UnknownError(e)
            }
        }
    }

    @Synchronized
    private fun setSyftWorkerId(workerId: String) {
        if (this.workerId == null)
            this.workerId = workerId
        else if (workerJob == null)
            this.workerId = workerId
    }
}
