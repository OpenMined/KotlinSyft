![KotlinSyft-logo](project_resources/pysyft_android.png)


![License](https://img.shields.io/github/license/openmined/KotlinSyft)
![Language](https://img.shields.io/github/languages/top/openmined/KotlinSyft)
![Android CI](https://github.com/OpenMined/KotlinSyft/workflows/Android%20CI/badge.svg)
![OpenCollective](https://img.shields.io/opencollective/all/openmined)

## KotlinSyft

### Syft Infrastructure 
With increasing concerns on user privacy and data regulations, we need systems that can learn and adapt and yet do not reveal the sensitive user information to an external entity. [Federated learning](https://ai.googleblog.com/2017/04/federated-learning-collaborative.html) comes to the rescue with the ability to train these machine learning models without sending private data to central servers for analysis. 
This when coupled with privacy preserving mechanisms like [Differential Privacy](https://en.wikipedia.org/wiki/Differential_privacy), [Multi Party Computation](https://en.wikipedia.org/wiki/Secure_multi-party_computation) and [secure aggregation](https://research.google/pubs/pub45808/) becomes a solid infrastructure upon which developers can build personalized applications without having to worry about violating user privacy.

We at [Openmined](https://www.openmined.org) set out to build this humongous ecosystem and are proudly the **first open source ecosystem**. KotlinSyft is a part of this ecosystem responsible for bringing secure federated learning to android devices.

As a developer, there are few steps to building your own secure learning system upon the openmined infrastructure:

- :robot: Generate your secure ML model using [PySyft](https://github.com/OpenMined/PySyft). PySyft, by design, is built upon PyTorch and TensorFlow so you **don't** need to learn a new ML framework.
- :pushpin: Host your model on [PyGrid](https://github.com/OpenMined/PyGrid) which will deal with all the federated learning specific components of your pipeline.
- :loop: Easily train your model on android (with [KotlinSyft](https://github.com/OpenMined/KotlinSyft)) or IOS (with [SwiftSyft](https://github.com/OpenMined/SwiftSyft)) devices and on web browser (with [Syft.js](https://github.com/OpenMined/syft.js)). 
- :tada: Finally, get the updated model at PyGrid all the while keeping **user data safely in the device**.
- :relieved: The best thing is you can even secure the model updates with a host of security protocols built into PySyft and PyGrid 
- :notebook: All this is described in detail in [roadmap](https://github.com/OpenMined/Roadmap/blob/master/web_and_mobile_team/projects/federated_learning.md)

If you want to know the internal details of how such scalable federated systems are built, [Federated Learning at scale](https://arxiv.org/pdf/1902.01046.pdf) is a good starter!   

### Features
**KotlinSyft** makes it easy for you to deploy and **train your PySyft models on android** devices. Follow section [QuickStart](#quick-start) to know how! It also gives you a host of other features like:

- :dart: KotlinSyft supports **MPC** and **secure aggregation protocols** using **peer-to-peer webRTC** connections **WIP**.
- :lock_with_ink_pen: KotlinSyft protects malicious android devices from damaging your model. We support **JWT authentication** to triage trust upon model trainers.
- Host of **inbuilt best practices** to prevent apps from over using device resources. 
    - :electric_plug: **Charge detection** to allow background training only when device is connected to charger
    - :zzz: **Sleep/Wake detection** so that the app does not occupy resource when user starts using the device
    - :money_with_wings: **Wifi/Metered network detection** to ensure the model updates do not use all the available data quota 
    - :no_bell: Of course, they all are **overridable** but we recommend not to!
- :mortar_board: Support for both reactive and callback patterns so you have your freedom of choice *WIP*
         

### Installation
KotlinSyft would soon be available on JCenter and Maven.

### Quick Start
You can the library as a frontend or as a background service. The section below gives a quick start usage. You need to add this code to your activity or your service to begin training.   
```kotlin
    val userId = "my Id"
    // make an http request to your server to get an authentication token
    // This step is optional
    val authToken = apiClient.requestToken("https://www.mywebsite.com/request-token/$userId")

    // The config defines all the adjustable properties of the syft worker
    // The url entered here must not define connection protocol like http/wss since the worker allots them by its own
    // `this` supplies the context. It can be activity context or a service context or an application context
    val config = SyftConfiguration.builder(this, "www.mypygrid-url.com").build()
    
    // Initiate Syft worker to handle all your jobs
    val worker = Syft.getInstance(authToken, configuration)
    //Create a new Job
    val newJob = syftWorker.newJob("mnist", "1.0.0")
    
    // Define training procedure for the job
    val jobStatusSubscriber = object : JobStatusSubscriber() {
        override fun onReady(
            model: SyftModel,
            plans: ConcurrentHashMap<String, Plan>,
            clientConfig: ClientConfig
        ) {
            // This function is called when KotlinSyft has downloaded the plans and protocols from PyGrid
            // You are ready to train your model on your data
            // param model stores the model weights given by PyGrid
            // param plans is a HashMap of all the planIDs and their plans. 
            // ClientConfig has hyper parameters like batchsize, learning rate, number of steps, etc
            
            // Plans are accessible by their plan Id used while hosting it on PyGrid.
            // eventually you would be able to use plan name here 
            val plan = plans["plan id"]

            repeat(clientConfig.maxUpdates) { step ->
                // your custom implementation to read a databatch from your data
                val batchData = dataRepository.loadDataBatch(clientConfig.batchSize)
                // plan.execute runs a single gradient step and returns the output as PyTorch IValue
                val output = plan.execute(
                    model,
                    batchData,
                    clientConfig
                )?.toTuple()
                // The output is a tuple with outputs defined by the pysyft plan along with all the model params
                output?.let { outputResult ->
                    val paramSize = model.modelState!!.syftTensors.size
                    // The model params are always appended at the end of the output tuple
                    val beginIndex = outputResult.size - paramSize
                    val updatedParams =
                            outputResult.slice(beginIndex until outputResult.size - 1)
                    // update your model. You can perform any arbitrary computation and checkpoint creation with these model weights
                    model.updateModel(updatedParams.map { it.toTensor() })
                    // get the required loss, accuracy, etc values just like you do in Pytorch Android
                    val accuracy = outputResult[1].toTensor().dataAsFloatArray.last()
                } ?: handleEmptyOutput() // This means the plan wasn't built properly 
            }
        }

        override fun onRejected(timeout: String) {
        // Implement this function to define what your worker will do when your worker is rejected from the cycle
        // timeout tells you after how much time you should try again for the cycle at PyGrid
        }

        override fun onError(throwable: Throwable) {
        // Implement this function to handle error during job execution 
        }
    }
    //start your job
    newJob.start(jobStatusSubscriber) 
    //Voila! You are done.
```

### Demo App

The demo app fetches the plans, protocols and model weights from pygrid server hosted locally. The plans are then deserialized and executed using libtorch.
<p align="center">
<img src="project_resources/demo.gif" height="354">
</p>

Follow these steps to setup an environment to run the demo app:

- Clone the repo [PyGrid](https://github.com/OpenMined/PyGrid) and change directory to it
```bash
git clone https://github.com/OpenMined/PyGrid
cd PyGrid
```
- Install [docker](https://github.com/OpenMined/PyGrid/#getting-started)
- Install docker-compose.
- Execute `docker-compose` in the command line to start pygrid server.
```bash
docker-compose up
```

- Install [PySyft](https://github.com/OpenMined/PySyft) `v0.2.5` in the virtual environment.
```bash
virtualenv -p python3 venv
source venv/bin/activate
pip install syft==0.2.5, jupyter==1.0.0, notebook==5.7.8
```
- host jupyter 
```bash
jupyter notebook
```
- Open a browser and navigate to [localhost:8888](http://localhost:8888/). You should be able to see the pysyft notebook console.
- In the jupyter notebook, navigate to `examples/experimental/FL Training Plan`
- Run the notebooks `Create Plan`. It should save three files in the `FL Training Plan` folder
- Run the notebook `Host Plan`. Now PyGrid is setup and the model is hosted over it.
- The android app connects to your PC's localhost via router (easier approach)
- Get the IP address of your computer by running 
    `ip address show | grep "inet " | grep -v 127.0.0.1` if using Linux/Mac. For windows there are different steps.

    Alternatively, if you want to run the Demo app in the emulator, use `10.0.2.2` as the IP address.

- Create a file `local_config.properties` under the folder `demo-app` and add the following line:

```
syft.base_url="<IP_address_from_step_16>:5000"
```

### Local Development

1. Star, Fork and clone the repo
2. Open Android Studio and import project
4. Do your work.
5. Push to your fork
6. Submit a PR to openmined/KotlinSyft

### Training Dataset

The demo app has a smaller randomly sampled subset of MNIST Training Data. You can replace the `demo-app/src/main/res/raw/mnist_train.csv` with the 100MB file from [here](https://drive.google.com/file/d/1oHegwSc9pDFQDZe0FeKW51-SQsTz4i3W/view?usp=sharing)

Download the above file and replace it with `train_mnist.csv` to train on complete mnist data. 

### Contributing

Read [CONTRIBUTING.md](https://github.com/OpenMined/KotlinSyft/blob/master/CONTRIBUTING.md). Additionally, we welcome you to the [slack](http://slack.openmined.org/) for queries related to the library and contribution in general. The channel `#lib_syft_mobile` is meant for android and IOS teams. See you there! 

### Contributors

These people were integral part of the efforts to bring KotlinSyft to fruition and in its active development. 