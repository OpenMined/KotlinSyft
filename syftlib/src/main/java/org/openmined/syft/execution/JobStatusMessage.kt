package org.openmined.syft.execution

import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel

sealed class JobStatusMessage {
    class JobCycleRejected(val timeout: String) : JobStatusMessage()
    class JobReady(val model: SyftModel, val clientConfig: ClientConfig?) : JobStatusMessage()
}