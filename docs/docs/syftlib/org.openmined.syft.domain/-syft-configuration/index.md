[syftlib](../../index.md) / [org.openmined.syft.domain](../index.md) / [SyftConfiguration](./index.md)

# SyftConfiguration

`@ExperimentalUnsignedTypes class SyftConfiguration`

### Types

| Name | Summary |
|---|---|
| [NetworkingClients](-networking-clients/index.md) | `enum class NetworkingClients` |
| [SyftConfigBuilder](-syft-config-builder/index.md) | `class SyftConfigBuilder` |

### Properties

| Name | Summary |
|---|---|
| [batteryCheckEnabled](battery-check-enabled.md) | `val batteryCheckEnabled: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [cacheTimeOut](cache-time-out.md) | `val cacheTimeOut: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |
| [computeSchedulers](compute-schedulers.md) | `val computeSchedulers: `[`ProcessSchedulers`](../../org.openmined.syft.threading/-process-schedulers/index.md) |
| [context](context.md) | `val context: `[`Context`](https://developer.android.com/reference/android/content/Context.html) |
| [filesDir](files-dir.md) | `val filesDir: `[`File`](https://docs.oracle.com/javase/6/docs/api/java/io/File.html) |
| [maxConcurrentJobs](max-concurrent-jobs.md) | `val maxConcurrentJobs: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [monitorDevice](monitor-device.md) | `val monitorDevice: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [networkConstraints](network-constraints.md) | `val networkConstraints: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`>` |
| [networkingSchedulers](networking-schedulers.md) | `val networkingSchedulers: `[`ProcessSchedulers`](../../org.openmined.syft.threading/-process-schedulers/index.md) |
| [transportMedium](transport-medium.md) | `val transportMedium: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |

### Functions

| Name | Summary |
|---|---|
| [getDownloader](get-downloader.md) | `fun getDownloader(): `[`HttpAPI`](../../org.openmined.syft.networking.requests/-http-a-p-i/index.md) |
| [getSignallingClient](get-signalling-client.md) | `fun getSignallingClient(): `[`CommunicationAPI`](../../org.openmined.syft.networking.requests/-communication-a-p-i/index.md) |
| [getWebRTCSignallingClient](get-web-r-t-c-signalling-client.md) | `fun getWebRTCSignallingClient(): `[`SocketClient`](../../org.openmined.syft.networking.clients/-socket-client/index.md) |

### Companion Object Functions

| Name | Summary |
|---|---|
| [builder](builder.md) | `fun builder(context: `[`Context`](https://developer.android.com/reference/android/content/Context.html)`, baseUrl: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): SyftConfigBuilder` |
