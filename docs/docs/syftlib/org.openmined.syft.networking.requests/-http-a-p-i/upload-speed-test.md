[syftlib](../../index.md) / [org.openmined.syft.networking.requests](../index.md) / [HttpAPI](index.md) / [uploadSpeedTest](./upload-speed-test.md)

# uploadSpeedTest

`@Multipart @POST("federated/speed-test") abstract fun uploadSpeedTest(@Query("worker_id") workerId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, @Query("random") random: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, @Part("description") description: RequestBody, @Part file_body: Part): Single<Response<`[`SpeedCheckResponse`](../../org.openmined.syft.networking.datamodels.syft/-speed-check-response/index.md)`>>`

Check upload speed to PyGrid Server by uploading a file using multipart post request.

### Parameters

`workerId` - Id of syft worker handling this job.

`random` - A random integer bit stream.

`description` - Meta-data for upload process.

`file_body` - A file to be uploaded to check upload speed.