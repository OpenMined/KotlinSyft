[syftlib](../../index.md) / [org.openmined.syft.monitor](../index.md) / [BroadCastListener](./index.md)

# BroadCastListener

`interface BroadCastListener`

### Functions

| Name | Summary |
|---|---|
| [subscribeStateChange](subscribe-state-change.md) | `abstract fun subscribeStateChange(): Flowable<`[`StateChangeMessage`](../-state-change-message/index.md)`>` |
| [unsubscribeStateChange](unsubscribe-state-change.md) | `abstract fun unsubscribeStateChange(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

### Inheritors

| Name | Summary |
|---|---|
| [BatteryStatusDataSource](../../org.openmined.syft.monitor.battery/-battery-status-data-source/index.md) | `class BatteryStatusDataSource : `[`BroadCastListener`](./index.md) |
| [BatteryStatusRepository](../../org.openmined.syft.monitor.battery/-battery-status-repository/index.md) | `class BatteryStatusRepository : `[`BroadCastListener`](./index.md) |
| [NetworkStatusRealTimeDataSource](../../org.openmined.syft.monitor.network/-network-status-real-time-data-source/index.md) | `class NetworkStatusRealTimeDataSource : `[`BroadCastListener`](./index.md) |
| [NetworkStatusRepository](../../org.openmined.syft.monitor.network/-network-status-repository/index.md) | `class NetworkStatusRepository : `[`BroadCastListener`](./index.md) |
