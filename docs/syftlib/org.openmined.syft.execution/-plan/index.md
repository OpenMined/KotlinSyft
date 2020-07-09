[syftlib](../../index.md) / [org.openmined.syft.execution](../index.md) / [Plan](./index.md)

# Plan

`@ExperimentalUnsignedTypes class Plan`

The Plan Class contains functions to load a PyTorch model from a TorchScript and
to run training through the forward function of the PyTorch Module.
A PyTorch Module is simply a container that takes in tensors as input and returns
tensor after doing some computation.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | The Plan Class contains functions to load a PyTorch model from a TorchScript and to run training through the forward function of the PyTorch Module. A PyTorch Module is simply a container that takes in tensors as input and returns tensor after doing some computation.`Plan(job: `[`SyftJob`](../-syft-job/index.md)`, planId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`)` |

### Properties

| Name | Summary |
|---|---|
| [job](job.md) | is the job hosting this plan`val job: `[`SyftJob`](../-syft-job/index.md) |
| [planId](plan-id.md) | is the unique id allotted to the plan by PyGrid`val planId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |

### Functions

| Name | Summary |
|---|---|
| [execute](execute.md) | Loads a serialized TorchScript module from the specified path on the disk.`fun execute(vararg iValues: IValue): IValue?` |
| [loadScriptModule](load-script-module.md) | Loads a TorchScript module from the specified path on the disk.`fun loadScriptModule(torchScriptLocation: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
