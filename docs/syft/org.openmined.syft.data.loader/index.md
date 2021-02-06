[syft](../index.md) / [org.openmined.syft.data.loader](./index.md)

## Package org.openmined.syft.data.loader

### Types

| Name | Summary |
|---|---|
| [AbstractDataLoader](-abstract-data-loader/index.md) | `abstract class AbstractDataLoader : `[`DataLoader`](-data-loader/index.md) |
| [DataLoader](-data-loader/index.md) | `interface DataLoader : `[`Iterable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-iterable/index.html)`<`[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<IValue>>` |
| [DataLoaderIterator](-data-loader-iterator/index.md) | `class DataLoaderIterator : `[`Iterator`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-iterator/index.html)`<`[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<IValue>>` |
| [SyftDataLoader](-syft-data-loader/index.md) | Data loader. Combines a dataset and a sampler, and provides an iterable over the given dataset. It supports map-style datasets with single-process loading and customizing loading order.`class SyftDataLoader : `[`AbstractDataLoader`](-abstract-data-loader/index.md) |
