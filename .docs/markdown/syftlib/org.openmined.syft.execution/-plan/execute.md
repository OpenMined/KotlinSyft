[syftlib](../../index.md) / [org.openmined.syft.execution](../index.md) / [Plan](index.md) / [execute](./execute.md)

# execute

`@ExperimentalStdlibApi fun execute(model: `[`SyftModel`](../../org.openmined.syft.proto/-syft-model/index.md)`, trainingBatch: `[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<IValue, IValue>, clientConfig: `[`ClientConfig`](../../org.openmined.syft.networking.datamodels/-client-config/index.md)`): IValue?`

Loads a serialized TorchScript module from the specified path on the disk.

### Parameters

`model` - Model hosting model parameters.

`trainingBatch` - Contains the training data at first position and the labels at second.

`clientConfig` - The hyper parameters for the model.

### Exceptions

`IllegalStateException` - if the device state does not fulfill the constraints set for running the job

**Return**
The output contains the loss, accuracy values as defined while creating plan. It also
    contains the updated parameters of the model. These parameters are then saved manually by user.

