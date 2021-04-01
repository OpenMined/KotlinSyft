[syft](../../index.md) / [org.openmined.syft.data.loader](../index.md) / [AbstractDataLoader](./index.md)

# AbstractDataLoader

`abstract class AbstractDataLoader : `[`DataLoader`](../-data-loader/index.md)

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `AbstractDataLoader(dataset: `[`Dataset`](../../org.openmined.syft.data/-dataset/index.md)`, sampler: `[`Sampler`](../../org.openmined.syft.data.samplers/-sampler/index.md)` = SequentialSampler(dataset), batchSize: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 1, dropLast: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false)` |

### Properties

| Name | Summary |
|---|---|
| [indexSampler](index-sampler.md) | `val indexSampler: `[`BatchSampler`](../../org.openmined.syft.data.samplers/-batch-sampler/index.md) |

### Functions

| Name | Summary |
|---|---|
| [fetch](fetch.md) | `abstract fun fetch(indices: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`>): `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<IValue>` |

### Inheritors

| Name | Summary |
|---|---|
| [SyftDataLoader](../-syft-data-loader/index.md) | Data loader. Combines a dataset and a sampler, and provides an iterable over the given dataset. It supports map-style datasets with single-process loading and customizing loading order.`class SyftDataLoader : `[`AbstractDataLoader`](./index.md) |
