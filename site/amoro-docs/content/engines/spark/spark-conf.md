---
title: "Spark Configuration"
url: spark-configuration
aliases:
    - "spark/configuration"
menu:
    main:
        parent: Spark
        weight: 200
---
<!--
 - Licensed to the Apache Software Foundation (ASF) under one or more
 - contributor license agreements.  See the NOTICE file distributed with
 - this work for additional information regarding copyright ownership.
 - The ASF licenses this file to You under the Apache License, Version 2.0
 - (the "License"); you may not use this file except in compliance with
 - the License.  You may obtain a copy of the License at
 -
 -   http://www.apache.org/licenses/LICENSE-2.0
 -
 - Unless required by applicable law or agreed to in writing, software
 - distributed under the License is distributed on an "AS IS" BASIS,
 - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 - See the License for the specific language governing permissions and
 - limitations under the License.
 -->
# Spark Configuration

## Catalogs configuration

### Using Mixed-Format in a standalone catalog

Starting from version 3.x, Spark supports configuring an independent Catalog.
If you want to use a Mixed-Format table in a standalone Catalog, you can create a mixed_catalog and load catalog 
metadata from AMS with following properties:

```properties
spark.sql.catalog.mixed_catalog=org.apache.amoro.spark.MixedFormatSparkCatalog
spark.sql.catalog.mixed_catalog.url=thrift://${AMS_HOST}:${AMS_PORT}/${AMS_CATALOG_NAME_HIVE}
```

Or create a mixed_catalog with local configurations with following properties:
```properties
spark.sql.catalog.mixed_catalog=org.apache.amoro.spark.MixedFormatSparkCatalog
# Configure mixed catalog type as you needed
spark.sql.catalog.mixed_catalog.type=hadoop
spark.sql.catalog.mixed_catalog.warehouse=/warehouse/hadoop_mixed_catalog
```

Then, execute the following SQL in the Spark SQL Client to switch to the corresponding catalog.

```sql
use mixed_catalog;
```

Of course, you can also access Mixed-Format tables by directly using the triplet
`mixed_catalog.{db_name}.{table_name}`.

You can also set Spark's default catalog to your configured catalog using the following properties.
In this way, you don't need to use the `use {catalog}` command to switch the default catalog.

```properties
spark.sql.defaultCatalog=mixed_catalog
spark.sql.catalog.mixed_catalog=org.apache.amoro.spark.MixedFormatSparkCatalog
spark.sql.catalog.mixed_catalog.url=thrift://${AMS_HOST}:${AMS_PORT}/${AMS_CATALOG_NAME_HIVE}
```

In a standalone AmoroSparkCatalog scenario, only Mixed-Format tables can be created and accessed in the corresponding
catalog

### Using Mixed-Format in session catalog

If you want to access both existing Hive tables or Spark datasource tables and Mixed-Format tables in Spark,
you can use the AmoroSparkSessionCatalog as the implementation of the Spark default session catalog.
The configuration method is as follows.

```properties
spark.sql.catalog.spark_catalog=org.apache.amoro.spark.MixedFormatSparkSessionCatalog
spark.sql.catalog.spark_catalog.url=thrift://${AMS_HOST}:${AMS_PORT}/${AMS_CATALOG_NAME_HIVE}
```

When using the `MixedFormatSparkSessionCatalog` as the implementation of the `spark_catalog`, it behaves as follows

- Load Table: When resolving a `db_name.table_name` identifier, it will load the table metadata by Spark's built-in
  session catalog implementation, and then checking the MixedFormat flag defined in table properties. If the table has
  the MixedFormat flag, it will be loaded by `MixedFormatSparkCatalog` again.

- Create Table: The behavior of `CREATE TABLE` is determined by the `using {provider}` clause in the DDL statement. If
  the clause contains `using mixed_iceberg` or `using mixed_hive`, a Mixed-Format table will be created. Otherwise, the default Spark implementation
  will be used to create the table.

When using the `MixedFormatSparkSessionCatalog`, there are several points to keep in mind:

- `MixedFormatSparkSessionCatalog` can only be configured under the `spark_catalog`
- The `spark.sql.catalogImplementation` must be configured as `HIVE`
- Catalogs registered on AMS must use a Metastore of the `Hive` type.

## The high availability configuration

If AMS is configured with high availability, you can configure the `spark.sql.catalog.{catalog_name}.url` property in
the following way to achieve higher availability.

```properties
spark.sql.catalog.mixed_catalog=org.apache.amoro.spark.MixedFormatSparkCatalog
spark.sql.catalog.mixed_catalog.url=zookeeper://{zookeeper-endpoint-list}/{cluster-name}/{catalog-name}
```

Among above:

- zookeeper-endpoint-list:  a list of host:port pairs separated by commas. A valid value could
  be `192.168.1.1:2181,192.168.1.2:2181,192.168.1.3:2181`
- cluster-name:  is the value of `ams.cluster.name` configured in the configuration file `config.yml` of AMS, which is
  used to identify the user space on ZooKeeper.
- catalog-name: the name of the Catalog on AMS.
