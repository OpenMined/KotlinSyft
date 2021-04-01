[syft](../../index.md) / [org.openmined.syft.execution](../index.md) / [SyftJob](./index.md)

# SyftJob

`@ExperimentalCoroutinesApi @ExperimentalUnsignedTypes class SyftJob`

### Parameters

`jobModel` - : Hold data about this job (modelName, version, plans, protocols)

### Properties

| Name | Summary |
|---|---|
| [jobModel](job-model.md) | : Hold data about this job (modelName, version, plans, protocols)`val jobModel: `[`JobModel`](../-job-model/index.md) |

### Functions

| Name | Summary |
|---|---|
| [dispose](dispose.md) | Dispose the job. Once disposed, a job cannot be resumed again.`fun dispose(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [request](request.md) | Starts the job by asking syft worker to request for cycle. Initialises Socket connection if not initialised already.`suspend fun request(): `[`JobStatusMessage`](../-job-status-message/index.md) |
| [train](train.md) | `fun train(plans: `[`ConcurrentHashMap`](https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/ConcurrentHashMap.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`Plan`](../-plan/index.md)`>, clientConfig: `[`ClientConfig`](../../org.openmined.syft.networking.datamodels/-client-config/index.md)`, dataLoader: `[`DataLoader`](../../org.openmined.syft.data.loader/-data-loader/index.md)`, trainingParameters: `[`TrainingParameters`](../../org.openmined.syft.domain/-training-parameters/index.md)`): Flow<`[`TrainingState`](../-training-state/index.md)`>` |

### Companion Object Functions

| Name | Summary |
|---|---|
| [create](create.md) | Creates a new Syft Job`fun create(modelName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, version: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, worker: `[`Syft`](../../org.openmined.syft/-syft/index.md)`, config: `[`SyftConfiguration`](../../org.openmined.syft.domain/-syft-configuration/index.md)`): `[`SyftJob`](./index.md) |
