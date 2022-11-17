# MultiDelegateSessionCatalog

## 概述

在实际生产场景中会遇到在 `spark_catalog` 下同时使用`arctic` 和 `iceberg` 以及 `hive` 表的需求，
但是 `spark_catalog` 只能配置一个 `SessionCatalog`，这时候就可以使用 `MultiDelegateSessionCatalog` 来解决这个问题。

`MultiDelegateSessionCatalog` 是一个 `SessionCatalog` 的实现，它可以同时使用多个 `SessionCatalog`，并且可以通过配置来指定delegate行为的顺序。

## 示例



```shell

${SPARK_HOME}/bin/spark-sql \
    --conf spark.sql.extensions=com.netease.arctic.spark.ArcticSparkExtensions,org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions \
    --conf spark.sql.catalog.spark_catalog=com.netease.arctic.spark.MultiDelegateSessionCatalog \
    --conf spark.sql.catalog.spark_catalog.delegates=arctic,iceberg \
    --conf spark.sql.catalog.spark_catalog.arctic=com.netease.arctic.spark.ArcticSessionCatalog \
    --conf spark.sql.catalog.spark_catalog.arctic.url=thrift://${AMS_HOST}:${AMS_PORT}/${AMS_CATALOG_NAME} \
    --conf spark.sql.catalog.spark_catalog.iceberg=org.apache.iceberg.spark.SparkSessionCatalog \
    --conf spark.sql.catalog.spark_catalog.iceberg.type=hive 

```

以上命令中，`spark_catalog` 会同时使用 `arctic` 和 `iceberg` 两个 `SessionCatalog`，并且 `arctic` 的优先级高于 `iceberg`。

## 配置

| 配置项                   | 配置样例                                                                                 | 描述                                    |
|-----------------------|--------------------------------------------------------------------------------------|---------------------------------------|
| delegates             | spark.sql.catalog.spark_catalog.delegates=iceberg,arctic                             | 指定需要delegate的多个catalog的顺序， catalog之间用逗号分隔 |
 | ${catalog}            | spark.sql.catalog.spark_catalog.iceberg=org.apache.iceberg.spark.SparkSessionCatalog | 指定catalog的实现类，${catalog} 为上面配置的catalog名称 |
| ${catalog}.${property} | spark.sql.catalog.spark_catalog.iceberg.type=hive                                    | 指定catalog 的 properties                |

## 注意事项

  * `MultiDelegateSessionCatalog` 会按照配置的顺序来delegate，如果第一个delegate的catalog中存在表，则不会再去后面的catalog中查找表。
  * `MultiDelegateSessionCatalog` 委托管理的 Catalog 必须是 `org.apache.spark.sql.connector.catalog.CatalogExtension` 的实现类，否则会抛出异常。