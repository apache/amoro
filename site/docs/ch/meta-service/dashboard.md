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
可以通过 AMS Dashboard 提供的 Catalog 管理功能导入更多测试或线上集群。

### 新增 Catalog

Arctic 中 catalog 为一组表的命名空间，在 catalog 之下会再分到不同的 database 中，database 下则是不同的 table，catalog.database.table 组成了一张表在 Arctic 中唯一的名称。
在实际应用场景下 catalog 一般对应着一个元数据服务，比如大数据中经常使用的 Hive Metastore，当然 Arctic MetaService 也可以作为一个元数据服务，
另外定义 catalog 时还需要选择它下面所使用的表格式，当前 Arctic 支持的表格式包括：Iceberg 与 Hive。

创建 catalog 的详细参数如下：

|配置项|默认值|类型|可用值|描述|
|--- |--- |--- |--- |--- |
|name|(none)|String|只支持数字、字母、_、- , 以字母开头|catalog名称|
|metastore|Arctic Metastore|String|Arctic Metastore(代表使用AMS存储元数据),Hive Metastore(代表使用HMS存储元数据),Hadoop(对应iceberg的Hadoop catalog), Custom(其他iceberg的catalog实现)|metastore类型|
|table format|(none)|String|Hive, Iceberg|数据存储的表格式，当前只有 metastore 类型 为Hive Metastore 时同时支持 Hive/Iceberg 两种，其他 metastore 类型均只支持 Iceberg, 当前一个Metastore只支持一种TableFormat, 未来会支持多种|
|core-site|(none)|file| 上传hadoop集群的core-site.xml |core-site.xml|
|hdfs-site|(none)|file| 上传hadoop集群的hdfs-site.xml |hdfs-site.xml|
|hive-site|(none)|file| 上传Hive的hive-site.xml |hive-site.xml|
|auth|(none)|string| SIMPLE, KERBEROS |指定Hadoop集群的认证方式|
|hadoop_username|(none)|string| Hadoop 用户名 |SIMPLE 认证方式下所使用的用户名|
|keytab|(none)|file| 上传 keytab 文件 | KERBEROS 认证方式下所使用的 keytab 文件 |
|principal|(none)|string| keytab 对应的principal |keytab 对应的principal|
|krb5|(none)|string| kerberos的krb5.conf配置文件 |kerberos的krb5.conf配置文件|
|Properties|(none)|Map|(none)|catalog需要添加的配置; 当metastore为**Custom**时，Properties必须定义 **catalog-impl**|

### 推荐
- 对于 Hive 用户，如果希望仍然能兼容已有的 Hive 使用习惯，建议选择创建 metastore 为 Hive Metastore，table format 为 Hive 的 Catalog
- 对于已经熟悉 Iceberg 的用户，希望体验 Arctic 带来了诸多新特性建议创建 metastore 为 Arctic Metastore，table format 为 Iceberg 的 Catalog
- 对于已经熟悉 Iceberg 的用户，并且希望让 Arctic 自动优化已经存在 Iceberg 表，则按实际情况选择 metastore 类型，table format 选择 Iceberg

???+ 注意

    当前 Arctic 支持的 Hadoop 版本包括：2.x、3.x，支持 的 Hive 版本包括：2.x、3.x。

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


## Terminal 相关参数配置

在 AMS 的默认配置文件 `conf/config.yaml` 中，支持如下有关 Terminal 的配置及其默认值

```yaml
  arctic.ams.terminal.backend: local              # Terminal SQL 执行引擎的实现，默认值为 local, 支持 local,kyuubi 两种引擎
  arctic.ams.terminal.result.limit: 1000          # Terminal SQL 执行查询时结果集最大抓取数量
  arctic.ams.terminal.stop-on-error: false        # 在执行多行的SQL脚本时，遇到执行错误时是否停止
  arctic.ams.terminal.session.timeout: 30         # Terminal Session 没有执行SQL时，多久后回收 Session 资源
```


### 使用 Kyuubi 作为 Terminal Backend

如果您希望在生产环境中使用 Terminal 执行 DML 类型的 SQL，强烈建议您使用 Kyuubi 作为 AMS Terminal 的 SQL执行引擎.

Kyuubi 是一个多租户的大数据 SQL Gateway, 有关 Kyuubi 的知识，
您可以从 [Kyuubi 的官网](https://kyuubi.apache.org/docs/latest/index.html) 进一步了解

在准备好 Kyuubi 环境后，修改 `conf/config.yml` 并重启 AMS.

```yaml
  # 新增或修改以下配置的值为 kyuubi 
  arctic.ams.terminal.backend: kyuubi

  # 新增以下配置
  arctic.ams.terminal.kyuubi.jdbc.url: jdbc:hive2://<endpoint>/;<params>    # kyuubi 的JDBC 连接信息
```

**关于Kyuubi的认证**

目前对于访问带 kerberos 的集群，terminal 使用 catalog 中配置的 keytab 信息创建 Connection. 

* 如果 KyuubiServer 开启了 Kerberos 认证，请确保此 Principal 可以访问 KyuubiServer. 
* 如果 KyuubiServer 采用账户密码认证，需要在 jdbc.url 中配置好认证信息

