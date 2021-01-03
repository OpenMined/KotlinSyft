package org.openmined.syft.execution

import org.openmined.syft.domain.DownloadStatus
import org.openmined.syft.networking.datamodels.ClientConfig
import org.openmined.syft.proto.SyftModel
import java.util.concurrent.ConcurrentHashMap

@ExperimentalUnsignedTypes
sealed class JobStatusMessage {
    object JobInit : JobStatusMessage()

    object JobCycleAwaiting : JobStatusMessage()

    class JobCycleRejected(val timeout: String) : JobStatusMessage()

    class JobReady(
        val model: SyftModel,
        val plans: ConcurrentHashMap<String, Plan>,
        val clientConfig: ClientConfig?
    ) : JobStatusMessage()

    object Complete : JobStatusMessage()

    class Error(val throwable: JobErrorThrowable) : JobStatusMessage()

    data class UnexpectedDownloadStatus(val downloadStatus: DownloadStatus) : JobStatusMessage()

    object ConditionsNotMet : JobStatusMessage()
}