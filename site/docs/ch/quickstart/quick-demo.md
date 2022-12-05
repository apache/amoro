# Setup

在尝试使用 Arctic 之前，可以需要进行一些步骤来完成必要的环境准备，这里提供了两种方式来完成 Quick Start Demo 所必须的准备工作。

1. [Setup from docker](./setup-from-docker.md)

2. [Setup from binary release](./setup-from-binary-release.md)



# Prepare 1: create catalog

在浏览器打开 [http://localhost:1630](http://localhost:1630) 进入 Dashboard 页面，输入 admin/admin  登录。
点击侧边栏 Catalogs ，然后点击 Catalog List 下的 `+` 按钮，添加第一个 Catalog， 设置其名字为 `demo_catalog`。
请按照以下截图设置 Catalog 基本配置

![Create catalog](../images/quickstart/create-catalog.png)

注：对于 Hadoop 配置文件，如果是采用 Docker setup，配置文件在 `<ARCIT-WORKSPACE>/hadoop-config` 目录，否则不用上传任何文件。

注：对于 warehouse.dir，如果是本地部署，填写一个本地目录即可，需要有 Hadoop Username 配置的用户的访问权限。

# Prepare 2: start optimizers

AMS默认配置下会有一个类型为 local 的 optimizer group，这里需要在此 group 下创建一个 optimizer。 
进入 AMS 的 Optimizing 页面，选择 Optimizers。

![Optimizers](../images/quickstart/Optimizing.png)

点击 `Scale-Out` 按钮选择对应 `Optimizer Group` 并且配置 optimizer 并发度，点击OK后即完成了 optimizer 的创建。

![ScaleOut](../images/quickstart/ScaleOut.png)

# Step 1:  initialize table

在左侧菜单栏切换到  Terminal Tab 页面，通过这里我们可以进行建表，数据读写等操作，
Terminal SQL 语法为 Spark SQL 语法，具体支持的语法请参考 Spark 部分。

切换到 demo_catalog 后输入以下 SQL 初始化表。

```shell

CREATE DATABASE IF NOT EXISTS db;
USE db;
CREATE TABLE IF NOT EXISTS user (
    id INT,
    name string,
    ts TIMESTAMP,
    PRIMARY KEY(id)
) USING arctic 
PARTITIONED BY (days(ts));

INSERT OVERWRITE user VALUES 
(1, "eric", timestamp("2022-07-01 12:32:00")),
(2, "frank", timestamp("2022-07-02 09:11:00")),
(3, "lee", timestamp("2022-07-01 10:11:00"));

SELECT * FROM user ;

```

然后点击SQL Editor 上方的 RUN  按钮，等待 SQL 执行完成后，可以在 当前页面看到 SQL 的查询结果。

# Step 2:  execute flink upsert

如果是通过 Docker 完成环境准备，Flink Cluster 已经自动启动，可以直接通过，
[http://localhost:8081](http://localhost:8081)  打开 Flink Dashboard UI  
如果是通过二进制包进行本地部署，可以通过以下命令启动 Standalone 的 Flink Cluster。

```shell
cd <FLINK_DIR>
./bin/start-cluster.sh
```

然后启动 Flink SQL Client 

```shell
# 登录 Flink 容器, 非 docker 启动跳过此步骤
docker exec -it flink bash

./bin/sql-client.sh embedded
```

然后输入以下SQL （ 由于 Flink SQL Client 不支持批量 SQL 输入，需要逐条输入以下 SQL )

```SQL
-- 创建 catalog，非 docker 启动将 url 替换为 'thrift://localhost:1260/demo_catalog'
CREATE CATALOG arctic WITH (
  'type' = 'arctic',
  'metastore.url'='thrift://ams:1260/demo_catalog'
);

-- 创建 CDC Socket 源表
CREATE TABLE cdc_source(
  id      INT,
  name    STRING,
  op_time STRING
) WITH (
    'connector' = 'socket',
    'hostname' = 'localhost',
    'port' = '9999',
    'format' = 'changelog-csv',
    'changelog-csv.column-delimiter' = '|'
);

-- 关闭 Flink 引擎对 Delete 数据的过滤
set table.exec.sink.upsert-materialize=none;

-- 往 Arctic 表实时写入数据
INSERT INTO `arctic`.`db`.`user`
/*+OPTIONS('flink.max-continuous-empty-commits'='9223372036854775807')*/
SELECT id,
       name,
       CAST(TO_TIMESTAMP(op_time) AS TIMESTAMP(6) WITH LOCAL TIME ZONE) ts
FROM cdc_source;
```

然后重新打开一个 Terminal 窗口，在新的窗口执行以下命令往 socket 中写入 CDC 数据

```shell
nc -lk 9999
```

然后输入测试数据(需要回车以输入最后一行数据)。

```shell
INSERT|4|rock|2022-07-02 09:01:00
INSERT|5|jack|2022-07-02 12:11:40
INSERT|6|mars|2022-07-02 11:19:10

```


等待至少10s(取决于 flink-conf.yaml 中配置的 checkpoint 间隔）  然后打开 Dashboard 并进入 Terminal 页面,  执行 

```shell
SELECT * FROM db.user ORDER BY id ;
```

此时的预期结果为:

![Upsert result](../images/quickstart/upsert-result.png)

继续输入数据:

```shell
DELETE|1|eric|2022-07-01 12:32:00
INSERT|7|randy|2022-07-03 19:11:00
DELETE|4|rock|2022-07-02 09:01:00
DELETE|3|lee|2022-07-01 10:11:00

```

然后通过 Terminal 查询数据，预期数据为：

![Upsert result2](../images/quickstart/upsert-result2.png)













