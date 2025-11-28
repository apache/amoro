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

Currently, Amoro mainly provides the following four table formats:

- **Iceberg format:** Users can directly entrust their Iceberg tables to Amoro for maintenance, so that users can not only use all the functions of Iceberg tables, but also enjoy the performance and stability improvements brought by Amoro.
- **Mixed-Iceberg format:** Amoro provides a set of more optimized formats for streaming update scenarios on top of the Iceberg format. If users have high performance requirements for streaming updates or have demands for CDC incremental data reading functions, they can choose to use the Mixed-Iceberg format.
- **Mixed-Hive format:** Many users do not want to affect the business originally built on Hive while using data lakes. Therefore, Amoro provides the Mixed-Hive format, which can upgrade Hive tables to Mixed-Hive format only through metadata migration, and the original Hive tables can still be used normally. This ensures business stability and benefits from the advantages of data lake computing.
- **Paimon format:** Amoro supports displaying metadata information in the Paimon format, including Schema, Options, Files, Snapshots, DDLs, and Compaction information.
