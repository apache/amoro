---
title: "Metrics"
url: metrics
aliases:
    - "user-guides/metrics"
menu:
    main:
        parent: User Guides
        weight: 500
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
# Metrics

Amoro build a metrics system to measure the behaviours of table management processes, like how long has it been since a table last performed self-optimizing process, and how much resources does a optimizer group currently has?

There are two types of metrics provided in the Amoro metric system: Gauge and Counter.

* Gauge: Provides a value of any type at a point in time.
* Counter: Used to count values by incrementing and decrementing.

Amoro has supported built-in metrics to measure status of table self-optimizing processes and optimizer resources, which can be [reported to external metric system like Prometheus etc](../deployment/#configure-metric-reporter).

## Self-optimizing metrics

| Metric Name                                       | Type    | Tags                     | Description                                                  |
|---------------------------------------------------|---------|--------------------------|--------------------------------------------------------------|
| table_optimizing_status_idle_duration_mills       | Gauge   | catalog, database, table | Duration in milliseconds after table be in idle status       |
| table_optimizing_status_pending_duration_mills    | Gauge   | catalog, database, table | Duration in milliseconds after table be in pending status    |
| table_optimizing_status_planning_duration_mills   | Gauge   | catalog, database, table | Duration in milliseconds after table be in planning status   |
| table_optimizing_status_executing_duration_mills  | Gauge   | catalog, database, table | Duration in milliseconds after table be in executing status  |
| table_optimizing_status_committing_duration_mills | Gauge   | catalog, database, table | Duration in milliseconds after table be in committing status |
| table_optimizing_process_total_count              | Counter | catalog, database, table | Count of all optimizing process since ams started            |
| table_optimizing_process_failed_count             | Counter | catalog, database, table | Count of failed optimizing process since ams started         |
| table_optimizing_minor_total_count                | Counter | catalog, database, table | Count of minor optimizing process since ams started          |
| table_optimizing_minor_failed_count               | Counter | catalog, database, table | Count of failed minor optimizing process since ams started   |
| table_optimizing_major_total_count                | Counter | catalog, database, table | Count of major optimizing process since ams started          |
| table_optimizing_major_failed_count               | Counter | catalog, database, table | Count of failed major optimizing process since ams started   |
| table_optimizing_full_total_count                 | Counter | catalog, database, table | Count of full optimizing rocess since ams started            |
| table_optimizing_full_failed_count                | Counter | catalog, database, table | Count of failed full optimizing process since ams started    |
| table_optimizing_status_in_idle                   | Gauge   | catalog, database, table | If currently table is in idle status                         |
| table_optimizing_status_in_pending                | Gauge   | catalog, database, table | If currently table is in pending status                      |
| table_optimizing_status_in_planning               | Gauge   | catalog, database, table | If currently table is in planning status                     |
| table_optimizing_status_in_executing              | Gauge   | catalog, database, table | If currently table is in executing status                    |
| table_optimizing_status_in_committing             | Gauge   | catalog, database, table | If currently table is in committing status                   |

## Optimizer Group metrics

| Metric Name                            | Type   | Tags  | Description                                      |
|----------------------------------------|--------|-------|--------------------------------------------------|
| optimizer_group_pending_tasks          | Gauge  | group | Number of pending tasks in optimizer group       |
| optimizer_group_executing_tasks        | Gauge  | group | Number of executing tasks in optimizer group     |
| optimizer_group_planing_tables         | Gauge  | group | Number of planing tables in optimizer group      |
| optimizer_group_pending_tables         | Gauge  | group | Number of pending tables in optimizer group      |
| optimizer_group_executing_tables       | Gauge  | group | Number of executing tables in optimizer group    |
| optimizer_group_optimizer_instances    | Gauge  | group | Number of optimizer instances in optimizer group |
| optimizer_group_memory_bytes_allocated | Gauge  | group | Memory bytes allocated in optimizer group        |
| optimizer_group_threads                | Gauge  | group | Number of total threads in optimizer group       |


## Ams service metrics
| Metric Name                                            | Type   |     Tags        | Description                                                      |
|--------------------------------------------------------|--------|-----------------|------------------------------------------------------------------|
| ams_jvm_cpu_load                                       | Gauge  |                 | The recent CPU usage of the AMS                                  |
| ams_jvm_cpu_time                                       | Gauge  |                 | The CPU time used by the AMS                                     |
| ams_jvm_memory_heap_used                               | Gauge  |                 | The amount of heap memory currently used (in bytes) by the AMS   |
| ams_jvm_memory_heap_committed                          | Gauge  |                 | The amount of memory in the heap committed for JVM use (bytes)   |
| ams_jvm_memory_heap_max                                | Gauge  |                 | The maximum heap memory (bytes), set by -Xmx JVM argument        |
| ams_jvm_threads_count                                  | Gauge  |                 | The total number of live threads used by the AMS                 |
| ams_jvm_garbage_collector_count                        | Gauge  |garbage_collector| The count of the JVM's Garbage Collector, such as G1 Young        |
| ams_jvm_garbage_collector_time                         | Gauge  |garbage_collector| The time spent by the JVM's Garbage Collector, such as G1 Young   |