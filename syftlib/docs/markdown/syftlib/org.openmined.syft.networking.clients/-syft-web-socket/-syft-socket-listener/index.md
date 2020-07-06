[syftlib](../../../index.md) / [org.openmined.syft.networking.clients](../../index.md) / [SyftWebSocket](../index.md) / [SyftSocketListener](./index.md)

# SyftSocketListener

`inner class SyftSocketListener : WebSocketListener`

Override WebSocketListener life cycle methods

### Constructors

| [&lt;init&gt;](-init-.md) | Override WebSocketListener life cycle methods`SyftSocketListener()` |

### Functions

| [onFailure](on-failure.md) | Handle socket failure and notify subscribers. And try to reconnect`fun onFailure(webSocket: WebSocket, t: `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`, response: Response?): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [onMessage](on-message.md) | Message received and emit the message to the subscribers`fun onMessage(webSocket: WebSocket, text: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [onOpen](on-open.md) | Connection accepted by PyGrid and notify subscribers`fun onOpen(webSocket: WebSocket, response: Response): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

