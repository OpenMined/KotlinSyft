[syft](../index.md) / [org.openmined.syft.execution](./index.md)

## Package org.openmined.syft.execution

### Types

| Name | Summary |
|---|---|
| [JobStatusMessage](-job-status-message/index.md) | `sealed class JobStatusMessage` |
| [JobStatusSubscriber](-job-status-subscriber/index.md) | This is passed as argument to [SyftJob.request](-syft-job/request.md) giving the overridden callbacks for different phases of the job cycle.`open class JobStatusSubscriber` |
| [Plan](-plan/index.md) | The Plan Class contains functions to load a PyTorch model from a TorchScript and to run training through the forward function of the PyTorch Module. A PyTorch Module is simply a container that takes in tensors as input and returns tensor after doing some computation.`class Plan` |
| [Protocol](-protocol/index.md) | `class Protocol` |
| [SyftJob](-syft-job/index.md) | `class SyftJob` |
| [TrainingState](-training-state/index.md) | `sealed class TrainingState` |

### Exceptions

| Name | Summary |
|---|---|
| [JobErrorThrowable](-job-error-throwable/index.md) | `sealed class JobErrorThrowable : `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html) |
