[syftlib](../../index.md) / [org.openmined.syft.networking.requests](../index.md) / [HttpAPI](index.md) / [authenticate](./authenticate.md)

# authenticate

`@GET("federated/authenticate") abstract fun authenticate(authRequest: `[`AuthenticationRequest`](../../org.openmined.syft.networking.datamodels.syft/-authentication-request/index.md)`): Single<`[`AuthenticationResponse`](../../org.openmined.syft.networking.datamodels.syft/-authentication-response/index.md)`>`

Calls **federated/authenticate** for authentication.

### Parameters

`authRequest` - Contains JWT auth-token. JWT authentication protects the model from sybil attacks.