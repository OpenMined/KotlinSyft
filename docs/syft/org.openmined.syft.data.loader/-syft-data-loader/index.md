[syft](../../index.md) / [org.openmined.syft.data.loader](../index.md) / [SyftDataLoader](./index.md)

# SyftDataLoader

`class SyftDataLoader : `[`AbstractDataLoader`](../-abstract-data-loader/index.md)

Data loader. Combines a dataset and a sampler, and provides an iterable over
the given dataset. It supports map-style datasets with single-process loading
and customizing loading order.

### Parameters

`dataset` - (Dataset)from which to load the data.

`batchSize` - (Int, optional): how many samples per batch to load (default: `1`).

`sampler` - (Boolean, optional) inject a sampler for the dataset

`dropLast` - (Boolean, optional): set to `True` to drop the last incomplete batch,
    if the dataset size is not divisible by the batch size. If `False` and
    the size of dataset is not divisible by the batch size, then the last batch
    will be smaller. (default: `False`)

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | Data loader. Combines a dataset and a sampler, and provides an iterable over the given dataset. It supports map-style datasets with single-process loading and customizing loading order.`SyftDataLoader(dataset: `[`Dataset`](../../org.openmined.syft.data/-dataset/index.md)`, sampler: `[`Sampler`](../../org.openmined.syft.data.samplers/-sampler/index.md)` = SequentialSampler(dataset), batchSize: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 1, dropLast: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false)` |

### Properties

| Name | Summary |
|---|---|
| [batchSize](batch-size.md) | (Int, optional): how many samples per batch to load (default: `1`).`var batchSize: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [dataLoaderIterator](data-loader-iterator.md) | `val dataLoaderIterator: `[`DataLoaderIterator`](../-data-loader-iterator/index.md) |
| [dataset](dataset.md) | (Dataset)from which to load the data.`var dataset: `[`Dataset`](../../org.openmined.syft.data/-dataset/index.md) |
| [dropLast](drop-last.md) | (Boolean, optional): set to `True` to drop the last incomplete batch,     if the dataset size is not divisible by the batch size. If `False` and     the size of dataset is not divisible by the batch size, then the last batch     will be smaller. (default: `False`)`var dropLast: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [sampler](sampler.md) | (Boolean, optional) inject a sampler for the dataset`var sampler: `[`Sampler`](../../org.openmined.syft.data.samplers/-sampler/index.md) |

### Functions

| Name | Summary |
|---|---|
| [fetch](fetch.md) | `fun fetch(indices: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`>): `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<IValue>` |
| [iterator](iterator.md) | `fun iterator(): `[`Iterator`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-iterator/index.html)`<`[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<IValue>>` |
| [reset](reset.md) | `fun reset(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
