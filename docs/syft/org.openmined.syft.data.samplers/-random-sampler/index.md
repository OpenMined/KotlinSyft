[syft](../../index.md) / [org.openmined.syft.data.samplers](../index.md) / [RandomSampler](./index.md)

# RandomSampler

`class RandomSampler : `[`Sampler`](../-sampler/index.md)

Samples elements randomly. If without replacement, then sample from a shuffled dataset.
If with replacement, then user can specify :attr:`num_samples` to draw.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | Samples elements randomly. If without replacement, then sample from a shuffled dataset. If with replacement, then user can specify :attr:`num_samples` to draw.`RandomSampler(dataset: `[`Dataset`](../../org.openmined.syft.data/-dataset/index.md)`)` |

### Properties

| Name | Summary |
|---|---|
| [indices](indices.md) | `val indices: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`>` |
| [length](length.md) | `val length: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
