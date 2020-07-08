[syftlib](../../index.md) / [org.openmined.syft.networking.requests](../index.md) / [SocketAPI](./index.md)

# SocketAPI

`interface SocketAPI : `[`CommunicationAPI`](../-communication-a-p-i/index.md)

Represent WebRTC connection API

### Functions

| Name | Summary |
|---|---|
| [joinRoom](join-room.md) | Request joining a federated learning cycle`abstract fun joinRoom(joinRoomRequest: `[`JoinRoomRequest`](../../org.openmined.syft.networking.datamodels.web-r-t-c/-join-room-request/index.md)`): Single<`[`JoinRoomResponse`](../../org.openmined.syft.networking.datamodels.web-r-t-c/-join-room-response/index.md)`>` |
| [sendInternalMessage](send-internal-message.md) | Send message via PyGrid`abstract fun sendInternalMessage(internalMessageRequest: `[`InternalMessageRequest`](../../org.openmined.syft.networking.datamodels.web-r-t-c/-internal-message-request/index.md)`): Single<`[`InternalMessageResponse`](../../org.openmined.syft.networking.datamodels.web-r-t-c/-internal-message-response/index.md)`>` |

### Inheritors

| Name | Summary |
|---|---|
| [SocketClient](../../org.openmined.syft.networking.clients/-socket-client/index.md) | Used to communicate and exchange data throw web socket with PyGrid`class SocketClient : `[`SocketAPI`](./index.md)`, Disposable` |
