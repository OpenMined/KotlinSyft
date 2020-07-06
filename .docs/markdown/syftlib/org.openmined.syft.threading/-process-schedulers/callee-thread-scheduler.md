[syftlib](../../index.md) / [org.openmined.syft.threading](../index.md) / [ProcessSchedulers](index.md) / [calleeThreadScheduler](./callee-thread-scheduler.md)

# calleeThreadScheduler

`abstract val calleeThreadScheduler: Scheduler`

calleeThreadScheduler defines the thread on which the callback to observable is run

``` kotlin
/**
     * calleeThreadScheduler defines the thread on which the callback to observable is run
     * @sample calleeThreadScheduler AndroidSchedulers.MainThread()
     */
    val calleeThreadScheduler: Scheduler
```

