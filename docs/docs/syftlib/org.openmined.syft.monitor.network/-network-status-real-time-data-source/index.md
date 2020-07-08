[syftlib](../../index.md) / [org.openmined.syft.monitor.network](../index.md) / [NetworkStatusRealTimeDataSource](./index.md)

# NetworkStatusRealTimeDataSource

`@ExperimentalUnsignedTypes class NetworkStatusRealTimeDataSource : `[`BroadCastListener`](../../org.openmined.syft.monitor/-broad-cast-listener/index.md)

### Functions

| Name | Summary |
|---|---|
| [getNetworkValidity](get-network-validity.md) | `fun getNetworkValidity(constraints: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`>): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [subscribeStateChange](subscribe-state-change.md) | `fun subscribeStateChange(): Flowable<`[`StateChangeMessage`](../../org.openmined.syft.monitor/-state-change-message/index.md)`>` |
| [unsubscribeStateChange](unsubscribe-state-change.md) | `fun unsubscribeStateChange(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [updateDownloadSpeed](update-download-speed.md) | `fun updateDownloadSpeed(workerId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, networkStatusModel: `[`NetworkStatusModel`](../-network-status-model/index.md)`): Completable` |
| [updatePing](update-ping.md) | `fun updatePing(workerId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, networkStatusModel: `[`NetworkStatusModel`](../-network-status-model/index.md)`): Completable` |
| [updateUploadSpeed](update-upload-speed.md) | `fun updateUploadSpeed(workerId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, networkStatusModel: `[`NetworkStatusModel`](../-network-status-model/index.md)`): Completable` |

### Companion Object Functions

| Name | Summary |
|---|---|
| [initialize](initialize.md) | `fun initialize(configuration: `[`SyftConfiguration`](../../org.openmined.syft.domain/-syft-configuration/index.md)`): `[`NetworkStatusRealTimeDataSource`](./index.md) |
