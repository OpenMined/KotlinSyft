package org.openmined.syft.networking.clients

import org.openmined.syftproto.types.torch.v1.ScriptModuleOuterClass

class MessageProcessor {

    fun processPlan(byteArray: ByteArray): ScriptModuleOuterClass.ScriptModule {
        return ScriptModuleOuterClass.ScriptModule.parseFrom(byteArray)
    }
}
