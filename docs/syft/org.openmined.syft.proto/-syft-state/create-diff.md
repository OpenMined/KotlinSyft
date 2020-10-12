[syft](../../index.md) / [org.openmined.syft.proto](../index.md) / [SyftState](index.md) / [createDiff](./create-diff.md)

# createDiff

`fun createDiff(oldSyftState: `[`SyftState`](index.md)`, diffScriptLocation: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`SyftState`](index.md)

Subtract the older state from the current state to generate the diff

### Parameters

`oldSyftState` - The state with respect to which the diff will be generated

`diffScriptLocation` - The location of the torchscript for performing the subtraction

### Exceptions

`IllegalArgumentException` - if the size newModelParams is not same.