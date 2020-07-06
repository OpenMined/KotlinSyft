[syftlib](../../index.md) / [org.openmined.syft.networking.requests](../index.md) / [CommunicationAPI](./index.md)

# CommunicationAPI

`interface CommunicationAPI`

### Functions

| [authenticate](authenticate.md) | `abstract fun authenticate(authRequest: `[`AuthenticationRequest`](../../org.openmined.syft.networking.datamodels.syft/-authentication-request/index.md)`): Single<`[`AuthenticationResponse`](../../org.openmined.syft.networking.datamodels.syft/-authentication-response/index.md)`>` |
| [getCycle](get-cycle.md) | `abstract fun getCycle(cycleRequest: `[`CycleRequest`](../../org.openmined.syft.networking.datamodels.syft/-cycle-request/index.md)`): Single<`[`CycleResponseData`](../../org.openmined.syft.networking.datamodels.syft/-cycle-response-data/index.md)`>` |
| [report](report.md) | `abstract fun report(reportRequest: `[`ReportRequest`](../../org.openmined.syft.networking.datamodels.syft/-report-request/index.md)`): Single<`[`ReportResponse`](../../org.openmined.syft.networking.datamodels.syft/-report-response/index.md)`>` |

### Inheritors

| [HttpAPI](../-http-a-p-i/index.md) | HttpAPI interface is used to implement http API service to PyGrid Server.`interface HttpAPI : `[`CommunicationAPI`](./index.md) |
| [SocketAPI](../-socket-a-p-i/index.md) | Represent WebRTC connection API`interface SocketAPI : `[`CommunicationAPI`](./index.md) |

