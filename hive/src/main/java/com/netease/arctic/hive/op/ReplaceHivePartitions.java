package com.netease.arctic.hive.op;

import com.netease.arctic.hive.HMSClient;
import com.netease.arctic.hive.exceptions.CannotAlterHiveLocationException;
import com.netease.arctic.hive.table.UnkeyedHiveTable;
import com.netease.arctic.hive.utils.HivePartitionUtil;
import com.netease.arctic.utils.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.relocated.com.google.common.base.Joiner;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.types.Types;
import org.apache.thrift.TException;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
      List<String> partitionValues = HivePartitionUtil.partitionValuesAsList(d.partition(), partitionSchema);
      String value = Joiner.on("/").join(partitionValues);
      String location = FileUtil.getFileDir(d.path().toString());
      partitionLocationMap.put(value, location);
      if (!partitionDataFileMap.containsKey(value)) {
        partitionDataFileMap.put(value, Lists.newArrayList());
      }
      partitionDataFileMap.get(value).add(d);
      partitionValueMap.put(value, partitionValues);
    }
    partitionLocationMap.forEach((k, v) -> checkDataFileInSameLocation(v, partitionDataFileMap.get(k)));

    for (String val : partitionValueMap.keySet()) {
      List<String> values = partitionValueMap.get(val);
      String location = partitionLocationMap.get(val);
      List<DataFile> dataFiles = partitionDataFileMap.get(val);

      try {
        Partition partition = hmsClient.run(c -> c.getPartition(db, tableName, values));
        rewriteHivePartitions(partition, location, dataFiles);
        rewritePartitions.add(partition);
      } catch (NoSuchObjectException e) {
        Partition p = HivePartitionUtil.newPartition(hiveTable, values, location, dataFiles);
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
      } catch (TException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void commitPartitionedTable() {
    try {
      transactionalHMSClient.run(c -> {
        if (!rewritePartitions.isEmpty()) {
          c.alter_partitions(db, tableName, rewritePartitions);
        }
        if (!newPartitions.isEmpty()) {
          c.add_partitions(newPartitions);
        }
        return 0;
      });
    } catch (TException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void checkDataFileInSameLocation(String partitionLocation, List<DataFile> files) {
    Path partitionPath = new Path(partitionLocation);
    for (DataFile df : files) {
      String fileDir = FileUtil.getFileDir(df.path().toString());
      Path dirPath = new Path(fileDir);
      if (!partitionPath.equals(dirPath)) {
        throw new CannotAlterHiveLocationException(
            "can't create new hive location: " + partitionLocation + " for data file: " + df.path().toString() +
                " is not under partition location path"
        );
      }
    }
  }

  private static void rewriteHivePartitions(Partition partition, String location, List<DataFile> dataFiles) {
    partition.getSd().setLocation(location);
    int lastAccessTime = (int) (System.currentTimeMillis() / 1000);
    partition.setLastAccessTime(lastAccessTime);
    int files = dataFiles.size();
    long totalSize = dataFiles.stream().map(ContentFile::fileSizeInBytes).reduce(0L, Long::sum);
    partition.putToParameters("transient_lastDdlTime", lastAccessTime + "");
    partition.putToParameters("totalSize", totalSize + "");
    partition.putToParameters("numFiles", files + "");
  }
}
