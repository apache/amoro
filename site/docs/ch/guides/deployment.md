用户可以通过从 Github 上下载稳定的 0.5.0 的 release zip 包，或者可以下载源码根据 README 进行编译。

## 环境要求

- Java 8, Trino 需要安装 Java17
- Optional: MySQL 5.5 及以上 或者 MySQL 8
- Optional: zookeeper 3.4.x 及以上
- Optional: Hive(2.x or 3.x)
- Optional: Hadoop(2.9.x or 3.x)

## 下载发行版

你可以在 [https://github.com/NetEase/arctic/releases](https://github.com/NetEase/arctic/releases) 找到已发行的版本，除了下载
arctic-x.y.z-bin.zip ( x.y.z 是发行版本号)外，可按需根据自己使用的引擎，下载对应各引擎不同版本的runtime包。执行 unzip
arctic-x.y.z-bin.zip 解压缩后在同级目录下生成 arctic-x.y.z 目录, 进入到目录 arctic-x.y.z。

## 源码编译

你可以基于 master 分支进行构建， 不编译 Trino 的 编译方法及编译结果目录说明如下

```shell
$ git clone https://github.com/NetEase/arctic.git
$ cd arctic
$ mvn clean package -DskipTests -pl '!Trino' [-Dcheckstyle.skip=true]
$ cd dist/target/
$ ls
arctic-x.y.z-bin.zip (目标版本包)
dist-x.y.z-tests.jar
dist-x.y.z.jar
archive-tmp/
maven-archiver/

$ cd ../../flink/v1.12/flink-runtime/target
$ ls 
arctic-flink-runtime-1.12-x.y.z-tests.jar
arctic-flink-runtime-1.12-x.y.z.jar (Flink 1.12 目标flink runtime 包)
original-arctic-flink-runtime-1.12-x.y.z.jar
maven-archiver/

或者从  dist/target 切换到 spark runtime 包
$ spark/v3.1/spark-runtime/target
$ ls
arctic-spark-3.1-runtime-x.y.z.jar (spark v3.1 目标flink runtime 包)
arctic-spark-3.1-runtime-x.y.z-tests.jar
arctic-spark-3.1-runtime-x.y.z-sources.jar
original-arctic-spark-3.1-runtime-x.y.z.jar
```

如果需要同时编译 Trino 模块，需要先本地安装 jdk17，并且在用户的 ${user.home}/.m2/ 目录下配置 toolchains.xml，然后执行 mvn
package -P toolchain 进行整个项目的编译即可。

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

## 配置 AMS

如果想要在正式场景使用AMS，建议参考以下配置步骤， 修改 `{ARCTIC_HOME}/conf/config.yaml` 。

### 配置服务地址

- server-expose-host 配置服务暴露的 IP 地址或者网段前缀, 如果在高可用模式下，可以使用 IP 地址前缀来达成在不同机器上使用相同配置文件的目的；
  如果只部署单节点，该配置也可以指定为完整的 IP 地址。
- AMS 本身对外提供 http 服务和 thrift 服务，需要配置这两个服务监听的端口。Http 服务默认端口1630， Thrift 服务默认端口1260

```shell
ams:
  server-expose-host: 127.
  
  thrift-server:
    bind-port: 1260
    
  http-server:
    bind-port: 1630
```

???+ 注意

    配置端口前请确认端口未被占用

### 配置系统库

用户可以使用 MySQL/Derby 作为 AMS 的系统库，默认为 Derby，如果要使用 MySQL 作为系统库，则需要先初始化库表：

```shell
$ mysql -h{mysql主机IP} -P{mysql端口} -u{username} -p
Enter password: #输入密码
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

在 `ams` 下添加 MySQL 配置：

```shell
ams:
  database:
    type: mysql
    jdbc-driver-class: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://{host}:{port}/{database}?useUnicode=true&characterEncoding=UTF8&autoReconnect=true&useAffectedRows=true&useSSL=false
    username: {user}
    passord: {password}
```

### 配置高可用

为了提高系统可用性，AMS 支持一主多备的高可用模式，通过 Zookeeper 来实现选主。

```shell
ams:
  ha:
    enabled: true
    cluster-name: default # 区分不同的 AMS 集群
    zookeeper-address: 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183
```

### 配置 Terminal

Terminal 默认在 local 模式执行的情况下，可以配置 Spark 相关参数

```shell
ams:
  terminal:
    backend: local
    local.spark.sql.session.timeZone: UTC
    local.spark.sql.iceberg.handle-timestamp-without-timezone: false
    local.using-session-catalog-for-hive: true
```

### 配置 Optimizer

Self-optimizing 需要配置 optimizer 资源，包含 Containers 配置和 Optimizer groups 配置。以配置 Flink 类型的 Optimizer
为例，配置如下， 详细的参数说明及其它类型的配置见 [managing-optimizers](managing-optimizers.md)

```shell
containers:
  - name: flinkContainer
    container-impl: com.netease.arctic.optimizer.FlinkOptimizerContainer
    properties:
      flink-home: "/opt/flink/"                              #Flink install home
      export.JVM_ARGS: "-Djava.security.krb5.conf=/opt/krb5.conf"   #Flink launch jvm args, like kerberos config when ues kerberos
      export.HADOOP_CONF_DIR: "/etc/hadoop/conf/"            #Hadoop config dir
      export.HADOOP_USER_NAME: "hadoop"                      #Hadoop user submit on yarn
      export.FLINK_CONF_DIR: "/etc/hadoop/conf/"             #Flink config dir

optimizer_groups:
  - name: flinkGroup             # container name, should be in the names of containers  
    container: flinkContainer
    properties:
      taskmanager.memory: "2048"
      jobmanager.memory: "1024"
```

### 完整配置

一个完整的配置样例如下：

```shell
ams:
  admin-username: admin
  admin-password: admin
  server-bind-host: "0.0.0.0"
  server-expose-host: "127.0.0.1"

  thrift-server:
    bind-port: 1260
    max-message-size: 104857600 # 100MB
    worker-thread-count: 20
    selector-thread-count: 2
    selector-queue-size: 4

  http-server:
    bind-port: 1630
    
  refresh-external-catalogs:
    interval: 180000 # 3min

  refresh-tables:
    thread-count: 10
    interval: 60000 # 1min

  self-optimizing:
    commit-thread-count: 10
    
  optimizer:
    heart-beat-timeout: 60000 # 1min
    task-ack-timeout: 30000 # 30s
    
  blocker:
    timeout: 60000 # 1min
    
  # optional features
  expire-snapshots:
    enabled: true
    thread-count: 10

  clean-orphan-files:
    enabled: true
    thread-count: 10

  sync-hive-tables:
    enabled: true
    thread-count: 10

  database:
    type: derby
    jdbc-driver-class: org.apache.derby.jdbc.EmbeddedDriver
    url: jdbc:derby:/tmp/arctic/derby;create=true

  #  MySQL database configuration.
  #  database:
  #    type: mysql
  #    jdbc-driver-class: com.mysql.cj.jdbc.Driver
  #    url: jdbc:mysql://127.0.0.1:3306?useUnicode=true&characterEncoding=UTF8&autoReconnect=true&useAffectedRows=true&useSSL=false
  #    username: root
  #    password: root

  terminal:
    backend: local
    local.spark.sql.session.timeZone: UTC
    local.spark.sql.iceberg.handle-timestamp-without-timezone: false

#  Kyuubi terminal backend configuration.
#  terminal:
#    backend: kyuubi
#    kyuubi.jdbc.url: jdbc:hive2://127.0.0.1:10009/


#  High availability configuration.
#  ha:
#    enabled: true
#    cluster-name: default
#    zookeeper-address: 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183

containers:
  - name: localContainer
    container-impl: com.netease.arctic.optimizer.LocalOptimizerContainer
    properties:
      export.JAVA_HOME: "/opt/java"   # JDK environment

#containers:
#  - name: flinkContainer
#    container-impl: com.netease.arctic.optimizer.FlinkOptimizerContainer
#    properties:
#      flink-home: "/opt/flink/"                                     # Flink install home
#      export.JVM_ARGS: "-Djava.security.krb5.conf=/opt/krb5.conf"   # Flink launch jvm args, like kerberos config when ues kerberos
#      export.HADOOP_CONF_DIR: "/etc/hadoop/conf/"                   # Hadoop config dir
#      export.HADOOP_USER_NAME: "hadoop"                             # Hadoop user submit on yarn
#      export.FLINK_CONF_DIR: "/etc/hadoop/conf/"                    # Flink config dir

optimizer_groups:
  - name: default
    container: localContainer
    properties:
      memory: "1024" # The size of memory allocated for each parallel

  - name: external-group
    container: external # The external container is used to host all externally launched optimizers.

#  - name: flinkGroup
#    container: flinkContainer
#    properties:
#      taskmanager.memory: "2048"
#      jobmanager.memory: "1024"

```

## 启动 AMS

进入到目录 arctic-x.y.z ， 执行 bin/ams.sh start 启动 AMS。

```shell
$ cd arctic-x.y.z
$ bin/ams.sh start
```

然后通过浏览器访问 http://localhost:1630 可以看到登录界面,则代表启动成功，登录的默认用户名和密码都是 admin。
