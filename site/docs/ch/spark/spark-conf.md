# Spark Configuration

## Catalogs
### 配置 Arctic Spark Catalog
如果您想要配置多个 Catalog ，即使用三元组的方式访问表。您可以配置多个 Arctic Spark Catalog，如您想要访问 Arctic 表，则可使用 Hadoop 类型的 Catalog，如您想要访问 Hive 表，则可使用 Hive 类型的 Catalog。

Arctic Spark Catalog 的配置是通过修改 Spark 配置下的 `spark.sql.catalog` 相关属性来实现的。

配置一个名为 `hive_catalog` 类型为 Hive 的 Catalog，您需要将 Catalog type 指定为 Hive，并将 Catalog url 指定为 Hive 类型的 Catalog：
```
spark.sql.catalog.hive_catalog=com.netease.arctic.spark.ArcticSparkCatalog
spark.sql.catalog.hive_catalog.type = hive
spark.sql.catalog.hive_catalog.url=thrift://${AMS_HOST}:${AMS_PORT}/${AMS_CATALOG_NAME_HIVE}
```

配置一个名为 `local_catalog` 类型为 Hadoop 的 Catalog，您需要将 Catalog type 指定为 Hadoop，并将 Catalog url 指定为 Hadoop 类型的 Catalog：
```
spark.sql.catalog.local_catalog=com.netease.arctic.spark.ArcticSparkCatalog
spark.sql.catalog.local_catalog.type = hadoop
spark.sql.catalog.local_catalog.url=thrift://${AMS_HOST}:${AMS_PORT}/${AMS_CATALOG_NAME_HADOOP}
```

配置完成后，您可以通过 `catalogName.db.table` 的方式来访问表。


### 配置 Arctic Spark Session Catalog
如果您希望使用二元组的方式，可以使用 Spark 内置的 Catalog 来支持 Arctic, 请配置 `spark_catalog` 为 `ArcticSparkSessionCatalog`：
```
spark.sql.catalog.spark_catalog=com.netease.arctic.spark.ArcticSparkSessionCatalog
```
您可以把 Spark 内置的 Catalog：`spark_catalog`配置为您想要的 Catalog 类型来实现访问不同的表。  

Arctic Spark 提供一个参数来控制 session catalog 的行为：`spark.arctic.sql.delegate.enabled`，该参数默认为 `true`。  

您可以在 Spark 的配置文件中修改该配置，也可以在启动 Spark 时通过命令行参数 `--conf spark.arctic.sql.delegate.enabled=${OPTION}` 来设置该参数。

将该参数设置为 `true` 表示ArcticSessionCatalog 会代理 Hive 表为 Arctic 表。
```
spark.arctic.sql.delegate.enabled=true
```
将该参数设置为 `false` 表示不进入代理模式，将使用 Spark 默认的 Catalog：
```
spark.arctic.sql.delegate.enabled=false
```