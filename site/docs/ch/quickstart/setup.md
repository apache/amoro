这篇文档介绍了通过 Docker-Compose 和发布包两种方式部署 Arctic demo 环境，以帮助你快速上手，如果希望通过编译源码部署，请参阅 [Deployment](../guides/deployment.md)。

## Setup from Docker-Compose

Arctic 提供了基于 Docker-Compose 部署 quick start 环境，基于这篇文档可以完成后续 [Quick Demo](quick-demo.md) 的所有环境准备工作。

### Requirements

在开始基于 Docker 部署 Arctic 前，请确保您的主机上已经安装了 Docker-Compose 环境，关于 Docker 如何安装请参考：[Docker 安装](https://docs.docker.com/get-docker/)

> 建议在 Linux 或 MacOs 进行操作，如果您使用的是 Windows 系统，可以考虑使用 WSL2 ，关于如何开启 WSL2 以及安装 Docker 
> 请参考 [Windows 安装](https://docs.docker.com/desktop/install/windows-install/)

在完成 Docker 安装后，请确保安装了 Docker-Compose 工具：[Docker-Compose 安装](https://github.com/docker/compose-cli/blob/main/INSTALL.md)

### Bring up demo cluster

在开始之前请准备一个干净的目录作为 Arctic Demo 演示的 Workspace，获取 Arctic demo 部署脚本：

```shell
cd <ARCTIC-WORKSPACE>
wget https://raw.githubusercontent.com/NetEase/arctic/master/docker/demo-cluster.sh
```

执行以下 Shell 命令以通过 Docker-Compose 拉起 demo cluster：

```shell
VERSION=0.4.0 && bash demo-cluster.sh -v $VERSION start
```

完成以上命令后，在当前目录下会有一个 data 目录以供不同的 docker container 之间共享文件，
通过 `docker ps --format "table {{.ID}}\t{{.Names}}\t{{.Image}}\t{{.Status}}" ` 命令可以看到正在运行中的容器，包括：

```shell

docker ps --format "table {{.ID}}\t{{.Names}}\t{{.Status}}"

CONTAINER ID   NAMES                           STATUS
eee36c86b89f   datanode                        Up About a minute
a77265d3b3d0   flink                           Up About a minute
ed0773a69e56   lakehouse-benchmark-ingestion   Up About a minute
76ee38376c50   lakehouse-benchmark             Up About a minute
f1af63be0154   namenode                        Up About a minute
ff352a0008d5   mysql                           Up About a minute
e76597faa82b   ams                             Up About a minute
```

## Setup from binary release

如果不方便安装 Docker 以及相关工具，也可以通过 Arctic 的发布包直接部署 Arctic demo cluster。

### Requirements

1. 在开始之前，请先确保安装了 Java 8 并且设置好了 JAVA_HOME 环境变量。
2. 请确保环境变量中没有 HADOOP_HOME，HADOOP_CONF_DIR，如果有请先 unset 这些环境变量

### Setup AMS

开始之前请准备一个干净的目录作为 Arctic demo 环境的 workspace，执行以下命令下载 Arctic 并启动 AMS：

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

在执行完成以上命令后，用浏览器访问 [http://127.0.0.1:1630/](http://127.0.0.1:1630/) 并通过 admin/admin 登录系统。
如果顺利登陆系统则表示 AMS 部署成功。

### Setup Flink environment

在进入 Quick demo 之前，需要部署 Flink 执行环境。执行以下命令下载 Flink 二进制分发包：


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


