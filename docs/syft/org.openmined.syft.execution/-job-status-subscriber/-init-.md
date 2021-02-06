[syft](../../index.md) / [org.openmined.syft.execution](../index.md) / [JobStatusSubscriber](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`JobStatusSubscriber()`

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

