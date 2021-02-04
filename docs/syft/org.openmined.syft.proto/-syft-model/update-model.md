[syft](../../index.md) / [org.openmined.syft.proto](../index.md) / [SyftModel](index.md) / [updateModel](./update-model.md)

# updateModel

`fun updateModel(newModelParams: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<IValue>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

This method is used to save/update SyftModel class.
This function must be called after every gradient step to update the model state for further plan executions.

### Exceptions

`IllegalArgumentException` - if the size newModelParams is not correct.

### Parameters

`newModelParams` - a list of PyTorch IValue that would be set as the current state