[syft](../../index.md) / [org.openmined.syft](../index.md) / [Syft](index.md) / [dispose](./dispose.md)

# dispose

`fun dispose(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Explicitly dispose off the worker. All the jobs running in the worker will be disposed off as well.
Clears the current singleton worker instance so the immediately next [getInstance](get-instance.md) call creates a new syft worker

