---
title: "Deployment"
url: deployment
aliases:
    - "admin-guides/deployment"
menu:
    main:
        parent: Admin Guides
        weight: 100
---
# Deployment

You can choose to download the stable release package from [download page](../../download/), or the source code form [Github](https://github.com/NetEase/amoro) and compile it according to the README.

## System requirements

- Java 8 is required. Java 17 is required for Trino.
- Optional: MySQL 5.5 or higher, or MySQL 8
- Optional: ZooKeeper 3.4.x or higher
- Optional: Hive (2.x or 3.x)
- Optional: Hadoop (2.9.x or 3.x)

## Download the distribution

All released package can be downaloded from [download page](../../download/).
You can download amoro-x.y.z-bin.zip (x.y.z is the release number), and you can also download the runtime packages for each engine version according to the engine you are using.
Unzip it to create the amoro-x.y.z directory in the same directory, and then go to the amoro-x.y.z directory.

## Source code compilation

You can build based on the master branch without compiling Trino. The compilation method and the directory of results are described below

```shell
git clone https://github.com/NetEase/amoro.git
cd amoro
base_dir=$(pwd) 
mvn clean package -DskipTests -pl '!Trino'
cd dist/target/
ls
amoro-x.y.z-bin.zip # AMS release pakcage
dist-x.y.z-tests.jar
dist-x.y.z.jar
archive-tmp/
maven-archiver/

cd ${base_dir}/flink/v1.12/flink-runtime/target
ls 
amoro-flink-runtime-1.12-x.y.z-tests.jar
amoro-flink-runtime-1.12-x.y.z.jar # Flink 1.12 runtime package
original-amoro-flink-runtime-1.12-x.y.z.jar
maven-archiver/

cd ${base_dir}/spark/v3.1/spark-runtime/target
ls
amoro-spark-3.1-runtime-0.4.0.jar # Spark v3.1 runtime package)
amoro-spark-3.1-runtime-0.4.0-tests.jar
amoro-spark-3.1-runtime-0.4.0-sources.jar
original-amoro-spark-3.1-runtime-0.4.0.jar
```

If you need to compile the Trino module at the same time, you need to install jdk17 locally and configure `toolchains.xml` in the user's ${user.home}/.m2/ directory, then run mvn
package -P toolchain to compile the entire project.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<toolchains>
    <toolchain>
        <type>jdk</type>
        <provides>
            <version>17</version>
            <vendor>sun</vendor>
        </provides>
        <configuration>
            <jdkHome>${YourJDK17Home}</jdkHome>
        </configuration>
    </toolchain>
</toolchains>
```

## Configuration

If you want to use AMS in a production environment, it is recommended to modify `{ARCTIC_HOME}/conf/config.yaml` by referring to the following configuration steps.

### Configure the service address

- The `ams.server-bind-host` configuration specifies the host to which AMS is bound. The default value, `0.0.0.0,` indicates binding to all network interfaces.
- The `ams.server-expose-host` configuration specifies the host exposed by AMS that the compute engine and optimizer use to connect to AMS. You can configure a specific IP address on the machine or an IP prefix. When AMS starts up, it will find the first host that matches this prefix.
- The `ams.thrift-server.table-service.bind-port` configuration specifies the binding port of the Thrift Server that provides the table service. The compute engine accesses AMS through this port, and the default value is 1260.
- The `ams.thrift-server.optimizing-service.bind-port` configuration specifies the binding port of the Thrift Server that provides the optimizing service. The optimizers accesses AMS through this port, and the default value is 1261.
- The `ams.http-server.bind-port` configuration specifies the port to which the HTTP service is bound. The Dashboard and Open API are bound to this port, and the default value is 1630.

```yaml
ams:
  server-bind-host: "0.0.0.0" #The IP address for service listening, default is 0.0.0.0.
  server-expose-host: "127.0.0.1" #The IP address for service external exposure, default is 127.0.0.1.
  
  thrift-server:
    table-service:
      bind-port: 1260 #The port for accessing AMS table service.
    optimizing-service:
      bind-port: 1261 #The port for accessing AMS optimizing service.

  http-server:
    bind-port: 1630 #The port for accessing AMS Dashboard.
```

{{< hint info >}}
make sure the port is not used before configuring it
{{< /hint >}}

### Configure system database

Users can use MySQL as the system database instead of Derby. 

When AMS is first started, it will automatically create table structures in MySQL.

One thing you need to do is Adding MySQL configuration under `config.yaml` of Ams:

```yaml
ams:
  database:
    type: mysql
    jdbc-driver-class: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/amoro?useUnicode=true&characterEncoding=UTF8&autoReconnect=true&useAffectedRows=true&useSSL=false
    username: root
    password: root
```

### Configure high availability

To improve stability, AMS supports a one-master-multi-backup HA mode. Zookeeper is used to implement leader election,
and the AMS cluster name and Zookeeper address are specified. The AMS cluster name is used to bind different AMS clusters
on the same Zookeeper cluster to avoid mutual interference.

```yaml
ams:
  ha:
    enabled: true  #Enable HA
    cluster-name: default # Differentiating binding multiple sets of AMS on the same ZooKeeper.
    zookeeper-address: 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183 # ZooKeeper server address.
```

### Configure containers

To scale out the optimizer through AMS, container configuration is required. 
If you choose to manually start an external optimizer, no additional container configuration is required. AMS will initialize a container named `external` by default to store all externally started optimizers.
AMS provides implementations of `LocalContainer` and `FlinkContainer` by default. Configuration for both container types can be found below:

```yaml
containers:
  - name: localContainer
    container-impl: com.netease.amoro.optimizer.LocalOptimizerContainer
    properties:
      export.JAVA_HOME: "/opt/java"   # JDK environment
  
  - name: flinkContainer
    container-impl: com.netease.amoro.optimizer.FlinkOptimizerContainer
    properties:
      flink-home: "/opt/flink/"                                     # The installation directory of Flink
      export.JVM_ARGS: "-Djava.security.krb5.conf=/opt/krb5.conf"   # Submitting Flink jobs with Java parameters, such as Kerberos parameters.
      export.HADOOP_CONF_DIR: "/etc/hadoop/conf/"                   # Hadoop configuration file directory
      export.HADOOP_USER_NAME: "hadoop"                             # Hadoop user
      export.FLINK_CONF_DIR: "/etc/hadoop/conf/"                    # Flink configuration file directory
```

### Configure terminal

The Terminal module in the AMS Dashboard allows users to execute SQL directly on the platform. Currently, the Terminal backend supports two implementations: `local` and `kyuubi`.
In local mode, an embedded Spark environment will be started in AMS. In kyuubi mode, an additional kyuubi service needs to be deployed.
The configuration for kyuubi mode can refer to: [Using Kyuubi with Terminal](../using-kyuubi/). Below is the configuration for the local mode:

```yaml
ams:
  terminal:
    backend: local
    local.spark.sql.iceberg.handle-timestamp-without-timezone: false
    # When the catalog type is Hive, it automatically uses the Spark session catalog to access Hive tables.
    local.using-session-catalog-for-hive: true
```

## Start AMS

Enter the directory amoro-x.y.z and execute bin/ams.sh start to start AMS.

```shell
cd amoro-x.y.z
bin/ams.sh start
```

Then, access http://localhost:1630 through a browser to see the login page. If it appears, it means that the startup is 
successful. The default username and password for login are both "admin".

You can also restart/stop AMS with the following command:

```shell
bin/ams.sh restart/stop
```