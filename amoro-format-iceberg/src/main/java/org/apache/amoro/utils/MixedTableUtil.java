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

package org.apache.amoro.utils;

import org.apache.amoro.TableFormat;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StatisticsFile;
import org.apache.iceberg.Table;
import org.apache.iceberg.util.StructLikeMap;

import java.util.List;
import java.util.function.Predicate;

public class MixedTableUtil {
  public static final String BLOB_TYPE_OPTIMIZED_SEQUENCE = "optimized-sequence";
  public static final String BLOB_TYPE_BASE_OPTIMIZED_TIME = "base-optimized-time";

  public static final String BLOB_TYPE_OPTIMIZED_SEQUENCE_EXIST = "optimized-sequence.exist";
  public static final String BLOB_TYPE_BASE_OPTIMIZED_TIME_EXIST = "base-optimized-time.exist";

  /** Return the base store of the mixed-format table. */
  public static UnkeyedTable baseStore(MixedTable mixedTable) {
    if (mixedTable.isKeyedTable()) {
      return mixedTable.asKeyedTable().baseTable();
    } else {
      return mixedTable.asUnkeyedTable();
    }
  }

  /** Return the table root location of the mixed-format table. */
  public static String tableRootLocation(MixedTable mixedTable) {
    String tableRootLocation;
    if (!TableFormat.ICEBERG.equals(mixedTable.format()) && mixedTable.isUnkeyedTable()) {
      tableRootLocation = TableFileUtil.getFileDir(mixedTable.location());
    } else {
      tableRootLocation = mixedTable.location();
    }
    return tableRootLocation;
  }

  public static StructLikeMap<Long> readOptimizedSequence(KeyedTable table) {
    return readWithLegacy(table.baseTable(), null, BLOB_TYPE_OPTIMIZED_SEQUENCE);
  }

  public static StructLikeMap<Long> readOptimizedSequence(KeyedTable table, long snapshotId) {
    return readWithLegacy(table.baseTable(), snapshotId, BLOB_TYPE_OPTIMIZED_SEQUENCE);
  }

  public static StructLikeMap<Long> readBaseOptimizedTime(KeyedTable table) {
    return readWithLegacy(table.baseTable(), null, BLOB_TYPE_BASE_OPTIMIZED_TIME);
  }

  public static StructLikeMap<Long> readBaseOptimizedTime(KeyedTable table, long snapshotId) {
    return readWithLegacy(table.baseTable(), snapshotId, BLOB_TYPE_BASE_OPTIMIZED_TIME);
  }

  private static StructLikeMap<Long> readWithLegacy(
      UnkeyedTable table, Long snapshotId, String type) {
    if (snapshotId == null) {
      Snapshot snapshot = table.currentSnapshot();
      if (snapshot == null) {
        return readLegacyPartitionProperties(table, type);
      } else {
        snapshotId = snapshot.snapshotId();
      }
    }
    StructLikeMap<Long> result = readFromStatisticsFile(table, snapshotId, type);
    return result != null ? result : readLegacyPartitionProperties(table, type);
  }

  private static StructLikeMap<Long> readFromStatisticsFile(
      UnkeyedTable table, long snapshotId, String type) {
    Snapshot snapshot = table.snapshot(snapshotId);
    Preconditions.checkArgument(snapshot != null, "Snapshot %s not found", snapshotId);
    Snapshot snapshotContainsType = findLatestValidSnapshot(table, snapshotId, isTypeExist(type));
    if (snapshotContainsType == null) {
      // Return null to read from legacy partition properties
      return null;
    }
    List<StatisticsFile> statisticsFiles =
        StatisticsFileUtil.getStatisticsFiles(table, snapshotContainsType.snapshotId(), type);
    Preconditions.checkState(
        !statisticsFiles.isEmpty(), "Statistics file not found for snapshot %s", snapshotId);
    Preconditions.checkState(
        statisticsFiles.size() == 1,
        "There should be only one statistics file for snapshot %s",
        snapshotId);
    List<StructLikeMap<Long>> result =
        StatisticsFileUtil.reader(table)
            .read(
                statisticsFiles.get(0),
                type,
                StatisticsFileUtil.createPartitionDataSerializer(table.spec(), Long.class));
    if (result.size() != 1) {
      throw new IllegalStateException(
          "There should be only one partition data in statistics file for blob type " + type);
    }
    return result.get(0);
  }

  /**
   * Find the latest valid snapshot which satisfies the condition.
   *
   * @param table - Iceberg table
   * @param currentSnapshotId - find from this snapshot
   * @param condition - the condition to satisfy
   * @return the latest valid snapshot or null if not exists
   */
  public static Snapshot findLatestValidSnapshot(
      Table table, long currentSnapshotId, Predicate<Snapshot> condition) {
    Long snapshotId = currentSnapshotId;
    while (true) {
      if (snapshotId == null || table.snapshot(snapshotId) == null) {
        return null;
      }
      Snapshot snapshot = table.snapshot(snapshotId);
      if (condition.test(snapshot)) {
        return snapshot;
      } else {
        // seek parent snapshot
        snapshotId = snapshot.parentId();
      }
    }
  }

  @VisibleForTesting
  public static Predicate<Snapshot> isTypeExist(String type) {
    switch (type) {
      case BLOB_TYPE_OPTIMIZED_SEQUENCE:
        return snapshot -> snapshot.summary().containsKey(BLOB_TYPE_OPTIMIZED_SEQUENCE_EXIST);
      case BLOB_TYPE_BASE_OPTIMIZED_TIME:
        return snapshot -> snapshot.summary().containsKey(BLOB_TYPE_BASE_OPTIMIZED_TIME_EXIST);
      default:
        throw new IllegalArgumentException("Unknown type: " + type);
    }
  }

  private static StructLikeMap<Long> readLegacyPartitionProperties(
      UnkeyedTable table, String type) {
    // To be compatible with old Amoro version 0.5.0 which didn't use puffin file and stored the
    // statistics in table properties
    switch (type) {
      case BLOB_TYPE_OPTIMIZED_SEQUENCE:
        return TablePropertyUtil.getPartitionLongProperties(
            table, TableProperties.PARTITION_OPTIMIZED_SEQUENCE);
      case BLOB_TYPE_BASE_OPTIMIZED_TIME:
        return TablePropertyUtil.getPartitionLongProperties(
            table, TableProperties.PARTITION_BASE_OPTIMIZED_TIME);
      default:
        throw new IllegalArgumentException("Unknown type: " + type);
    }
  }

  /**
   * Return the {@link PartitionSpec} of the mixed-format table by {@link PartitionSpec#specId()},
   * Mix format table will return directly after checking}.
   */
  public static PartitionSpec getMixedTablePartitionSpecById(MixedTable mixedTable, int specId) {
    if (TableFormat.ICEBERG.equals(mixedTable.format())) {
      return mixedTable.asUnkeyedTable().specs().get(specId);
    } else {
      PartitionSpec spec = mixedTable.spec();
      if (spec.specId() != specId) {
        throw new IllegalArgumentException(
            "Partition spec id " + specId + " not found in table " + mixedTable.name());
      }
      return spec;
    }
  }
}
