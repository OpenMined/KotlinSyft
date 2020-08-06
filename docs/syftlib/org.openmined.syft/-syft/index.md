[syftlib](../../index.md) / [org.openmined.syft](../index.md) / [Syft](./index.md)

# Syft

`@ExperimentalUnsignedTypes class Syft : Disposable`

This is the main syft worker handling creation and deletion of jobs. This class is also responsible for monitoring device resources via DeviceMonitor

### Functions

| Name | Summary |
|---|---|
| [dispose](dispose.md) | Explicitly dispose off the worker. All the jobs running in the worker will be disposed off as well. Clears the current singleton worker instance so the immediately next [getInstance](get-instance.md) call creates a new syft worker`fun dispose(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [isDisposed](is-disposed.md) | Check if the syft worker has been disposed`fun isDisposed(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [newJob](new-job.md) | Create a new job for the worker.`fun newJob(model: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, version: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null): `[`SyftJob`](../../org.openmined.syft.execution/-syft-job/index.md) |

### Companion Object Functions

| Name | Summary |
|---|---|
| [getInstance](get-instance.md) | Only a single worker must be instantiated across an app lifecycle. The [getInstance](get-instance.md) ensures creation of the singleton object if needed or returns the already created worker. This method is thread safe so getInstance calls across threads do not suffer`fun getInstance(syftConfiguration: `[`SyftConfiguration`](../../org.openmined.syft.domain/-syft-configuration/index.md)`, authToken: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null): `[`Syft`](./index.md) |
