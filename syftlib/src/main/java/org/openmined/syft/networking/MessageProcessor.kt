package org.openmined.syft.networking.clients

import org.openmined.syftproto.execution.v1.PlanOuterClass
import org.openmined.syftproto.types.torch.v1.ScriptModuleOuterClass

class MessageProcessor {

    fun processTorchScript(byteArray: ByteArray): ScriptModuleOuterClass.ScriptModule {
        return ScriptModuleOuterClass.ScriptModule.parseFrom(byteArray)
    }

    fun processPlan(byteArray: ByteArray): PlanOuterClass.Plan {
        return PlanOuterClass.Plan.parseFrom(byteArray)
    }
}
