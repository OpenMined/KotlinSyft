[syftlib](../index.md) / [org.openmined.syft.networking.requests](./index.md)

## Package org.openmined.syft.networking.requests

### Types

| Name | Summary |
|---|---|
| [CommunicationAPI](-communication-a-p-i/index.md) | `interface CommunicationAPI` |
| [DOWNLOAD](-d-o-w-n-l-o-a-d/index.md) | `enum class DOWNLOAD : `[`MessageTypes`](-message-types/index.md) |
| [HttpAPI](-http-a-p-i/index.md) | HttpAPI interface is used to implement http API service to PyGrid Server.`interface HttpAPI : `[`CommunicationAPI`](-communication-a-p-i/index.md) |
| [MessageTypes](-message-types/index.md) | `interface MessageTypes` |
| [NetworkingProtocol](-networking-protocol/index.md) | `sealed class NetworkingProtocol` |
| [REQUESTS](-r-e-q-u-e-s-t-s/index.md) | `enum class REQUESTS : `[`ResponseMessageTypes`](-response-message-types/index.md) |
| [ResponseMessageTypes](-response-message-types/index.md) | `interface ResponseMessageTypes : `[`MessageTypes`](-message-types/index.md) |
| [SocketAPI](-socket-a-p-i/index.md) | Represent WebRTC connection API`interface SocketAPI : `[`CommunicationAPI`](-communication-a-p-i/index.md) |
| [WebRTCMessageTypes](-web-r-t-c-message-types/index.md) | `enum class WebRTCMessageTypes : `[`MessageTypes`](-message-types/index.md) |
