[syftlib](../../index.md) / [org.openmined.syft.networking.clients](../index.md) / [SocketClient](./index.md)

# SocketClient

`@ExperimentalUnsignedTypes class SocketClient : `[`SocketAPI`](../../org.openmined.syft.networking.requests/-socket-a-p-i/index.md)`, Disposable`

Used to communicate and exchange data throw web socket with PyGrid

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `SocketClient(baseUrl: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, timeout: `[`UInt`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-u-int/index.html)`, schedulers: `[`ProcessSchedulers`](../../org.openmined.syft.threading/-process-schedulers/index.md)`)`<br>Used to communicate and exchange data throw web socket with PyGrid`SocketClient(syftWebSocket: `[`SyftWebSocket`](../-syft-web-socket/index.md)`, timeout: `[`UInt`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-u-int/index.html)` = 20000u, schedulers: `[`ProcessSchedulers`](../../org.openmined.syft.threading/-process-schedulers/index.md)`)` |

### Functions

| Name | Summary |
|---|---|
| [authenticate](authenticate.md) | Authenticate socket connection with PyGrid`fun authenticate(authRequest: `[`AuthenticationRequest`](../../org.openmined.syft.networking.datamodels.syft/-authentication-request/index.md)`): Single<`[`AuthenticationResponse`](../../org.openmined.syft.networking.datamodels.syft/-authentication-response/index.md)`>` |
| [dispose](dispose.md) | `fun dispose(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [getCycle](get-cycle.md) | Request or get current active federated learning cycle`fun getCycle(cycleRequest: `[`CycleRequest`](../../org.openmined.syft.networking.datamodels.syft/-cycle-request/index.md)`): Single<`[`CycleResponseData`](../../org.openmined.syft.networking.datamodels.syft/-cycle-response-data/index.md)`>` |
| [initiateNewWebSocket](initiate-new-web-socket.md) | Listen and handle socket events`fun initiateNewWebSocket(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [isDisposed](is-disposed.md) | `fun isDisposed(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [joinRoom](join-room.md) | Used by WebRTC to request PyGrid joining a FL cycle`fun joinRoom(joinRoomRequest: `[`JoinRoomRequest`](../../org.openmined.syft.networking.datamodels.web-r-t-c/-join-room-request/index.md)`): Single<`[`JoinRoomResponse`](../../org.openmined.syft.networking.datamodels.web-r-t-c/-join-room-response/index.md)`>` |
| [report](report.md) | Report model state to PyGrid after the cycle completes`fun report(reportRequest: `[`ReportRequest`](../../org.openmined.syft.networking.datamodels.syft/-report-request/index.md)`): Single<`[`ReportResponse`](../../org.openmined.syft.networking.datamodels.syft/-report-response/index.md)`>` |
| [sendInternalMessage](send-internal-message.md) | Used by WebRTC connection to send message via PyGrid`fun sendInternalMessage(internalMessageRequest: `[`InternalMessageRequest`](../../org.openmined.syft.networking.datamodels.web-r-t-c/-internal-message-request/index.md)`): Single<`[`InternalMessageResponse`](../../org.openmined.syft.networking.datamodels.web-r-t-c/-internal-message-response/index.md)`>` |
