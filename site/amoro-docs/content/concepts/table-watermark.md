---
title: "Table Watermark"
url: table-watermark
aliases:
    - "concept/table-watermark"
menu:
    main:
        parent: Concepts
        weight: 400
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
# Table Watermark

## Table freshness

Data freshness represents timeliness, and in many discussions, freshness is considered one of the important indicators of data quality. In traditional
offline data warehouses, higher cost typically means better performance, creating a typical binary paradox in terms of cost-performance trade-off. 
However, in high-freshness streaming data warehouses, massive small files and frequent updates can lead to performance degradation. The higher the
freshness, the greater the impact on performance. To achieve the required performance, users must incur higher costs. Thus, for streaming data
warehouses, data freshness, query performance, and cost form a tripartite paradox.

<img src="../images/concepts/freshness_cost_performance.png" alt="Freshness, cost and performance" width="60%" height="60%">

Amoro offers a resolution to the tripartite paradox for users by utilizing AMS management functionality and a self-optimizing mechanism. Unlike
traditional data warehouses, Lakehouse tables are utilized in a multitude of data pipelines, AI, and BI scenarios. Measuring data freshness is
crucially important for data developers, analysts, and administrators, and Amoro addresses this challenge by adopting the watermark concept in stream
computing to gauge table freshness.

## Table watermark

In the Mixed Format, data freshness is measured through table watermark.

Strictly speaking, table watermark is used to describe the writing progress of a table. Specifically, it is a timestamp attribute on the table that
indicates that data with timestamps earlier than this watermark have been written to the table. It is typically used to monitor the progress of table
writes and can also serve as a trigger indicator for downstream batch computing tasks.

Mixed Format uses the following configurations to configure watermark:

```sql
  'table.event-time-field' = 'op_time',
  'table.watermark-allowed-lateness-second' = '60'
```

In the example above, `op_time` is set as the event time field for the table, and the watermark for the table is calculated using the `op_time` of the
data being written. To handle out-of-order writes, a maximum delay of 60 seconds is allowed for calculating the watermark. Unlike in stream
processing, data with event_time values smaller than the watermark will not be rejected, but they will not affect the advancement of the watermark
either.

You can view the current watermark of a table in the AMS Dashboard's table details, or you can use the following SQL query in the terminal to query
the watermark of a table:

```SQL
SHOW TBLPROPERTIES test_db.test_log_store ('watermark.table');
```
You can also query the table watermark of the BaseStore using the following command, which can be combined with native reads from Hive or Iceberg for
greater flexibility:

```SQL
SHOW TBLPROPERTIES test_db.test_log_store ('watermark.base');
```

You can learn about how to use Watermark in detail by referring to [Using tables](../using-tables/).