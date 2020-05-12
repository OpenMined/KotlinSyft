# <img src="project_resources/pysyft_android.png" height="54"> KotlinSyft

![License](https://img.shields.io/github/license/openmined/KotlinSyft)
![Language](https://img.shields.io/github/languages/top/openmined/KotlinSyft)
![Size](https://img.shields.io/github/repo-size/openmined/KotlinSyft)
![Android CI](https://github.com/OpenMined/KotlinSyft/workflows/Android%20CI/badge.svg)
[![Version](https://api.bintray.com/packages/openmined/KotlinSyft/syft/images/download.svg?version=0.0.1)](https://bintray.com/openmined/KotlinSyft/syft/0.0.1/link)

## Introduction
This is the android worker Library for [PySyft](https://github.com/OpenMined/PySyft)

Of course, [PySyft](https://github.com/openmined/pysyft) can run in its own environment but the final training procedure needs to deployed on the mobile workers using Torchscript. 

**KotlinSyft employs P2P connectivity for realization of distributed pysyft protocols.** 

### Demo App

The demo app fetches the plans,protocols and model weights from pygrid server hosted locally. The plans are then deserialized and executed using libtorch and performs training steps.
<p align="center">
<img src="project_resources/demo.gif" height="354">
</p>

Follow these steps to setup an environment where to test the demo app:

- Clone the repo [PyGrid](https://github.com/OpenMined/PyGrid) and change directory to it
```
git clone https://github.com/OpenMined/PyGrid
cd PyGrid
```
- Install [docker](https://github.com/OpenMined/PyGrid/#getting-started)
- Install docker-compose.
- Execute `docker-compose` in the command line to start pygrid server.
```
docker-compose up
```

- Clone the repo [PySyft](https://github.com/OpenMined/PySyft) and create the virtual environment.
```
cd PySyft
virtualenv -p python3 venv
make venv
make notebook
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

### Development Roadmap

For understanding the overall objective of the library please refer to [openmined roadmap](https://github.com/OpenMined/Roadmap/blob/master/web_and_mobile_team/projects/federated_learning.md)

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

