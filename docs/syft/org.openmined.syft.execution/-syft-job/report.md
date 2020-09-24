[syftlib](../../index.md) / [org.openmined.syft.execution](../index.md) / [SyftJob](index.md) / [report](./report.md)

# report

`fun report(diff: `[`SyftState`](../../org.openmined.syft.proto/-syft-state/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Once training is finished submit the new model weights to PyGrid to complete the cycle

### Parameters

`diff` - the difference of the new and old model weights serialised into [State](../../org.openmined.syft.proto/-syft-state/index.md)