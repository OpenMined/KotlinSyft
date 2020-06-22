package org.openmined.syft.execution

import android.util.Log
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.openmined.syft.datasource.LocalDataSource
import org.openmined.syft.datasource.RemoteDataSource
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

internal const val PLAN_OP_TYPE = "torchscript"
private const val TAG = "JobDownloader"

@ExperimentalUnsignedTypes
internal class JobDownloader(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {

    private val trainingParamsStatus = AtomicReference(DownloadStatus.NOT_STARTED)
    val status: DownloadStatus
        get() = trainingParamsStatus.get()

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
        if (trainingParamsStatus.get() != DownloadStatus.NOT_STARTED) {
            Log.d(TAG, "download already running")
            return
        }
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

        model.pyGridModelId?.let {
            downloadList.add(processModel(workerId, config, request, it, model))
        } ?: throw IllegalStateException("model id has not been set")

        return downloadList
    }

    //We might want to make these public if needed later
    private fun processModel(
        workerId: String,
        config: SyftConfiguration,
        requestKey: String,
        modelId: String,
        model: SyftModel
    ): Single<String> {
        return remoteDataSource.downloadModel(workerId, requestKey, modelId)
                .flatMap { modelInputStream ->
                    localDataSource.save(
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
        return remoteDataSource.downloadPlan(
            workerId,
            requestKey,
            plan.planId,
            PLAN_OP_TYPE
        )
                .flatMap { planInputStream ->
                    localDataSource.save(planInputStream, destinationDir, "${plan.planId}.pb")
                }.flatMap { filepath ->
                    Single.create<String> { emitter ->
                        plan.generateScriptModule(destinationDir, filepath)
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
        return remoteDataSource.downloadProtocol(workerId, requestKey, protocolId)
                .flatMap { protocolInputStream ->
                    localDataSource.save(
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
