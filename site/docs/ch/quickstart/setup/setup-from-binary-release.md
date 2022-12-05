# Setup From Binary Release

Arctic 提供了基于 Docker Compose 部署的 Quick Start 环境，
如果您的环境不方便安装 Docker 以及相关工具，您也可以尝试直接通过以下方式直接部署 Arctic Demo Cluster.

# Requirements

1. 在开始之前，请先确保您安装了 Java 8 并且设置好了 JAVA_HOME 环境变量。
2. 请确保您的环境变量中没有 HADOOP_HOME，HADOOP_CONF_DIR，如果有请先 unset 这些环境变量


# Download Required Packages

在开始之前请准备一个干净的目录以作为 Arctic Demo 演示的 Workspace。
然后执行以下命令以下载 Arctic 并启动 Arctic Metastore Service。

```shell

cd <ARCTIC-WORKSPACE>

export ARCTIC_VERSION=0.4.0
export RELEASE_TAG=v0.4.0-rc1
# 下载二进制分发包
wget https://github.com/NetEase/arctic/releases/download/${RELEASE_TAG}/arctic-${ARCTIC_VERSION}-bin.zip

# 解压文件
unzip arctic-${ARCTIC-VERSION}-bin.zip
cd arctic-${ARCTIC-VERSION}
./bin/ams.sh start


```

在执行完成以上命令后，请通过浏览器访问 [http://127.0.0.1:1630/](http://127.0.0.1:1630/)  并通过 admin/admin 登录系统。
如果顺利登陆系统则表示 AMS 部署成功。
执行以下命令，下载 Flink 二进制分发包


```shell
cd <ARCTIC-WORKSPACE>

FLINK_VERSION=1.15.3
FLINK_MAJOR_VERSION=1.15
ARCTIC_VERSION=0.4.0
ARCTIC_RELEASE=v0.4.0
APACHE_FLINK_URL=archive.apache.org/dist/flink
HADOOP_VERSION=2.7.5

## 下载二进制分发包，目前 Arctic 使用 Scala 2.12 的分发包
wget ${APACHE_FLINK_URL}/flink-${FLINK_VERSION}/flink-${FLINK_VERSION}-bin-scala_2.12.tgz
## 解压文件
tar -zxvf flink-${FLINK_VERSION}-bin-scala_2.12.tgz

cd flink-${FLINK_VERSION}
# 下载 hadoop 依赖
wget https://repo1.maven.org/maven2/org/apache/flink/flink-shaded-hadoop-2-uber/${HADOOP_VERSION}-10.0/flink-shaded-hadoop-2-uber-${HADOOP_VERSION}-10.0.jar
# 下载 arctic flink connector
wget https://github.com/NetEase/arctic/releases/download/${ARCTIC_RELEASE}/arctic-flink-runtime-${FLINK_MAJOR_VERSION}-${ARCTIC_VERSION}.jar

# 将必须的jar包复制到 lib 目录下
mv arctic-flink-runtime-${FLINK_MAJOR_VERSION}-${ARCTIC_VERSION}.jar lib
mv flink-shaded-hadoop-2-uber-${HADOOP_VERSION}-10.0.jar lib
cp examples/table/ChangelogSocketExample.jar lib
```

然后修改 flink-conf.yaml 文件

```shell
vim conf/flink-conf.yaml
```

移除以下配置项的注释，并将值修改为以下值

```shell
# 需要同时运行两个流任务，增加 slot
taskmanager.numberOfTaskSlots: 4

# 开启 Checkpoint。Checkpoint 间隔为 Flink 写入数据延迟，这里修改小一些
execution.checkpointing.interval: 5s
```

注释掉以下默认配置

```shell
# rest.bind-address: localhost
```


