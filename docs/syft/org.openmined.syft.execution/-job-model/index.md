[syft](../../index.md) / [org.openmined.syft.execution](../index.md) / [JobModel](./index.md)

# JobModel

`@ExperimentalUnsignedTypes data class JobModel`

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `JobModel(modelName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, version: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, plans: `[`ConcurrentHashMap`](https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/ConcurrentHashMap.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`Plan`](../-plan/index.md)`>, protocols: `[`ConcurrentHashMap`](https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/ConcurrentHashMap.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`Protocol`](../-protocol/index.md)`>, requiresSpeedTest: `[`AtomicBoolean`](https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/atomic/AtomicBoolean.html)`, isDisposed: `[`AtomicBoolean`](https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/atomic/AtomicBoolean.html)`, cycleStatus: `[`AtomicReference`](https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/atomic/AtomicReference.html)`<`[`CycleStatus`](../-cycle-status/index.md)`>)` |

### Properties

| Name | Summary |
|---|---|
| [cycleStatus](cycle-status.md) | `val cycleStatus: `[`AtomicReference`](https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/atomic/AtomicReference.html)`<`[`CycleStatus`](../-cycle-status/index.md)`>` |
| [id](id.md) | `val id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [isDisposed](is-disposed.md) | `val isDisposed: `[`AtomicBoolean`](https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/atomic/AtomicBoolean.html) |
| [modelName](model-name.md) | `val modelName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [plans](plans.md) | `val plans: `[`ConcurrentHashMap`](https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/ConcurrentHashMap.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`Plan`](../-plan/index.md)`>` |
| [protocols](protocols.md) | `val protocols: `[`ConcurrentHashMap`](https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/ConcurrentHashMap.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`Protocol`](../-protocol/index.md)`>` |
| [requiresSpeedTest](requires-speed-test.md) | `val requiresSpeedTest: `[`AtomicBoolean`](https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/atomic/AtomicBoolean.html) |
| [version](version.md) | `val version: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
