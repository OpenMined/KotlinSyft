[syftlib](../../index.md) / [org.openmined.syft.networking.requests](../index.md) / [HttpAPI](index.md) / [downloadProtocol](./download-protocol.md)

# downloadProtocol

`@GET("/federated/get-protocol") abstract fun downloadProtocol(@Query("worker_id") workerId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, @Query("request_key") requestKey: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, @Query("protocol_id") protocolId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): Single<Response<ResponseBody>>`

Downloads Protocols from PyGrid Server.

### Parameters

`workerId` - Id of syft worker handling this job.

`requestKey` - A unique key required for authorised communication with PyGrid server.
It ensures that only workers accepted for a cycle can receive Protocol data from server.

`protocolId` - Id of the Protocol to be downloaded.