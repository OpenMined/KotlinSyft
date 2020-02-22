package org.openmined.syft.Processes

sealed class JobStatusMessage {
    object JobCycleAccepted : JobStatusMessage()
    object JobReady : JobStatusMessage()
    object JobFinished : JobStatusMessage()
}