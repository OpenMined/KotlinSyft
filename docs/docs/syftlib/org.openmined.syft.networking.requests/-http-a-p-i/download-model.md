[syftlib](../../index.md) / [org.openmined.syft.networking.requests](../index.md) / [HttpAPI](index.md) / [downloadModel](./download-model.md)

# downloadModel

`@GET("/federated/get-model") abstract fun downloadModel(@Query("worker_id") workerId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, @Query("request_key") requestKey: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, @Query("model_id") modelId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): Single<Response<ResponseBody>>`

Download Model from PyGrid Server.

### Parameters

`workerId` - Id of syft worker handling this job.

`requestKey` - A unique key required for authorised communication with PyGrid server.
It ensures that only workers accepted for a cycle can receive Model data from server.

`modelId` - Id of the model to be downloaded.