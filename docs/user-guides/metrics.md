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

| Metric Name                                          | Type    | Tags                     | Description                                                                            |
|------------------------------------------------------|---------|--------------------------|----------------------------------------------------------------------------------------|
| table_optimizing_status_idle_duration_mills          | Gauge   | catalog, database, table, group | Duration in milliseconds after table be in idle status                                 |
| table_optimizing_status_pending_duration_mills       | Gauge   | catalog, database, table, group | Duration in milliseconds after table be in pending status                              |
| table_optimizing_status_planning_duration_mills      | Gauge   | catalog, database, table, group | Duration in milliseconds after table be in planning status                             |
| table_optimizing_status_executing_duration_mills     | Gauge   | catalog, database, table, group | Duration in milliseconds after table be in executing status                            |
| table_optimizing_status_committing_duration_mills    | Gauge   | catalog, database, table, group | Duration in milliseconds after table be in committing status                           |
| table_optimizing_process_total_count                 | Counter | catalog, database, table, group | Count of all optimizing process since ams started                                      |
| table_optimizing_process_failed_count                | Counter | catalog, database, table, group | Count of failed optimizing process since ams started                                   |
| table_optimizing_minor_total_count                   | Counter | catalog, database, table, group | Count of minor optimizing process since ams started                                    |
| table_optimizing_minor_failed_count                  | Counter | catalog, database, table, group | Count of failed minor optimizing process since ams started                             |
| table_optimizing_major_total_count                   | Counter | catalog, database, table, group | Count of major optimizing process since ams started                                    |
| table_optimizing_major_failed_count                  | Counter | catalog, database, table, group | Count of failed major optimizing process since ams started                             |
| table_optimizing_full_total_count                    | Counter | catalog, database, table, group | Count of full optimizing process since ams started                                     |
| table_optimizing_full_failed_count                   | Counter | catalog, database, table, group | Count of failed full optimizing process since ams started                              |
| table_optimizing_status_in_idle                      | Gauge   | catalog, database, table, group | If currently table is in idle status                                                   |
| table_optimizing_status_in_pending                   | Gauge   | catalog, database, table, group | If currently table is in pending status                                                |
| table_optimizing_status_in_planning                  | Gauge   | catalog, database, table, group | If currently table is in planning status                                               |
| table_optimizing_status_in_executing                 | Gauge   | catalog, database, table, group | If currently table is in executing status                                              |
| table_optimizing_status_in_committing                | Gauge   | catalog, database, table, group | If currently table is in committing status                                             |
| table_optimizing_since_last_minor_optimization_mills | Gauge   | catalog, database, table, group | Duration in milliseconds since last successful minor optimization                      |
| table_optimizing_since_last_major_optimization_mills | Gauge   | catalog, database, table, group | Duration in milliseconds since last successful major optimization                      |
| table_optimizing_since_last_full_optimization_mills  | Gauge   | catalog, database, table, group | Duration in milliseconds since last successful full optimization                       |
| table_optimizing_since_last_optimization_mills       | Gauge   | catalog, database, table, group | Duration in milliseconds since last successful optimization                            |
| table_optimizing_lag_duration_mills                  | Gauge   | catalog, database, table, group | Duration in milliseconds between last self-optimizing snapshot and refreshed snapshot  |

## Optimizer Group metrics

| Metric Name                             | Type   | Tags  | Description                                      |
|-----------------------------------------|--------|-------|--------------------------------------------------|
| optimizer_group_pending_tasks           | Gauge  | group | Number of pending tasks in optimizer group       |
| optimizer_group_executing_tasks         | Gauge  | group | Number of executing tasks in optimizer group     |
| optimizer_group_planing_tables          | Gauge  | group | Number of planing tables in optimizer group      |
| optimizer_group_pending_tables          | Gauge  | group | Number of pending tables in optimizer group      |
| optimizer_group_executing_tables        | Gauge  | group | Number of executing tables in optimizer group    |
| optimizer_group_idle_tables             | Gauge  | group | Number of idle tables in optimizer group         |
| optimizer_group_committing_tables       | Gauge  | group | Number of committing tables in optimizer group   |
| optimizer_group_optimizer_instances     | Gauge  | group | Number of optimizer instances in optimizer group |
| optimizer_group_memory_bytes_allocated  | Gauge  | group | Memory bytes allocated in optimizer group        |
| optimizer_group_threads                 | Gauge  | group | Number of total threads in optimizer group       |
| optimizer_group_idle_optimizers         | Gauge  | group | Number of optimizer instances with no in-flight task in optimizer group |
| optimizer_group_config_invalid          | Gauge  | group | 1 while the group's dynamic allocation configuration is invalid and the fail-safe fallback is active, else 0 |

The following metrics are exported only while dynamic allocation is effectively enabled on the group
(see [Managing optimizers](../managing-optimizers/)); the counters count attempted scaling actions, so
one scale-out round — whatever its instance count and whether its resource requests succeed — and one
drain start each count 1. A rising `scale_up_total` without a matching rise of
`optimizer_group_optimizer_instances`, sustained beyond the pod boot window, signals failing
resource requests; short gaps are normal while requested pods are still booting.

| Metric Name                               | Type    | Tags  | Description                                        |
|-------------------------------------------|---------|-------|----------------------------------------------------|
| optimizer_group_pending_removal_optimizers | Gauge  | group | Number of optimizer instances in graceful drain in optimizer group |
| optimizer_group_effective_threads         | Gauge   | group | Number of registered threads plus threads of optimizers pending registration in optimizer group |
| optimizer_group_backlog_duration_ms       | Gauge   | group | Duration in milliseconds since demand first exceeded capacity in optimizer group, 0 while there is no backlog |
| optimizer_group_scale_up_total            | Counter | group | Cumulative count of attempted scale-up actions in optimizer group |
| optimizer_group_scale_down_total          | Counter | group | Cumulative count of scale-down actions in optimizer group |

## Orphan Files Cleaning metrics

| Metric Name                                        | Type    | Tags                     | Description                                                                    |
|----------------------------------------------------|---------|--------------------------|--------------------------------------------------------------------------------|
| table_orphan_content_file_cleaning_count           | Counter | catalog, database, table | Count of orphan content files cleaned in the table since ams started           |
| table_orphan_metadata_file_cleaning_count          | Counter | catalog, database, table | Count of orphan metadata files cleaned in the table since ams started          |
| table_expected_orphan_content_file_cleaning_count  | Counter | catalog, database, table | Expected Count of orphan content files cleaned in the table since ams started  |
| table_expected_orphan_metadata_file_cleaning_count | Counter | catalog, database, table | Expected Count of orphan metadata files cleaned in the table since ams started |


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

## table summary metrics

| Metric Name                                   | Type    | Tags                     | Description                                   |
|-----------------------------------------------|---------|--------------------------|-----------------------------------------------|
| table_summary_total_files                     | Gauge   | catalog, database, table | Total number of files in the table            |
| table_summary_data_files                      | Gauge   | catalog, database, table | Number of data files in the table             |
| table_summary_equality_delete_files           | Gauge   | catalog, database, table | Number of equality delete files in the table  |
| table_summary_position_delete_files           | Gauge   | catalog, database, table | Number of position delete files in the table  |
| table_summary_dangling_delete_files           | Gauge   | catalog, database, table | Number of dangling delete files in the table  |
| table_summary_total_files_size                | Gauge   | catalog, database, table | Total size of files in the table              |
| table_summary_data_files_size                 | Gauge   | catalog, database, table | Size of data files in the table               |
| table_summary_equality_delete_files_size      | Gauge   | catalog, database, table | Size of equality delete files in the table    |
| table_summary_position_delete_files_size      | Gauge   | catalog, database, table | Size of position delete files in the table    |
| table_summary_total_records                   | Gauge   | catalog, database, table | Total records in the table                    |
| table_summary_data_files_records              | Gauge   | catalog, database, table | Records of data files in the table            |
| table_summary_equality_delete_files_records   | Gauge   | catalog, database, table | Records of equality delete files in the table |
| table_summary_position_delete_files_records   | Gauge   | catalog, database, table | Records of position delete files in the table |
| table_summary_snapshots                       | Gauge   | catalog, database, table | Number of snapshots in the table              |
| table_summary_health_score                    | Gauge   | catalog, database, table | Health score of the table                     |