/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.formats.paimon.process;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.paimon.CoreOptions;
import org.apache.paimon.options.ExpireConfig;
import org.apache.paimon.table.AppendOnlyFileStoreTable;
import org.apache.paimon.table.FileStoreTable;
import org.apache.paimon.table.PrimaryKeyFileStoreTable;
import org.apache.paimon.table.Table;
import org.apache.paimon.utils.BranchManager;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/** Table eligibility and native retention configuration for snapshot expiration. */
final class PaimonSnapshotExpiration {
  private static final String ENABLED = "amoro.expire-snapshots.enabled";
  private static final boolean ENABLED_DEFAULT = true;

  private PaimonSnapshotExpiration() {}

  static PaimonTable refresh(TableRuntime runtime) {
    AmoroTable<?> table = runtime.loadTable();
    if (!(table instanceof PaimonTable)) {
      throw new IllegalArgumentException("Snapshot expiration requires a Paimon table");
    }
    return ((PaimonTable) table).refresh();
  }

  /** Null means that this table is eligible; other values are safe diagnostic reason codes. */
  static String skipReason(PaimonTable table) {
    Map<String, String> properties = table.properties();
    if (!Boolean.parseBoolean(properties.getOrDefault(ENABLED, String.valueOf(ENABLED_DEFAULT)))) {
      return "governance-disabled";
    }
    Table nativeTable = table.originalTable();
    if (!(nativeTable instanceof PrimaryKeyFileStoreTable)
        && !(nativeTable instanceof AppendOnlyFileStoreTable)) {
      return "unsupported-table-type";
    }
    FileStoreTable fileStoreTable = (FileStoreTable) nativeTable;
    if (!BranchManager.isMainBranch(fileStoreTable.snapshotManager().branch())) {
      return "non-default-branch";
    }
    CoreOptions options = fileStoreTable.store().options();
    if (!options.writeOnly()) {
      return "write-only-required";
    }
    return null;
  }

  static ExpireConfig configuration(FileStoreTable table) {
    ExpireConfig config = table.store().options().expireConfig();
    validate(
        config.getSnapshotRetainMin(),
        config.getSnapshotRetainMax(),
        config.getSnapshotTimeRetain(),
        config.getSnapshotMaxDeletes());
    validate(
        config.getChangelogRetainMin(),
        config.getChangelogRetainMax(),
        config.getChangelogTimeRetain(),
        config.getChangelogMaxDeletes());
    return config;
  }

  private static void validate(int min, int max, Duration time, int limit) {
    if (min < 1 || max < min || time == null || time.isNegative() || limit <= 0) {
      throw new IllegalArgumentException("Invalid Paimon retention configuration");
    }
    // The native expiration implementation operates in milliseconds.
    time.toMillis();
  }

  static Map<String, String> summary(ExpireConfig config) {
    Map<String, String> result = new LinkedHashMap<>();
    result.put("snapshot.num-retained.min", String.valueOf(config.getSnapshotRetainMin()));
    result.put("snapshot.num-retained.max", String.valueOf(config.getSnapshotRetainMax()));
    result.put("snapshot.time-retained", config.getSnapshotTimeRetain().toString());
    result.put("snapshot.expire.limit", String.valueOf(config.getSnapshotMaxDeletes()));
    result.put("changelog.num-retained.min", String.valueOf(config.getChangelogRetainMin()));
    result.put("changelog.num-retained.max", String.valueOf(config.getChangelogRetainMax()));
    result.put("changelog.time-retained", config.getChangelogTimeRetain().toString());
    result.put("effective-changelog-max-deletes", String.valueOf(config.getChangelogMaxDeletes()));
    return result;
  }
}
