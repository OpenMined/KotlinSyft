[syftlib](../../index.md) / [org.openmined.syft.monitor](../index.md) / [DeviceMonitor](./index.md)

# DeviceMonitor

`@ExperimentalUnsignedTypes class DeviceMonitor : Disposable`

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `DeviceMonitor(networkStatusRepository: `[`NetworkStatusRepository`](../../org.openmined.syft.monitor.network/-network-status-repository/index.md)`, batteryStatusRepository: `[`BatteryStatusRepository`](../../org.openmined.syft.monitor.battery/-battery-status-repository/index.md)`, processSchedulers: `[`ProcessSchedulers`](../../org.openmined.syft.threading/-process-schedulers/index.md)`, subscribe: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`)` |

### Functions

| Name | Summary |
|---|---|
| [dispose](dispose.md) | `fun dispose(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [getBatteryStatus](get-battery-status.md) | `fun getBatteryStatus(): `[`BatteryStatusModel`](../../org.openmined.syft.monitor.battery/-battery-status-model/index.md) |
| [getNetworkStatus](get-network-status.md) | `fun getNetworkStatus(workerId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, requiresSpeedTest: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): Single<`[`NetworkStatusModel`](../../org.openmined.syft.monitor.network/-network-status-model/index.md)`>` |
| [isActivityStateValid](is-activity-state-valid.md) | `fun isActivityStateValid(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [isBatteryStateValid](is-battery-state-valid.md) | `fun isBatteryStateValid(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [isDisposed](is-disposed.md) | `fun isDisposed(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [isNetworkStateValid](is-network-state-valid.md) | `fun isNetworkStateValid(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |

### Companion Object Functions

| Name | Summary |
|---|---|
| [construct](construct.md) | `fun construct(syftConfig: `[`SyftConfiguration`](../../org.openmined.syft.domain/-syft-configuration/index.md)`): `[`DeviceMonitor`](./index.md) |
