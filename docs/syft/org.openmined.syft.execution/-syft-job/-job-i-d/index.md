[syftlib](../../../index.md) / [org.openmined.syft.execution](../../index.md) / [SyftJob](../index.md) / [JobID](./index.md)

# JobID

`data class JobID`

A uniquer identifier class for the job

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | A uniquer identifier class for the job`JobID(modelName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, version: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null)` |

### Properties

| Name | Summary |
|---|---|
| [modelName](model-name.md) | The name of the model used in the job while querying PyGrid`val modelName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [version](version.md) | The model version in PyGrid`val version: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |

### Functions

| Name | Summary |
|---|---|
| [matchWithResponse](match-with-response.md) | Check if two [JobID](./index.md) are same. Matches both model names and version if [version](match-with-response.md#org.openmined.syft.execution.SyftJob.JobID$matchWithResponse(kotlin.String, kotlin.String)/version) is not null for param and current jobId.`fun matchWithResponse(modelName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, version: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
