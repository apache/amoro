# Deploying Amoro in IntelliJ IDEA

This guide describes how to import the Amoro project into IntelliJ IDEA and deploy it.

## Requirements
+ Java Version: Java 8 or Java 11 is required.

### Required Plugins
1. Go to `Settings` → `Plugins` in IntelliJ IDEA.
2. Select the “Marketplace” tab. 
3. Search for and install the following plugins:
    - Scala
4. Restart IntelliJ IDEA if prompted.

## Building Amoro in IntelliJ IDEA
This guide is based on IntelliJ IDEA 2024. Some details might differ in other versions.

1. Clone the Repository and  Create the Configuration File:

```shell
git clone https://github.com/apache/amoro.git
cd amoro
# base_dir=$(pwd)
mkdir -p conf
cp dist/src/main/amoro-bin/conf/config.yaml conf/config.yaml
```

2. Import the Project: 
    1. Open IntelliJ IDEA and select `File`->`Open`.
    2. Choose the root folder of the cloned Amoro repository.
3. Configure Java Version:
    1. Open the `Maven` tab on the right side of the IDE.
    2. Expand `Profiles` and ensure the selected Java version matches your IDE's Java version.
    3. To check or change the project SDK, go to `File`->`Project Structure...`->`Project`->`SDK`.
4. Modify the Configuration File:

   The AMS server requires configuration for database connections. Edit {base_dir}/conf/config.yaml as follows:

```yaml
ams:
  database:
    type: derby
    jdbc-driver-class: org.apache.derby.jdbc.EmbeddedDriver
    url: jdbc:derby:{base_dir}/conf/derby;create=true
    connection-pool-max-total: 20
    connection-pool-max-idle: 16
    connection-pool-max-wait-millis: 30000
```

	 Note: Replace {base_dir} with the actual path to your project root.

5. Load Dependencies

   In the `Maven` tab,  click the `Reload All Maven Projects` botton, or right-click the imported Amoro project in the Project view and select `Maven`->`Reload project`.

## Starting AMS
1. Open the following file: 

`
{base_dir}/amoro-ams/src/main/java/org/apache/amoro/server/AmoroServiceContainer.java
`

2. In the top right corner of IntelliJ IDEA, click the `Run AmoroServiceContainer` button to start the AMS service.
3. Once the service has started, open your web browser and navigate to: [http://localhost:1630](http://localhost:1630/)
4. If you see the login page, the startup was successful. The default username and password for login are both `admin`.

# Starting the Optimizer in IntelliJ IDEA
### Add an Optimizer Group
1. Open http://localhost:1630 in your browser and log in with admin/admin.
2. Click on `Optimizing` in the sidebar, select `Optimizer Groups`, and click the `Add Group` button to create a new group.
3. Configure the newly added Optimizer group:
   ![config-optimizer-group](../images/admin/config-optimizer-group.png)

   The following configuration needs to be filled in:

    - Name: the name of the optimizer group, which can be seen in the list of optimizer groups on the front-end page.
    - Container: the name of a container configured in containers.
    - Properties: the default configuration under this group, is used as a configuration parameter for tasks when the optimize page is scaled out. 

### Modify Configuration
1. Add log4j Dependencies:

   Insert the following into the {base_dir}/amoro-optimizer/amoro-optimizer-standalone/pom.xml:

```xml
<properties>
    <log4j.version>2.20.0</log4j.version>
</properties>
```

2. Add log4j Dependencies:

   Insert the following into the <dependencies> section of the same pom.xml file:

```xml
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-slf4j-impl</artifactId>
</dependency>

<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-api</artifactId>
</dependency>

<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
</dependency>
```

3. In the `Maven` tab,  click the `Reload All Maven Projects` botton, or right-click the imported amoro project in the Project view and select `Maven`->`Reload project`.

## Start the Optimizer
1. Open the following file in IntelliJ IDEA: 

`
{base_dir}/amoro-optimizer/amoro-optimizer-standalone/src/main/java/org/apache/amoro/optimizer/standalone/StandaloneOptimizer.java
`

2. Click the `Run/Debug Configurations` botton in the top right corner of IntelliJ IDEA and select `Current File`.
3. Click `More Actions` on the right side and select `Run with Parameters...`.
4. In `Build and run`, enter the following parameters in the `Program arguments:`:`-a thrift://127.0.0.1:1261 -p 1 -g local`

   The description of the relevant parameters is shown in the following table:

| Property | Required | Description                                                                                                                                                                                                                        |
|-----|----|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| -a  | Yes | The address of the AMS thrift service, for example: thrift://127.0.0.1:1261, can be obtained from the config.yaml configuration.                                                                                                   |
| -g  | Yes | Group name created in advance under external container.                                                                                                                                                                            |
| -p  | Yes | Optimizer parallelism usage.                                                                                                                                                                                                       |
| -hb | No | Heart beat interval with ams, should be smaller than configuration ams.optimizer.heart-beat-timeout in AMS configuration conf/config.yaml which is 60000 milliseconds by default, default 10000(ms).                               |
| -eds | No | Whether extend storage to disk, default false.                                                                                                                                                                                     |
| -dsp | No | Defines the directory where the storage files are saved, the default temporary-file directory is specified by the system property `java.io.tmpdir`. On UNIX systems the default value of this property is typically `/tmp` or `/var/tmp`. |
| -msz | No | Memory storage size limit when extending disk storage(MB), default 512(MB).                                                                                                                                                        |
| -id | No | Resource id |

5. Click `Apply` and `Run` to start an optimizer.
6. In the Amoro dashboard, click on `Optimizing` in the sidebar and choose `Optimizers`. If you see a newly created optimzier, the startup was successful.

## Quickstart
To quickly explore Amoro's core features, such as self-optimizing, visit [https://amoro.apache.org/quick-start/](https://amoro.apache.org/quick-start/).





