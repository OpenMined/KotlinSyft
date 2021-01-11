[syft](../../index.md) / [org.openmined.syft.execution](../index.md) / [SyftJob](index.md) / [train](./train.md)

# train

`@ExperimentalStdlibApi fun train(plans: `[`ConcurrentHashMap`](https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/ConcurrentHashMap.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`Plan`](../-plan/index.md)`>, clientConfig: `[`ClientConfig`](../../org.openmined.syft.networking.datamodels/-client-config/index.md)`, syftDataLoader: `[`SyftDataLoader`](../../org.openmined.syft.domain/-syft-data-loader/index.md)`, trainingParameters: `[`TrainingParameters`](../../org.openmined.syft.domain/-training-parameters/index.md)`): Flow<`[`TrainingState`](../-training-state/index.md)`>`