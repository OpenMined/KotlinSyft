[syft](../../index.md) / [org.openmined.syft.proto](../index.md) / [SyftState](./index.md)

# SyftState

`@ExperimentalUnsignedTypes data class SyftState`

SyftState class is responsible for storing all the weights of the neural network.
We update these model weights after every plan.execute

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | SyftState class is responsible for storing all the weights of the neural network. We update these model weights after every plan.execute`SyftState(placeholders: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`Placeholder`](../-placeholder/index.md)`>, iValueTensors: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<IValue>)` |

### Properties

| Name | Summary |
|---|---|
| [iValueTensors](i-value-tensors.md) | the IValue tensors for the model params`val iValueTensors: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<IValue>` |
| [placeholders](placeholders.md) | the variables describing the location of tensor in the plan torchscript`val placeholders: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`Placeholder`](../-placeholder/index.md)`>` |
| [syftTensorArray](syft-tensor-array.md) | `val syftTensorArray: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`SyftTensor`](../-syft-tensor/index.md)`>` |
| [tensorArray](tensor-array.md) | `val tensorArray: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<Tensor!>` |

### Functions

| Name | Summary |
|---|---|
| [createDiff](create-diff.md) | Subtract the older state from the current state to generate the diff`fun createDiff(oldSyftState: `[`SyftState`](./index.md)`, diffScriptLocation: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`SyftState`](./index.md) |
| [equals](equals.md) | `fun equals(other: `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`?): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [hashCode](hash-code.md) | `fun hashCode(): `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [serialize](serialize.md) | Generate StateOuterClass.State object using Placeholders list and syftTensor list`fun serialize(): State` |
| [updateState](update-state.md) | This method is used to save/update SyftState parameters.`fun updateState(newStateTensors: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<IValue>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

### Companion Object Functions

| Name | Summary |
|---|---|
| [loadSyftState](load-syft-state.md) | Load the [SyftTensors](../-syft-tensor/index.md) and [placeholders](../-placeholder/index.md) from the file`fun loadSyftState(fileLocation: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`SyftState`](./index.md) |
