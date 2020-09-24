[syftlib](../../index.md) / [org.openmined.syft.execution](../index.md) / [SyftJob](./index.md)

# SyftJob

`@ExperimentalUnsignedTypes class SyftJob : Disposable`

### Parameters

`modelName` - : The model being trained or used in inference

`version` - : The version of the model with name modelName

### Types

| Name | Summary |
|---|---|
| [JobID](-job-i-d/index.md) | A uniquer identifier class for the job`data class JobID` |

### Properties

| Name | Summary |
|---|---|
| [jobId](job-id.md) | `val jobId: JobID` |

### Functions

| Name | Summary |
|---|---|
| [createDiff](create-diff.md) | Create a diff between the model parameters downloaded from the PyGrid with the current state of model parameters The diff is sent to [report](report.md) for sending it to PyGrid`fun createDiff(): `[`SyftState`](../../org.openmined.syft.proto/-syft-state/index.md) |
| [dispose](dispose.md) | Dispose the job. Once disposed, a job cannot be resumed again.`fun dispose(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [isDisposed](is-disposed.md) | Identifies if the job is already disposed`fun isDisposed(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [report](report.md) | Once training is finished submit the new model weights to PyGrid to complete the cycle`fun report(diff: `[`SyftState`](../../org.openmined.syft.proto/-syft-state/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [start](start.md) | Starts the job by asking syft worker to request for cycle. Initialises Socket connection if not initialised already.`fun start(subscriber: `[`JobStatusSubscriber`](../-job-status-subscriber/index.md)` = JobStatusSubscriber()): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [subscribe](subscribe.md) | This method can be called when the user needs to attach a listener to the job but do not wish to start it`fun subscribe(subscriber: `[`JobStatusSubscriber`](../-job-status-subscriber/index.md)`, schedulers: `[`ProcessSchedulers`](../../org.openmined.syft.threading/-process-schedulers/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

### Companion Object Functions

| Name | Summary |
|---|---|
| [create](create.md) | Creates a new Syft Job`fun create(modelName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, version: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, worker: `[`Syft`](../../org.openmined.syft/-syft/index.md)`, config: `[`SyftConfiguration`](../../org.openmined.syft.domain/-syft-configuration/index.md)`): `[`SyftJob`](./index.md) |
