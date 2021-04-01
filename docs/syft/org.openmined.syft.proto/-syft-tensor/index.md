[syft](../../index.md) / [org.openmined.syft.proto](../index.md) / [SyftTensor](./index.md)

# SyftTensor

`@ExperimentalUnsignedTypes data class SyftTensor`

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `SyftTensor(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, contents: TensorData, shape: `[`MutableList`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-list/index.html)`<`[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`>, dtype: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, chain: `[`SyftTensor`](./index.md)`? = null, grad_chain: `[`SyftTensor`](./index.md)`? = null, tags: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, description: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`)` |

### Properties

| Name | Summary |
|---|---|
| [chain](chain.md) | `val chain: `[`SyftTensor`](./index.md)`?` |
| [contents](contents.md) | `val contents: TensorData` |
| [description](description.md) | `val description: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [dtype](dtype.md) | `val dtype: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [grad_chain](grad_chain.md) | `val grad_chain: `[`SyftTensor`](./index.md)`?` |
| [id](id.md) | `var id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [shape](shape.md) | `val shape: `[`MutableList`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-list/index.html)`<`[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`>` |
| [tags](tags.md) | `val tags: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>` |

### Functions

| Name | Summary |
|---|---|
| [getTorchTensor](get-torch-tensor.md) | `fun getTorchTensor(): Tensor` |
| [serialize](serialize.md) | `fun serialize(): TorchTensor` |
