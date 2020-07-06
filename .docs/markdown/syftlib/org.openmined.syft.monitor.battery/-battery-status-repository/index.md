[syftlib](../../index.md) / [org.openmined.syft.monitor.battery](../index.md) / [BatteryStatusRepository](./index.md)

# BatteryStatusRepository

`@ExperimentalUnsignedTypes class BatteryStatusRepository : `[`BroadCastListener`](../../org.openmined.syft.monitor/-broad-cast-listener/index.md)

### Functions

| [getBatteryState](get-battery-state.md) | `fun getBatteryState(): `[`BatteryStatusModel`](../-battery-status-model/index.md) |
| [getBatteryValidity](get-battery-validity.md) | `fun getBatteryValidity(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [subscribeStateChange](subscribe-state-change.md) | `fun subscribeStateChange(): Flowable<`[`StateChangeMessage`](../../org.openmined.syft.monitor/-state-change-message/index.md)`>` |
| [unsubscribeStateChange](unsubscribe-state-change.md) | `fun unsubscribeStateChange(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

### Companion Object Functions

| [initialize](initialize.md) | `fun initialize(configuration: `[`SyftConfiguration`](../../org.openmined.syft.domain/-syft-configuration/index.md)`): `[`BatteryStatusRepository`](./index.md) |

