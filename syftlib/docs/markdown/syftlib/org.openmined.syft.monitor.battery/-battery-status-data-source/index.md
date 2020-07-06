[syftlib](../../index.md) / [org.openmined.syft.monitor.battery](../index.md) / [BatteryStatusDataSource](./index.md)

# BatteryStatusDataSource

`@ExperimentalUnsignedTypes class BatteryStatusDataSource : `[`BroadCastListener`](../../org.openmined.syft.monitor/-broad-cast-listener/index.md)

### Types

| [BatteryChangeReceiver](-battery-change-receiver/index.md) | `inner class BatteryChangeReceiver : `[`BroadcastReceiver`](https://developer.android.com/reference/android/content/BroadcastReceiver.html) |

### Constructors

| [&lt;init&gt;](-init-.md) | `BatteryStatusDataSource(context: `[`Context`](https://developer.android.com/reference/android/content/Context.html)`, batteryCheckEnabled: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`, statusProcessor: PublishProcessor<`[`StateChangeMessage`](../../org.openmined.syft.monitor/-state-change-message/index.md)`> = PublishProcessor.create())` |

### Functions

| [checkIfCharging](check-if-charging.md) | `fun checkIfCharging(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [getBatteryLevel](get-battery-level.md) | `fun getBatteryLevel(): `[`Float`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)`?` |
| [getBatteryValidity](get-battery-validity.md) | `fun getBatteryValidity(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [subscribeStateChange](subscribe-state-change.md) | `fun subscribeStateChange(): Flowable<`[`StateChangeMessage`](../../org.openmined.syft.monitor/-state-change-message/index.md)`>` |
| [unsubscribeStateChange](unsubscribe-state-change.md) | `fun unsubscribeStateChange(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

### Companion Object Functions

| [initialize](initialize.md) | `fun initialize(configuration: `[`SyftConfiguration`](../../org.openmined.syft.domain/-syft-configuration/index.md)`): `[`BatteryStatusDataSource`](./index.md) |

