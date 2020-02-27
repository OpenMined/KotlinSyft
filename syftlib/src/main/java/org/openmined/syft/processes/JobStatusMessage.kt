package org.openmined.syft.processes

sealed class JobStatusMessage {
    object JobCycleAccepted : JobStatusMessage()
    object JobReady : JobStatusMessage()
    object JobFinished : JobStatusMessage()
}