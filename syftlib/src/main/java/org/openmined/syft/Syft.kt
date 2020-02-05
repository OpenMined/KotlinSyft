package org.openmined.syft

import io.reactivex.disposables.CompositeDisposable
import org.openmined.syft.network.CommunicationDataFactory
import org.openmined.syft.network.REQUEST
import org.openmined.syft.network.SignallingClient
import org.openmined.syft.threading.ProcessSchedulers

@ExperimentalUnsignedTypes
class Syft private constructor(
    private val signallingClient: SignallingClient,
    private val schedulers: ProcessSchedulers
) {
    private lateinit var syftWorker: Syft

    private val workerJobs = mutableListOf<Job>()
    private val compositeDisposable = CompositeDisposable().add(
        signallingClient.start()
                .subscribeOn(schedulers.computeThreadScheduler)
                .observeOn(schedulers.calleeThreadScheduler)
                .subscribe()
    )

    //TODO decide if we go by dependency injection who will initiate module
    fun getSyftWorker(signallingClient: SignallingClient, schedulers: ProcessSchedulers): Syft {
        if (!::syftWorker.isInitialized)
            syftWorker = Syft(signallingClient, schedulers)

        return syftWorker
    }

    fun newJob(modelName: String, version: String): Job {
        val job = Job(modelName, version)
        signallingClient.send(REQUEST.JOB, CommunicationDataFactory.jobRequest(job))
        workerJobs.add(job)
        return job
    }
}
