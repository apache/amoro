# Arctic Dashboard

如[概述](index.md)中所述，AMS(Arctic Meta Service)是Arctic中负责元数据管理与结构优化的独立服务，使用Arctic的第一步就是部署AMS。

### 下载
AMS依赖 Java8 环境，你可以通过以下命令来检查 Java 是否已经安装正确。
```shell
java -version
```
可以通过这个[链接](https://github.com/NetEase/arctic/releases/download/v0.3.0-rc1/arctic-0.3.0-bin.zip)下载到最新版的AMS压缩包。

### 参数配置
AMS所有配置项都在`conf/config.yaml`文件中:

```yaml
  arctic.ams.server-host: 127.0.0.1                #optimizer连接ams时使用的地址，需配置ams所在机器ip或可访问的hostname
  arctic.ams.thrift.port: 1260                     #thrift服务端口
  arctic.ams.http.port: 1630                       #http服务端口，即ams页面端口
  arctic.ams.optimize.check.thread.pool-size: 10   #table optimize task任务运行时信息同步任务线程池大小
  arctic.ams.optimize.commit.thread.pool-size: 10  #optimize task异步commit线程池大小
  arctic.ams.expire.thread.pool-size: 10           #执行arctic表快照过期任务线程池大小
  arctic.ams.orphan.clean.thread.pool-size: 10     #删除arctic表过期快照及文件任务线程池大小
  arctic.ams.file.sync.thread.pool-size: 10        #同步表文件信息任务线程池大小
```
默认参数即可应对大多数场景，如果要在分布式环境下使用则需要修改`arctic.ams.server-host`配置为AMS所在机器的正确地址。

### 启动/重启/关闭
AMS安装包中提供了脚本文件`bin/ams.sh`用以处理AMS的日常运维需求，可以通过下面的命令完成AMS的启动、重启或关闭需求。
```shell
./bin/ams.sh start     #启动
./bin/ams.sh restart   #重启
./bin/ams.sh stop      #关闭
```
AMS完成启动后即可登录[AMS Dashboard](http://localhost:1630)来访问AMS的页面，默认的用户名密码为：`admin/admin`。

### 使用MySQL作为系统库
AMS默认使用Derby作为系统库存储自己的元数据，在生产环境下我们建议换成MySQL以提升系统的高可用。

**1.修改配置文件**

为使用MySQL作为AMS的系统库需要修改配置文件`conf/config.yaml`将Derby（默认系统数据库）配置修改为MySQL配置，需要修改配置项包括：

```yaml
arctic.ams.mybatis.ConnectionURL: jdbc:mysql://{host}:{port}/{database}  #MySQL服务url
arctic.ams.mybatis.ConnectionDriverClassName: com.mysql.jdbc.Driver      #MySQL jdbc driver
arctic.ams.mybatis.ConnectionUserName: {user}                            #MySQL访问用户名
arctic.ams.mybatis.ConnectionPassword: {password}                        #MySQL访问密码
arctic.ams.database.type: mysql                                          #系统库类型
```

???+ 注意

    目前只支持 MySQL 5.x 版本，不支持 MySQL 8。

**2.初始化MySQL表**

根据`conf/ams-init.sql`初始化AMS所需表：

```shell
mysql -h {mysql_host} -P {mysql_port} -u {user} -p {password} {database} < {AMS_HOME_DIR}/conf/ams-init.sql
```

**3.重启AMS**

参考 [启动/重启/关闭](#_3)。

### 导入hadoop集群

在默认的AMS配置中，我们已经初始化了一个名为`local`的基于AMS本地文件系统的集群以方便你的测试。
生产环境中我们需要导入hadoop集群，为此我们需要在AMS的配置中新增一个catalog，并在创建和使用arctic表时使用该catalog。

新增catalog通过在`conf/config.yaml`中`catalogs`中增加以下配置：

```yaml
  - name:                           #catalog名称
    type: hadoop
    storage_config:
      storage.type: hdfs
      core-site:                    #hadoop集群core-site.xml配置文件绝对路径
      hdfs-site:                    #hadoop集群hdfs-site.xml配置文件绝对路径
    auth_config:
      type: SIMPLE                  #认证类型，目前支持KERBEROS和SIMPLE两种类型
      hadoop_username: hadoop       #访问hadoop集群用户名
    properties:
      warehouse.dir: hdfs://default/default/warehouse         #hadoop集群仓库地址
```

如果需要使用KERBEROS认证方式访问hadoop集群可以修改catalog中auth_config如下：

```yaml
    auth_config:
      type: KERBEROS                       #认证类型，目前支持KERBEROS和SIMPLE两种类型
      principal: user/admin@EXAMPLE.COM    #kerberos认证主体
      keytab: /etc/user.keytab             #kerberos密钥文件
      krb5: /etc/user.conf                 #kerberos配置文件
```

???+ 注意

    修改配置文件后需重启AMS服务才可生效，参考[启动/重启/关闭](#_3)。

### 使用Flink执行结构优化

在默认的AMS配置中，我们已经初始化了一个名为`default`的optimize group，它会在AMS本地新启动一个进程完成local catalog中表的结构优化。
生产环境中我们通常在yarn集群红使用flink来完成表的结构优化。

**1.新增flink类型container**

新增container通过在`conf/config.yaml`中`containers`增加以下配置:

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

**2.新增optimizer group**

新增optimizer group通过在`conf/config.yaml`中`optimize_group`增加以下配置:

```yaml
  - name: flinkOG                     #optimize group名称，在ams页面中可见
    container: flinkContainer         #optimize group对应flink类型container名称
    properties:
      taskmanager.memory: 1024        #flink job TM内存大小，单位为MB
      jobmanager.memory: 1024         #flink job JM内存大小，单位为MB
```

???+ 注意

    修改配置文件后需重启AMS服务才可生效，参考[启动/重启/关闭](#_3)。

**3.启动optimizer**

新配置的optimize group中还未启动任何的optimizer，所以还需要登录[AMS Dashboard](http://localhost:1630)手动启动至少一个optimizer。




