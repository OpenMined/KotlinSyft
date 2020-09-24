[syft](../../index.md) / [org.openmined.syft.proto](../index.md) / [SyftState](index.md) / [updateState](./update-state.md)

# updateState

`fun updateState(newStateTensors: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<IValue>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

This method is used to save/update SyftState parameters.

### Exceptions

`IllegalArgumentException` - if the size newModelParams is not correct.

### Parameters

`newStateTensors` - a list of PyTorch Tensor that would be converted to syftTensor