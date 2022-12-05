# Setup From Docker

Arctic 提供了基于 Docker 部署的 Quick Start 环境，基于此部分的文档可以完成后续 Quick Demo 的所有环境准备工作。


# Requirements

在开始基于 Docker 部署 Arctic 前，请确保您的主机上已经安装了 Docker-Compose 环境，关于 Docker 如何安装请参考 
[https://docs.docker.com/get-docker/](https://docs.docker.com/get-docker/)

注：建议在 Linux 或 MacOs 进行操作，如果您使用的是 Windows 系统，可以考虑使用 WSL2 ，关于如何开启 WSL2 以及安装 Docker 
请参考 [https://docs.docker.com/desktop/install/windows-install/](https://docs.docker.com/desktop/install/windows-install/)

在完成 Docker 安装后，请确保安装了 Docker-Compose 工具，关于 Docker-Compose 工具如何安装请参考
[https://github.com/docker/compose-cli/blob/main/INSTALL.md](https://github.com/docker/compose-cli/blob/main/INSTALL.md)


# Bring up Demo Cluster

在开始之前请准备一个干净的目录以作为 Arctic Demo 演示的 Workspace。
然后执行以下 Shell 命令以通过 Docker 拉起 Demo Cluster

```shell
cd <ARCTIC-WORKSPACE>
wget https://raw.githubusercontent.com/NetEase/arctic/master/docker/demo-cluster.sh
VERSION=0.4.0 && bash demo-cluster.sh -v $VERSION start
```


执行完成以上命令后，在当前目录下会有一个 data 目录以供不同的 docker container 之间共享文件，
通过 `docker ps --format "table {{.ID}}\t{{.Names}}\t{{.Image}}\t{{.Status}}" ` 命令可以看到正在运行中的容器，包括

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