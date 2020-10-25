package org.openmined.syft.domain

import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

    fun persistToLocalStorage(
        input: InputStream,
        parentDir: String,
        fileName: String,
        overwrite: Boolean = false
    ): String {
        return jobLocalDataSource.save(input, parentDir, fileName, overwrite)
    }

    suspend fun downloadData(
        workerId: String,
        config: SyftConfiguration,
        requestKey: String,
        jobStatusProcessor: PublishProcessor<JobStatusMessage>,
        clientConfig: ClientConfig?,
        plans: ConcurrentHashMap<String, Plan>,
        model: SyftModel,
        protocols: ConcurrentHashMap<String, Protocol>
    ) {
        Log.d(TAG, "beginning download")
        trainingParamsStatus.set(DownloadStatus.RUNNING)

        // We need to launch all the downloadables at the same time

        coroutineScope {
            val planFunctions = plans.values.map { plan ->
                async {
                    processPlans(
                        workerId,
                        requestKey,
                        "${config.filesDir}/plans",
                        plan
                    )
                }
            }
            val protocolFunctions = protocols.values.map { protocol ->
                async {
                    protocol.protocolFileLocation = "${config.filesDir}/protocols"
                    processProtocols(
                        workerId,
                        requestKey,
                        protocol.protocolFileLocation,
                        protocol.protocolId
                    )
                }
            }
            val modelFunction = async {
                processModel(
                    workerId,
                    config,
                    requestKey,
                    model
                )
            }
            planFunctions.awaitAll() + protocolFunctions.awaitAll() + modelFunction.await()
        }
    }
//            )
//        { successMessages ->
//                successMessages.joinToString(
//                    ",",
//                    prefix = "files ",
//                    postfix = " downloaded successfully"
//                )
//            }
//                    .compose(config.networkingSchedulers.applySingleSchedulers())
//                    .subscribe(
//                        { successMsg: String ->
//                            Log.d(TAG, successMsg)
//                            trainingParamsStatus.set(DownloadStatus.COMPLETE)
//                            jobStatusProcessor.offer(
//                                JobStatusMessage.JobReady(
//                                    model,
//                                    plans,
//                                    clientConfig
//                                )
//                            )
//                        },
//                        { e -> jobStatusProcessor.onError(e) }
//                    )
//        )

    private suspend fun processModel(
        workerId: String,
        config: SyftConfiguration,
        requestKey: String,
        model: SyftModel
    ): String? {
        val modelId = model.pyGridModelId ?: throw IllegalStateException("Model id not initiated")
        return jobRemoteDataSource.downloadModel(workerId, requestKey, modelId)
                ?.let { modelInputStream ->
                    val modelFile = jobLocalDataSource.saveAsync(
                        modelInputStream,
                        "${config.filesDir}/models",
                        "$modelId.pb"
                    )
                    model.loadModelState(modelFile)
                    modelFile
//                emitter.onSuccess(modelFile)
                }
    }

    private suspend fun processPlans(
        workerId: String,
        requestKey: String,
        destinationDir: String,
        plan: Plan
    ): String {
        val planInputStream = jobRemoteDataSource.downloadPlan(
            workerId,
            requestKey,
            plan.planId,
            PLAN_OP_TYPE
        )
        return if (planInputStream != null) {
            val filepath = jobLocalDataSource.saveAsync(
                planInputStream,
                destinationDir,
                "${plan.planId}.pb"
            )

            val torchscriptLocation = jobLocalDataSource.saveTorchScript(
                destinationDir,
                filepath,
                "torchscript_${plan.planId}.pt"
            )
            plan.loadScriptModule(torchscriptLocation)
            filepath
        } else {
            ""
        }
    }

    private suspend fun processProtocols(
        workerId: String,
        requestKey: String,
        destinationDir: String,
        protocolId: String
    ): String? {
        return jobRemoteDataSource.downloadProtocol(workerId, requestKey, protocolId)
                ?.let { protocolInputStream ->
                    jobLocalDataSource.saveAsync(
                        protocolInputStream,
                        destinationDir,
                        "$protocolId.pb"
                    )
                }
    }
}

enum class DownloadStatus {
    NOT_STARTED, RUNNING, COMPLETE
}
