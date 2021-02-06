[syft](../../index.md) / [org.openmined.syft.data.samplers](../index.md) / [BatchSampler](./index.md)

# BatchSampler

`class BatchSampler : `[`Sampler`](../-sampler/index.md)

Wraps another sampler to yield a mini-batch of indices.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | Wraps another sampler to yield a mini-batch of indices.`BatchSampler(indexer: `[`Sampler`](../-sampler/index.md)`, batchSize: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 1, dropLast: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false)` |

### Properties

| Name | Summary |
|---|---|
| [indices](indices.md) | `val indices: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`>` |
| [length](length.md) | `val length: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |

### Functions

| Name | Summary |
|---|---|
| [reset](reset.md) | `fun reset(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
