package com.netease.arctic.hive.utils;

import com.netease.arctic.hive.catalog.ArcticHiveCatalog;
import com.netease.arctic.hive.table.HiveMetaStore;
import com.netease.arctic.hive.table.HiveTable;
import com.netease.arctic.hive.table.KeyedHiveTable;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.utils.SchemaUtil;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoUtils;
import org.apache.iceberg.ClientPool;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HiveUtils {

  private static final Logger LOG = LoggerFactory.getLogger(HiveUtils.class);

  private static final int PARTITION_COLUMN_START_ID = 2;

  public static void alterTableLocation(
      HiveMetaStore hiveMetastore, TableIdentifier tableIdentifier,
      String newPath) throws IOException {
    try {
      hiveMetastore.run((ClientPool.Action<Void, HiveMetaStoreClient, TException>) client -> {
        Table newTable = hiveMetastore.getHiveTable(tableIdentifier).getTable().deepCopy();
        newTable.getSd().setLocation(newPath);
        client.alter_table(tableIdentifier.getDatabase(), tableIdentifier.getTableName(), newTable);
        return null;
      });
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  public static void alterPartition(
      HiveMetaStore hiveMetastore, TableIdentifier tableIdentifier,
      String partition, String newPath) throws IOException {
    try {
      LOG.info("alter table {} hive partition {} to new location {}",
          tableIdentifier, partition, newPath);
      Partition oldPartition = hiveMetastore.run(
          client -> client.getPartition(
              tableIdentifier.getDatabase(),
              tableIdentifier.getTableName(),
              partition));
      Partition newPartition = new Partition(oldPartition);
      newPartition.getSd().setLocation(newPath);
      hiveMetastore.run((ClientPool.Action<Void, HiveMetaStoreClient, TException>) client -> {
        client.alter_partition(tableIdentifier.getDatabase(),
            tableIdentifier.getTableName(),
            newPartition, null);
        return null;
      });
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  public static List<String> getHivePartitions(HiveMetaStore hiveMetaStore, TableIdentifier tableIdentifier)
      throws TException, InterruptedException {
    return hiveMetaStore.run(client -> client.listPartitionNames(tableIdentifier.getDatabase(),
        tableIdentifier.getTableName(),
        Short.MAX_VALUE)).stream().collect(Collectors.toList());
  }

  public static List<String> getHivePartitionLocations(HiveMetaStore hiveMetaStore,
                                                       TableIdentifier tableIdentifier) throws Exception {

    return hiveMetaStore.run(client -> client.listPartitions(tableIdentifier.getDatabase(),
        tableIdentifier.getTableName(),
        Short.MAX_VALUE))
        .stream()
        .map(partition -> partition.getSd().getLocation())
        .collect(Collectors.toList());
  }

  public static Map<Partition, List<HiveFileInfo>> getHiveTableFiles(
      TableIdentifier tableIdentifier, HiveMetaStore hiveMetaStore, ArcticFileIO io) {
    try {
      HiveTable hiveTable = hiveMetaStore.getHiveTable(tableIdentifier);
      Map<Partition, List<HiveFileInfo>> allFiles = Maps.newHashMap();
      if (hiveTable.getTable().getPartitionKeysSize() == 0) {
        // non-partition table
        List<HiveFileInfo> hiveFileInfos = io.list(hiveTable.getSd().getLocation()).stream()
            .map(HiveFileInfo::of)
            .collect(Collectors.toList());
        allFiles.put(null, hiveFileInfos);
      } else {
        // partition table
        List<Partition> partitions = hiveMetaStore.run(
            client -> client
                .listPartitions(tableIdentifier.getDatabase(), tableIdentifier.getTableName(),
                    Short.MAX_VALUE));
        partitions.forEach(partition -> {
          List<HiveFileInfo> hiveFileInfos = io.list(partition.getSd().getLocation()).stream()
              .map(HiveFileInfo::of)
              .collect(Collectors.toList());
          allFiles.put(partition, hiveFileInfos);
        });
      }
      return allFiles;
    } catch (TException e) {
      throw new RuntimeException("Failed to list files of hive table " + tableIdentifier, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted in call list files of hive table " + tableIdentifier,
          e);
    }
  }

  public static KeyData getPartition(Types.StructType partitionType, List<String> values) {
    KeyData partition = KeyData.get(partitionType);
    for (int i = 0; i < values.size(); i++) {
      partition.set(i, values.get(i));
    }
    return partition;
  }

  public static Types.StructType hiveTablePartitionSpec(HiveMetaStore hiveMetaStore, TableIdentifier tableIdentifier) {
    Table table = hiveMetaStore.getHiveTable(tableIdentifier).getTable();
    int[] id = {PARTITION_COLUMN_START_ID};
    List<Types.NestedField> fields = table.getPartitionKeys()
        .stream()
        .map(fieldSchema -> {
          Type type = HiveTypeUtils.toIcebergType(TypeInfoUtils.getTypeInfoFromTypeString(fieldSchema.getType()));
          return Types.NestedField.optional(id[0]++, fieldSchema.getName(), type, fieldSchema.getComment());
        })
        .collect(Collectors.toList());
    return Types.StructType.of(fields);
  }
}
