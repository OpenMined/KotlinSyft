

### All Types

| Name | Summary |
|---|---|
|

##### [org.openmined.syft.data.loader.AbstractDataLoader](../org.openmined.syft.data.loader/-abstract-data-loader/index.md)


|

##### [org.openmined.syft.data.samplers.BatchSampler](../org.openmined.syft.data.samplers/-batch-sampler/index.md)

Wraps another sampler to yield a mini-batch of indices.


|

##### [org.openmined.syft.networking.datamodels.ClientConfig](../org.openmined.syft.networking.datamodels/-client-config/index.md)

All the user defined parameters will be serialised and sent by the PyGrid in the form of [ClientConfig](../org.openmined.syft.networking.datamodels/-client-config/index.md)


|

##### [org.openmined.syft.networking.datamodels.ClientProperties](../org.openmined.syft.networking.datamodels/-client-properties/index.md)

Client properties specific to the job description


|

##### [org.openmined.syft.execution.CycleStatus](../org.openmined.syft.execution/-cycle-status/index.md)


|

##### [org.openmined.syft.data.loader.DataLoader](../org.openmined.syft.data.loader/-data-loader/index.md)


|

##### [org.openmined.syft.data.loader.DataLoaderIterator](../org.openmined.syft.data.loader/-data-loader-iterator/index.md)


|

##### [org.openmined.syft.data.Dataset](../org.openmined.syft.data/-dataset/index.md)


|

##### [org.openmined.syft.domain.DownloadStatus](../org.openmined.syft.domain/-download-status/index.md)


|

##### [org.openmined.syft.domain.InputParamType](../org.openmined.syft.domain/-input-param-type/index.md)


| (extensions in package org.openmined.syft.proto)

##### [org.pytorch.IValue](../org.openmined.syft.proto/org.pytorch.-i-value/index.md)


|

##### [org.openmined.syft.execution.JobErrorThrowable](../org.openmined.syft.execution/-job-error-throwable/index.md)


|

##### [org.openmined.syft.execution.JobId](../org.openmined.syft.execution/-job-id/index.md)

A uniquer identifier class for the job


|

##### [org.openmined.syft.execution.JobModel](../org.openmined.syft.execution/-job-model/index.md)


|

##### [org.openmined.syft.execution.JobStatusMessage](../org.openmined.syft.execution/-job-status-message/index.md)


|

##### [org.openmined.syft.execution.JobStatusSubscriber](../org.openmined.syft.execution/-job-status-subscriber/index.md)

This is passed as argument to [SyftJob.request](../org.openmined.syft.execution/-syft-job/request.md) giving the overridden callbacks for different phases of the job cycle.


|

##### [org.openmined.syft.domain.OutputParamType](../org.openmined.syft.domain/-output-param-type/index.md)


|

##### [org.openmined.syft.proto.Placeholder](../org.openmined.syft.proto/-placeholder/index.md)


|

##### [org.openmined.syft.execution.Plan](../org.openmined.syft.execution/-plan/index.md)

The Plan Class contains functions to load a PyTorch model from a TorchScript and
to run training through the forward function of the PyTorch Module.
A PyTorch Module is simply a container that takes in tensors as input and returns
tensor after doing some computation.


|

##### [org.openmined.syft.domain.PlanInputSpec](../org.openmined.syft.domain/-plan-input-spec/index.md)


|

##### [org.openmined.syft.domain.PlanOutputSpec](../org.openmined.syft.domain/-plan-output-spec/index.md)


|

##### [org.openmined.syft.domain.ProcessData](../org.openmined.syft.domain/-process-data/index.md)


|

##### [org.openmined.syft.threading.ProcessSchedulers](../org.openmined.syft.threading/-process-schedulers/index.md)


|

##### [org.openmined.syft.execution.Protocol](../org.openmined.syft.execution/-protocol/index.md)


|

##### [org.openmined.syft.data.samplers.RandomSampler](../org.openmined.syft.data.samplers/-random-sampler/index.md)

Samples elements randomly. If without replacement, then sample from a shuffled dataset.
If with replacement, then user can specify :attr:`num_samples` to draw.


|

##### [org.openmined.syft.data.samplers.Sampler](../org.openmined.syft.data.samplers/-sampler/index.md)

Base class for all Samplers.
Every Sampler subclass has to provide an :method:`indices` method, providing a
way to iterate over indices of dataset elements, and a :method:`length` method
that returns the length of the returned iterators.


|

##### [org.openmined.syft.data.samplers.SequentialSampler](../org.openmined.syft.data.samplers/-sequential-sampler/index.md)

Samples elements sequentially, always in the same order.


| (extensions in package org.openmined.syft.proto)

##### [org.openmined.syftproto.execution.v1.StateOuterClass.State](../org.openmined.syft.proto/org.openmined.syftproto.execution.v1.-state-outer-class.-state/index.md)


|

##### [org.openmined.syft.Syft](../org.openmined.syft/-syft/index.md)

This is the main syft worker handling creation and deletion of jobs. This class is also responsible for monitoring device resources via DeviceMonitor


|

##### [org.openmined.syft.domain.SyftConfiguration](../org.openmined.syft.domain/-syft-configuration/index.md)


|

##### [org.openmined.syft.data.loader.SyftDataLoader](../org.openmined.syft.data.loader/-syft-data-loader/index.md)

Data loader. Combines a dataset and a sampler, and provides an iterable over
the given dataset. It supports map-style datasets with single-process loading
and customizing loading order.


|

##### [org.openmined.syft.execution.SyftJob](../org.openmined.syft.execution/-syft-job/index.md)


|

##### [org.openmined.syft.domain.SyftLogger](../org.openmined.syft.domain/-syft-logger/index.md)


|

##### [org.openmined.syft.proto.SyftModel](../org.openmined.syft.proto/-syft-model/index.md)

SyftModel is the data model class for storing the weights of the neural network used for
training or inference.


|

##### [org.openmined.syft.proto.SyftState](../org.openmined.syft.proto/-syft-state/index.md)

SyftState class is responsible for storing all the weights of the neural network.
We update these model weights after every plan.execute


|

##### [org.openmined.syft.proto.SyftTensor](../org.openmined.syft.proto/-syft-tensor/index.md)


| (extensions in package org.openmined.syft.proto)

##### [org.pytorch.Tensor](../org.openmined.syft.proto/org.pytorch.-tensor/index.md)


| (extensions in package org.openmined.syft.proto)

##### [org.openmined.syftproto.types.torch.v1.Tensor.TorchTensor](../org.openmined.syft.proto/org.openmined.syftproto.types.torch.v1.-tensor.-torch-tensor/index.md)


|

##### [org.openmined.syft.domain.TrainingParameters](../org.openmined.syft.domain/-training-parameters/index.md)


|

##### [org.openmined.syft.execution.TrainingState](../org.openmined.syft.execution/-training-state/index.md)


