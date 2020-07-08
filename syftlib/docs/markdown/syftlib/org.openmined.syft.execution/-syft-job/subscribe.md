[syftlib](../../index.md) / [org.openmined.syft.execution](../index.md) / [SyftJob](index.md) / [subscribe](./subscribe.md)

# subscribe

`fun subscribe(subscriber: `[`JobStatusSubscriber`](../-job-status-subscriber/index.md)`, schedulers: `[`ProcessSchedulers`](../../org.openmined.syft.threading/-process-schedulers/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

This method can be called when the user needs to attach a listener to the job but do not wish to start it

``` kotlin
val job = SyftJob.create(
    model,
    version,
    this,
    syftConfig
)
if (syftConfig.maxConcurrentJobs == workerJobs.size)
    throw IndexOutOfBoundsException("maximum number of allowed jobs reached")

workerJobs[job.jobId] = job
job.subscribe(object : JobStatusSubscriber() {
    override fun onComplete() {
        workerJobs.remove(job.jobId)
    }

    override fun onError(throwable: Throwable) {
        Log.e(TAG, throwable.message.toString())
        workerJobs.remove(job.jobId)
    }
}, syftConfig.networkingSchedulers)

return job
```

### Parameters

`subscriber` - (Optional) Contains the methods overridden by the user to be called upon job success/error

**See Also**

[org.openmined.syft.execution.JobStatusSubscriber](../-job-status-subscriber/index.md)

