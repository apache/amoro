用户可以通过从 Github 上下载稳定的 0.4.0 的 release zip 包，或者可以下载源码根据 README 进行编译。

## 环境要求

- Java 8, Trino 需要安装 Java11
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
arctic-spark-3.1-runtime-0.4.0.jar (spark v3.1 目标flink runtime 包)
arctic-spark-3.1-runtime-0.4.0-tests.jar
arctic-spark-3.1-runtime-0.4.0-sources.jar
original-arctic-spark-3.1-runtime-0.4.0.jar
```

如果需要同时编译 Trino 模块，需要先本地安装 jdk11，并且在用户的 ${user.home}/.m2/ 目录下配置 toolchains.xml，然后执行 mvn
package -P toolchain 进行整个项目的编译即可。

```shell
<?xml version="1.0" encoding="UTF-8"?>
<toolchains>
    <toolchain>
        <type>jdk</type>
        <provides>
            <version>11</version>
            <vendor>sun</vendor>
        </provides>
        <configuration>
            <jdkHome>${yourJdk11Home}</jdkHome>
        </configuration>
    </toolchain>
</toolchains>
```

## 配置 AMS

如果想要在正式场景使用AMS，建议参考以下配置步骤， 修改 `{ARCTIC_HOME}/conf/config.yaml` 。

### 配置服务地址

- arctic.ams.server-host.prefix 配置选择你服务绑定的 IP 地址或者网段前缀, 目的是在 HA
  模式下，用户可以在多台主机上使用相同的配置文件；如果用户只部署单节点，该配置也可以直接指定完整的 IP 地址。
- AMS 本身对外提供 http 服务和 thrift 服务，需要配置这两个服务监听的端口。Http 服务默认端口1630， Thrift 服务默认端口1260

```shell
ams:
  arctic.ams.server-host.prefix: "127." #To facilitate batch deployment can config server host prefix.Must be enclosed in double quotes
  arctic.ams.thrift.port: 1260   # ams thrift服务访问的端口
  arctic.ams.http.port: 1630    # ams dashboard 访问的端口
```

???+ 注意

    配置端口前请确认端口未被占用

### 配置系统库

用户可以使用 MySQL 作为系统库使用，默认为 Derby，首先在 MySQL 中初始化系统库：

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
mysql> source {ARCTIC_HOME}/conf/mysql/x.y.z-init.sql
```

在 `ams` 下添加 MySQL 配置：

```shell
ams:
  arctic.ams.mybatis.ConnectionURL: jdbc:mysql://{host}:{port}/{database}?useUnicode=true&characterEncoding=UTF8&autoReconnect=true&useAffectedRows=true&useSSL=false
  arctic.ams.mybatis.ConnectionDriverClassName: com.mysql.jdbc.Driver
  arctic.ams.mybatis.ConnectionUserName: {user}
  arctic.ams.mybatis.ConnectionPassword: {password}
  arctic.ams.database.type: mysql
```

### 配置高可用

为了提高稳定性，AMS 支持一主多备的 HA 模式，通过 Zookeeper 来实现选主，指定 AMS 集群名和 Zookeeper 地址。AMS集群名用来在同一套
Zookeeper 集群上绑定不同的 AMS 集群，避免相互影响。

```shell
ams:
  #HA config
  arctic.ams.ha.enable: true     #开启 ha
  arctic.ams.cluster.name: default  # 区分同一套 zookeeper 上绑定多套 AMS
  arctic.ams.zookeeper.server: 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183  # zookeeper server地址
```

### 配置 Optimizer

Self-optimizing 需要配置 optimizer 资源，包含 Containers 配置和 Optimizer group 配置。以配置 Flink 类型的 Optimizer
为例，配置如下， 详细的参数说明及其它类型的配置见 [managing-optimizers](managing-optimizers.md)

```shell
containers:
  - name: flinkContainer
    type: flink
    properties:
      FLINK_HOME: /opt/flink/        #flink install home
      HADOOP_CONF_DIR: /etc/hadoop/conf/       #hadoop config dir
      HADOOP_USER_NAME: hadoop       #hadoop user submit on yarn
      JVM_ARGS: -Djava.security.krb5.conf=/opt/krb5.conf       #flink launch jvm args, like kerberos config when ues kerberos
      FLINK_CONF_DIR: /etc/hadoop/conf/        #flink config dir
optimize_group:
  - name: flinkOp
    # container name, should be in the names of containers  
    container: flinkContainer
    properties:
      taskmanager.memory: 2048
      jobmanager.memory: 1024
```

一个完整的配置样例如下：

```shell
ams:
  arctic.ams.server-host.prefix: "127." #To facilitate batch deployment can config server host prefix.Must be enclosed in double quotes
  arctic.ams.thrift.port: 1260   # ams thrift服务访问的端口
  arctic.ams.http.port: 1630    # ams dashboard 访问的端口
  arctic.ams.optimize.check.thread.pool-size: 10
  arctic.ams.optimize.commit.thread.pool-size: 10
  arctic.ams.expire.thread.pool-size: 10
  arctic.ams.orphan.clean.thread.pool-size: 10
  arctic.ams.file.sync.thread.pool-size: 10
  # derby config.sh 
  # arctic.ams.mybatis.ConnectionDriverClassName: org.apache.derby.jdbc.EmbeddedDriver
  # arctic.ams.mybatis.ConnectionURL: jdbc:derby:/tmp/arctic/derby;create=true
  # arctic.ams.database.type: derby
  # mysql config
  arctic.ams.mybatis.ConnectionURL: jdbc:mysql://{host}:{port}/{database}?useUnicode=true&characterEncoding=UTF8&autoReconnect=true&useAffectedRows=true&useSSL=false
  arctic.ams.mybatis.ConnectionDriverClassName: com.mysql.jdbc.Driver
  arctic.ams.mybatis.ConnectionUserName: {user}
  arctic.ams.mybatis.ConnectionPassword: {password}
  arctic.ams.database.type: mysql

  #HA config
  arctic.ams.ha.enable: true     #开启ha
  arctic.ams.cluster.name: default  # 区分同一套zookeeper上绑定多套AMS
  arctic.ams.zookeeper.server: 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183

  # Kyuubi config
  arctic.ams.terminal.backend: kyuubi
  arctic.ams.terminal.kyuubi.jdbc.url: jdbc:hive2://127.0.0.1:10009/
  
  # login config
  login.username: admin
  login.password: admin

# extension properties for like system
extension_properties:
#test.properties: test
containers:
  # arctic optimizer container config.sh
  - name: localContainer
    type: local
    properties:
      hadoop_home: /opt/hadoop
      # java_home: /opt/java
  - name: flinkContainer
    type: flink
    properties:
      FLINK_HOME: /opt/flink/        #flink install home
      HADOOP_CONF_DIR: /etc/hadoop/conf/       #hadoop config dir
      HADOOP_USER_NAME: hadoop       #hadoop user submit on yarn
      JVM_ARGS: -Djava.security.krb5.conf=/opt/krb5.conf       #flink launch jvm args, like kerberos config when ues kerberos
      FLINK_CONF_DIR: /etc/hadoop/conf/        #flink config dir
  - name: externalContainer
    type: external
    properties:
optimize_group:
  - name: default
    # container name, should equal with the name that containers config.sh
    container: localContainer
    properties:
      # unit MB
      memory: 1024
  - name: flinkOp
    container: flinkContainer
    properties:
      taskmanager.memory: 1024
      jobmanager.memory: 1024
  - name: externalOp
    container: external
    properties:
```

### 配置 Terminal

Terminal 在 local 模式执行的情况下，可以配置 Spark 相关参数

```shell
arctic.ams.terminal.backend: local
arctic.ams.terminal.local.spark.sql.session.timeZone: UTC
arctic.ams.terminal.local.spark.sql.iceberg.handle-timestamp-without-timezone: false
# When the catalog type is hive, using spark session catalog automatically in the terminal to access hive tables
arctic.ams.terminal.local.using-session-catalog-for-hive: true
```

## 启动 AMS

进入到目录 arctic-x.y.z ， 执行 bin/ams.sh start 启动 AMS。

```shell
$ cd arctic-x.y.z
$ bin/ams.sh start
```

然后通过浏览器访问 http://localhost:1630 可以看到登录界面,则代表启动成功，登录的默认用户名和密码都是 admin。
