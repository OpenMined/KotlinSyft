[syftlib](../../index.md) / [org.openmined.syft.networking.requests](../index.md) / [HttpAPI](index.md) / [getCycle](./get-cycle.md)

# getCycle

`@POST("federated/cycle-request") abstract fun getCycle(@Body cycleRequest: `[`CycleRequest`](../../org.openmined.syft.networking.datamodels.syft/-cycle-request/index.md)`): Single<`[`CycleResponseData`](../../org.openmined.syft.networking.datamodels.syft/-cycle-response-data/index.md)`>`

Calls **federated/cycle-request** for requesting PyGrid server for training cycle.
Response of server can be CycleAccept or CycleReject

### Parameters

`cycleRequest` - @see org.openmined.syft.networking.datamodels.syft.CycleRequest

**See Also**

[CycleResponseData.CycleAccept](../../org.openmined.syft.networking.datamodels.syft/-cycle-response-data/-cycle-accept/index.md)

[CycleResponseData.CycleReject](../../org.openmined.syft.networking.datamodels.syft/-cycle-response-data/-cycle-reject/index.md)

