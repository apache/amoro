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

To register an existing Paimon catalog on the local filesystem, select **FileSystem** as the metastore,
**PAIMON** as the table format, and **Local** as the storage type. Set the `warehouse` catalog property
to an absolute local URI, for example `file:///data/paimon/warehouse`. Hadoop configuration files and
authentication settings are not required for Local storage. The warehouse must be accessible to the
AMS process; a path on the browser's machine is not sufficient.

{{< hint info >}}
If you want to use S3 or OSS, please download the 
[S3](https://repo.maven.apache.org/maven2/org/apache/paimon/paimon-s3/0.5.0-incubating/paimon-s3-0.5.0-incubating.jar), 
[OSS](https://repo.maven.apache.org/maven2/org/apache/paimon/paimon-oss/0.5.0-incubating/paimon-oss-0.5.0-incubating.jar) 
package and put it in the 'lib' directory of the Amoro installation package.
{{< /hint >}}


## Snapshot expiration

AMS can periodically expire snapshots for ordinary Paimon primary-key and append-only tables
on the default branch. Install the Paimon format module (build AMS with
`-Psupport-paimon-format`) and enable the `paimon` entry in
`conf/plugins/process-factories.yaml`:

```yaml
  - name: paimon
    enabled: true
    priority: 100
    properties:
      expire-snapshots.enabled: "true"
      expire-snapshots.interval: "1h"
```

The distributed configuration disables this plugin. The plugin property
`expire-snapshots.enabled` defaults to `true`; setting it to `false` prevents registration of the
snapshot-expiration action. An explicit positive interval of at least
one millisecond is required; without it, snapshot expiration remains disabled.
Restart AMS after changing plugin configuration.

Add `pool.paimon-expire-snapshots.thread-count: 1` to the existing `local` execution engine's
properties in `conf/plugins/execute-engines.yaml` (included in the distributed configuration).
This selects a dedicated pool; without it, the local engine falls back to its default pool.
Paimon may use additional internal threads for file deletion.

The Amoro table property `amoro.expire-snapshots.enabled` defaults to `true`.
After enabling the plugin and configuring its interval, eligible tables are scheduled without
an explicit table switch. For example, the following Paimon Flink SQL makes the settings explicit:

```sql
ALTER TABLE my_table SET (
  'write-only' = 'true',
  'amoro.expire-snapshots.enabled' = 'true'
);
```

Set `amoro.expire-snapshots.enabled=false` on an individual table to exclude it; removing the property
restores the default `true`. Both the plugin action switch and the table switch must be enabled,
and `write-only=true` is still required. This native option skips automatic compaction as well
as snapshot expiration on the write path; this feature does not provide replacement compaction.
Enabling the plugin assigns snapshot expiration for eligible tables to AMS; coordinate or stop
external snapshot-expiration jobs before enabling it.
AMS does not detect other maintenance jobs or change table properties automatically.
System tables and non-default branches are skipped. Tables with independent changelog retention
remain eligible for snapshot expiration.
This release does not schedule orphan-file deletion, independent changelog expiration, compaction,
partition expiration or tag lifecycle management.

### Retention settings

Each execution, including retries and execution after recovery, reads the latest table settings
through its catalog. AMS uses the catalog's configured credentials and delegates snapshot
expiration to Paimon without overriding the table's retention policy.
The relevant native settings are `snapshot.num-retained.min`, `snapshot.num-retained.max`,
`snapshot.time-retained` and `snapshot.expire.limit`. With Paimon 1.2.0, defaults are respectively
10, 2147483647, 1 hour and 50.
The maximum snapshot count can expire snapshots within the time window; time retention alone
is not an absolute guarantee. Paimon remains responsible for tag and consumer protection.
Invalid retention configurations fail before expiration.

With independent changelog retention, native snapshot expiration preserves the files needed by
changelog readers and archives expired snapshot metadata as changelog metadata.
Archived changelogs are not expired by this action, even on later runs.
Because `write-only=true` also disables writer-side automatic changelog expiration, arrange
separate native changelog maintenance when needed; setting changelog retention alone does not
make AMS enforce that retention limit.

Choose the interval and native expiration limit according to snapshot generation rate, storage
load and retention needs. The interval controls scheduling checks, not guaranteed cleanup
completion frequency. Checks also respect the last successful execution time, so asynchronous
execution can make the effective period approach twice the configured interval; queuing and
failures can extend it further. The expiration limit applies to each attempt, not the cumulative
work across retries. A task can retry up to three times after its initial attempt; later scheduling
checks can create new tasks even after these retries are exhausted.

### Viewing and canceling tasks

The table's **Cleanup** tab lists AMS tasks and their latest-attempt summaries:

- `outcome=executed` or `noop` means the native method returned a positive count or zero.
  `reported-expired-snapshots` is Paimon's reported snapshot result, not a verified deleted-file
  count or reclaimed byte count. Normal return does not prove every file deletion succeeded.
- `outcome=skipped` means eligibility changed before execution; no expiration was invoked and
  the last successful cleanup time is unchanged.
- `error-stage` and `error-message` identify a failure. A failed task may already have expired
  snapshots. Retrying replaces the previous attempt's summary; this is not a complete attempt history.

In the Cleanup tab, expand an AMS-managed Paimon snapshot expiration task to see its summary.
Users with write permission can cancel a running task from that row. A repeated request or a
request for a completed task returns an error. Cancellation prevents pending retries of that task,
but later scheduling checks can create another task. To stop future automatic expiration for a
table, set `amoro.expire-snapshots.enabled=false`.

### Known limitations

- Cancellation or disabling table maintenance does not guarantee that an already-started cleanup
  stops immediately. Files already deleted stay deleted. The default single-threaded Paimon pool
  queues a replacement task; with multiple threads, a replacement can run while the canceled task
  finishes.
- A successful task can have an empty finish time, and its displayed duration can continue to
  increase on refresh.
- A task still recorded as `PENDING` when AMS stops remains in the history as `PENDING` after
  restart; later scheduling can create a new task. Recovery may repeat expiration and does not
  guarantee exactly-once execution.
