# OPS-SAT Datapool Stress Tester App
An NMF App to stress test fetching and setting OBSW datapool parameters

## Installation

### Requirements
- Java 8
- Maven 3.X.X

Tested environment:
```powershell
Apache Maven 3.8.1 (05c21c65bdfed0f71a2f2ada8b84da59348c4c5d)
Maven home: C:\Users\Georges\Development\Tools\apache-maven-3.8.1\bin\..
Java version: 1.8.0_291, vendor: Oracle Corporation, runtime: C:\Program Files\Java\jdk1.8.0_291\jre
Default locale: en_US, platform encoding: Cp1252
OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"
```

### Steps
1. Install the `dev` branch of NanoSatMO Framework (NMF) following [the NMF quick start guide](https://nanosat-mo-framework.readthedocs.io/en/latest/quickstart.html)

2. Get and build the Datapool Stress Tester application
```
$ git clone https://github.com/georgeslabreche/opssat-datapool-stresstester
$ mvn install
```

3. Deploy the application in the NMF SDK following [the NMF deployment guide](https://nanosat-mo-framework.readthedocs.io/en/latest/apps/packaging.html). Replacing instances of "sobel" by "datapool-stresstester" and "Sobel" by "StressTesterApp" in the main class name.

## Starting
Follow [the last 3 steps of the NMF SDK guide](https://nanosat-mo-framework.readthedocs.io/en/latest/sdk.html#running-the-cubesat-simulator). 

Alternatively, use the `build.bat` script after editing the `PROJECT_DIR` and `NMF_SDK_PACKAGE_DIR` to match your development environment's file system. Running `build.bat` builds the project. Running `build.bat 1` builds and runs the project. 
