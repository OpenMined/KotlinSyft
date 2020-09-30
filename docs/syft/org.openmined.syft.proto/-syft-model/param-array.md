[syft](../../index.md) / [org.openmined.syft.proto](../index.md) / [SyftModel](index.md) / [paramArray](./param-array.md)

# paramArray

`val paramArray: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<Tensor>?`

**Return**
The array of [Tensor](https://pytorch.org/javadoc/org/pytorch/Tensor.html) of model weights or null if not set
This can be fed directly to the [org.openmined.syft.execution.Plan.execute](../../org.openmined.syft.execution/-plan/execute.md) by converting it to [IValue](https://pytorch.org/javadoc/org/pytorch/IValue.html)

