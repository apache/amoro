---
title: "Overview"
url: formats-overview
aliases:
    - "formats/overview"
menu:
    main:
        parent: Formats
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
# Formats Overview

Table format (aka. format) was first proposed by Iceberg, which can be described as follows:

- It defines the relationship between tables and files, and any engine can query and retrieve data files according to the table format.
- New formats such as Iceberg/Delta/Hudi further define the relationship between tables and snapshots, and the relationship between snapshots and files.
  All write operations on the table will generate new snapshots, and all read operations on the table are based on snapshots.
  Snapshots bring MVCC, ACID, and Transaction capabilities to data lakes.

In addition, new table formats such as [Iceberg](https://Iceberg.apache.org/) also provide many advanced features such as schema evolve, hidden partition, and data skip.
[Hudi](https://hudi.apache.org/) and [Delta](https://delta.io/) may have some differences in specific functions, but we see that the standard of table formats is gradually established with the functional convergence of these three open-source projects in the past two years.

For users, the design goal of Amoro is to provide an out-of-the-box data lake system. Internally, Amoro's design philosophy is to use different table formats as storage engines for data lakes.
This design pattern is more common in open-source systems such as MySQL and ClickHouse.

Currently, Amoro supports the following table formats:

- **[Iceberg format](../iceberg-format/):** Users can directly entrust their Iceberg tables to Amoro for maintenance, so that users can not only use all the functions of Iceberg tables, but also enjoy the performance and stability improvements brought by Amoro. Automatic maintenance and self-optimizing apply to V1/V2 tables with the required settings; AMS maintenance and optimizing are unavailable for V3, and metadata reads depend on the features supported by the bundled Iceberg reader (see [version compatibility](../iceberg-format/#format-version-compatibility)).
- **[Mixed-Iceberg format](../mixed-iceberg-format/):** Amoro provides a set of more optimized formats for streaming update scenarios on top of the Iceberg format. If users have high performance requirements for streaming updates or have demands for CDC incremental data reading functions, they can choose to use the Mixed-Iceberg format.
- **[Mixed-Hive format](../mixed-hive-format/):** Many users do not want to affect the business originally built on Hive while using data lakes. Therefore, Amoro provides the Mixed-Hive format, which can upgrade Hive tables to Mixed-Hive format only through metadata migration, and the original Hive tables can still be used normally. This ensures business stability and benefits from the advantages of data lake computing.
- **[Paimon format](../paimon-format/):** Amoro supports displaying metadata information in the Paimon format, including Schema, Options, Files, Snapshots, DDLs, and Compaction information. Native Paimon compaction metadata is displayed; AMS does not schedule that work.
- **[Hudi format](../hudi-format/):** Amoro supports displaying metadata information for existing Hudi tables, including Schema, Files, Timeline, and native compaction/clustering metadata. Native Hudi compaction/clustering metadata is displayed; AMS does not schedule that work.
- **[Lance format](../lance-format/):** Amoro supports displaying metadata information for existing Lance datasets, including Schema, Files, Dataset Versions, Tags, and Branches.

| Format | Metadata Browsing | AMS Self-Optimizing | Built-in AMS Maintenance |
|---|---|---|---|
| Iceberg | Table, snapshot and file metadata | V1/V2 only, when configured | V1/V2 only, when configured |
| Mixed-Iceberg | Table, snapshot and file metadata | Yes, when configured | Format-specific actions, when enabled |
| Mixed-Hive | Table, snapshot and file metadata | Yes, when configured | Format-specific actions, when enabled |
| Paimon | Schema, Options, Files, Snapshots, DDLs, Compaction Info | No (native compaction displayed only) | No |
| Hudi | Schema, Files, Timeline, Native Compaction/Clustering Metadata | No (native compaction/clustering displayed only) | No |
| Lance | Schema, Files, Dataset Versions, Tags, Branches | No | No |

Built-in maintenance means the AMS actions available for that format and enabled by server and table settings; individual actions differ between formats. Displaying native Paimon or Hudi compaction information does not make AMS schedule those jobs.
