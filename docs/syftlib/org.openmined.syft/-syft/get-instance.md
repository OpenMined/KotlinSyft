[syftlib](../../index.md) / [org.openmined.syft](../index.md) / [Syft](index.md) / [getInstance](./get-instance.md)

# getInstance

`fun getInstance(syftConfiguration: `[`SyftConfiguration`](../../org.openmined.syft.domain/-syft-configuration/index.md)`, authToken: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null): `[`Syft`](index.md)

Only a single worker must be instantiated across an app lifecycle.
The [getInstance](./get-instance.md) ensures creation of the singleton object if needed or returns the already created worker.
This method is thread safe so getInstance calls across threads do not suffer

### Parameters

`syftConfiguration` - The SyftConfiguration object specifying the mutable properties of syft worker

`authToken` - (Optional) The JWT token to be passed by the user

**Return**
Syft instance

