[syftlib](../../index.md) / [org.openmined.syft.execution](../index.md) / [JobStatusSubscriber](./index.md)

# JobStatusSubscriber

`@ExperimentalUnsignedTypes open class JobStatusSubscriber`

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `JobStatusSubscriber()` |

### Functions

| Name | Summary |
|---|---|
| [onComplete](on-complete.md) | `open fun onComplete(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [onError](on-error.md) | `open fun onError(throwable: `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [onJobStatusMessage](on-job-status-message.md) | `fun onJobStatusMessage(jobStatusMessage: `[`JobStatusMessage`](../-job-status-message/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [onReady](on-ready.md) | `open fun onReady(model: `[`SyftModel`](../../org.openmined.syft.proto/-syft-model/index.md)`, plans: `[`ConcurrentHashMap`](https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/ConcurrentHashMap.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`Plan`](../-plan/index.md)`>, clientConfig: `[`ClientConfig`](../../org.openmined.syft.networking.datamodels/-client-config/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [onRejected](on-rejected.md) | `open fun onRejected(timeout: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
