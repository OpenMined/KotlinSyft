[syft](../../index.md) / [org.openmined.syft.data](../index.md) / [Dataset](./index.md)

# Dataset

`interface Dataset`

### Properties

| Name | Summary |
|---|---|
| [length](length.md) | This method is called to return the size of the dataset, needs to be overridden.`abstract val length: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |

### Functions

| Name | Summary |
|---|---|
| [getItem](get-item.md) | This method is called to fetch a data sample for a given key.`abstract fun getItem(index: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<IValue>` |
