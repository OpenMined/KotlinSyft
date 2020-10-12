[syft](../../index.md) / [org.openmined.syft.execution](../index.md) / [SyftJob](index.md) / [create](./create.md)

# create

`fun create(modelName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, version: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, worker: `[`Syft`](../../org.openmined.syft/-syft/index.md)`, config: `[`SyftConfiguration`](../../org.openmined.syft.domain/-syft-configuration/index.md)`): `[`SyftJob`](index.md)

Creates a new Syft Job

``` kotlin
val job = SyftJob.create(
    model,
    version,
    this,
    syftConfig
)
if (workerJob != null)
    throw IndexOutOfBoundsException("maximum number of allowed jobs reached")

workerJob = job
job.subscribe(object : JobStatusSubscriber() {
    override fun onComplete() {
        workerJob = null
    }

    override fun onError(throwable: Throwable) {
        Log.e(TAG, throwable.message.toString())
        workerJob = null
    }
}, syftConfig.networkingSchedulers)

return job
```

### Parameters

`modelName` - : The model being trained or used in inference

`version` - : The version of the model with name modelName

`worker` - : The syft worker handling this job

`config` - : The configuration class for schedulers and clients