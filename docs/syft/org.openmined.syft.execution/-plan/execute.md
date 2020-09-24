[syft](../../index.md) / [org.openmined.syft.execution](../index.md) / [Plan](index.md) / [execute](./execute.md)

# execute

`@ExperimentalStdlibApi fun execute(vararg iValues: IValue): IValue?`

Loads a serialized TorchScript module from the specified path on the disk.

### Parameters

`iValues` - The input to the torchscript. The training batch, the hyper parameters and the model weights must be sent here

### Exceptions

`IllegalStateException` - if the device state does not fulfill the constraints set for running the job

**Return**
The output contains the loss, accuracy values as defined while creating plan. It also
    contains the updated parameters of the model. These parameters are then saved manually by user.

