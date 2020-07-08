[syftlib](../../index.md) / [org.openmined.syft.proto](../index.md) / [SyftModel](index.md) / [createDiff](./create-diff.md)

# createDiff

`fun createDiff(oldSyftState: `[`SyftState`](../-syft-state/index.md)`, diffScriptLocation: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`SyftState`](../-syft-state/index.md)

Subtract the older state from the current state to generate the diff for Upload to PyGrid

### Parameters

`oldSyftState` - The state with respect to which the diff will be generated

`diffScriptLocation` - The location of the torchscript for performing the subtraction

### Exceptions

`IllegalArgumentException` - if model params are not downloaded yet.

**See Also**

[SyftState.createDiff](../-syft-state/create-diff.md)

