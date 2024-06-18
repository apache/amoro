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

You can choose to download the stable release package from [download page](../../../download/), or the source code form [Github](https://github.com/apache/amoro) and compile it according to the README.

## System requirements

- Java 8 is required. Java 17 is required for Trino.
- Optional: MySQL 5.5 or higher
- Optional: PostgreSQL 14.x or higher
- Optional: ZooKeeper 3.4.x or higher
- Optional: Hive (2.x or 3.x)
- Optional: Hadoop (2.9.x or 3.x)

## Download the distribution

All released package can be downloaded from [download page](../../../download/).
You can download apache-amoro-x.y.z-bin.tar.gz (x.y.z is the release number), and you can also download the runtime packages for each engine version according to the engine you are using.
Unzip it to create the amoro-x.y.z directory in the same directory, and then go to the amoro-x.y.z directory.

## Source code compilation

You can build based on the master branch without compiling Trino. The compilation method and the directory of results are described below:

```shell
git clone https://github.com/apache/amoro.git
cd amoro
base_dir=$(pwd) 
mvn clean package -DskipTests
cd amoro-ams/dist/target/
ls
apache-amoro-x.y.z-bin.tar.gz # AMS release package
dist-x.y.z-tests.jar
dist-x.y.z.jar
archive-tmp/
maven-archiver/

cd ${base_dir}/amoro-mixed-format/amoro-mixed-format-flink/v1.15/amoro-mixed-format-flink-runtime-1.15/target
ls 
amoro-mixed-format-flink-runtime-1.15-x.y.z-tests.jar
amoro-mixed-format-flink-runtime-1.15-x.y.z.jar # Flink 1.15 runtime package
original-amoro-mixed-format-flink-runtime-1.15-x.y.z.jar
maven-archiver/

cd ${base_dir}/amoro-mixed-format/amoro-mixed-format-spark/v3.2/amoro-mixed-format-spark-runtime-3.2/target
ls
amoro-mixed-format-spark-runtime-3.2-x.y.z.jar # Spark v3.2 runtime package)
amoro-mixed-format-spark-runtime-3.2-x.y.z-tests.jar
amoro-mixed-format-spark-runtime-3.2-x.y.z-sources.jar
original-amoro-mixed-format-spark-runtime-3.2-x.y.z.jar
```

If the Flink version in the amoro-ams/amoro-ams-optimizer/amoro-optimizer-flink module you compiled is lower than 1.15, you must add the `-Pflink-pre-1.15` parameter before mvn.
for example `mvn clean package -Pflink-pre-1.15 -Dflink-optimizer.flink-version=1.14.6 -DskipTests` to compile.

If you need to compile the Trino module at the same time, you need to install jdk17 locally and configure `toolchains.xml` in the user's `${user.home}/.m2/` directory,
then run `mvn package -Ptoolchain,build-mixed-format-trino` to compile the entire project.

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

If you want to use AMS in a production environment, it is recommended to modify `{AMORO_HOME}/conf/config.yaml` by referring to the following configuration steps.

### Configure the service address

- The `ams.server-bind-host` configuration specifies the host to which AMS is bound. The default value, `0.0.0.0,` indicates binding to all network interfaces.
- The `ams.server-expose-host` configuration specifies the host exposed by AMS that the compute engines and optimizers used to connect to AMS. You can configure a specific IP address on the machine, or an IP prefix. When AMS starts up, it will find the first host that matches this prefix.
- The `ams.thrift-server.table-service.bind-port` configuration specifies the binding port of the Thrift Server that provides the table service. The compute engines access AMS through this port, and the default value is 1260.
- The `ams.thrift-server.optimizing-service.bind-port` configuration specifies the binding port of the Thrift Server that provides the optimizing service. The optimizers access AMS through this port, and the default value is 1261.
- The `ams.http-server.bind-port` configuration specifies the port to which the HTTP service is bound. The Dashboard and Open API are bound to this port, and the default value is 1630.
- The `ams.http-server.rest-auth-type` configuration specifies the REST API auth type, which could be token(default) or basic. The basic auth would reuse `ams.admin-username` and `ams.admin-password` for authentication. 

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
Make sure the port is not used before configuring it.
{{< /hint >}}

### Configure system database

You can use MySQL/PostgreSQL as the system database instead of the default Derby.

If you would like to use MySQL as the system database, you need to manually download the [MySQL JDBC Connector](https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.1.0/mysql-connector-j-8.1.0.jar)
and move it into the `{AMORO_HOME}/lib/` directory. You can use the following command to complete these operations:
```shell
cd ${AMORO_HOME}
MYSQL_JDBC_DRIVER_VERSION=8.0.30
wget https://repo1.maven.org/maven2/mysql/mysql-connector-java/${MYSQL_JDBC_DRIVER_VERSION}/mysql-connector-java-${MYSQL_JDBC_DRIVER_VERSION}.jar
mv mysql-connector-java-${MYSQL_JDBC_DRIVER_VERSION}.jar lib
```

Create an empty database in MySQL/PostgreSQL, then AMS will automatically create table structures in this MySQL/PostgreSQL database when it first started.

One thing you need to do is Adding MySQL/PostgreSQL configuration under `config.yaml` of Ams:

```yaml
# MySQL
ams:
  database:
    type: mysql
    jdbc-driver-class: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/amoro?useUnicode=true&characterEncoding=UTF8&autoReconnect=true&useAffectedRows=true&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: root
# PostgreSQL
#ams:
#  database:
#    type: postgres
#    jdbc-driver-class: org.postgresql.Driver
#    url: jdbc:postgresql://127.0.0.1:5432/amoro
#    username: user
#    password: passwd
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

### Configure optimizer containers

To scale out the optimizer through AMS, container configuration is required.
If you choose to manually start an external optimizer, no additional container configuration is required. AMS will initialize a container named `external` by default to store all externally started optimizers.
AMS provides implementations of `LocalContainer` and `FlinkContainer` by default. Configuration for both container types can be found below:

```yaml
containers:
  - name: localContainer
    container-impl: org.apache.amoro.optimizer.LocalOptimizerContainer
    properties:
      export.JAVA_HOME: "/opt/java"   # JDK environment
  
  - name: flinkContainer
    container-impl: org.apache.amoro.optimizer.FlinkOptimizerContainer
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

### Configure metric reporter

Amoro provides metric reporters by plugin mechanism to connect to  external metric systems.

All metric-reporter plugins are configured in `$AMORO_CONF_DIR/plugins/metric-repoters.yaml` .

The configuration format of the plug-in is:

```yaml

metric-reporters:
  - name:                 # the unified plugin name.
    enabled:              # if this plugin is enabled, default is true.
    properties:           # a map defines properties of plugin.
```

Currently, there is only one reporter is available.

#### Prometheus Exporter

By enable the `prometheus-exporter` plugin, the AMS will start a prometheus http exporter server.

```yaml
metric-reporters:
  - name: prometheus-exporter            # configs for prometheus exporter
    enabled: true
    properties:
       port: 9090                        # the port that the prometheus-exporter listens on.
```

You can add a scrape job in your prometheus configs

```yaml
# Your prometheus configs file.
scrape_configs:
  - job_name: 'amoro-exporter'
    scrape_interval: 15s
    static_configs:
      - targets: ['localhost:9090']  # The host and port that you configured in Amoro plugins configs file.
```


### Environments variables

The following environment variables take effect during the startup process of AMS,
you can set up those environments to overwrite the default value.

| Environments variable name | Default value      | Description                                | 
|----------------------------|--------------------|--------------------------------------------|
| AMORO_CONF_DIR             | ${AMORO_HOME}/conf | location where Amoro loading config files. |
| AMORO_LOG_DIR              | ${AMORO_HOME}/logs | location where the logs files output       | 

Note: `$AMORO_HOME` can't be overwritten from environment variable. It always points to the parent dir of `./bin`.

### Configure AMS JVM

The following JVM options could be set in `${AMORO_CONF_DIR}/jvm.properties`.

| Property Name   | Related Jvm option                             | Description              |
|-----------------|------------------------------------------------|--------------------------|
| xms             | "-Xms${value}m                                 | Xms config for jvm       |
| xmx             | "-Xmx${value}m                                 | Xmx config for jvm       |
| jmx.remote.port | "-Dcom.sun.management.jmxremote.port=${value}  | Enable remote debug      |
| extra.options   | "JAVA_OPTS="${JAVA_OPTS} ${JVM_EXTRA_CONFIG}"  | The addition jvm options |

### Terminal configurations

Terminal support local and kyuubi, the default is local. If the user uses local to run in the spark local context, you can set the **spark.*** configuration, and if you use kyuubi, you can set the **kyuubi.*** configuration

| Key                      | Default | Description                                                                                       |
|--------------------------|---------|---------------------------------------------------------------------------------------------------|
| terminal.backend         | local   | Terminal backend implementation. local, kyuubi and custom are valid values.                       |
| terminal.factory         | -       | Session factory implement of terminal, `terminal.backend` must be `custom` if this is set.        |
| terminal.result.limit    | 1000    | Row limit of result-set                                                                           |
| terminal.stop-on-error   | false   | When a statement fails to execute, stop execution or continue executing the remaining statements. |
| terminal.session.timeout | 30      | Session timeout in minutes.                                                                       |


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

## Upgrade AMS

### Upgrade system databases

You can find all the upgrade SQL scripts under `{AMORO_HOME}/conf/mysql/` with name pattern `upgrade-a.b.c-to-x.y.z.sql`.
Execute the upgrade SQL scripts one by one to your system database based on your starting and target versions.

### Replace all libs and plugins

Replace all contents in the original `{AMORO_HOME}/lib` directory with the contents in the lib directory of the new installation package.
Replace all contents in the original `{AMORO_HOME}/plugin` directory with the contents in the plugin directory of the new installation package.

{{< hint info >}}
Backup the old content before replacing it, so that you can roll back the upgrade operation if necessary.
{{< /hint >}}

### Configure new parameters

The old configuration file `{AMORO_HOME}/conf/config.yaml` is usually compatible with the new version, but the new version may introduce new parameters. Try to compare the configuration files of the old and new versions, and reconfigure the parameters if necessary.

### Restart AMS

Restart AMS with the following commands:
```shell
bin/ams.sh restart
```

