[syft](../../index.md) / [org.openmined.syft.data.samplers](../index.md) / [Sampler](./index.md)

# Sampler

`interface Sampler`

Base class for all Samplers.
Every Sampler subclass has to provide an :method:`indices` method, providing a
way to iterate over indices of dataset elements, and a :method:`length` method
that returns the length of the returned iterators.

### Properties

| Name | Summary |
|---|---|
| [indices](indices.md) | `abstract val indices: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`>` |
| [length](length.md) | `abstract val length: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |

### Inheritors

| Name | Summary |
|---|---|
| [BatchSampler](../-batch-sampler/index.md) | Wraps another sampler to yield a mini-batch of indices.`class BatchSampler : `[`Sampler`](./index.md) |
| [RandomSampler](../-random-sampler/index.md) | Samples elements randomly. If without replacement, then sample from a shuffled dataset. If with replacement, then user can specify :attr:`num_samples` to draw.`class RandomSampler : `[`Sampler`](./index.md) |
| [SequentialSampler](../-sequential-sampler/index.md) | Samples elements sequentially, always in the same order.`class SequentialSampler : `[`Sampler`](./index.md) |
