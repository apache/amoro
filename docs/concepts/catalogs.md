---
title: "Catalogs"
url: catalogs
aliases:
    - "concept/catalogs"
menu:
    main:
        parent: Concepts
        weight: 100
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
# Catalogs

## Introduce multi-catalog

A catalog is a metadata namespace that stores information about databases, tables, views, indexes, users, and UDFs. It provides a higher-level
namespace for `table` and `database`. Typically, a catalog is associated with a specific type of data source or cluster. In Flink, Spark and Trino,
the multi-catalog feature can be used to support SQL across data sources, such as:

```SQL
SELECT c.ID, c.NAME, c.AGE, o.AMOUNT
FROM ${CATALOG_A}.ONLINE.CUSTOMERS c JOIN ${CATALOG_B}.OFFLINE.ORDERS o
ON (c.ID = o.CUSTOMER_ID)
```

In the past, data lakes were managed using the Hive Metastore (HMS) to handle metadata. Unfortunately, HMS does not support multi-catalog, which
limits the capabilities of engines on the data lake. For example, some users may want to use Spark to perform federated computation across different
Hive clusters by specifying the catalog name, requiring them to develop a Hive catalog plugin in the upper layer. Additionally, data lake formats are
moving from a single Hive-centric approach to a landscape of competing formats such as Iceberg, Delta, and Hudi. These new data lake formats are more
cloud-friendly and will facilitate the migration of data lakes to the cloud. In this context, a management system that supports multi-catalog is
needed to help users govern data lakes with different environments and formats.

Users can create catalogs in Amoro for different environments, clusters, and table formats, and leverage the multi-catalog feature in Flink, Spark
and Trino to enable federated computation across multiple clusters and formats. Additionally, properties configured in catalogs can be shared by all
tables and users, avoiding duplication. By leveraging the multi-catalog design, Amoro provides support for a metadata center in data platforms.

When AMS and HMS are used together, HMS serves as the storage foundation for AMS. With the [Iceberg Format](../iceberg-format/), users can leverage the
multi-catalog management functionality of AMS without introducing any Amoro dependencies.

## How to use

Amoro v0.4 introduced the catalog management feature, where table creation is performed under a catalog. Users can create, edit, and delete catalogs
in the catalogs module, which requires configuration of metastore, table format, and environment information upon creation. For more information,
please refer to the documentation: [Managing catalogs](../managing-catalogs/).

## Future work

AMS will focus on two goals to enhance the value of the metadata center in the future:

- Expand data sources: In addition to data lakes, message queues, databases, and data warehouses can all be managed as objects in the catalog.
Through metadata center and SQL-based federated computing of the computing engine, AMS will provide infrastructure solutions for data platforms
such as DataOps and DataFabric
- Automatic catalog detection: In compute engines like Spark and Flink, it is possible to automatically detect the creation and changes of a
catalog, enabling a one-time configuration for permanent scalability.
  
