package org.openmined.syft

import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import org.openmined.syft.networking.clients.HttpClient
import org.openmined.syft.networking.clients.SocketClient
import org.openmined.syft.networking.datamodels.syft.AuthenticationResponse
import org.openmined.syft.networking.datamodels.syft.CycleRequest
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.requests.CommunicationAPI
import org.openmined.syft.networking.requests.HttpAPI
import org.openmined.syft.execution.JobStatusSubscriber
import org.openmined.syft.execution.SyftJob
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
    private val computeSchedulers: ProcessSchedulers,
    //todo change this to read from syft configuration
    private val networkingSchedulers: ProcessSchedulers

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
    private val compositeDisposable = CompositeDisposable()

    //todo decide if this can be changed by pygrid or will remain same irrespective of the requests we make
    @Volatile
    lateinit var workerId: String

    fun newJob(
        model: String,
        version: String? = null
    ): SyftJob {
        val job = SyftJob(this, computeSchedulers, networkingSchedulers, model, version)
        val jobId = SyftJob.JobID(model, version)
        workerJobs[jobId] = job
        job.subscribe(object : JobStatusSubscriber() {
            override fun onComplete() {
                workerJobs.remove(jobId)
            }

            override fun onError(throwable: Throwable) {
                workerJobs.remove(jobId)
            }
        }, networkingSchedulers)

        return job
    }

    fun requestCycle(job: SyftJob) {
        if (this::workerId.isInitialized)
            socketClient.getCycle(
                CycleRequest(
                    workerId,
                    job.modelName,
                    job.version,
                    getPing(),
                    getDownloadSpeed(),
                    getUploadSpeed()
                )
            ).compose(networkingSchedulers.applySingleSchedulers())
                    .subscribe { response: CycleResponseData ->
                        when (response) {
                            is CycleResponseData.CycleAccept -> handleCycleAccept(response)
                            is CycleResponseData.CycleReject -> handleCycleReject(response)
                        }
                    }
        else {
            compositeDisposable.add(socketClient.authenticate()
                    .compose(networkingSchedulers.applySingleSchedulers())
                    .subscribe { t: AuthenticationResponse ->
                        when (t) {
                            is AuthenticationResponse.AuthenticationSuccess -> {
                                if (!this::workerId.isInitialized)
                                    setSyftWorkerId(t.workerId)
                                requestCycle(job)
                            }
                            is AuthenticationResponse.AuthenticationError ->
                                Log.d(TAG, t.errorMessage)
                        }

                    }
            )
        }
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

    @Synchronized
    private fun setSyftWorkerId(workerId: String) {
        if (!this::workerId.isInitialized)
            this.workerId = workerId
        else if (workerJobs.isEmpty())
            this.workerId = workerId
    }

    private fun getPing() = "1"
    private fun getDownloadSpeed() = "1000"
    private fun getUploadSpeed() = "1000"

    private fun handleCycleReject(responseData: CycleResponseData.CycleReject) {
        var jobId = SyftJob.JobID(responseData.modelName)
        val job = workerJobs.getOrElse(jobId, {
            jobId = SyftJob.JobID(responseData.modelName)
            workerJobs.getValue(jobId)
        })
        job.cycleRejected(responseData)
    }

    private fun handleCycleAccept(responseData: CycleResponseData.CycleAccept) {
        val jobId = SyftJob.JobID(responseData.modelName)
        val job = workerJobs.getOrElse(jobId, {
            //todo change this when pygrid updates
            workerJobs.getValue(
                SyftJob.JobID(
                    responseData.modelName,
                    responseData.clientConfig.modelVersion
                )
            )
        })
        job.setJobArguments(responseData)
        job.downloadData()
    }


}
