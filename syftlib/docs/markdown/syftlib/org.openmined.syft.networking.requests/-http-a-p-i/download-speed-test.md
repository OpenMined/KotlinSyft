[syftlib](../../index.md) / [org.openmined.syft.networking.requests](../index.md) / [HttpAPI](index.md) / [downloadSpeedTest](./download-speed-test.md)

# downloadSpeedTest

`@Streaming @GET("federated/speed-test") abstract fun downloadSpeedTest(@Query("worker_id") workerId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, @Query("random") random: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): Single<Response<ResponseBody>>`

Check download speed from PyGrid Server.

### Parameters

`workerId` - Id of syft worker handling this job.

`random` - A random integer bit stream.