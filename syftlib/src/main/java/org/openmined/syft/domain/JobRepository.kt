package org.openmined.syft.domain

import android.util.Log
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.openmined.syft.datasource.JobLocalDataSource
import org.openmined.syft.datasource.JobRemoteDataSource
import org.openmined.syft.execution.JobStatusMessage
import org.openmined.syft.execution.Plan
import org.openmined.syft.execution.Protocol
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

internal const val PLAN_OP_TYPE = "torchscript"
private const val TAG = "JobDownloader"

@ExperimentalUnsignedTypes
internal class JobRepository(
    private val jobLocalDataSource: JobLocalDataSource,
    private val jobRemoteDataSource: JobRemoteDataSource
) {

    private val trainingParamsStatus = AtomicReference(DownloadStatus.NOT_STARTED)
    val status: DownloadStatus
        get() = trainingParamsStatus.get()

    fun getDiffScript(config: SyftConfiguration) =
            jobLocalDataSource.getDiffScript(config)

    fun persistToLocalStorage(input: InputStream, parentDir: String, fileName: String): String {
        return jobLocalDataSource.save(input, parentDir, fileName).blockingGet()
    }

    fun downloadData(
        workerId: String,
        config: SyftConfiguration,
        requestKey: String,
        networkDisposable: CompositeDisposable,
        jobStatusProcessor: PublishProcessor<JobStatusMessage>,
        clientConfig: ClientConfig?,
        plans: ConcurrentHashMap<String, Plan>,
        model: SyftModel,
        protocols: ConcurrentHashMap<String, Protocol>
    ) {
        Log.d(TAG, "beginning download")
        trainingParamsStatus.set(DownloadStatus.RUNNING)

        networkDisposable.add(
            Single.zip(
                getDownloadables(
                    workerId,
                    config,
                    requestKey,
                    model,
                    plans,
                    protocols
                )
            ) { successMessages ->
                successMessages.joinToString(
                    ",",
                    prefix = "files ",
                    postfix = " downloaded successfully"
                )
            }
                    .compose(config.networkingSchedulers.applySingleSchedulers())
                    .subscribe(
                        { successMsg: String ->
                            Log.d(TAG, successMsg)
                            trainingParamsStatus.set(DownloadStatus.COMPLETE)
                            jobStatusProcessor.offer(
                                JobStatusMessage.JobReady(
                                    model,
                                    plans,
                                    clientConfig
                                )
                            )
                        },
                        { e -> jobStatusProcessor.onError(e) }
                    )
        )
    }

    private fun getDownloadables(
        workerId: String,
        config: SyftConfiguration,
        request: String,
        model: SyftModel,
        plans: ConcurrentHashMap<String, Plan>,
        protocols: ConcurrentHashMap<String, Protocol>
    ): List<Single<String>> {
        val downloadList = mutableListOf<Single<String>>()
        plans.forEach { (_, plan) ->
            downloadList.add(
                processPlans(
                    workerId,
                    config,
                    request,
                    "${config.filesDir}/plans",
                    plan
                )
            )
        }
        protocols.forEach { (protocolId, protocol) ->
            protocol.protocolFileLocation = "${config.filesDir}/protocols"
            downloadList.add(
                processProtocols(
                    workerId,
                    config,
                    request,
                    protocol.protocolFileLocation,
                    protocolId
                )
            )
        }
        downloadList.add(processModel(workerId, config, request, model))
        return downloadList
    }

    private fun processModel(
        workerId: String,
        config: SyftConfiguration,
        requestKey: String,
        model: SyftModel
    ): Single<String> {
        val modelId = model.pyGridModelId ?: throw IllegalStateException("Model id not initiated")
        return jobRemoteDataSource.downloadModel(workerId, requestKey, modelId)
                .flatMap { modelInputStream ->
                    jobLocalDataSource.save(
                        modelInputStream,
                        "${config.filesDir}/models",
                        "$modelId.pb"
                    )
                }.flatMap { modelFile ->
                    Single.create<String> { emitter ->
                        model.loadModelState(modelFile)
                        emitter.onSuccess(modelFile)
                    }
                }
                .compose(config.networkingSchedulers.applySingleSchedulers())
    }

    private fun processPlans(
        workerId: String,
        config: SyftConfiguration,
        requestKey: String,
        destinationDir: String,
        plan: Plan
    ): Single<String> {
        return jobRemoteDataSource.downloadPlan(
            workerId,
            requestKey,
            plan.planId,
            PLAN_OP_TYPE
        )
                .flatMap { planInputStream ->
                    jobLocalDataSource.save(planInputStream, destinationDir, "${plan.planId}.pb")
                }.flatMap { filepath ->
                    Single.create<String> { emitter ->
                        val torchscriptLocation = jobLocalDataSource.saveTorchScript(
                            destinationDir,
                            filepath,
                            "torchscript_${plan.planId}.pt"
                        )
                        plan.loadScriptModule(torchscriptLocation)
                        emitter.onSuccess(filepath)
                    }
                }
                .compose(config.networkingSchedulers.applySingleSchedulers())

    }

    private fun processProtocols(
        workerId: String,
        config: SyftConfiguration,
        requestKey: String,
        destinationDir: String,
        protocolId: String
    ): Single<String> {
        return jobRemoteDataSource.downloadProtocol(workerId, requestKey, protocolId)
                .flatMap { protocolInputStream ->
                    jobLocalDataSource.save(
                        protocolInputStream,
                        destinationDir,
                        "$protocolId.pb"
                    )
                }
                .compose(config.networkingSchedulers.applySingleSchedulers())
    }
}

enum class DownloadStatus {
    NOT_STARTED, RUNNING, COMPLETE
}
