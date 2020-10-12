[syft](../../index.md) / [org.openmined.syft.execution](../index.md) / [SyftJob](index.md) / [start](./start.md)

# start

`fun start(subscriber: `[`JobStatusSubscriber`](../-job-status-subscriber/index.md)` = JobStatusSubscriber()): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Starts the job by asking syft worker to request for cycle.
Initialises Socket connection if not initialised already.

### Parameters

`subscriber` - (Optional) Contains the methods overridden by the user to be called upon job success/error

**See Also**

[org.openmined.syft.execution.JobStatusSubscriber](../-job-status-subscriber/index.md)

