[syftlib](../../index.md) / [org.openmined.syft.proto](../index.md) / [SyftState](index.md) / [syftTensors](./syft-tensors.md)

# syftTensors

`val syftTensors: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`SyftTensor`](../-syft-tensor/index.md)`>`

the tensors for the model params

``` kotlin
modelSyftState?.let { state ->
    if (state.syftTensors.size != newModelParams.size) {
        throw IllegalArgumentException("The size of the list of new parameters ${newModelParams.size} is different than the list of params of the model ${state.syftTensors.size}")
    }
    newModelParams.forEachIndexed { index, pytorchTensor ->
        state.syftTensors[index] = pytorchTensor.toSyftTensor()
    }
}
```

``` kotlin
modelSyftState = SyftState.loadSyftState(modelFile)
Log.d(TAG, "Model loaded from $modelFile")
```

### Property

`syftTensors` - the tensors for the model params