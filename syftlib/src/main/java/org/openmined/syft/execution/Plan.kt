package org.openmined.syft.execution

import android.util.Log
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor

private const val TAG = "syft.processes.Plan"

/**
 * The Plan Class contains functions to load a PyTorch model from a TorchScript and
 * to run training through the forward function of the PyTorch Module.
 * A PyTorch Module is simply a container that takes in tensors as input and returns
 * tensor after doing some computation.
 * @property job is the job hosting this plan
 * @property planId is the unique id allotted to the plan by PyGrid
 * @property planName is the name of the plan
 */
@ExperimentalUnsignedTypes
class Plan(val job: SyftJob, val planId: String, val planName : String) {
    private var pyTorchModule: Module? = null

    /**
     * Loads a serialized TorchScript module from the specified path on the disk.
     *
     * @param iValues The input to the torchscript. The training batch, the hyper parameters and the model weights must be sent here
     * @return The output contains the loss, accuracy values as defined while creating plan. It also
     *         contains the updated parameters of the model. These parameters are then saved manually by user.
     * @throws IllegalStateException if the device state does not fulfill the constraints set for running the job
     */
    @Throws(IllegalStateException::class)
    @ExperimentalStdlibApi
    fun execute(vararg iValues: IValue): IValue? {
        if (job.throwErrorIfBatteryInvalid()) {
            //todo decide how we want to handle this. Throw an error or quietly skip execution
            return null
        }

        val localModuleState = pyTorchModule
        if (localModuleState == null) {
            Log.e(TAG, "pytorch module not initialized yet")
            return null
        }
        // We feed in the training data to the forward function of the pyTorchModule.
        return localModuleState.forward(*iValues)
    }

    /**
     * Loads a TorchScript module from the specified path on the disk.
     *
     * @param torchScriptLocation location of the TorchScript plan.
     */
    fun loadScriptModule(torchScriptLocation: String) {
        pyTorchModule = Module.load(torchScriptLocation)
    }
}