[syftlib](../../index.md) / [org.openmined.syft.networking.requests](../index.md) / [HttpAPI](index.md) / [downloadPlan](./download-plan.md)

# downloadPlan

`@GET("/federated/get-plan") abstract fun downloadPlan(@Query("worker_id") workerId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, @Query("request_key") requestKey: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, @Query("plan_id") planId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, @Query("receive_operations_as") op_type: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): Single<Response<ResponseBody>>`

Download Plans from PyGrid Server.

### Parameters

`workerId` - Id of syft worker handling this job.

`requestKey` - A unique key required for authorised communication with PyGrid server.
It ensures that only workers accepted for a cycle can receive Plan data from server.

`planId` - Id of the Plan to be downloaded.

`op_type` - Format in which Plan operations are defined, Can be torchScript.