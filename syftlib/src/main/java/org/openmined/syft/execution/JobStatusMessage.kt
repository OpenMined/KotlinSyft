package org.openmined.syft.execution

import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel
import java.util.concurrent.ConcurrentHashMap

@ExperimentalUnsignedTypes
sealed class JobStatusMessage {
    class JobCycleRejected(val timeout: String) : JobStatusMessage()
    class JobReady(
        val model: SyftModel,
        val plans: ConcurrentHashMap<String, Plan>,
        val clientConfig: ClientConfig?
    ) : JobStatusMessage()
}