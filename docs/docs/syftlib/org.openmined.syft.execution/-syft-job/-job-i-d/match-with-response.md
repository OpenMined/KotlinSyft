[syftlib](../../../index.md) / [org.openmined.syft.execution](../../index.md) / [SyftJob](../index.md) / [JobID](index.md) / [matchWithResponse](./match-with-response.md)

# matchWithResponse

`fun matchWithResponse(modelName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, version: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)

Check if two [JobID](index.md) are same. Matches both model names and version if [version](match-with-response.md#org.openmined.syft.execution.SyftJob.JobID$matchWithResponse(kotlin.String, kotlin.String)/version) is not null for param and current jobId.

### Parameters

`modelName` - the modelName of the jobId which has to be compared with the current object

`version` - the version of the jobID which ahs to be compared with the current jobId

**Return**
true if JobId match

**Return**
false otherwise

