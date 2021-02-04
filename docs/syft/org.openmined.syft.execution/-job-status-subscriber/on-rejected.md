[syft](../../index.md) / [org.openmined.syft.execution](../index.md) / [JobStatusSubscriber](index.md) / [onRejected](./on-rejected.md)

# onRejected

`open fun onRejected(timeout: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

This method is called when the worker has been rejected from the cycle by the PyGrid

### Parameters

`timeout` - is the timestamp indicating the time after which the worker should retry joining into the cycle. This will be empty if it is the last cycle.