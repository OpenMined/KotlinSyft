[syftlib](../../index.md) / [org.openmined.syft.networking.requests](../index.md) / [ResponseMessageTypes](./index.md)

# ResponseMessageTypes

`interface ResponseMessageTypes : `[`MessageTypes`](../-message-types/index.md)

### Properties

| [jsonParser](json-parser.md) | `abstract val jsonParser: Json` |

### Functions

| [parseJson](parse-json.md) | `abstract fun parseJson(jsonString: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`NetworkModels`](../../org.openmined.syft.networking.datamodels/-network-models/index.md) |
| [serialize](serialize.md) | `abstract fun serialize(obj: `[`NetworkModels`](../../org.openmined.syft.networking.datamodels/-network-models/index.md)`): JsonElement` |

### Inheritors

| [REQUESTS](../-r-e-q-u-e-s-t-s/index.md) | `enum class REQUESTS : `[`ResponseMessageTypes`](./index.md) |

