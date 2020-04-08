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
- [ ] Implement Protobuf in Android
- [x] Set up deployment to Maven
- [ ] Implement split and stitch algorithm for data channels in Android
- [x] Add support for background task scheduling in Android
- [ ] Implement sleep/wake detection in Android
- [ ] Add support for charge detection and wifi detection in Android
- [ ] Add bandwidth and Internet connectivity test in Android
- [ ] Scaffold basic proposed worker API in Android
- [ ] Execute plans in Android
- [ ] Execute protocols in Android
- [ ] Allow for training state to be persisted to temporary storage in the event of a failure in Android

### Local Development

1. Fork and clone
2. Open Android Studio and import project
4. Do your work.
5. Push to your fork
6. Submit a PR to openmined/KotlinSyft

### Demo App

To run the demo app, create a file `local_config.properties` under the folder `demo-app` and add the following line:

```
syft.base_url="10.0.2.2:5000"
```

Change the URL accordingly to connect to your PyGrid instance. The above example URL is used by the emulator.

### Contributing

Read [CONTRIBUTING.md](https://github.com/OpenMined/KotlinSyft/blob/master/CONTRIBUTING.md)

