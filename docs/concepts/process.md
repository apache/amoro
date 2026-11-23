---
title: "Processes"
url: process
menu:
    main:
        parent: Concepts
        weight: 250
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
# Processes

This page describes the current development version (1.0.0-SNAPSHOT).

Amoro manages table maintenance through a process framework. A **process** is a unit of work that performs a specific maintenance action on a table. Understanding processes helps you configure maintenance behavior, monitor execution, and troubleshoot issues.

## Actions and instances

An **action** is a category of maintenance work defined by the Amoro process framework. Examples include:

- `expire-snapshots` – remove old table snapshots.
- `clean-orphan-files` – delete files that are no longer referenced by any snapshot.
- `clean-dangling-delete-files` – remove dangling delete files.
- `expire-data` – remove data older than a configured retention period.
- `auto-create-tags` – automatically create tags on Iceberg tables.
- `sync-hive-tables` – synchronize Hive metadata for Mixed-Hive tables.

An **instance** is a concrete execution of an action against a specific table at a specific time. Each instance has a unique process ID, its own lifecycle, and may capture status, timing, and summary information.

## Process lifecycle

Process instances move through a series of states. The lifecycle includes shared states such as `PENDING`, `SUBMITTED`, `RUNNING`, `SUCCESS`, `FAILED`, `CANCELED`, and `KILLED`. Not every process visits every state; the actual transitions depend on the engine and the operation.

AMS maintains a history of maintenance process instances in its process store. This history is retained for operator visibility and troubleshooting. Expired process history is cleaned automatically based on the AMS configuration (see the [AMS Configuration](../ams-config/) page and the deployment guide's retention settings).

## Three configuration layers

Process configuration is split between AMS service settings, plugin configuration, and table properties.

1. **AMS service settings**
   AMS settings under `ams:` in `$AMORO_CONF_DIR/config.yaml` control service-wide behavior. Process history retention defaults to 7 days (`process.history-data-keep-time`); self-optimizing runtime data retention defaults to 30 days (`self-optimizing.runtime-data-keep-time`), with an expiration check interval of 1 hour (`self-optimizing.runtime-data-expire-interval`). See the [AMS Configuration](../ams-config/) reference and [duration-key migration examples](../deployment/#migrate-duration-keys). These settings control retained history, rather than enabling a table action.

2. **Plugin configuration: process factories and execute engines**
   The process plugin configuration contains both **process-factories.yaml** and **execute-engines.yaml**. This layer registers available actions and execution engines. The shipped `iceberg` factory registers actions and their trigger intervals. The built-in `local` engine executes actions inside the AMS JVM using thread pools. An action must be registered for the table format. The factory also needs an enabled local engine before it can trigger these maintenance processes.

3. **Table properties and format eligibility**
   Individual tables can enable or disable cleanup actions using table properties. For example:
   - `table-expire.enabled` (default `true`)
   - `clean-orphan-file.enabled` (default `false`)
   - `clean-dangling-delete-files.enabled` (default `true`)
   - `data-expire.enabled` (default `false`)
   - `tag.auto-create.enabled` (default `false`)

   The `gc.enabled` property (default `true`) gates snapshot expiration, orphan file cleaning, dangling delete file cleaning, and data expiration. For data expiration, the table must also define a valid expiration field and a positive retention time.

   Format restrictions apply: `auto-create-tags` is only available for `ICEBERG` tables; `sync-hive-tables` is only available for `MIXED_HIVE` tables.

A table is eligible for an action only when all relevant checks pass. Even then, an interval defines when the action is *scheduled*, not a guarantee that it runs at that exact moment.

## Defaults and per-table enablement

The process factory declares defaults for action enablement and intervals. With its shipped nonempty configuration, the `iceberg` factory enables all actions except `sync-hive-tables`, with intervals as shown below. Keep the `properties` block in the [factory configuration](../deployment/#configure-process-factories): an empty properties map does not register Iceberg maintenance actions.

| Action | Factory enabled | Default interval | Table-level conditions and format limits |
|---|---|---|---|
| `expire-snapshots` | true | 1h | Requires `table-expire.enabled=true` and `gc.enabled=true`. Applies to `ICEBERG`, `MIXED_ICEBERG`, `MIXED_HIVE`. |
| `clean-orphan-files` | true | 1d | Requires `clean-orphan-file.enabled=true` (singular file property) and `gc.enabled=true`. Applies to `ICEBERG`, `MIXED_ICEBERG`, `MIXED_HIVE`. |
| `clean-dangling-delete-files` | true | 1d | Requires `clean-dangling-delete-files.enabled=true` and `gc.enabled=true`. Applies to `ICEBERG`, `MIXED_ICEBERG`, `MIXED_HIVE`. |
| `expire-data` | true | 1d | Requires `data-expire.enabled=true` and `gc.enabled=true`. Requires valid expiration field and positive retention time. Applies to `ICEBERG`, `MIXED_ICEBERG`, `MIXED_HIVE`. |
| `auto-create-tags` | true | 1m | Requires `tag.auto-create.enabled=true`. Only applies to `ICEBERG` tables. |
| `sync-hive-tables` | false | 10m | When enabled, requires table format `MIXED_HIVE`. There is no table-level switch for this action; enabling is via factory configuration only. |

Table properties have their own defaults. For example, table expiration is enabled by default, but orphan file cleaning and data expiration are disabled by default. This means an action may be registered by the factory but still not execute for a table until the table property is turned on.

## Local engine and thread pools

The local engine executes processes in thread pools. Each process declares a **tag** that is used to select a pool. The six concrete maintenance processes in the Iceberg factory use tags that match their action names, for example `expire-snapshots`, `clean-orphan-files`, `clean-dangling-delete-files`, `expire-data`, `auto-create-tags`, and `sync-hive-tables`.

The local engine looks for a configured pool named `pool.<tag>.thread-count`. If that pool exists, it is used; otherwise the process falls back to the `default` pool. The default pool size is 10 threads. Custom pool sizes must be positive integers.

The engine also keeps a short-lived cache of recently completed process statuses. By default, finished process status is cached for 4 hours (`process.status.ttl`). This cache is separate from the persisted process history and unrelated to any job timeout.

## Managed process abstraction

`LocalProcess` runs maintenance logic in the AMS JVM. The `ManagedProcess` API lets AMS modules submit, complete, retry, or kill a process. The Iceberg maintenance actions described here use the local engine.

Self-optimizing has its own tuning and execution model; see the [Self-optimizing](../self-optimizing/) page for optimizer configuration.

## Next steps

- Configure process factories and the local engine in the [Deployment guide](../deployment/).
- Set per-table cleanup properties in the [Table Configurations guide](../configurations/).
- Review AMS service settings in the [AMS Configuration](../ams-config/) reference.
