---
title: "Lance"
url: lance-format
aliases:
    - "formats/lance"
menu:
    main:
        parent: Formats
        weight: 700
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
# Lance Format

Amoro can register existing [Lance](https://lancedb.github.io/lance/) datasets and display their metadata.

## Prerequisites

Use an AMS build that includes the Lance format module. For source builds, enable `support-lance-format` or `support-all-formats`. Check that the selected distribution includes the module.

The examples below connect existing tables or datasets. Replace example paths and service addresses with resources reachable by AMS, and configure storage access through [Managing Catalogs](../managing-catalogs/).

## Registering Lance Catalogs

Lance catalogs must set `table-formats=LANCE` to avoid default format fallback. Choose metastore type (`filesystem`, `hive`, or `rest`) separately from properties.

### Filesystem Metastore (Directory)

Uses a root directory containing Lance datasets. All tables live in the `default` database; creating or dropping databases is unsupported.
Filesystem layout: `warehouse/example.lance`. The `warehouse` property is required. Local paths must be reachable by AMS.

Catalog Properties:

```json
{
  "table-formats": "LANCE",
  "warehouse": "/path/to/lance/warehouse"
}
```

### Hive Metastore

Lance Hive catalogs require `warehouse` (must not be `hdfs` scheme) and the raw property `uri` for the Hive metastore. The implementation writes `hive.metastore.uris` internally. Upload Hadoop configuration files including `hive-site.xml` via the Managing Catalogs workflow. Pure Lance Hive catalogs reject Kerberos authentication.

Catalog Properties:

```json
{
  "table-formats": "LANCE",
  "warehouse": "s3://bucket/path",
  "uri": "thrift://hive-metastore-host:9083"
}
```

### REST Metastore

Lance REST catalogs require the raw property `uri`. The endpoint must implement the Lance Namespace REST protocol, not just an Iceberg REST endpoint. Use actual reachable service addresses.

Catalog Properties:

```json
{
  "table-formats": "LANCE",
  "uri": "http://lance-rest-host:8080"
}
```

## Browsing Lance Datasets

After registering, AMS discovers existing Lance datasets. For filesystem catalogs, datasets are found as `warehouse/<name>.lance`. For Hive and REST, the namespace is queried.

Displayed metadata includes:

- Schema (Arrow fields)
- Files
- Dataset versions (like snapshots)
- Tags
- Branches

Not available: DDL history, partitions, optimizing tasks, consumer information.

## Example Filesystem Layout

```
/path/to/lance/warehouse/
└── example.lance/
    └── (Lance dataset files)
```
