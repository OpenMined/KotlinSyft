[syftlib](../../index.md) / [org.openmined.syft.networking.datamodels](../index.md) / [ClientConfig](./index.md)

# ClientConfig

`data class ClientConfig`

All the user defined parameters will be serialised and sent by the PyGrid in the form of [ClientConfig](./index.md)

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | All the user defined parameters will be serialised and sent by the PyGrid in the form of [ClientConfig](./index.md)`ClientConfig(properties: `[`ClientProperties`](../-client-properties/index.md)`, planArgs: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>)` |

### Properties

| Name | Summary |
|---|---|
| [planArgs](plan-args.md) | A [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html) containing the keys and values of the hyper parameters of the model. All the values are serialized as string and the user must deserialize them at runtime.`val planArgs: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>` |
| [properties](properties.md) | Contains job specific descriptions. See [ClientProperties](../-client-properties/index.md)`val properties: `[`ClientProperties`](../-client-properties/index.md) |
