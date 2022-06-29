# OPS-SAT SaaSyML App
An NMF App for the OPS-SAT spacecraft. The app uses ML to train AI models with the spacecraft's OBSW datapool parameters as training data. 

## References
- [The NMF quick start guide](https://nanosat-mo-framework.readthedocs.io/en/latest/quickstart.html)
- [The NMF deployment guide](https://nanosat-mo-framework.readthedocs.io/en/latest/apps/packaging.html)
- [Vert.x Core Manual](https://vertx.io/docs/vertx-core/java/)

## Installing

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

If in step 2 the app was cloned to a different folder name than the default `opssat-saasy-ml`, then the following copy configuration in `sdk/sdk-package/pom.xml` must be updated:

```xml
<copy todir="${esa.nmf.sdk.assembly.outputdir}/home/saasy-ml">
    <fileset dir="${basedir}/src/main/resources/space-common"/>
    <fileset dir="${basedir}/src/main/resources/space-app-root"/>
    <fileset dir="${basedir}/../../../opssat-saasy-ml/conf"/>
</copy>
```

Specifically, the following line:

```xml
<fileset dir="${basedir}/../../../opssat-saasy-ml/conf"/>
```

must be update to:

```xml
<fileset dir="${basedir}/../../../<the_app_folder_name>/conf"/>
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
- This URI follows the pattern `maltcp://<SUPERVISOR_HOST>:<SUPERVISOR_PORT>/nanosat-mo-supervisor-Directory`.

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

#### 6. Make an API request

##### 6.1. Subscribe to a training data feed
Use an API platform like [Postman](https://www.postman.com/) to make an POST request to the following endpoint:
```
http://<SUPERVISOR_HOST>:9999/api/v1/training/data/subscribe
```

With the payload:
```json
{
    "expId": 123,
    "datasetId": 1,
    "iterations": 10,
    "interval": 2,
    "params": ["GNC_0005", "GNC_0011", "GNC_0007"]
}
```

Make several of these requests with different values for `expId`, `datasetId`, `interval`, and `params`. The fetched values will appear as log outputs in the CTT's console.

##### 6.2. Unsubscribe to a training data feed
Unsubscribe to the data feed with a POST request to the following endpoint:
```
http://<SUPERVISOR_HOST>:9999/api/v1/training/data/unsubscribe
```

With the payload:
```json
{
    "expId": 123,
    "datasetId": 1
}
```

##### 6.3. Train a model
Make an POST request to the following endpoint:
```
http://<SUPERVISOR_HOST>:9999/api/v1/training/:type/:group/:algorithm
```

With the payload:
```json
{
    "expId": 123,
    "datasetId": 1
}
```

## Terminating the App
Note: examples in this section are in PowerShell.

Situation: The App did not shutdown gracefully despite terminating the Supervisor and the CTT. 
Problem: Attempting to repeat installation step #3 to redeploy the app will result in a locked file error, e.g.:

```powershell
 Failed to execute goal org.apache.maven.plugins:maven-dependency-plugin:3.1.0:copy-dependencies (copy-dependencies) on project package: Error copying artifact from C:\Users\honeycrisp\.m2\repository\com\tanagraspace\nmf\apps\saasy-ml\2.1.0-SNAPSHOT\saasy-ml-2.1.0-SNAPSHOT.jar to C:\Users\honeycrisp\Dev\Tanagra\ESA\opssat\saasy-ml\opssat-saasy-ml-nmf\sdk\sdk-package\target\nmf-sdk-2.1.0-SNAPSHOT\home\nmf\lib\saasy-ml-2.1.0-SNAPSHOT.jar: C:\Users\honeycrisp\Dev\Tanagra\ESA\opssat\saasy-ml\opssat-saasy-ml-nmf\sdk\sdk-package\target\nmf-sdk-2.1.0-SNAPSHOT\home\nmf\lib\saasy-ml-2.1.0-SNAPSHOT.jar: The process cannot access the file because it is being used by another process. -> [Help 1]
```

In this case, use the jps command to identify the process id of the culprit process (i.e. the SaaSyMLApp java process):
```powershell
> jps
55880 org.eclipse.equinox.launcher_1.6.400.v20210924-0641.jar
95404 SaaSyMLApp
150140 Jps
```

Force kill the process, e.g. in Windows Terminal:
```powershell
> taskkill /F /PID 95404
SUCCESS: The process with PID 95404 has been terminated.
```

Check that the process was indeed killed:
```powershell
> jps
111924 Jps
55880 org.eclipse.equinox.launcher_1.6.400.v20210924-0641.jar
```

Now the App can be redeployed.

## API
TBD

