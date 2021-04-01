package org.openmined.syft.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.openmined.syft.datasource.JobLocalDataSource
import org.openmined.syft.datasource.JobRemoteDataSource
import org.openmined.syft.execution.JobModel
import org.openmined.syft.execution.Plan
import org.openmined.syft.execution.Protocol
import org.openmined.syft.proto.SyftModel
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

internal const val PLAN_OP_TYPE = "torchscript"
private const val TAG = "JobRepository"

@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
internal class JobRepository(
    private val jobModel: JobModel,
    private val jobLocalDataSource: JobLocalDataSource,
    private val jobRemoteDataSource: JobRemoteDataSource
) {

    companion object {

        fun create(
            config: SyftConfiguration,
            jobModel: JobModel
        ): JobRepository {
            return JobRepository(
                jobModel,
                JobLocalDataSource(config),
                JobRemoteDataSource(config.getDownloader())
            )
        }

    }

    private val trainingParamsStatus = AtomicReference(DownloadStatus.NOT_STARTED)
    val status: DownloadStatus
        get() = trainingParamsStatus.get()

    fun getModelsPath() = jobLocalDataSource.getModelsPath(jobModel.id)

    fun getPlansPath() = jobLocalDataSource.getPlansPath(jobModel.id)

    fun getProtocolsPath() = jobLocalDataSource.getProtocolsPath(jobModel.id)


    fun getDiffScript() = jobLocalDataSource.getDiffScript()

    internal fun persistToLocalStorage(
        input: InputStream,
        parentDir: String,
        fileName: String,
        overwrite: Boolean = false
    ): String {
        return jobLocalDataSource.save(input, parentDir, fileName, overwrite)
    }

    suspend fun retrievePlanData(
        workerId: String,
        requestKey: String,
        plans: ConcurrentHashMap<String, Plan>
    ): List<String> {
        return plans.values.map { plan ->
            processPlans(
                workerId,
                requestKey,
                getPlansPath(),
                plan
            )
        }
    }

    suspend fun retrieveProtocolData(
        workerId: String,
        requestKey: String,
        protocols: ConcurrentHashMap<String, Protocol>
    ): List<String?> {
        return protocols.values.map { protocol ->
            protocol.protocolFileLocation = getProtocolsPath()
            processProtocols(
                workerId,
                requestKey,
                protocol.protocolFileLocation,
                protocol.protocolId
            )
        }
    }

    internal suspend fun retrieveModel(
        workerId: String,
        requestKey: String,
        model: SyftModel
    ): String? {
        val modelId = model.pyGridModelId ?: throw IllegalStateException("Model id not initiated")
        return jobRemoteDataSource.downloadModel(workerId, requestKey, modelId)
                ?.let { modelInputStream ->
                    val modelFile = jobLocalDataSource.saveAsync(
                        modelInputStream,
                        getModelsPath(),
                        "$modelId.pb"
                    )
                    model.loadModelState(modelFile)
                    modelFile
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
