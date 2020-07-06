[syftlib](../../index.md) / [org.openmined.syft.proto](../index.md) / [SyftModel](./index.md)

# SyftModel

`@ExperimentalUnsignedTypes data class SyftModel`

SyftModel is the data model class for storing the weights of the neural network used for
training or inference.

### Constructors

| [&lt;init&gt;](-init-.md) | SyftModel is the data model class for storing the weights of the neural network used for training or inference.`SyftModel(modelName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, version: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, pyGridModelId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, modelSyftState: `[`SyftState`](../-syft-state/index.md)`? = null)` |

### Properties

| [modelName](model-name.md) | : A string to hold the name of the model specified while hosting the plan on pygrid.`val modelName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [modelSyftState](model-syft-state.md) | : Responsible for Holding the model weights of the neural network.`var modelSyftState: `[`SyftState`](../-syft-state/index.md)`?` |
| [pyGridModelId](py-grid-model-id.md) | : A unique id assigned by Pygrid to very model hosted over it. pyGridModelId is used for downloading the appropriate model files from PyGrid as an argument.`var pyGridModelId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [version](version.md) | : A string specifying the version of the model.`val version: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |

### Functions

| [createDiff](create-diff.md) | Subtract the older state from the current state to generate the diff for Upload to PyGrid`fun createDiff(oldSyftState: `[`SyftState`](../-syft-state/index.md)`, diffScriptLocation: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`SyftState`](../-syft-state/index.md) |
| [loadModelState](load-model-state.md) | This method is used to load SyftModel from file`fun loadModelState(modelFile: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [updateModel](update-model.md) | This method is used to save/update SyftModel class. This function must be called after every gradient step to update the model state for further plan executions.`fun updateModel(newModelParams: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<Tensor>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

