package org.openmined.syft.networking.clients

import org.openmined.syftproto.execution.v1.PlanOuterClass

class MessageProcessor {

    fun processPlan(byteArray: ByteArray): PlanOuterClass.Plan {
        return PlanOuterClass.Plan.parseFrom(byteArray)
    }
}
