# Arctic Dashboard

如[概述](../index.md)中所述，AMS(Arctic Meta Service) 是 Arctic 中负责元数据管理与结构优化的独立服务，使用 Arctic 的第一步就是部署 AMS。

## 下载
AMS 依赖 Java8 环境，你可以通过以下命令来检查 Java 是否已经安装正确。
```shell
java -version
```
可以通过这个[链接](https://github.com/NetEase/arctic/releases/download/v0.3.2-rc1/arctic-0.3.2-bin.zip)下载到最新版的AMS压缩包。

## 参数配置
AMS所有配置项都在 `conf/config.yaml` 文件中:

```yaml
  arctic.ams.server-host.prefix: "127.0.0.1"       #AMS的地址前缀，可以填写完整的地址，也可以只填写用于匹配真实地址的前缀(例如"192.168"，注意必须加双引号)
  arctic.ams.thrift.port: 1260                     #thrift服务端口
  arctic.ams.http.port: 1630                       #http服务端口，即ams页面端口
  arctic.ams.optimize.check.thread.pool-size: 10   #table optimize task任务运行时信息同步任务线程池大小
  arctic.ams.optimize.commit.thread.pool-size: 10  #optimize task异步commit线程池大小
  arctic.ams.expire.thread.pool-size: 10           #执行arctic表快照过期任务线程池大小
  arctic.ams.orphan.clean.thread.pool-size: 10     #删除arctic表过期快照及文件任务线程池大小
  arctic.ams.file.sync.thread.pool-size: 10        #同步表文件信息任务线程池大小
```
默认参数即可应对大多数场景，如果要在分布式环境下使用则需要修改 `arctic.ams.server-host` 配置为AMS所在机器的正确地址。

## 启动/重启/关闭
AMS 安装包中提供了脚本文件 `bin/ams.sh` 用以处理AMS的日常运维需求，可以通过下面的命令完成AMS的启动、重启或关闭需求。
```shell
./bin/ams.sh start     #启动
./bin/ams.sh restart   #重启
./bin/ams.sh stop      #关闭
```
AMS完成启动后即可登录 [AMS Dashboard](http://localhost:1630) 来访问 AMS 的页面，默认的用户名密码为：`admin/admin`。

## 使用 MySQL 作为系统库
AMS 默认使用 Derby 作为系统库存储自己的元数据，在生产环境下我们建议换成MySQL以提升系统的高可用。支持 Mysql 5.x 到8.0版本。

**1.修改配置文件**

为使用 MySQL 作为 AMS 的系统库需要修改配置文件`conf/config.yaml`将 Derby（默认系统数据库）配置修改为 MySQL 配置，需要修改配置项包括：

```yaml
arctic.ams.mybatis.ConnectionURL: jdbc:mysql://{host}:{port}/{database}  #MySQL 服务url
arctic.ams.mybatis.ConnectionDriverClassName: com.mysql.jdbc.Driver      #MySQL jdbc driver
arctic.ams.mybatis.ConnectionUserName: {user}                            #MySQL 访问用户名
arctic.ams.mybatis.ConnectionPassword: {password}                        #MySQL 访问密码
arctic.ams.database.type: mysql                                          #系统库类型
```


**2.初始化 MySQL 表**

根据 `conf/mysql/{arctic-version}-init.sql` 初始化 AMS 所需表：

```shell
mysql -h {mysql_host} -P {mysql_port} -u {user} -p {password} {database} < {AMS_HOME_DIR}/conf/mysql/{arctic-version}-init.sql
```

???+ 注意

    如需升级 ams 版本，请根据 `conf/mysql/upgrade-*.sql` 的升级语句进行升级。

**3.重启 AMS**

参考 [启动/重启/关闭](#_3)。

## 导入集群

在默认的 AMS 配置中，我们已经初始化了一个名为 `local` 的基于 AMS 本地文件系统的集群以方便你的测试。

### 导入Hadoop集群

生产环境中我们如果需要导入 Hadoop 集群，需要在AMS的配置中新增一个 Arctic catalog，并在创建和使用 Arctic 表时使用该 catalog。

新增 Arctic catalog 通过在 `conf/config.yaml` 中 `catalogs` 中增加以下配置：

```yaml
  - name:                           #catalog名称
    type: hadoop                    #catalog类型配置为hadoop
    storage_config:
      storage.type: hdfs
      core-site:                    #Hadoop集群core-site.xml配置文件绝对路径
      hdfs-site:                    #Hadoop集群hdfs-site.xml配置文件绝对路径
    auth_config:
      type: SIMPLE                  #认证类型，目前支持KERBEROS和SIMPLE两种类型
      hadoop_username: hadoop       #访问Hadoop集群用户名
    properties:
      warehouse.dir: hdfs://default/default/warehouse         #Hadoop集群仓库地址
```

### 导入Hive集群

生产环境中我们如果需要导入 Hive 集群并利用 Arctic 提供的 Hive 兼容相关功能，需要在AMS的配置中新增一个 Arctic Hive catalog，并在创建和使用 Arctic 表时使用该 Catalog。

新增 Arctic Hive catalog 通过在 `conf/config.yaml` 中 `catalogs` 中增加以下配置：

```yaml
  - name:                           #catalog名称
    type: hive                      #catalog类型配置为 hive
    storage_config:
      storage.type: hdfs
      core-site:                    #Hive集群core-site.xml配置文件绝对路径
      hdfs-site:                    #Hive集群hdfs-site.xml配置文件绝对路径
      hive-site:                    #Hive集群hive-site.xml配置文件绝对路径
    auth_config:
      type: SIMPLE                  #认证类型，目前支持KERBEROS和SIMPLE两种类型
      hadoop_username: hadoop       #访问Hive集群用户名
    properties:
      warehouse.dir: hdfs://default/default/warehouse         #Hive集群仓库地址
```

### 修改认证方式

如果需要使用 KERBEROS 认证方式访问 Hadoop 或 Hive 集群可以修改 Catalog 中 auth_config 如下：

```yaml
    auth_config:
      type: KERBEROS                       #认证类型，目前支持KERBEROS和SIMPLE两种类型
      principal: user/admin@EXAMPLE.COM    #kerberos认证主体
      keytab: /etc/user.keytab             #kerberos密钥文件
      krb5: /etc/user.conf                 #kerberos配置文件
```

???+ 注意

    修改配置文件后需重启 AMS 服务才可生效，参考[启动/重启/关闭](#_3)。

## 使用 Flink 执行结构优化

在默认的 AMS 配置中，我们已经初始化了一个名为 `default` 的 optimizer group，它会在 AMS 本地新启动一个进程完成 local catalog 中表的结构优化。
生产环境中我们通常在 Yarn 集群中使用 Flink 来完成表的结构优化。

**1.新增 Flink 类型 container**

新增 container 通过在 `conf/config.yaml` 中 `containers` 增加以下配置:

```yaml
  - name: flinkContainer                                 #Container名称
    type: flink                                          #Container类型，目前支持flink和local两种
    properties:
      FLINK_HOME: /opt/flink/                            #flink安装目录，用于启动flink类型Optimizer
      HADOOP_CONF_DIR: /etc/hadoop/conf/                 #flink任务运行所需hadoop集群配置文件所在目录
      HADOOP_USER_NAME: hadoop                           #提交到yarn集群用户
      JVM_ARGS: -Djava.security.krb5.conf=/opt/krb5.conf #flink任务启动参数，例如需要指定kerberos配置文件
      FLINK_CONF_DIR: /etc/hadoop/conf/                  #flink配置文件所在目录
```

**2.新增 optimizer group**

新增 optimizer group 通过在 `conf/config.yaml` 中 `optimize_group` 增加以下配置:

```yaml
  - name: flinkOG                     #optimize group名称，在ams页面中可见
    container: flinkContainer         #optimize group对应flink类型container名称
    properties:
      taskmanager.memory: 1024        #flink job TM内存大小，单位为MB
      jobmanager.memory: 1024         #flink job JM内存大小，单位为MB
```

???+ 注意

    修改配置文件后需重启 AMS 服务才可生效，参考[启动/重启/关闭](#_3)。

**3.启动 optimizer**

新配置的 optimizer group 中还未启动任何的 optimizer，所以还需要登录 [AMS Dashboard](http://localhost:1630) 手动启动至少一个 optimizer。


## AMS开启高可用


**1.部署Apache Zookeeper**

参考 [Apache QuickStart](https://zookeeper.apache.org/doc/r3.7.1/zookeeperStarted.html)

**2.新增配置**

```yaml
  arctic.ams.ha.enable: true                        #是否开启高可用
  arctic.ams.cluster.name: default                  #ams集群名称，一个集群内会保持一主多备
  arctic.ams.zookeeper.server: 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183   #zookeeper server地址
```

**3.重启 AMS**

参考 [启动/重启/关闭](#_3)。

**4.更改引擎中的uri**

AMS开启高可用后引擎端配置catalog uri格式变为：zookeeper://{zookeeper server}/{cluster name}/{catalog name}

例如上述增加的配置对应catalog uri为：zookeeper://127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183/default/local_catalog