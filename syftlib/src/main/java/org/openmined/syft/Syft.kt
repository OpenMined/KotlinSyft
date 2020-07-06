package org.openmined.syft

import org.openmined.syft.fp.Either
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

/**
 * The Syft Class is the main worker handling all the federated classes. 
 * You are only required a single instance of the syft worker running in Android.
 * @property syftConfig is  for the adjustable properties of the syft worker.
 * @property deviceMonitor is the job hosting this plan.
 * @property authToken is to request to an authentication token.
 * @property isSpeedTestEnable to check whether Speed tests have been enabled.
 */

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
        
        /**
        * Function to get the instance of the Syft Worker.
        *
        * Returns the instance of Syft worker provided no auth errors occur.
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
                    authToken = authToken,
                    isSpeedTestEnable = true
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

    /**
    * Function to create a new Syft Worker Job.
    *
    * Returns the job.
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

    // Function to retrieve the Syft worker ID
    internal fun getSyftWorkerId() = workerId

    /**
    * Function to execute each cycle per Syft Worker Job.
    *
    * Returns job execution.
    */
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
    
    // Function to get the state of the Syft Worker.
    override fun isDisposed() = isDisposed.get()

   // Function to dispose of a Syft Worker.
    override fun dispose() {
        Log.d(TAG, "disposing syft worker")
        deviceMonitor.dispose()
        compositeDisposable.clear()
        workerJobs.forEach { (_, job) -> job.dispose() }
        INSTANCE = null
    }
    
    /**
    * Function to check for Network Errors while performing Syft Worker Job.
    *
    * Returns the error.
    */
    internal fun jobErrorIfNetworkInvalid(job: SyftJob): Boolean {
        if (!deviceMonitor.isNetworkStateValid()) {
            job.throwError(IllegalStateException("network constraints failed"))
            disposeSocketClient()
            return true
        }
        return false
    }

    /**
    * Function to check if Battery is Valid or not.
    *
    * Returns the error if job battery not valid.
    */
    internal fun jobErrorIfBatteryInvalid(job: SyftJob): Boolean {
        if (!deviceMonitor.isBatteryStateValid()) {
            job.throwError(IllegalStateException("Battery constraints failed"))
            return true
        }
        return false
    }

    /**
    * Function to request Syft Worker Job Cycle.
    *
    * Returns the Cycle request.
    */
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

    
    /**
     * Function to check Network availability.
     *
     * Returns errors when properties are null.
     */
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

    // Function to handle rejected Syft Worker Job cycle.
    private fun handleCycleReject(responseData: CycleResponseData.CycleReject) {
        val job = workerJobs.getValue(
            SyftJob.JobID(
                responseData.modelName,
                responseData.version
            )
        )
        job.cycleRejected(responseData)
    }

    /**
    * Function to handle accepted Syft Worker Job.
    *
    * Returns the valid Syft Worker ID.
    */
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

    // Function to execute Authentication of the Syft Worker Job
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

    // Function to set the Syft Worker ID 
    @Synchronized
    private fun setSyftWorkerId(workerId: String) {
        if (this.workerId == null)
            this.workerId = workerId
        else if (workerJobs.isEmpty())
            this.workerId = workerId
    }

    // Function to dispoe of the open RTC client
    private fun disposeSocketClient() {
        syftConfig.getWebRTCSignallingClient().dispose()
    }

}
