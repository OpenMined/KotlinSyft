# <img src="logo/pysyft_android.png" height="54"> KotlinSyft

![License](https://img.shields.io/github/license/openmined/KotlinSyft)
![Language](https://img.shields.io/github/languages/top/openmined/KotlinSyft)
![Size](https://img.shields.io/github/repo-size/openmined/KotlinSyft)
![Android CI](https://github.com/OpenMined/KotlinSyft/workflows/Android%20CI/badge.svg)
[![Version](https://api.bintray.com/packages/openmined/KotlinSyft/syft/images/download.svg?version=0.0.1)](https://bintray.com/openmined/KotlinSyft/syft/0.0.1/link)

## Introduction
This is the android worker Library for [PySyft](https://github.com/OpenMined/PySyft)

Of course, [PySyft](https://github.com/openmined/pysyft) has the ability to run in its own environment but the final training procedure needs to deployed on the mobile workers using Torchscript. 

**KotlinSyft employs P2P connectivity for realization of distributed pysyft protocols.**

### Development Roadmap

For understanding the overall objective of the library please refer to [openmined roadmap](https://github.com/OpenMined/Roadmap/blob/master/web_and_mobile_team/projects/federated_learning.md)

- [x] Create the basic structure of KotlinSyft
- [x] Implement WebRTC flow in Android
- [x] Implement Protobuf in Android
- [x] Set up deployment to Maven
- [ ] Implement split and stitch algorithm for data channels in Android
- [x] Add support for background task scheduling in Android
- [ ] Implement sleep/wake detection in Android
- [ ] Add support for charge detection and wifi detection in Android
- [ ] Add bandwidth and Internet connectivity test in Android
- [ ] Scaffold basic proposed worker API in Android
- [x] Execute plans in Android
- [ ] Execute protocols in Android
- [ ] Allow for training state to be persisted to temporary storage in the event of a failure in Android

### Local Development

1. Fork and clone
2. Open Android Studio and import project
4. Do your work.
5. Push to your fork
6. Submit a PR to openmined/KotlinSyft

### Demo App

Follow these steps to setup an environment where to test the demo app.


1. Clone the repo https://github.com/OpenMined/PyGrid
2. `cd PyGrid`
3. Install docker: https://github.com/OpenMined/PyGrid/#getting-started
4. Install docker-compose.
5. Execute `docker-compose up` in the command line.
6. Clone the repo https://github.com/OpenMined/PySyft
7. `cd PySyft`
8. `virtualenv -p python3 venv`
9. `make venv`
10. `make notebook`
11. Open a browser and navigate to http://localhost:8888/
12. In the jupyter notebook, navigate to `examples/experimental/FL Training Plan`
13. Run the notebooks `Create Plan`. It should save three files in the `FL Training Plan` folder
14. Run the notebook `Host Plan`. Now PyGrid is setup and the model is hosted over it.
15. The android app connects to your PC's localhost via router (easier approach)
16. Get the IP address of your computer by running 
    `ip address show | grep "inet " | grep -v 127.0.0.1` if using Linux/Mac. For windows there are different steps.

    Alternatively, if you want to run the Demo app in the emulator, use `10.0.2.2` as the IP address.

17. Create a file `local_config.properties` under the folder `demo-app` and add the following line:

```
syft.base_url="<IP_address_from_step_16>:5000"
```

### Training Dataset

The demo app has a smaller randomly sampled subset of MNIST Training Data. You can replace the `demo-app/src/main/res/raw/mnist_train.csv` with the 100MB file from [here](https://drive.google.com/file/d/1oHegwSc9pDFQDZe0FeKW51-SQsTz4i3W/view?usp=sharing)

Download the above file and replace it with `train_mnist.csv` to train on complete mnist data. 
### Contributing

Read [CONTRIBUTING.md](https://github.com/OpenMined/KotlinSyft/blob/master/CONTRIBUTING.md)

