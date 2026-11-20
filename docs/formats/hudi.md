---
title: "Hudi"
url: hudi-format
aliases:
    - "formats/hudi"
menu:
    main:
        parent: Formats
        weight: 600
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
# Hudi Format

Amoro can register existing [Apache Hudi](https://hudi.apache.org/) tables and display their metadata.

## Prerequisites

Use an AMS build that includes the Hudi format module. For source builds, enable `support-hudi-format` or `support-all-formats`. Check that the selected distribution includes the module.

The examples below connect existing tables or datasets. Replace example paths and service addresses with resources reachable by AMS, and configure storage access through [Managing Catalogs](../managing-catalogs/).

## Registering Hudi Catalogs

To browse existing Hudi tables, select `filesystem` or `hive` as the metastore type, then set `table-formats=HUDI` in Catalog Properties. Selecting the metastore and setting Catalog Properties are separate steps; an omitted `table-formats` selects a different default format.

### Filesystem Metastore

Select `filesystem` as the metastore type (legacy `hadoop` is normalized to `filesystem`). The catalog requires `warehouse` pointing to the Hudi root.
Layout: `warehouse/database/table/.hoodie`. Local storage paths must be reachable by the AMS process.

Catalog Properties:

```json
{
  "table-formats": "HUDI",
  "warehouse": "/path/to/hudi/warehouse"
}
```

### Hive Metastore

Hudi Hive catalogs use Hadoop configuration plus an uploaded `hive-site.xml` containing `hive.metastore.uris` (a raw catalog property `hive.metastore.uris` is not read by the Hudi Hive implementation). Upload `hive-site.xml`, plus `core-site.xml` and `hdfs-site.xml` as needed for storage, through [Managing Catalogs](../managing-catalogs/). A minimal `hive-site.xml`:

```xml
<configuration>
  <property>
    <name>hive.metastore.uris</name>
    <value>thrift://hive-metastore-host:9083</value>
  </property>
</configuration>
```

Catalog Properties:

```json
{
  "table-formats": "HUDI"
}
```

## Browsing Hudi Tables

After registering the catalog, AMS lists existing Hudi tables. The following metadata is available:

- Schema (from Avro schema)
- Table properties
- Files (latest base and log files)
- Timeline (commits)
- Native compaction and clustering metadata (display only; AMS does not schedule these)

Not available: DDL history, tags, consumer information, and AMS-managed optimizing processes.

## Example Filesystem Layout

An existing Hudi dataset can be laid out like:

```
/path/to/hudi/warehouse/
└── mydb/
    └── mytable/
        └── .hoodie/
            └── hoodie.properties
```
