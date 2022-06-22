# OPS-SAT SaaSyML App
An NMF App for the OPS-SAT spacecraft. The app uses ML to train AI models with the spacecraft's OBSW datapool parameters as training data. 

## Installing

References:
- [the NMF quick start guide](https://nanosat-mo-framework.readthedocs.io/en/latest/quickstart.html)
- [the NMF deployment guide](https://nanosat-mo-framework.readthedocs.io/en/latest/apps/packaging.html)

### Requirements
- Java 8
- Maven 3.X.X

Tested environment on Windows 10:
```powershell
Apache Maven 3.8.1 (05c21c65bdfed0f71a2f2ada8b84da59348c4c5d)
Maven home: C:\Users\Georges\Development\Tools\apache-maven-3.8.1\bin\..
Java version: 1.8.0_291, vendor: Oracle Corporation, runtime: C:\Program Files\Java\jdk1.8.0_291\jre
Default locale: en_US, platform encoding: Cp1252
OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"
```

Tested environment on Ubuntu 18.04.5 on Windows:
```shell
Apache Maven 3.8.4 (9b656c72d54e5bacbed989b64718c159fe39b537)
Maven home: /mnt/c/Users/honeycrisp/Tools/apache-maven-3.8.4
Java version: 1.8.0_312, vendor: Private Build, runtime: /usr/lib/jvm/java-8-openjdk-amd64/jre
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "5.10.16.3-microsoft-standard-wsl2", arch: "amd64", family: "unix"
```

### Steps

#### 1. Install the SaaSyML App
```shell
$ git clone https://github.com/tanagraspace/opssat-saasy-ml
$ cd opssat-saasy-ml
$ mvn install
$ cd ..
```

#### 2. Install NMF
```shell
$ git clone https://github.com/tanagraspace/opssat-saasy-ml-nmf.git
$ cd opssat-saasy-ml-nmf
$ mvn install
```

#### 3. Deploy the SaaSyML App
```shell
$ cd sdk/sdk-package/
$ mvn install
```

#### 4. Supervisor and CTT
Open a second terminal window to run both the Supervisor and the Consumer Test Tool (CTT).

The Supervisor:
```shell
cd target/nmf-sdk-2.1.0-SNAPSHOT/home/nmf/nanosat-mo-supervisor-sim
./nanosat-mo-supervisor-sim.sh 
```

- The Supervisor outputs a URI on the console.
- This URI follows the pattern maltcp://SOME_ADDRESS:PORT/nanosat-mo-supervisor-Directory.

The CTT:
```shell
cd target/nmf-sdk-2.1.0-SNAPSHOT/home/nmf/consumer-test-tool
./consumer-test-tool.sh
```

#### 5. Start the SaaSyML App
- Paste the URI given by the Supervisor into the **Communication Settings** field of the CTT.
- Click the **Fetch information** button.
- Click the **Connect to Selected Provider** button.
- A new tab appears: **nanosat-mo-supervisor**. 
- Select the **saasy-ml** app under the **Apps Launcher Servce" table.
- Click the **runApp** button.

## Configuration

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