package org.openmined.syft.execution

import android.util.Log
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.openmined.syft.Syft
import org.openmined.syft.data.DataLoader
import org.openmined.syft.data.Dataset
import org.openmined.syft.datasource.DIFF_SCRIPT_NAME
import org.openmined.syft.datasource.JobLocalDataSource
import org.openmined.syft.datasource.JobRemoteDataSource
import org.openmined.syft.domain.DownloadStatus
import org.openmined.syft.domain.InputParamType
import org.openmined.syft.domain.JobRepository
import org.openmined.syft.domain.OutputParamType
import org.openmined.syft.domain.SyftConfiguration
import org.openmined.syft.domain.TrainingParameters
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.networking.datamodels.syft.CycleResponseData
import org.openmined.syft.proto.SyftModel
import org.openmined.syft.proto.SyftState
import org.pytorch.IValue
import org.pytorch.Tensor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

private const val TAG = "SyftJob"

/**
 * @param modelName : The model being trained or used in inference
 * @param version : The version of the model with name modelName
 * @property worker : The syft worker handling this job
 * @property config : The configuration class for schedulers and clients
 */
@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
class SyftJob internal constructor(
    val modelName: String,
    val version: String? = null,
    private val worker: Syft,
    private val config: SyftConfiguration
) {

    companion object {

        /**
         * Creates a new Syft Job
         *
         * @param modelName : The model being trained or used in inference
         * @param version : The version of the model with name modelName
         * @param worker : The syft worker handling this job
         * @param config : The configuration class for schedulers and clients
         * @sample org.openmined.syft.Syft.newJob
         */
        fun create(
            modelName: String,
            version: String? = null,
            worker: Syft,
            config: SyftConfiguration
        ): SyftJob {
            return SyftJob(
                modelName,
                version,
                worker,
                config
            )
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val jobRepository = JobRepository.create(config, modelName, version)

    internal var cycleStatus = AtomicReference(CycleStatus.APPLY)

    internal val requiresSpeedTest = AtomicBoolean(true)

    private val isDisposed = AtomicBoolean(false)

    private val plans = ConcurrentHashMap<String, Plan>()

    private val protocols = ConcurrentHashMap<String, Protocol>()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val model = SyftModel(modelName, version)

    private var requestKey = ""

    private val jobScope = CoroutineScope(Dispatchers.IO)

    /**
     * Starts the job by asking syft worker to request for cycle.
     * Initialises Socket connection if not initialised already.
     * @return jobStatusMessage Status of this job after requesting a cycle.
     * @see org.openmined.syft.execution.JobStatusMessage
     *
     *
     * ```kotlin
     * val jobStatusMessage = job.request()
     *
     * job.train(...)
     * ```
     */
    suspend fun request(): JobStatusMessage {
        return when {
            cycleStatus.get() == CycleStatus.REJECT -> {
                Log.d(TAG, "job awaiting timer completion to resend the Cycle Request")
                JobStatusMessage.JobCycleAwaiting
            }
            isDisposed.get() -> {
                Log.e(TAG, "cannot start a disposed job")
                JobStatusMessage.Error(JobErrorThrowable.RunningDisposedJob)
            }
            else -> {
                worker.executeCycleRequest(this)
            }
        }
    }

    @ExperimentalStdlibApi
    fun train(
        plans: ConcurrentHashMap<String, Plan>,
        clientConfig: ClientConfig,
        dataLoader: DataLoader,
        trainingParameters: TrainingParameters
    ): Flow<TrainingState> = flow {

        plans["training_plan"]?.let { plan ->

            // TODO What do we do with this? Should all clients be forced to use "batch_size"?
            val batchSize = (clientConfig.planArgs["batch_size"]
                             ?: error("batch_size doesn't exist")).toInt()

            dataLoader.batchSize = batchSize

            val batchIValue = IValue.from(
                Tensor.fromBlob(longArrayOf(batchSize.toLong()), longArrayOf(1))
            )
            repeat(clientConfig.properties.maxUpdates) { step ->

                emit(TrainingState.Epoch(step + 1))
                dataLoader.reset()
                // TODO We should check requirements before arriving to this point
//                    emit(TrainingState.Error(IllegalStateException("No params in the model")))
                val modelParams = model.paramArray ?: emptyArray()
                val paramIValue = IValue.listFrom(*modelParams)

                for (batchData in dataLoader) {
                    val planArgs: List<IValue> =
                            trainingParameters.inputParams.fold(mutableListOf()) { args, inputSpec ->
                                when (inputSpec.type) {
                                    InputParamType.Data -> { args.apply { add(batchData[0]) } }
                                    InputParamType.Target -> { args.apply { add(batchData[1]) } }
                                    InputParamType.ModelParameter -> { args.apply { add(paramIValue) } }
                                    InputParamType.Value -> {
                                        val tensor = if (inputSpec.value != null) {
                                            inputSpec.value
                                        } else {
                                            val clientConfigValue = clientConfig.planArgs[inputSpec.name]
                                            clientConfigValue?.let {
                                                IValue.from(Tensor.fromBlob(
                                                    floatArrayOf(clientConfigValue.toFloat()),
                                                    longArrayOf(1)
                                                ))
                                            } ?: null
                                        }
                                        args.apply {
                                            tensor?.let {
                                                add(tensor)
                                            } ?: args
                                        }
                                    }
                                }
                            }

                    val output = plan.execute(*planArgs.toTypedArray())?.toTuple()

                    output?.let { outputResult ->
                        trainingParameters.outputParams.mapIndexed { index, outputSpec ->
                            when (outputSpec.type) {
                                OutputParamType.Loss -> { emit(TrainingState.Loss(outputResult[index].toTensor().dataAsFloatArray.last())) }
                                OutputParamType.Metric -> {
                                    emit(TrainingState.Metric(outputSpec.name, outputResult[index].toTensor().dataAsFloatArray.last()))
                                }
                                OutputParamType.ModelParameter -> {
                                    val updatedParams = outputResult.slice(index until index + model.stateTensorSize!!)
                                    model.updateModel(updatedParams)
                                }
                            }
                        }
                    }
                }
            }
        }

        emit(TrainingState.Message("Training done!\n Reporting diff"))
        val diff = createDiff()
        when (val reportStatus = report(diff)) {
            is JobStatusMessage.Complete -> {
                emit(TrainingState.Message("Model reported to PyGrid"))
                emit(TrainingState.Complete)
            }
            else -> {
                emit(TrainingState.Error(IllegalStateException("Report finished with an error")))
            }
        }
    }

    /**
     * This method is called by [Syft Worker][org.openmined.syft.Syft] on being accepted by PyGrid into a cycle
     * @param responseData The training parameters and requestKey returned by PyGrid
     */
    @Synchronized
    internal fun cycleAccepted(responseData: CycleResponseData.CycleAccept) {
        Log.d(TAG, "setting Request Key")
        responseData.plans.forEach { (planName, planId) ->
            plans[planName] = Plan(this, planId, planName)
        }
        responseData.protocols.forEach { (protocolName, protocolId) ->
            protocols[protocolName] = Protocol(protocolId)
        }
        requestKey = responseData.requestKey
        model.pyGridModelId = responseData.modelId
        cycleStatus.set(CycleStatus.ACCEPTED)
    }

    /**
     * This method is called by [Syft Worker][org.openmined.syft.Syft] on being rejected by PyGrid into a cycle
     * @param responseData The timeout returned by PyGrid after which the worker should retry
     */
    internal fun cycleRejected(responseData: CycleResponseData.CycleReject): JobStatusMessage {
        cycleStatus.set(CycleStatus.REJECT)
        return JobStatusMessage.JobCycleRejected(responseData.timeout)
    }

    /**
     * Downloads all the plans, protocols and the model weights from PyGrid
     * @param workerId The unique id assigned to the syft worker by PyGrid
     * @param responseData contains the cycle accept request key and training parameters
     */
    internal suspend fun downloadData(
        workerId: String,
        responseData: CycleResponseData.CycleAccept
    ): JobStatusMessage {
        return when {
            cycleStatus.get() != CycleStatus.ACCEPTED -> {
                publishError(JobErrorThrowable.CycleNotAccepted("Cycle not accepted. Download cannot start"))
                JobStatusMessage.Error(JobErrorThrowable.CycleNotAccepted("Cycle not accepted. Download cannot start"))
            }
            jobRepository.status == DownloadStatus.NOT_STARTED -> {
                val planRetriever = jobScope.async {
                    jobRepository.retrievePlanData(
                        workerId,
                        responseData.requestKey,
                        plans
                    )
                }
                val protocolRetriever = jobScope.async {
                    jobRepository.retrieveProtocolData(
                        workerId,
                        responseData.requestKey,
                        protocols
                    )
                }
                val modelRetriever = jobScope.async {
                    jobRepository.retrieveModel(workerId, responseData.requestKey, model)
                }

                Log.d(TAG, "Start downloading all data")
                planRetriever.await() + protocolRetriever.await() + modelRetriever.await()

                Log.d(TAG, "Data downloaded")

                JobStatusMessage.JobReady(model, plans, responseData.clientConfig)
            }
            else -> {
                JobStatusMessage.UnexpectedDownloadStatus(jobRepository.status)
            }
        }
    }

    /**
     * Create a diff between the model parameters downloaded from the PyGrid with the current state of model parameters
     * The diff is sent to [report] for sending it to PyGrid
     */
    private fun createDiff(): SyftState {
        val modulePath = jobRepository.persistToLocalStorage(
            jobRepository.getDiffScript(),
            config.filesDir.toString(),
            DIFF_SCRIPT_NAME
        )
        val oldState =
                SyftState.loadSyftState("${jobRepository.getModelsPath()}/${model.pyGridModelId}.pb")
        return model.createDiff(oldState, modulePath)
    }

    /**
     * Once training is finished submit the new model weights to PyGrid to complete the cycle
     * @param diff the difference of the new and old model weights serialised into [State][org.openmined.syft.proto.SyftState]
     */
    private suspend fun report(diff: SyftState): JobStatusMessage {
        val workerId = worker.getSyftWorkerId()
        if (throwErrorIfNetworkInvalid() ||
            throwErrorIfBatteryInvalid()
        ) return JobStatusMessage.ConditionsNotMet

        return if (!workerId.isNullOrEmpty() && requestKey.isNotEmpty()) {
            val reportResponse = config.getSignallingClient()
                    .report(
                        org.openmined.syft.networking.datamodels.syft.ReportRequest(
                            workerId,
                            requestKey,
                            android.util.Base64.encodeToString(
                                diff.serialize().toByteArray(),
                                android.util.Base64.DEFAULT
                            )
                        )
                    )
            when {
                reportResponse.error != null -> {
                    publishError(JobErrorThrowable.NetworkResponseFailure(reportResponse.error))
                    JobStatusMessage.Error(JobErrorThrowable.NetworkResponseFailure(reportResponse.error))
                }
                reportResponse.status != null -> {
                    Log.d(TAG, "report status ${reportResponse.status}")
                    JobStatusMessage.Complete
                }
                else -> {
                    JobStatusMessage.Error(JobErrorThrowable.IllegalJobState(IllegalStateException("Could not send report")))
                }
            }
        } else {
            JobStatusMessage.Error(JobErrorThrowable.IllegalJobState(IllegalStateException("Could not send report")))
        }
    }

    /**
     * Throw an error when network constraints fail.
     * @param publish when false the error is thrown for the error handler otherwise caught and published on the status processor
     * @return true if error is thrown otherwise false
     */
    internal fun throwErrorIfNetworkInvalid(publish: Boolean = true): Boolean {
        val validity = worker.isNetworkValid()
        if (publish && !validity)
            publishError(JobErrorThrowable.NetworkConstraintsFailure)
        else if (!validity)
            throwError(JobErrorThrowable.NetworkConstraintsFailure)
        return !validity
    }

    /**
     * Throw an error when battery constraints fail
     * @param publish when false the error is thrown for the error handler otherwise caught and published on the status processor
     * @return true if error is thrown otherwise false
     */
    internal fun throwErrorIfBatteryInvalid(publish: Boolean = true): Boolean {
        val validity = worker.isBatteryValid()
        if (publish && !validity)
            publishError(JobErrorThrowable.BatteryConstraintsFailure)
        else if (!validity)
            throwError(JobErrorThrowable.BatteryConstraintsFailure)
        return !validity
    }

    /**
     * Notify all the listeners about the error and dispose the job
     */
    internal fun publishError(throwable: JobErrorThrowable) {
        isDisposed.set(true)
    }

    /**
     * Throw the error to be caught by error handlers
     */
    private fun throwError(throwable: JobErrorThrowable) {
        isDisposed.set(true)
        throw throwable
    }

    /**
     * Identifies if the job is already disposed
     */
    private fun isDisposed() = isDisposed.get()

    /**
     * Dispose the job. Once disposed, a job cannot be resumed again.
     */
    fun dispose() {
        if (!isDisposed()) {
            isDisposed.set(true)
            Log.d(TAG, "job disposed")
        } else
            Log.d(TAG, "job already disposed")
    }

    internal enum class CycleStatus {
        APPLY, REJECT, ACCEPTED
    }
}
