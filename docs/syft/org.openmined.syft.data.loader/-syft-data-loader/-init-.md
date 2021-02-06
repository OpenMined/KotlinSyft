[syft](../../index.md) / [org.openmined.syft.data.loader](../index.md) / [SyftDataLoader](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`SyftDataLoader(dataset: `[`Dataset`](../../org.openmined.syft.data/-dataset/index.md)`, sampler: `[`Sampler`](../../org.openmined.syft.data.samplers/-sampler/index.md)` = SequentialSampler(dataset), batchSize: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 1, dropLast: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false)`

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