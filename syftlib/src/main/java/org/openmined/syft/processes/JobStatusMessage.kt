package org.openmined.syft.processes

import org.openmined.syft.networking.datamodels.ClientConfig

sealed class JobStatusMessage {
    class JobCycleRejected(val timeout: String) : JobStatusMessage()
    class JobReady(val model: String, val clientConfig: ClientConfig)  : JobStatusMessage()
}