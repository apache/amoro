Users can download the stable 0.4.0 release zip package from Github or download the source code and compile it according to the README.

## System Requirements

- Java 8 is required. Java 17 is required for Trino.
- Optional: MySQL 5.5 or higher, or MySQL 8
- Optional: ZooKeeper 3.4.x or higher
- Optional: Hive (2.x or 3.x)
- Optional: Hadoop (2.9.x or 3.x)

## Download the distribution

You can find the released versions at [https://github.com/NetEase/arctic/releases](https://github.com/NetEase/arctic/releases), in addition to downloading
You can download arctic-x.y.z-bin.zip (x.y.z is the release number), but you can also download the runtime packages for each engine version according to the engine you are using. Execute unzip
arctic-x.y.z-bin.zip and unzip it to create the arctic-x.y.z directory in the same directory, and then go to the arctic-x.y.z directory.

## Source code compilation

You can build based on the master branch without compiling Trino. The compilation method and the directory of results are described below

```shell
$ git clone https://github.com/NetEase/arctic.git
$ cd arctic
$ mvn clean package -DskipTests -pl '!Trino' [-Dcheckstyle.skip=true]
$ cd dist/target/
$ ls
arctic-x.y.z-bin.zip (Target Pakcage)
dist-x.y.z-tests.jar
dist-x.y.z.jar
archive-tmp/
maven-archiver/

$ cd ../../flink/v1.12/flink-runtime/target
$ ls 
arctic-flink-runtime-1.12-x.y.z-tests.jar
arctic-flink-runtime-1.12-x.y.z.jar (Flink 1.12 Target flink runtime package)
original-arctic-flink-runtime-1.12-x.y.z.jar
maven-archiver/

Or switch from the dist/target directory to the spark runtime package directory
$ spark/v3.1/spark-runtime/target
$ ls
arctic-spark-3.1-runtime-0.4.0.jar (spark v3.1 Target flink runtime package)
arctic-spark-3.1-runtime-0.4.0-tests.jar
arctic-spark-3.1-runtime-0.4.0-sources.jar
original-arctic-spark-3.1-runtime-0.4.0.jar
```

If you need to compile the Trino module at the same time, you need to install jdk17 locally and configure toolchains.xml in the user's ${user.home}/.m2/ directory, then run mvn
package -P toolchain to compile the entire project.

```shell
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

## Configuring AMS

If you want to use AMS in a formal scenario, it is recommended to modify `{ARCTIC_HOME}/conf/config.yaml` by referring to the following configuration steps.

### Configure the service address

- The arctic.ams.server-host.prefix configuration selects the IP address or segment prefix for your service bindings, with the intent that in HA
  mode, users can use the same configuration file on multiple hosts; if you deploy only a single node, this configuration can also directly specify the full IP address.
- AMS itself provides http service and thrift service, you need to configure the ports that these two services listen on. 1630 is the default port for Http service and 1260 is the default port for Thrift service.

```shell
ams:
  server-bind-host: "0.0.0.0" #The IP address for service listening, default is 0.0.0.0.
  server-expose-host: "127.0.0.1" #The IP address for service external exposure, default is 127.0.0.1.
  
  thrift-server:
    bind-port: 1260 #The port for accessing AMS Thrift service.

  http-server:
    bind-port: 1630 #The port for accessing AMS Dashboard.
```

???+ Attention

    make sure the port is not used before configuring it

### Configuration System Database

Users can use MySQL as the system database instead of Derby. To do so, the system database must first be initialized in MySQL：

```shell
$ mysql -h{mysql-host-IP} -P{mysql-port} -u{username} -p
Enter password: 
'Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 41592724
Server version: 5.7.20-v3-log Source distribution

Copyright (c) 2000, 2020, Oracle and/or its affiliates. All rights reserved.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql>
mysql> create database arctic;
Query OK, 1 row affected (0.01 sec)

mysql> use arctic;
Database changed
mysql> source {ARCTIC_HOME}/conf/mysql/ams-mysql-init.sql
```

Add MySQL configuration under `ams`:

```shell
ams:
  database:
    type: mysql
    jdbc-driver-class: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/db?useUnicode=true&characterEncoding=UTF8&autoReconnect=true&useAffectedRows=true&useSSL=false
    username: root
    password: root
```

### Configuring high availability

To improve stability, AMS supports a one-master-multi-backup HA mode. Zookeeper is used to implement leader election,
and the AMS cluster name and Zookeeper address are specified. The AMS cluster name is used to bind different AMS clusters
on the same Zookeeper cluster to avoid mutual interference.

```shell
ams:
  ha:
    enabled: true  #Enable HA
    cluster-name: default # Differentiating binding multiple sets of AMS on the same ZooKeeper.
    zookeeper-address: 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183 # ZooKeeper server address.
```

### 配置 Optimizer

Self-optimizing requires configuration of optimizer resources, including containers configuration and optimizer group 
configuration. Taking the configuration of Flink type optimizer as an example, the configuration is as follows. For 
detailed parameter instructions and configuration of other types, please refer to [managing-optimizers](managing-optimizers.md)

```shell
containers:
  - name: flinkContainer
    container-impl: com.netease.arctic.optimizer.FlinkOptimizerContainer
    properties:
      flink-home: "/opt/flink/"                                     # The installation directory of Flink
      export.JVM_ARGS: "-Djava.security.krb5.conf=/opt/krb5.conf"   # Submitting Flink jobs with Java parameters, such as Kerberos parameters.
      export.HADOOP_CONF_DIR: "/etc/hadoop/conf/"                   # Hadoop configuration file directory
      export.HADOOP_USER_NAME: "hadoop"                             # Hadoop user
      export.FLINK_CONF_DIR: "/etc/hadoop/conf/"                    # Flink configuration file directory
optimizer_groups:
  - name: flinkGroup
    container: flinkContainer
    properties:
      taskmanager.memory: "2048"
      jobmanager.memory: "1024"
```

An example of a complete configuration is as follows:

```shell
ams:
  admin-username: admin
  admin-password: admin
  server-bind-host: "0.0.0.0"
  server-expose-host: "127.0.0.1"
  refresh-external-catalog-interval: 180000 # 3min
  refresh-table-thread-count: 10
  refresh-table-interval: 60000 #1min
  expire-table-thread-count: 10
  clean-orphan-file-thread-count: 10
  sync-hive-tables-thread-count: 10

  blocker:
    timeout: 60000 # 1min

  thrift-server:
    bind-port: 1260
    max-message-size: 104857600 # 100MB
    worker-thread-count: 20
    selector-thread-count: 2
    selector-queue-size: 4

  http-server:
    bind-port: 1630

  self-optimizing:
    commit-thread-count: 10

  optimizer:
    heart-beat-timeout: 60000 # 1min
    task-ack-timeout: 30000 # 30s

  #database:
  #  type: derby
  #  jdbc-driver-class: org.apache.derby.jdbc.EmbeddedDriver
  #  url: jdbc:derby:/tmp/arctic/derby;create=true

  #  MySQL database configuration.
    database:
      type: mysql
      jdbc-driver-class: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://127.0.0.1:3306/db?useUnicode=true&characterEncoding=UTF8&autoReconnect=true&useAffectedRows=true&useSSL=false
      username: root
      password: root

  terminal:
    backend: local
    local.spark.sql.session.timeZone: UTC
    local.spark.sql.iceberg.handle-timestamp-without-timezone: false

#  Kyuubi terminal backend configuration.
#  terminal:
#    backend: kyuubi
#    kyuubi.jdbc.url: jdbc:hive2://127.0.0.1:10009/


#  High availability configuration.
  ha:
    enabled: true
    cluster-name: default
    zookeeper-address: 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183

containers:
  - name: localContainer
    container-impl: com.netease.arctic.optimizer.LocalOptimizerContainer
    properties:
      export.JAVA_HOME: "/opt/java"   # JDK environment

  - name: flinkContainer
    container-impl: com.netease.arctic.optimizer.FlinkOptimizerContainer
    properties:
      flink-home: "/opt/flink/"                                     # Flink install home
      export.JVM_ARGS: "-Djava.security.krb5.conf=/opt/krb5.conf"   # Flink launch jvm args, like kerberos config when ues kerberos
      export.HADOOP_CONF_DIR: "/etc/hadoop/conf/"                   # Hadoop config dir
      export.HADOOP_USER_NAME: "hadoop"                             # Hadoop user submit on yarn
      export.FLINK_CONF_DIR: "/etc/hadoop/conf/"                    # Flink config dir

optimizer_groups:
  - name: default
    container: localContainer
    properties:
      memory: "1024" # The size of memory allocated for each parallel

  - name: external-group
    container: external # The external container is used to host all externally launched optimizers.

  - name: flinkGroup
    container: flinkContainer
    properties:
      taskmanager.memory: "2048"
      jobmanager.memory: "1024"
```

### 配置 Terminal

When Terminal is executed in local mode, Spark-related parameters can be configured.

```shell
ams:
  terminal:
    backend: local
    local.spark.sql.session.timeZone: UTC
    local.spark.sql.iceberg.handle-timestamp-without-timezone: false
    # When the catalog type is Hive, it automatically uses the Spark session catalog to access Hive tables.
    local.using-session-catalog-for-hive: true
```

## 启动 AMS

Enter the directory arctic-x.y.z and execute bin/ams.sh start to start AMS.

```shell
$ cd arctic-x.y.z
$ bin/ams.sh start
```

Then, access http://localhost:1630 through a browser to see the login page. If it appears, it means that the startup is 
successful. The default username and password for login are both "admin".
