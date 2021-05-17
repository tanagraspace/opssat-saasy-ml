# OPS-SAT Datapool Parameter Dispatcher App
An NMF App for the OPS-SAT spacecraft. The app fetches parameters from the spacecraft's OBSW datapool and writes their values into CSV files.

## Installing

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

2. Get and build
```
$ git clone https://github.com/georgeslabreche/opssat-datapool-param-dispatcher
$ mvn install
```

3. Deploy the application in the NMF SDK following [the NMF deployment guide](https://nanosat-mo-framework.readthedocs.io/en/latest/apps/packaging.html). Replacing instances of "sobel" by "datapool-param-dispatcher" and "Sobel" by "DatapoolParameterDispatcherApp" in the main class name.

## Starting

### Configuring

The app is configured via the `config.properties` file. Number of aggregations to build and the row write frequency in which the fetched values are written to the output CSV files:
```
aggregations=4
flush.write.at=10
```

Each aggregation is configured to fetch `n` parameters of a certain type for `i` iterations and whether or not the output is appended to an existing CSV file or if a new file is created on each run. For instance, to fetch 15 Float parameters for 5 iterations at intervals of 2 seconds:

```
iterations.1=5
interval.1=2000
params.get.count.1=15
params.get.type.1=Float
params.get.output.csv.1=toGround/thread_01.csv
params.get.output.csv.append.1=false
```

Parameters can also be explicitly listed:

```
iterations.2=10
interval.2=1000
params.get.names.2=GNC_0005,GNC_0011,GNC_0007
params.get.output.csv.2=toGround/thread_02.csv
params.get.output.csv.append.2=true
```

### Running
Follow [the last 3 steps of the NMF SDK guide](https://nanosat-mo-framework.readthedocs.io/en/latest/sdk.html#running-the-cubesat-simulator). 

Alternatively, use the `build.bat` script after editing the `PROJECT_DIR` and `NMF_SDK_PACKAGE_DIR` to match your development environment's file system. Running `build.bat` builds the project. Running `build.bat 1` builds and runs the project. 

