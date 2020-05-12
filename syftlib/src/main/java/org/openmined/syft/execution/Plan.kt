package org.openmined.syft.execution

import android.util.Log
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel
import org.openmined.syftproto.execution.v1.PlanOuterClass
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream

private const val TAG = "syft.processes.Plan"

/** 
 * The Plan Class contains functions to load a Pytorch model from a TorchScript and
 * passing the training data through the forward function of the Pytorch Module.
 * A Pytorch Module is simply a container that takes in tensors as input and returns 
 * tensor after doing some computation.
 */
@ExperimentalUnsignedTypes
class Plan(val planId: String) {
    private var pytorchModule: Module? = null

    /**
     * Loads a serialized TorchScript module from the specified path on the disk.
     *
     * @param model The model parameters.
     * @param trainingBatch The training data.
     * @param clientConfig The hyperparamters for the model.
     * @return The output of passing the training data through the pytorch Module.
     */
    @ExperimentalStdlibApi
    fun execute(
        model: SyftModel,
        trainingBatch: Pair<IValue, IValue>,
        clientConfig: ClientConfig
    ): IValue? {
        val localModuleState = pytorchModule
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

    // TODO The way a plan is generated should be provided.
    // TODO We should enforce this to  happen in a background thread.
    /**
     * Loads a serialized TorchScript module from the specified path on the disk.
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
        pytorchModule = Module.load(torchscriptLocation)
    }

    /**
     * Loads a serialized TorchScript module from the specified path on the disk.
     *
     * @param filesDir directory where the .pt file is present, on which the TorchScript model is saved.
     * @param obj Stream containing the contents of the TorchScript module.
     * @return the absolute path of the file containing the TorchScript model.
     */
    private fun saveScript(filesDir: String, obj: com.google.protobuf.ByteString): String {
        val file = File(filesDir, "torchscript_${planId}.pt")
        FileOutputStream(file).use {
            it.write(obj.toByteArray())
            it.flush()
            it.close()
        }
        return file.absolutePath
    }
}