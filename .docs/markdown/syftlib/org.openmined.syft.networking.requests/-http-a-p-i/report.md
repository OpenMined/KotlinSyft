[syftlib](../../index.md) / [org.openmined.syft.networking.requests](../index.md) / [HttpAPI](index.md) / [report](./report.md)

# report

`@POST("federated/report") abstract fun report(@Body reportRequest: `[`ReportRequest`](../../org.openmined.syft.networking.datamodels.syft/-report-request/index.md)`): Single<`[`ReportResponse`](../../org.openmined.syft.networking.datamodels.syft/-report-response/index.md)`>`

Calls **federated/report** for sending the updated model back to PyGrid.

### Parameters

`reportRequest` - Contains worker-id and request-key.