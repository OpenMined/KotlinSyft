[syftlib](../../index.md) / [org.openmined.syft.proto](../index.md) / [SyftState](index.md) / [iValueTensors](./i-value-tensors.md)

# iValueTensors

`val iValueTensors: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<IValue>`

the IValue tensors for the model params

``` kotlin
modelSyftState?.updateState(newModelParams.toTypedArray())
```

``` kotlin
modelSyftState = SyftState.loadSyftState(modelFile)
Log.d(TAG, "Model loaded from $modelFile")
```

### Property

`iValueTensors` - the IValue tensors for the model params