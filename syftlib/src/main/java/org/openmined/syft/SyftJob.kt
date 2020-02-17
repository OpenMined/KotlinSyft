package org.openmined.syft

import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import org.openmined.syft.networking.datamodels.CycleResponseData
import org.openmined.syft.networking.requests.CommunicationDataFactory
import org.openmined.syft.networking.requests.REQUESTS
import org.openmined.syft.training.Plan
import org.openmined.syft.training.Protocol
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@ExperimentalUnsignedTypes
class SyftJob(private val worker: Syft, val modelName: String, val version: String? = null) {

    var cycleStatus = CycleStatus.APPLY
    var trainingParamsStatus = AtomicReference<DownloadStatus>()
    val compositeDisposable = CompositeDisposable()

    private lateinit var requestKey: String
    //todo adjust into plans class
    private val plans = ConcurrentHashMap<String, Plan>()
    private val protocols = ConcurrentHashMap<String, Protocol>()

    /**
     * create a worker job
     */
    fun start() {
        //todo all this in syft.kt
        //todo check for connection if doesn't exist establish one
        //todo before calling this function syft should have checked the bandwidth etc requirements
        trainingParamsStatus.set(DownloadStatus.NOT_STARTED)
        worker.signallingClient.send(
            REQUESTS.CYCLE_REQUEST,
            CommunicationDataFactory.requestCycle(worker.workerId, this, "", "", "")
        )
    }

    fun cycle(responseData: CycleResponseData) {
        when (responseData) {
            is CycleResponseData.CycleAccept -> {
                setProperties(responseData)
                compositeDisposable.add(
                    getDownloader(responseData)
                            .flatMap { getTrainer(Plan(), listOf(), listOf()) }
                            .flatMap { getAggregationProtocol(Protocol()) }
                            .observeOn(worker.schedulers.computeThreadScheduler)
                            .subscribeOn(worker.schedulers.calleeThreadScheduler)
                            .subscribe{it:String->report(it)}
                )
            }
            is CycleResponseData.CycleReject ->
                compositeDisposable.add(Completable
                        .timer(responseData.timeout.toLong(), TimeUnit.MILLISECONDS)
                        .subscribeOn(worker.schedulers.computeThreadScheduler)
                        .observeOn(worker.schedulers.calleeThreadScheduler)
                        .subscribe { this@SyftJob.start() }
                )

        }
    }

    @Synchronized
    private fun setProperties(responseData: CycleResponseData.CycleAccept) {
        requestKey = responseData.requestKey
        cycleStatus = CycleStatus.ACCEPTED

    }

    private fun getDownloader(cycleAcceptResponse: CycleResponseData.CycleAccept): Flowable<Int> {

    }

    private fun getTrainer(plan: Plan, input: List<String>, target: List<String>) =
            Flowable.create<Float>({ progressEmitter ->
                (input zip target).forEachIndexed { batchIndex, (input_batch, target_batch) ->
                    plan.execute(input_batch, target_batch)
                    progressEmitter.onNext(batchIndex / input.size.toFloat())
                }
                progressEmitter.onComplete()
            }, BackpressureStrategy.LATEST)

    private fun getAggregationProtocol(protocol: Protocol) =
            Single.create<Boolean> { protocol.execute() }


    /**
     * report the results back to PyGrid
     */
    private fun report(diff: String) {
        worker.signallingClient.send(
            REQUESTS.REPORT,
            CommunicationDataFactory.report(worker.workerId, requestKey, diff)
        )
    }

    enum class DownloadStatus {
        NOT_STARTED, INCOMPLETE, COMPLETE
    }

    enum class CycleStatus {
        APPLY, REJECTED, ACCEPTED
    }

}