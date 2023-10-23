package com.netease.arctic.utils;

import com.netease.arctic.catalog.IcebergCatalogWrapper;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StatisticsFile;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.util.StructLikeMap;

import java.util.List;

public class ArcticTableUtil {
  public static final String BLOB_TYPE_OPTIMIZED_SEQUENCE = "optimized-sequence";
  public static final String BLOB_TYPE_BASE_OPTIMIZED_TIME = "base-optimized-time";

  /**
   * check arctic table is iceberg table format
   *
   * @param arcticTable target arctic table
   * @return Whether iceberg table format
   */
  public static boolean isIcebergTableFormat(ArcticTable arcticTable) {
    return arcticTable instanceof IcebergCatalogWrapper.BasicIcebergTable;
  }

  /** Return the base store of the arctic table. */
  public static UnkeyedTable baseStore(ArcticTable arcticTable) {
    
    if (arcticTable.isKeyedTable()) {
      return arcticTable.asKeyedTable().baseTable();
    } else {
      return arcticTable.asUnkeyedTable();
    }
  }

  /** Return the table root location of the arctic table. */
  public static String tableRootLocation(ArcticTable arcticTable) {
    String tableRootLocation;
    if (!ArcticTableUtil.isIcebergTableFormat(arcticTable) && arcticTable.isUnkeyedTable()) {
      tableRootLocation = TableFileUtil.getFileDir(arcticTable.location());
    } else {
      tableRootLocation = arcticTable.location();
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

  private static StructLikeMap<Long> readWithLegacy(UnkeyedTable table, Long snapshotId, String type) {
    if (snapshotId == null) {
      Snapshot snapshot = table.currentSnapshot();
      if (snapshot == null) {
        return readLegacyPartitionProperties(table, type);
      } else {
        snapshotId = snapshot.snapshotId();
      }
    }
    StructLikeMap<Long> result = readFromPuffin(table, snapshotId, type);
    return result != null ? result : readLegacyPartitionProperties(table, type);
  }

  private static StructLikeMap<Long> readFromPuffin(UnkeyedTable table, long snapshotId, String type) {
    Snapshot snapshot = table.snapshot(snapshotId);
    Preconditions.checkArgument(snapshot != null, "Snapshot %s not found", snapshotId);
    List<StatisticsFile> statisticsFiles =
        PuffinUtil.findLatestValidStatisticsFiles(table, snapshot.snapshotId(), PuffinUtil.containsBlobOfType(type));
    if (statisticsFiles != null && !statisticsFiles.isEmpty()) {
      Preconditions.checkArgument(statisticsFiles.size() == 1,
          "There should be only one statistics file for blob type %s", type);
      return PuffinUtil.reader(table)
          .read(statisticsFiles.get(0), type, PuffinUtil.createPartitionDataSerializer(table.spec()));
    } else {
      return null;
    }
  }

  private static StructLikeMap<Long> readLegacyPartitionProperties(UnkeyedTable table, String type) {
    // to be compatible with old Amoro version 0.5.0 which didn't use puffin file and stored the statistics in
    // table properties
    switch (type) {
      case BLOB_TYPE_OPTIMIZED_SEQUENCE:
        return TablePropertyUtil.getPartitionLongProperties(table, TableProperties.PARTITION_OPTIMIZED_SEQUENCE);
      case BLOB_TYPE_BASE_OPTIMIZED_TIME:
        return TablePropertyUtil.getPartitionLongProperties(table, TableProperties.PARTITION_BASE_OPTIMIZED_TIME);
      default:
        throw new IllegalArgumentException("Unknown type: " + type);
    }
  }
}
