[syftlib](../../index.md) / [org.openmined.syft.monitor.network](../index.md) / [NetworkStatusRepository](./index.md)

# NetworkStatusRepository

`@ExperimentalUnsignedTypes class NetworkStatusRepository : `[`BroadCastListener`](../../org.openmined.syft.monitor/-broad-cast-listener/index.md)

### Functions

| [getNetworkStatus](get-network-status.md) | `fun getNetworkStatus(workerId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, requiresSpeedTest: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): Single<`[`NetworkStatusModel`](../-network-status-model/index.md)`>` |
| [getNetworkValidity](get-network-validity.md) | `fun getNetworkValidity(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [subscribeStateChange](subscribe-state-change.md) | `fun subscribeStateChange(): Flowable<`[`StateChangeMessage`](../../org.openmined.syft.monitor/-state-change-message/index.md)`>` |
| [unsubscribeStateChange](unsubscribe-state-change.md) | `fun unsubscribeStateChange(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

### Companion Object Functions

| [initialize](initialize.md) | `fun initialize(configuration: `[`SyftConfiguration`](../../org.openmined.syft.domain/-syft-configuration/index.md)`): `[`NetworkStatusRepository`](./index.md) |

