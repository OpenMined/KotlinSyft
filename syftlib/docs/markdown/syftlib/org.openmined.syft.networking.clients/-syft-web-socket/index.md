[syftlib](../../index.md) / [org.openmined.syft.networking.clients](../index.md) / [SyftWebSocket](./index.md)

# SyftWebSocket

`@ExperimentalUnsignedTypes class SyftWebSocket`

SyftWebSocket initialize and configure Web Socket connection

### Parameters

`protocol` - {@link org.openmined.syft.networking.requests.NetworkingProtocol NetworkingProtocol} to be used

`address` - Address to connect

`keepAliveTimeout` - Timeout period

### Types

| [SyftSocketListener](-syft-socket-listener/index.md) | Override WebSocketListener life cycle methods`inner class SyftSocketListener : WebSocketListener` |

### Constructors

| [&lt;init&gt;](-init-.md) | SyftWebSocket initialize and configure Web Socket connection`SyftWebSocket(protocol: `[`NetworkingProtocol`](../../org.openmined.syft.networking.requests/-networking-protocol/index.md)`, address: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, keepAliveTimeout: `[`UInt`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-u-int/index.html)`)` |

### Functions

| [close](close.md) | Close web socket connection`fun close(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [send](send.md) | Send the data over the Socket connection to PyGrid`fun send(message: JsonObject): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [start](start.md) | connect socket to PyGrid, manage back-pressure with emitting messages`fun start(): Flowable<`[`NetworkMessage`](../-network-message/index.md)`>` |

