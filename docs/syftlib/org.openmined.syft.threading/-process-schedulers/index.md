[syftlib](../../index.md) / [org.openmined.syft.threading](../index.md) / [ProcessSchedulers](./index.md)

# ProcessSchedulers

`interface ProcessSchedulers`

### Properties

| Name | Summary |
|---|---|
| [calleeThreadScheduler](callee-thread-scheduler.md) | calleeThreadScheduler defines the thread on which the callback to observable is run`abstract val calleeThreadScheduler: Scheduler` |
| [computeThreadScheduler](compute-thread-scheduler.md) | computeThreadScheduler defines the thread on which observable runs`abstract val computeThreadScheduler: Scheduler` |

### Functions

| Name | Summary |
|---|---|
| [applyFlowableSchedulers](apply-flowable-schedulers.md) | `open fun <T> applyFlowableSchedulers(): (Flowable<T>) -> Flowable<T>!` |
| [applySingleSchedulers](apply-single-schedulers.md) | `open fun <T> applySingleSchedulers(): (Single<T>) -> Single<T>` |
