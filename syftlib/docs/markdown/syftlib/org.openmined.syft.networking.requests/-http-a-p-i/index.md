[syftlib](../../index.md) / [org.openmined.syft.networking.requests](../index.md) / [HttpAPI](./index.md)

# HttpAPI

`interface HttpAPI : `[`CommunicationAPI`](../-communication-a-p-i/index.md)

HttpAPI interface is used to implement http API service to PyGrid Server.

**See Also**

[org.openmined.syft.networking.clients.HttpClient](../../org.openmined.syft.networking.clients/-http-client/index.md)

### Functions

| [authenticate](authenticate.md) | Calls **federated/authenticate** for authentication.`abstract fun authenticate(authRequest: `[`AuthenticationRequest`](../../org.openmined.syft.networking.datamodels.syft/-authentication-request/index.md)`): Single<`[`AuthenticationResponse`](../../org.openmined.syft.networking.datamodels.syft/-authentication-response/index.md)`>` |
| [checkPing](check-ping.md) | Check connection speed by ping to PyGrid Server.`abstract fun checkPing(isPing: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 1, workerId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, random: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): Single<Response<`[`SpeedCheckResponse`](../../org.openmined.syft.networking.datamodels.syft/-speed-check-response/index.md)`>>` |
| [downloadModel](download-model.md) | Download Model from PyGrid Server.`abstract fun downloadModel(workerId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, requestKey: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, modelId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): Single<Response<ResponseBody>>` |
| [downloadPlan](download-plan.md) | Download Plans from PyGrid Server.`abstract fun downloadPlan(workerId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, requestKey: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, planId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, op_type: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): Single<Response<ResponseBody>>` |
| [downloadProtocol](download-protocol.md) | Downloads Protocols from PyGrid Server.`abstract fun downloadProtocol(workerId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, requestKey: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, protocolId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): Single<Response<ResponseBody>>` |
| [downloadSpeedTest](download-speed-test.md) | Check download speed from PyGrid Server.`abstract fun downloadSpeedTest(workerId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, random: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): Single<Response<ResponseBody>>` |
| [getCycle](get-cycle.md) | Calls **federated/cycle-request** for requesting PyGrid server for training cycle. Response of server can be CycleAccept or CycleReject`abstract fun getCycle(cycleRequest: `[`CycleRequest`](../../org.openmined.syft.networking.datamodels.syft/-cycle-request/index.md)`): Single<`[`CycleResponseData`](../../org.openmined.syft.networking.datamodels.syft/-cycle-response-data/index.md)`>` |
| [report](report.md) | Calls **federated/report** for sending the updated model back to PyGrid.`abstract fun report(reportRequest: `[`ReportRequest`](../../org.openmined.syft.networking.datamodels.syft/-report-request/index.md)`): Single<`[`ReportResponse`](../../org.openmined.syft.networking.datamodels.syft/-report-response/index.md)`>` |
| [uploadSpeedTest](upload-speed-test.md) | Check upload speed to PyGrid Server by uploading a file using multipart post request.`abstract fun uploadSpeedTest(workerId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, random: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, description: RequestBody, file_body: Part): Single<Response<`[`SpeedCheckResponse`](../../org.openmined.syft.networking.datamodels.syft/-speed-check-response/index.md)`>>` |

