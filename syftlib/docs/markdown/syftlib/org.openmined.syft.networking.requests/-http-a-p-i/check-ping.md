[syftlib](../../index.md) / [org.openmined.syft.networking.requests](../index.md) / [HttpAPI](index.md) / [checkPing](./check-ping.md)

# checkPing

`@GET("federated/speed-test") abstract fun checkPing(@Query("is_ping") isPing: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 1, @Query("worker_id") workerId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, @Query("random") random: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): Single<Response<`[`SpeedCheckResponse`](../../org.openmined.syft.networking.datamodels.syft/-speed-check-response/index.md)`>>`

Check connection speed by ping to PyGrid Server.

### Parameters

`isPing` - Allow PyGrid to differentiate between CheckPing request vs DownloadSpeedTest request.
In case of CheckPing it is always true.

`workerId` - Id of syft worker handling this job.

`random` - A random integer bit stream.