---
title: "Paimon"
url: paimon-format
aliases:
    - "formats/paimon"
menu:
    main:
        parent: Formats
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
# Paimon Format

Paimon format refers to [Apache Paimon](https://paimon.apache.org/) table.
Paimon is a streaming data lake platform with high-speed data ingestion, changelog tracking and efficient real-time analytics.

By registering Paimon's catalog with Amoro, users can view information such as Schema, Options, Files, Snapshots, DDLs, Compaction information, and more for Paimon tables.
Furthermore, they can operate on Paimon tables using Spark SQL in the Terminal. The current supported catalog types and file system types for Paimon are all supported.

For registering catalog operation steps, please refer to [Managing Catalogs](../managing-catalogs/).

{{< hint info >}}
If you want to use S3 or OSS, please download the 
[S3](https://repo.maven.apache.org/maven2/org/apache/paimon/paimon-s3/0.5.0-incubating/paimon-s3-0.5.0-incubating.jar), 
[OSS](https://repo.maven.apache.org/maven2/org/apache/paimon/paimon-oss/0.5.0-incubating/paimon-oss-0.5.0-incubating.jar) 
package and put it in the 'lib' directory of the Amoro installation package.
{{< /hint >}}

## Self-optimizing for BUCKET_UNAWARE tables

Amoro can automatically merge small files for Paimon AppendOnly tables whose bucket
is set to `-1` (the `BUCKET_UNAWARE` mode). Primary-key tables and tables with a
fixed positive bucket count are skipped — the planner treats them as out-of-scope
and does not touch them.

### Enable the optimizer plugin

The Paimon optimizer plugin is **disabled by default**. To turn it on, add the
following property to the plugin-properties section of the Paimon `ProcessFactory`
in your AMS configuration:

```yaml
optimizing-plugins:
  paimon:
    paimon-optimizer.enabled: true
```

When this flag is off, `PaimonProcessFactory.supportedFormats()` returns an empty
set, so the AMS optimizer queue cannot route any Paimon table into this factory —
it is a clean kill-switch for grey-scale rollout.

### Watching progress

Once enabled, every registered Paimon AppendOnly table is refreshed periodically
by the same `TableRuntimeRefreshExecutor` that drives Iceberg. Each optimizing
cycle transitions the table runtime state through:

```
IDLE → PLANNING → OPTIMIZING → COMMITTING → IDLE
```

You can observe this progression on the Dashboard `Optimizing` tab, or by querying
`GET /api/ams/v1/optimize/tables` via the REST API. Every task shows up with
`OptimizingType.MINOR` and the `task-executor-factory-impl` property set to
`PaimonCompactionExecutorFactory`.

The `summary()` of each finished task carries four keys (all byte-level counts):

```
compacted-files, compacted-bytes, produced-files, produced-bytes
```

### Troubleshooting

| Symptom | Likely cause / fix |
| --- | --- |
| Table stays `IDLE` even though many small files exist | Check `paimon-optimizer.enabled=true` is in AMS config; check the table is `AppendOnly + bucket=-1`; primary-key tables are skipped by design. |
| `NoClassDefFoundError: org.apache.paimon.append.AppendCompactCoordinator` in the Optimizer log | `paimon-bundle` jar is missing from the Optimizer distribution `lib/` directory. Rebuild the distribution or drop the jar in manually. |
| `OptimizingCommitException: Paimon commit failed … RuntimeException` | An external writer committed concurrently between plan and commit; AMS will mark this process failed and re-plan automatically on the next tick. No action needed unless it repeats indefinitely. |
| `IllegalStateException: missing required fields (table / taskBytes)` in Optimizer | The task input was truncated or the Planner shipped an empty `PaimonCompactionInput`; usually an upgrade-skew issue. Check that AMS and Optimizer are on the same Amoro version. |
| Table goes `PLANNING → IDLE` immediately | The planner ran `AppendCompactCoordinator.run()` and found zero candidate files. Lower `target-file-size` or `compaction.min.file-num` on the table if you expect more aggressive compaction. |

### Limitations in the first version

* Only AppendOnly tables (`bucket=-1`, no primary key) are supported. Dynamic-
  bucket and HASH_FIXED tables are planned follow-ups.
* REST Catalog is not tested; FileSystem and Hive Metastore catalogs are the
  tested code paths.
* Paimon-specific metrics (compaction lag, rewrite throughput) are not yet
  exposed to Dashboard — the first version reuses the standard `optimizing_*`
  metric family.
