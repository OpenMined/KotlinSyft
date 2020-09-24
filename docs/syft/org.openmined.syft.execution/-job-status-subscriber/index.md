[syftlib](../../index.md) / [org.openmined.syft.execution](../index.md) / [JobStatusSubscriber](./index.md)

# JobStatusSubscriber

`@ExperimentalUnsignedTypes open class JobStatusSubscriber`

This is passed as argument to [SyftJob.start](../-syft-job/start.md) giving the overridden callbacks for different phases of the job cycle.

``` kotlin
val jobStatusSubscriber = object : JobStatusSubscriber() {
     override fun onReady(
     model: SyftModel,
     plans: ConcurrentHashMap<String, Plan>,
     clientConfig: ClientConfig
     ) {
     }

     override fun onRejected(timeout: String) {
     }

     override fun onError(throwable: Throwable) {
     }
}

job.start(jobStatusSubscriber)
```

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | This is passed as argument to [SyftJob.start](../-syft-job/start.md) giving the overridden callbacks for different phases of the job cycle.`JobStatusSubscriber()` |

### Functions

| Name | Summary |
|---|---|
| [onComplete](on-complete.md) | This method is called when the job cycle finishes successfully. Override this method to clear the worker and the jobs`open fun onComplete(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [onError](on-error.md) | This method is called when the job throws an error`open fun onError(throwable: `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [onReady](on-ready.md) | This method is called when KotlinSyft has downloaded all the plans and protocols from PyGrid and it is ready to train the model.`open fun onReady(model: `[`SyftModel`](../../org.openmined.syft.proto/-syft-model/index.md)`, plans: `[`ConcurrentHashMap`](https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/ConcurrentHashMap.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`Plan`](../-plan/index.md)`>, clientConfig: `[`ClientConfig`](../../org.openmined.syft.networking.datamodels/-client-config/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [onRejected](on-rejected.md) | This method is called when the worker has been rejected from the cycle by the PyGrid`open fun onRejected(timeout: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
