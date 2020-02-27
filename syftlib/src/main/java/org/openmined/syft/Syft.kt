package org.openmined.syft

import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import org.openmined.syft.networking.clients.HttpClient
import org.openmined.syft.networking.clients.SocketClient
import org.openmined.syft.networking.datamodels.syft.AuthenticationSuccess
import org.openmined.syft.networking.datamodels.syft.CycleRequest
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.networking.requests.CommunicationAPI
import org.openmined.syft.networking.requests.HttpAPI
import org.openmined.syft.processes.SyftJob
import org.openmined.syft.threading.ProcessSchedulers
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

private const val TAG = "Syft"

@ExperimentalUnsignedTypes
class Syft private constructor(
    private val socketClient: SocketClient,
    private val httpClient: HttpClient,
    private val schedulers: ProcessSchedulers
) {
    companion object {
        @Volatile
        private var INSTANCE: Syft? = null

        fun getInstance(
            socketClient: SocketClient,
            httpClient: HttpClient,
            schedulers: ProcessSchedulers
        ): Syft =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: Syft(
                        socketClient,
                        httpClient,
                        schedulers
                    ).also { INSTANCE = it }
                }
    }

    private val workerJobs = ConcurrentHashMap<SyftJob.JobID, SyftJob>()
    private val compositeDisposable = CompositeDisposable()
    //todo decide if this can be changed by pygrid or will remain same irrespective of the requests we make
    @Volatile
    lateinit var workerId: String

    fun newJob(model: String, version: String? = null): SyftJob {
        val job = SyftJob(this, schedulers, model, version)
        val jobId = SyftJob.JobID(model, version)
        workerJobs[jobId] = job
        compositeDisposable.add(job.getStatusProcessor()
                .compose(schedulers.applyFlowableSchedulers())
                .subscribe(
                    {},
                    { workerJobs.remove(jobId) },
                    { workerJobs.remove(jobId) }
                )
        )

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
            ).compose(schedulers.applySingleSchedulers())
                    .subscribe { response: CycleResponseData ->
                        when (response) {
                            is CycleResponseData.CycleAccept -> handleCycleAccept(response)
                            is CycleResponseData.CycleReject -> handleCycleReject(response)
                        }
                    }
        else {
            compositeDisposable.add(socketClient.authenticate()
                    .compose(schedulers.applySingleSchedulers())
                    .subscribe { t: AuthenticationSuccess ->
                        if (!this::workerId.isInitialized)
                            setSyftWorkerId(t.workerId)
                        requestCycle(job)
                    }
            )
        }
    }

    fun getDownloader(): HttpAPI = httpClient.apiClient
    //todo decide this based on configuration
    fun getSignallingClient(): CommunicationAPI = socketClient

    fun getWebRTCSignallingClient(): SocketClient = socketClient

    @Synchronized
    private fun setSyftWorkerId(workerId: String) {
        if (!this::workerId.isInitialized)
            this.workerId = workerId
    }

    private fun getPing() = ""
    private fun getDownloadSpeed() = ""
    private fun getUploadSpeed() = ""

    private fun handleCycleReject(responseData: CycleResponseData.CycleReject) {
        var jobId = SyftJob.JobID(responseData.modelName, responseData.version)
        val job = workerJobs.getOrElse(jobId, {
            jobId = SyftJob.JobID(responseData.modelName)
            workerJobs.getValue(jobId)
        })
        job.cycleStatus.set(SyftJob.CycleStatus.REJECT)
        compositeDisposable.add(
            Completable
                    .timer(responseData.timeout.toLong(), TimeUnit.MILLISECONDS)
                    .compose(schedulers.applyCompletableSchedulers())
                    .subscribe {
                        job.cycleStatus.set(SyftJob.CycleStatus.APPLY)
                        job.start()
                    }
        )
    }

    private fun handleCycleAccept(responseData: CycleResponseData.CycleAccept) {
        var jobId = SyftJob.JobID(responseData.modelName, responseData.version)
        val job = workerJobs.getOrElse(jobId, {
            jobId = SyftJob.JobID(responseData.modelName)
            workerJobs.getValue(jobId)
        })
        job.setRequestKey(responseData)
        job.downloadData()
    }


}
