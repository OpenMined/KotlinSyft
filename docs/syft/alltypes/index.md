

### All Types

| Name | Summary |
|---|---|
|

##### [org.openmined.syft.networking.datamodels.ClientConfig](../org.openmined.syft.networking.datamodels/-client-config/index.md)

All the user defined parameters will be serialised and sent by the PyGrid in the form of [ClientConfig](../org.openmined.syft.networking.datamodels/-client-config/index.md)


|

##### [org.openmined.syft.networking.datamodels.ClientProperties](../org.openmined.syft.networking.datamodels/-client-properties/index.md)

Client properties specific to the job description


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


| (extensions in package org.openmined.syft.proto)

##### [org.openmined.syftproto.execution.v1.StateOuterClass.State](../org.openmined.syft.proto/org.openmined.syftproto.execution.v1.-state-outer-class.-state/index.md)


|

##### [org.openmined.syft.Syft](../org.openmined.syft/-syft/index.md)

This is the main syft worker handling creation and deletion of jobs. This class is also responsible for monitoring device resources via DeviceMonitor


|

##### [org.openmined.syft.domain.SyftConfiguration](../org.openmined.syft.domain/-syft-configuration/index.md)


|

##### [org.openmined.syft.domain.SyftDataLoader](../org.openmined.syft.domain/-syft-data-loader/index.md)


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


