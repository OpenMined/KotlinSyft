[syft](../../index.md) / [org.openmined.syft.data.loader](../index.md) / [SyftDataLoader](index.md) / [dropLast](./drop-last.md)

# dropLast

`var dropLast: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)

(Boolean, optional): set to `True` to drop the last incomplete batch,
    if the dataset size is not divisible by the batch size. If `False` and
    the size of dataset is not divisible by the batch size, then the last batch
    will be smaller. (default: `False`)

