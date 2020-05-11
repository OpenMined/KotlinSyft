package org.openmined.syft

import android.accounts.NetworkErrorException
import android.util.Log
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.openmined.syft.device.repositories.NetworkStateRepository
import org.openmined.syft.execution.JobStatusMessage
import org.openmined.syft.execution.JobStatusSubscriber
import org.openmined.syft.execution.SyftJob
import org.openmined.syft.networking.clients.HttpClient
import org.openmined.syft.networking.clients.SocketClient
import org.openmined.syft.networking.datamodels.syft.AuthenticationRequest
import org.openmined.syft.networking.datamodels.syft.AuthenticationResponse
import org.openmined.syft.networking.datamodels.syft.CycleRequest
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.requests.CommunicationAPI
import org.openmined.syft.networking.requests.HttpAPI
import org.openmined.syft.networking.requests.SocketAPI
import org.openmined.syft.threading.ProcessSchedulers
import java.util.concurrent.ConcurrentHashMap


private const val TAG = "Syft"

@ExperimentalUnsignedTypes
class Syft internal constructor(
    private val authToken: String,
    private var socketClient: SocketClient,
    private var httpClient: HttpClient,
    //todo this will be removed by syft configuration class
    private val networkingSchedulers: ProcessSchedulers,
    //todo change this to read from syft configuration
    private val computeSchedulers: ProcessSchedulers

) {
    companion object {
        @Volatile
        private var INSTANCE: Syft? = null

        fun getInstance(
            baseUrl: String,
            authToken: String,
            networkingSchedulers: ProcessSchedulers,
            //todo this will be removed by syft configuration class
            computeSchedulers: ProcessSchedulers
        ): Syft =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: Syft(
                        authToken,
                        SocketClient(baseUrl, 2000u, networkingSchedulers),
                        HttpClient(baseUrl),
                        networkingSchedulers,
                        computeSchedulers
                    ).also { INSTANCE = it }
                }
    }

    private val workerJobs = ConcurrentHashMap<SyftJob.JobID, SyftJob>()
    private val jobStatusProcessors =
            ConcurrentHashMap<SyftJob.JobID, PublishProcessor<JobStatusMessage>>()
    private val compositeDisposable = CompositeDisposable()
    
    //todo battery state and sleep/wake state will also come.
    // Eventually Configuration class must handle these states
    // Config class will give the final decision to worker whether to execute the job or not
    private val networkStateRepository = NetworkStateRepository(getDownloader())

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
            computeSchedulers,
            networkingSchedulers
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
        }, networkingSchedulers)

        return job
    }


    fun getDownloader(): HttpAPI = httpClient.apiClient

    //todo decide this based on configuration
    fun getSignallingClient(): CommunicationAPI = socketClient
    fun getWebRTCSignallingClient(): SocketAPI = socketClient

    fun setHttpClient(httpClient: HttpClient) {
        this.httpClient = httpClient
    }

    fun setSocketClient(socketClient: SocketClient) {
        this.socketClient = socketClient
    }

    fun getSyftWorkerId() = workerId

    fun executeCycleRequest(job: SyftJob) {
        workerId?.let { id ->
            compositeDisposable.add(
                networkStateRepository.getNetworkState(id).flatMap { networkState ->
                    val ping = networkState.ping
                    val downloadSpeed = networkState.downloadSpeed
                    val uploadSpeed = networkState.uploadspeed
                    requestCycle(id, job, ping, downloadSpeed, uploadSpeed)
                }
                        .compose(networkingSchedulers.applySingleSchedulers())
                        .subscribe(
                            { response: CycleResponseData ->
                                when (response) {
                                    is CycleResponseData.CycleAccept -> handleCycleAccept(response)
                                    is CycleResponseData.CycleReject -> handleCycleReject(response)
                                }
                            },
                            { errorMsg: Throwable ->
                                jobStatusProcessors[job.jobId]?.offer(
                                    JobStatusMessage.JobError(errorMsg)
                                )
                            })
            )
        } ?: executeAuthentication(job)
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
            else -> getSignallingClient().getCycle(
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
        job.downloadData()
    }

    private fun executeAuthentication(job: SyftJob) {
        compositeDisposable.add(
            socketClient.authenticate(AuthenticationRequest(authToken))
                    .compose(networkingSchedulers.applySingleSchedulers())
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
                        jobStatusProcessors[job.jobId]?.offer(JobStatusMessage.JobError(it))
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
