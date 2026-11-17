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
one millisecond is required; missing configuration does not register any Paimon maintenance
actions, even when AMS automatically discovers plugins. Restart AMS after changing plugin
configuration. The common plugin loader defaults an omitted `enabled` field to `true`.

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

Each execution, retry and recovery reloads the table through its existing catalog, invalidating
cached metadata. Paimon reads retention settings through `CoreOptions.expireConfig()` and applies
them using `newExpireSnapshots().config(...).expire()`; AMS supplies no retention overrides.
The relevant native settings are `snapshot.num-retained.min`, `snapshot.num-retained.max`,
`snapshot.time-retained` and `snapshot.expire.limit`. With Paimon 1.2.0, defaults are respectively
10, 2147483647, 1 hour and 50. Changelog settings retain their native values and fallback rules. In Paimon 1.1.1 and 1.2.0,
the effective changelog deletion limit also comes from `snapshot.expire.limit`; there is no
separate `changelog.expire.limit` table option in these versions.
With independent changelog retention, native snapshot expiration preserves the files needed by
changelog readers and archives expired snapshot metadata as changelog metadata. AMS does not call
`newExpireChangelog()`: archived changelogs are not expired by this action, even on later runs.
Because `write-only=true` also disables writer-side automatic changelog expiration, arrange
separate native changelog maintenance when needed; setting changelog retention alone does not
make AMS enforce that retention limit.
The maximum snapshot count can expire snapshots within the time window; time retention alone
is not an absolute guarantee. Paimon remains responsible for tag and consumer protection.
Invalid retention configurations fail before expiration.

Choose the interval and native expiration limit according to snapshot generation rate, storage
load and retention needs. The interval controls scheduling checks, not guaranteed cleanup
completion frequency. Checks also respect the last successful execution time, so asynchronous
execution can make the effective period approach twice the configured interval; queuing and
failures can extend it further. One process attempt makes one native call, without an unbounded
cleanup loop. The limit applies to each invocation, not the cumulative work across retries.

The table's **Cleanup** tab lists AMS tasks and their latest-attempt summaries:

- `outcome=executed` or `noop` means the native method returned a positive count or zero.
  `reported-expired-snapshots` is Paimon's reported snapshot result, not a verified deleted-file
  count or reclaimed byte count. Normal return does not prove every file deletion succeeded.
- `outcome=skipped` means eligibility changed before execution; no expiration was invoked and
  the last successful cleanup time is unchanged.
- `error-stage` and `error-message` identify a failure. If expiration returns but recording the
  last-success time fails, the task can fail while retaining the completed invocation's result.
  Retrying replaces the previous attempt's summary; this is not a complete attempt history.

Expand a task to see its summary and, with write permission, cancel a running task. Cancellation
is routed through AMS ProcessService for both Paimon and Iceberg cleanup tasks. A request that
cannot cancel a tracked active task returns an error rather than reporting success. Iceberg
self-optimizing tasks retain their existing cancellation and partial-commit behavior. A failed
attempt that still has retries remaining can be canceled before its retry starts. If cancellation
wins before the engine identifier is recorded, the executor forwards cancellation once that
identifier becomes available; a task still queued at that point will not run. Cancellation
uses the same framework semantics as Iceberg: it does not undo completed deletion or guarantee
that underlying file operations immediately stop. Failures use the existing bounded retries
(up to three additional attempts per process); later scheduling checks can create a new process.
Recovery repeats the operation with current settings and does not guarantee exactly-once deletion.
