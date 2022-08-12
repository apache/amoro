package com.netease.arctic.hive.op;

import com.netease.arctic.hive.CachedHiveClientPool;
import com.netease.arctic.hive.HMSClient;
import com.netease.arctic.op.KeyedPartitionRewrite;
import com.netease.arctic.table.KeyedTable;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.PrincipalPrivilegeSet;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.relocated.com.google.common.base.Joiner;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.StructLikeMap;
import org.apache.thrift.TException;

import java.util.List;
import java.util.Map;

public class HiveRewritePartitions extends KeyedPartitionRewrite {
  private HMSClient client;
  private final String db;
  private final String tableName;
  private final KeyedTable keyedTable;

  private List<Partition> rewritePartitions;
  private List<Partition> newPartitions;
  private String hiveLocation;

  public HiveRewritePartitions(KeyedTable keyedTable, HMSClient client) {
    super(keyedTable);
    this.client = client;
    this.db = keyedTable.id().getDatabase();
    this.tableName = keyedTable.id().getTableName();
    this.keyedTable = keyedTable;
  }

  @Override
  protected StructLikeMap<Long> apply(Transaction transaction, StructLikeMap<Long> partitionMaxTxId) {
    StructLikeMap<Long> partitionMaxId = super.apply(transaction, partitionMaxTxId);
    if (!addFiles.isEmpty()) {
      try {
        if (keyedTable.spec().isUnpartitioned()) {
          hiveLocation = dataFileDirPath(addFiles.get(0));
        } else {
          rewritePartitions();
        }
      } catch (TException | InterruptedException e) {
        throw new IllegalStateException("failed to load partition information from hive metastore");
      }
    }
    return partitionMaxId;
  }

  @Override
  public void commit() {
    super.commit();

    try {
      if (keyedTable.spec().isUnpartitioned() && !StringUtils.isEmpty(hiveLocation)) {
        client.run(c -> {
          Table tbl = c.getTable(db, tableName);
          tbl.getSd().setLocation(hiveLocation);
          c.alter_table(db, tableName, tbl);
          return 0;
        });
      } else if (!keyedTable.spec().isUnpartitioned() &&
          (rewritePartitions != null || newPartitions != null)) {
        client.run(
            c -> {
              if (newPartitions != null) {
                c.add_partitions(newPartitions);
              }
              if (rewritePartitions != null) {
                c.alter_partitions(db, tableName, rewritePartitions);
              }
              return 0;
            }
        );
      }
    } catch (TException | InterruptedException e) {
      throw new IllegalStateException("failed to update data location in hive metastore");
    }
  }

  private String dataFileDirPath(DataFile d) {
    Path path = new Path(d.path().toString());
    return path.getParent().toString();
  }

  private void rewritePartitions() throws TException, InterruptedException {
    Types.StructType partitionSchema = keyedTable.spec().partitionType();

    // partitionValue -> partitionLocation.
    Map<String, String> partitionLocationMap = Maps.newHashMap();
    Map<String, List<DataFile>> partitionDataFileMap = Maps.newHashMap();
    Map<String, String[]> partitionValueMap = Maps.newHashMap();
    for (DataFile d : addFiles) {
      String[] partitionValues = partitionValues(d.partition(), partitionSchema);
      String value = Joiner.on("/").join(partitionValues);
      String location = dataFileDirPath(d);
      partitionLocationMap.put(value, location);
      if (!partitionDataFileMap.containsKey(value)) {
        partitionDataFileMap.put(value, Lists.newArrayList());
      }
      partitionDataFileMap.get(value).add(d);
      partitionValueMap.put(value, partitionValues);
    }

    List<Partition> tablePartitions = client.run(
        c -> c.listPartitions(db, tableName, (short) -1)
    );

    List<Partition> rewritePartition = Lists.newArrayList();
    List<String> matchedPartition = Lists.newArrayList();
    for (Partition p : tablePartitions) {
      String values = Joiner.on("/").join(p.getValues());
      if (partitionLocationMap.containsKey(values)) {
        String newLocation = partitionLocationMap.get(values);
        p.getSd().setLocation(newLocation);
        int lastAccessTime = (int) (System.currentTimeMillis() / 1000);
        p.setLastAccessTime(lastAccessTime);

        List<DataFile> partitionDataFiles = partitionDataFileMap.get(values);
        int files = partitionDataFiles.size();
        long totalSize = partitionDataFiles.stream().map(ContentFile::fileSizeInBytes).reduce(0L, Long::sum);
        p.putToParameters("transient_lastDdlTime", lastAccessTime + "");
        p.putToParameters("totalSize", totalSize + "");
        p.putToParameters("numFiles", files + "");

        rewritePartition.add(p);
        matchedPartition.add(values);
      }
    }
    if (!rewritePartition.isEmpty()) {
      this.rewritePartitions = rewritePartition;
    }

    // remove rewrite partition, left partitions are new partitions.
    matchedPartition.forEach(partitionLocationMap::remove);

    if (!partitionLocationMap.isEmpty()) {
      this.newPartitions = Lists.newArrayList();

      Table tbl = client.run(c -> c.getTable(db, tableName));
      StorageDescriptor tableSd = tbl.getSd();
      PrincipalPrivilegeSet privilegeSet = tbl.getPrivileges();
      for (String val : partitionLocationMap.keySet()) {
        String location = partitionLocationMap.get(val);
        String[] values = partitionValueMap.get(val);
        int lastAccessTime = (int) (System.currentTimeMillis() / 1000);

        Partition p = new Partition();
        p.setValues(Lists.newArrayList(values));
        p.setDbName(db);
        p.setTableName(tableName);
        p.setCreateTime(lastAccessTime);
        p.setLastAccessTime(lastAccessTime);
        StorageDescriptor sd = tableSd.deepCopy();
        sd.setLocation(location);
        p.setSd(sd);
        List<DataFile> partitionDataFiles = partitionDataFileMap.get(val);
        int files = partitionDataFiles.size();
        long totalSize = partitionDataFiles.stream().map(ContentFile::fileSizeInBytes).reduce(0L, Long::sum);
        p.putToParameters("transient_lastDdlTime", lastAccessTime + "");
        p.putToParameters("totalSize", totalSize + "");
        p.putToParameters("numFiles", files + "");
        if (privilegeSet != null) {
          p.setPrivileges(privilegeSet.deepCopy());
        }
        newPartitions.add(p);
      }
    }
  }

  private static String[] partitionValues(StructLike partitionData, Types.StructType partitionSchema) {
    List<Types.NestedField> fields = partitionSchema.fields();
    String[] values = new String[fields.size()];
    for (int i = 0; i < values.length; i++) {
      Type type = fields.get(i).type();
      Object value = partitionData.get(i, type.typeId().javaClass());
      values[i] = value.toString();
    }
    return values;
  }
}
