package org.openmined.syft.execution

import android.util.Log
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel
import org.openmined.syft.utilities.FileWriter
import org.openmined.syftproto.execution.v1.PlanOuterClass
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File

private const val TAG = "syft.processes.Plan"

/**
 * The Plan Class contains functions to load a PyTorch model from a TorchScript and
 * to run training through the forward function of the PyTorch Module.
 * A PyTorch Module is simply a container that takes in tensors as input and returns
 * tensor after doing some computation.
 * @property planId is the unique id allotted to the plan by PyGrid
 * @property job is the job hosting this plan
 */
@ExperimentalUnsignedTypes
class Plan(val job: SyftJob, val planId: String) {
    private var pyTorchModule: Module? = null

    /**
     * Loads a serialized TorchScript module from the specified path on the disk.
     *
     * @param model Model hosting model parameters.
     * @param trainingBatch Contains the training data at first position and the labels at second.
     * @param clientConfig The hyper parameters for the model.
     * @return The output contains the loss, accuracy values as defined while creating plan. It also
     *         contains the updated parameters of the model. These parameters are then saved manually by user.
     * @throws IllegalStateException if the device state does not fulfill the constraints set for running the job
     */
    @Throws(IllegalStateException::class)
    @ExperimentalStdlibApi
    fun execute(
        model: SyftModel,
        trainingBatch: Pair<IValue, IValue>,
        clientConfig: ClientConfig
    ): IValue? {
        if (job.returnErrorIfStateInvalid())
        //todo decide how we want to handle this. Throw an error or quietly skip execution
            return null

        val localModuleState = pyTorchModule
        if (localModuleState == null) {
            Log.e(TAG, "pytorch module not initialized yet")
            return null
        }
        val params = model.modelState?.getIValueTensorArray()
        if (params == null) {
            Log.e(TAG, "model state not initialised yet")
            return null
        }
        val x = trainingBatch.first
        val y = trainingBatch.second

        // batchSize is the pytorch IValue tensor containing the batchSize specified in the client configs.
        val batchSize = IValue.from(
            Tensor.fromBlob(longArrayOf(clientConfig.batchSize), longArrayOf(1))
        )

        // lr is the pytorch IValue tensor containing the learning rate specificed in the client configs.
        val lr = IValue.from(
            Tensor.fromBlob(floatArrayOf(clientConfig.lr), longArrayOf(1))
        )

        // We feed in the training data to the forward function of the pytorchModule.
        return localModuleState.forward(x, y, batchSize, lr, *params)
    }

    /**
     * Loads a TorchScript module from the specified path on the disk.
     *
     * @param filesDir directory where the TorchScript is saved. 
     * @param torchScriptPlan location where the TorchScript plan is located.
     */
    fun generateScriptModule(filesDir: String, torchScriptPlan: String) {
        val scriptModule = PlanOuterClass.Plan.parseFrom(
            File(torchScriptPlan).readBytes()
        )
        val torchscriptLocation = saveScript(filesDir, scriptModule.torchscript)
        Log.d(TAG, "TorchScript saved at $torchscriptLocation")
        pyTorchModule = Module.load(torchscriptLocation)
    }

    /**
     * Writes the module to the torchscript and returns the absolute path.
     *
     * @param filesDir The directory where the torchscript file will be written.
     * @param obj Protobuf stream containing the contents of the TorchScript module.
     * @return the absolute path of the file containing the TorchScript model.
     */
    private fun saveScript(filesDir: String, obj: com.google.protobuf.ByteString): String {
        val fileWriter = FileWriter(filesDir, "torchscript_${planId}.pt")
        fileWriter.outputStream().use {
            it.write(obj.toByteArray())
        }
        return fileWriter.absolutePath
    }
}