[syft](../../index.md) / [org.openmined.syft.execution](../index.md) / [JobErrorThrowable](./index.md)

# JobErrorThrowable

`sealed class JobErrorThrowable : `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)

### Types

| Name | Summary |
|---|---|
| [BatteryConstraintsFailure](-battery-constraints-failure/index.md) | `object BatteryConstraintsFailure : `[`JobErrorThrowable`](./index.md) |
| [NetworkConstraintsFailure](-network-constraints-failure/index.md) | `object NetworkConstraintsFailure : `[`JobErrorThrowable`](./index.md) |
| [RunningDisposedJob](-running-disposed-job/index.md) | `object RunningDisposedJob : `[`JobErrorThrowable`](./index.md) |
| [UninitializedWorkerError](-uninitialized-worker-error/index.md) | `object UninitializedWorkerError : `[`JobErrorThrowable`](./index.md) |

### Exceptions

| Name | Summary |
|---|---|
| [AuthenticationFailure](-authentication-failure/index.md) | `class AuthenticationFailure : `[`JobErrorThrowable`](./index.md) |
| [CycleNotAccepted](-cycle-not-accepted/index.md) | `class CycleNotAccepted : `[`JobErrorThrowable`](./index.md) |
| [DownloadIncomplete](-download-incomplete/index.md) | `class DownloadIncomplete : `[`JobErrorThrowable`](./index.md) |
| [ExternalException](-external-exception/index.md) | `class ExternalException : `[`JobErrorThrowable`](./index.md) |
| [IllegalJobState](-illegal-job-state/index.md) | `class IllegalJobState : `[`JobErrorThrowable`](./index.md) |
| [NetworkResponseFailure](-network-response-failure/index.md) | `class NetworkResponseFailure : `[`JobErrorThrowable`](./index.md) |
| [NetworkUnreachable](-network-unreachable/index.md) | `class NetworkUnreachable : `[`JobErrorThrowable`](./index.md) |
