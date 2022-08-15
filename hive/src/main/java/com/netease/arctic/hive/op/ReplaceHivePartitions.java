package com.netease.arctic.hive.op;

import com.netease.arctic.hive.HMSClient;
import com.netease.arctic.hive.table.UnkeyedHiveTable;
import com.netease.arctic.utils.FileUtil;
import java.util.Map;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.PrincipalPrivilegeSet;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.Snapshot;

import java.util.List;
import java.util.function.Consumer;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.relocated.com.google.common.base.Joiner;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.apache.thrift.TException;

public class ReplaceHivePartitions implements ReplacePartitions {

  final ReplacePartitions delegate;
  final HMSClient hmsClient;

  final HMSClient transactionalHMSClient;

  final UnkeyedHiveTable table;

  final List<DataFile> addFiles = Lists.newArrayList();

  final String db;
  final String tableName;

  final Table hiveTable;

  List<Partition> rewritePartitions = Lists.newArrayList();
  List<Partition> newPartitions = Lists.newArrayList();

  public ReplaceHivePartitions(
      ReplacePartitions delegate,
      UnkeyedHiveTable table,
      HMSClient client,
      HMSClient transactionalClient) {
    this.delegate = delegate;
    this.hmsClient = client;
    this.transactionalHMSClient = transactionalClient;
    this.table = table;
    this.db = table.id().getDatabase();
    this.tableName = table.id().getTableName();
    try {
      this.hiveTable = client.run(c -> c.getTable(db, tableName));
    } catch (TException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ReplacePartitions addFile(DataFile file) {
    delegate.addFile(file);
    String tableLocation = hiveTable.getSd().getLocation();
    String dataFileLocation = file.path().toString();
    if (dataFileLocation.toLowerCase().contains(tableLocation.toLowerCase())) {
      // only handle file in hive location
      this.addFiles.add(file);
    }
    return this;
  }

  @Override
  public ReplacePartitions validateAppendOnly() {
    delegate.validateAppendOnly();
    return this;
  }

  @Override
  public ReplacePartitions set(String property, String value) {
    delegate.set(property, value);
    return this;
  }

  @Override
  public ReplacePartitions deleteWith(Consumer<String> deleteFunc) {
    delegate.deleteWith(deleteFunc);
    return this;
  }

  @Override
  public ReplacePartitions stageOnly() {
    delegate.stageOnly();
    return this;
  }

  @Override
  public Snapshot apply() {
    return delegate.apply();
  }

  @Override
  public void commit() {
    if (!addFiles.isEmpty() && !table.spec().isUnpartitioned()) {
      applyHivePartitions();
    }

    delegate.commit();
    if (!addFiles.isEmpty()) {
      if (table.spec().isUnpartitioned()) {
        commitUnPartitionedTable();
      } else {
        commitPartitionedTable();
      }
    }
  }

  @Override
  public Object updateEvent() {
    return delegate.updateEvent();
  }

  private void applyHivePartitions() {
    Types.StructType partitionSchema = table.spec().partitionType();

    // partitionValue -> partitionLocation.
    Map<String, String> partitionLocationMap = Maps.newHashMap();
    Map<String, List<DataFile>> partitionDataFileMap = Maps.newHashMap();
    Map<String, List<String>> partitionValueMap = Maps.newHashMap();

    for (DataFile d : addFiles) {
      List<String> partitionValues = partitionValues(d.partition(), partitionSchema);
      String value = Joiner.on("/").join(partitionValues);
      String location = FileUtil.getFileDir(d.path().toString());
      partitionLocationMap.put(value, location);
      if (!partitionDataFileMap.containsKey(value)) {
        partitionDataFileMap.put(value, Lists.newArrayList());
      }
      partitionDataFileMap.get(value).add(d);
      partitionValueMap.put(value, partitionValues);
    }

    for (String val : partitionValueMap.keySet()) {
      List<String> values = partitionValueMap.get(val);
      String location = partitionLocationMap.get(val);
      List<DataFile> dataFiles = partitionDataFileMap.get(val);

      try {
        Partition partition = hmsClient.run(c -> c.getPartition(db, tableName, values));
        rewriteHivePartitions(partition, location, dataFiles);
        rewritePartitions.add(partition);
      } catch (NoSuchObjectException e) {
        Partition p = newPartition(hiveTable, values, location, dataFiles);
        newPartitions.add(p);
      } catch (TException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void commitUnPartitionedTable() {
    if (!addFiles.isEmpty()) {
      final String newDataLocation = FileUtil.getFileDir(addFiles.get(0).path().toString());
      try {
        transactionalHMSClient.run(c -> {
          Table tbl = c.getTable(db, tableName);
          tbl.getSd().setLocation(newDataLocation);
          c.alter_table(db, tableName, tbl);
          return 0;
        });
      } catch (TException |InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void commitPartitionedTable() {
    try {
      transactionalHMSClient.run(c -> {
        if (!rewritePartitions.isEmpty()){
          c.alter_partitions(db, tableName, rewritePartitions);
        }
        if (!newPartitions.isEmpty()){
          c.add_partitions(newPartitions);
        }
        return 0;
      });
    } catch (TException |InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private static Partition rewriteHivePartitions(Partition partition, String location, List<DataFile> dataFiles) {
    partition.getSd().setLocation(location);
    int lastAccessTime = (int) (System.currentTimeMillis() / 1000);
    partition.setLastAccessTime(lastAccessTime);
    int files = dataFiles.size();
    long totalSize = dataFiles.stream().map(ContentFile::fileSizeInBytes).reduce(0L, Long::sum);
    partition.putToParameters("transient_lastDdlTime", lastAccessTime + "");
    partition.putToParameters("totalSize", totalSize + "");
    partition.putToParameters("numFiles", files + "");
    return partition;
  }

  private static Partition newPartition(
      Table hiveTable,
      List<String> values,
      String location,
      List<DataFile> dataFiles) {
    StorageDescriptor tableSd = hiveTable.getSd();
    PrincipalPrivilegeSet privilegeSet = hiveTable.getPrivileges();
    int lastAccessTime = (int) (System.currentTimeMillis() / 1000);
    Partition p = new Partition();
    p.setValues(values);
    p.setDbName(hiveTable.getDbName());
    p.setTableName(hiveTable.getTableName());
    p.setCreateTime(lastAccessTime);
    p.setLastAccessTime(lastAccessTime);
    StorageDescriptor sd = tableSd.deepCopy();
    sd.setLocation(location);
    p.setSd(sd);

    int files = dataFiles.size();
    long totalSize = dataFiles.stream().map(ContentFile::fileSizeInBytes).reduce(0L, Long::sum);
    p.putToParameters("transient_lastDdlTime", lastAccessTime + "");
    p.putToParameters("totalSize", totalSize + "");
    p.putToParameters("numFiles", files + "");
    if (privilegeSet != null) {
      p.setPrivileges(privilegeSet.deepCopy());
    }
    return p;
  }

  private static List<String> partitionValues(StructLike partitionData, Types.StructType partitionSchema) {
    List<Types.NestedField> fields = partitionSchema.fields();
    List<String> values = Lists.newArrayList();
    for (int i = 0; i < fields.size(); i++) {
      Type type = fields.get(i).type();
      Object value = partitionData.get(i, type.typeId().javaClass());
      values.add(value.toString());
    }
    return values;
  }
}
