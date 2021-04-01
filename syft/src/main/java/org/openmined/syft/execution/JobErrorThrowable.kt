package org.openmined.syft.execution

sealed class JobErrorThrowable : Throwable() {
    object NetworkConstraintsFailure : JobErrorThrowable() {
        override val message: String = "network constraints failed"
    }

    object BatteryConstraintsFailure : JobErrorThrowable() {
        override val message: String = "battery constraints failed"
    }

    object UninitializedWorkerError : JobErrorThrowable(){
        override val message: String = "worker is not initialised"
    }

    object RunningDisposedJob : JobErrorThrowable(){
        override val message: String = "Attempting to run a disposed job"
    }

    class AuthenticationFailure(override val message: String = "Authentication failed") :
        JobErrorThrowable()

    class NetworkUnreachable(override val message: String = "network unreachable") :
        JobErrorThrowable()

    class NetworkResponseFailure(override val message: String? = "server response error ") :
        JobErrorThrowable()

    class CycleNotAccepted(override val message: String) : JobErrorThrowable()

    class DownloadIncomplete(override val message: String) : JobErrorThrowable()

    class ExternalException(override val message: String?, override val cause: Throwable?) :
        JobErrorThrowable()

    class IllegalJobState(override val cause: Throwable?) : JobErrorThrowable()
}