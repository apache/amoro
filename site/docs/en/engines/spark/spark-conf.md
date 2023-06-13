# Spark Configuration

## Catalogs Configuration

### 使用独立的 ArcticSparkCatalog

Spark 从 3.x 版本开始支持配置独立的 Catalog, 如果您希望在一个独立的 Catalog 使用 Arctic mixed-format table,
您可以采用如下方式配置。

```properties
spark.sql.catalog.arctic_catalog=com.netease.arctic.spark.ArcticSparkCatalog
spark.sql.catalog.arctic_catalog.url=thrift://${AMS_HOST}:${AMS_PORT}/${AMS_CATALOG_NAME_HIVE}
```

然后在 Spark SQL Client 中执行

```sql
use arctic_catalog;
```

以切换到对应的 catalog 下。

当然您也可以直接使用三元组 `arctic_catalog.{db_name}.{table_name}` 的方式访问 Arctic mixed-format table

您也可以以如下的方式将 Arctic Catalog 的实现配置到 Spark 的默认 Catalog 下。这样就不用使用 `use catalog` 的指令切换默认的
catalog。

```properties
spark.sql.catalog.spark_catalog=com.netease.arctic.spark.ArcticSparkCatalog
spark.sql.catalog.spark_catalog.url=thrift://${AMS_HOST}:${AMS_PORT}/${AMS_CATALOG_NAME_HIVE}
```

在使用独立的 ArcticSparkCatalog 场景下，在对应的 Catalog 将只能创建和访问 Arctic mixed-format Table。

### 使用 ArcticSparkSessionCatalog

如果你想在 Spark 中同时访问原有的 Hive table 或 Spark datasource table 以及 Arctic mixed-format table，您可以使用
ArcticSparkSessionCatalog 作为 Spark 默认 Catalog 的实现，配置方式如下。

```properties
spark.sql.catalog.spark_catalog=com.netease.arctic.spark.ArcticSparkSessionCatalog
spark.sql.catalog.spark_catalog.url=thrift://${AMS_HOST}:${AMS_PORT}/${AMS_CATALOG_NAME_HIVE}
```

在使用 ArcticSparkSessionCatalog 作为 `spark_catalog` 实现时。其行为如下：

- Load Table: 将优先解析 `db_name.table_name` 为 Arctic mixed-format table,
  如果解析失败，则再次委托 Spark 内置的实现去解析该 table 标识符。
  根据 `spark.sql.catalogImplementation` 的配置值，如果是 `hive` 则会去 Hive Metastore 中进行解析
  如果是 `memory` 则根据 `spark.sql.warehouse.dir` 配置的路径下根据目录进行解析

- Create Table: Create Table 的行为由建表SQL中的 `using {provider}` 决定，如果有 `using arctic` 标识，则会创建一张
  Arctic mixed-format table 并注册在配置的 AMS 中。否则会调用 Spark 的默认实现进行建表。

在使用 ArcticSparkSessionCatalog 时有以下几点需要注意：

- ArcticSparkSessionCatalog 只能配置在 spark_catalog 下
- 当 `{db_name}.{table_name}` 和 Spark 原有的表发生冲突时，
  在 load 表的行为中会覆盖 spark 原有的表优先解析为 Arctic mixed-format

## 高可用配置

如果 AMS 开启了高可用，可以以如下方式配置 `spark.sql.catalog.{catalog_name}.url` 属性以获得更高的可用性。

```properties
spark.sql.catalog.arctic_catalog=com.netease.arctic.spark.ArcticSparkCatalog
spark.sql.catalog.arctic_catalog.url=zookeeper://{zookeeper-endpoint-list}/{cluster-name}/{catalog-name}
```

其中:

- zookeeper-endpoint-list: 是以 `,` 分割的 host:port
  组，一个合法的取值可以是 `192.168.1.1:2181,192.168.1.2:2181,192.168.1.3:2181`
- cluster-name: 是在 AMS 的配置文件 `config.yml` 中配置的 `ams.cluster.name` 的值，用于标识 zookeeper 上的用户空间
- catalog-name: 是 AMS 上 Catalog 的名字。
