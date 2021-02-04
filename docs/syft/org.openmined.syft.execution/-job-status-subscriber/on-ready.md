[syft](../../index.md) / [org.openmined.syft.execution](../index.md) / [JobStatusSubscriber](index.md) / [onReady](./on-ready.md)

# onReady

`open fun onReady(model: `[`SyftModel`](../../org.openmined.syft.proto/-syft-model/index.md)`, plans: `[`ConcurrentHashMap`](https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/ConcurrentHashMap.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`Plan`](../-plan/index.md)`>, clientConfig: `[`ClientConfig`](../../org.openmined.syft.networking.datamodels/-client-config/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

This method is called when KotlinSyft has downloaded all the plans and protocols from PyGrid and it is ready to train the model.

### Parameters

`model` - stores the model weights given by PyGrid

`plans` - is a HashMap of all the planIDs and their plans.

`clientConfig` - has hyper parameters like batchsize, learning rate, number of steps, etc