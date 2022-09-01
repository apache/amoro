# Spark Configuration

## Catalogs
Spark Catalog 配置是通过修改 Spark 配置下的 `spark.sql.catalog` 相关属性来实现的。

配置一个名为 `hive_catalog` 类型为 hive 的 Catalog：
```
spark.sql.catalog.hive_catalog=com.netease.arctic.spark.ArcticSparkCatalog
spark.sql.catalog.hive_catalog.type = hive
spark.sql.catalog.hive_catalog.url=thrift://${AMS_HOST}:${AMS_PORT}/${AMS_CATALOG_NAME}
```

配置一个名为 `local_catalog` 类型为 hadoop 的 Catalog：
```
spark.sql.catalog.local_catalog=com.netease.arctic.spark.ArcticSparkCatalog
spark.sql.catalog.local_catalog.type = hive
spark.sql.catalog.local_catalog.url=thrift://${AMS_HOST}:${AMS_PORT}/${AMS_CATALOG_NAME}
```

### 配置 session catalog
要使用 Spark 内置的 Catalog 来支持 Arctic, 请配置 `spark_catalog` 为 `ArcticSparkSessionCatalog`：
```
spark.sql.catalog.spark_catalog=com.netease.arctic.spark.ArcticSparkSessionCatalog
```

Arctic Spark 提供一个参数来控制 session catalog 的行为：`arctic.sql.delegate.enable`，该参数默认为`true` 。  
将该参数设置为 `true` 表示ArcticSessionCatalog 会代理 hive 表为 arctic 表。
```
set arctic.sql.delegate.enable=true
```
将该参数设置为 `false` 表示不进入代理模式：
```
set arctic.sql.delegate.enable=false
```