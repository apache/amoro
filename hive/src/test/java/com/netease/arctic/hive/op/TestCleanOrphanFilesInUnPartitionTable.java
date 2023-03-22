package com.netease.arctic.hive.op;

import com.netease.arctic.hive.MockDataFileBuilder;
import com.netease.arctic.hive.catalog.HiveTableTestBase;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.netease.arctic.hive.op.UpdateHiveFiles.DELETE_UNTRACKED_HIVE_FILE;

public class TestCleanOrphanFilesInUnPartitionTable extends HiveTableTestBase {

  public TestCleanOrphanFilesInUnPartitionTable() {
    super(false, false);
  }

  @Test
  public void testCleanUnPartitionedOrphanFileWhenOverwrite() throws TException {
    List<Map.Entry<String, String>> orphanFiles = Lists.newArrayList(
        Maps.immutableEntry(null, "/test_path/hive_data_location/orphan-a1.parquet"),
        Maps.immutableEntry(null, "/test_path/hive_data_location/orphan-a2.parquet"),
        Maps.immutableEntry(null, "/test_path/hive_data_location/orphan-a3.parquet")
    );
    UnkeyedTable table = getArcticTable().asUnkeyedTable();
    table.updateProperties().set(DELETE_UNTRACKED_HIVE_FILE, "true").commit();
    AppendFiles appendFiles = table.newAppend();
    MockDataFileBuilder dataFileBuilder = new MockDataFileBuilder(table, TEST_HMS.getHiveClient());
    List<DataFile> orphanDataFiles = dataFileBuilder.buildList(orphanFiles);
    orphanDataFiles.forEach(appendFiles::appendFile);
    appendFiles.commit();

    List<Map.Entry<String, String>> files = Lists.newArrayList(
        Maps.immutableEntry(null, "/test_path/hive_data_location/data-a1.parquet"),
        Maps.immutableEntry(null, "/test_path/hive_data_location/data-a2.parquet"),
        Maps.immutableEntry(null, "/test_path/hive_data_location/data-a3.parquet")
    );
    List<DataFile> dataFiles = dataFileBuilder.buildList(files);

    OverwriteFiles overwriteFiles = table.newOverwrite();
    overwriteFiles.set(DELETE_UNTRACKED_HIVE_FILE, "true");
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();

    List<String> exceptedFiles = new ArrayList<>();
    exceptedFiles.add("data-a1.parquet");
    exceptedFiles.add("data-a2.parquet");
    exceptedFiles.add("data-a3.parquet");
    Table hiveTable = TEST_HMS.getHiveClient().getTable(table.id().getDatabase(), table.name());
    List<String> fileNameList = table.io().list(hiveTable.getSd().
        getLocation()).stream().map(f -> f.getPath().getName()).sorted().collect(Collectors.toList());
    Assert.assertEquals(exceptedFiles, fileNameList);
  }

  @Test
  public void testCleanUnPartitionedOrphanFileWhenRewrite() throws TException {
    List<Map.Entry<String, String>> orphanFiles = Lists.newArrayList(
        Maps.immutableEntry(null, "/test_path/hive_data_location/data-a1.parquet"),
        Maps.immutableEntry(null, "/test_path/hive_data_location/data-a2.parquet"),
        Maps.immutableEntry(null, "/test_path/hive_data_location/data-a3.parquet"),
        Maps.immutableEntry(null, "/test_path/hive_data_location/orphan-a1.parquet"),
        Maps.immutableEntry(null, "/test_path/hive_data_location/orphan-a2.parquet"),
        Maps.immutableEntry(null, "/test_path/hive_data_location/orphan-a3.parquet")
    );
    UnkeyedTable table = getArcticTable().asUnkeyedTable();
    table.updateProperties().set(DELETE_UNTRACKED_HIVE_FILE, "true").commit();
    AppendFiles appendFiles = table.newAppend();
    MockDataFileBuilder dataFileBuilder = new MockDataFileBuilder(table, TEST_HMS.getHiveClient());
    List<DataFile> orphanDataFiles = dataFileBuilder.buildList(orphanFiles);
    orphanDataFiles.forEach(appendFiles::appendFile);
    appendFiles.commit();

    List<Map.Entry<String, String>> files = Lists.newArrayList(
        Maps.immutableEntry(null, "/test_path/hive_data_location/data-a1.parquet")
    );
    Set<DataFile> initDataFiles = new HashSet<>(dataFileBuilder.buildList(files));

    List<Map.Entry<String, String>> newFiles = Lists.newArrayList(
        Maps.immutableEntry(null, "/test_path/hive_data_location/data-a1.parquet"),
        Maps.immutableEntry(null, "/test_path/hive_data_location/data-a3.parquet")
    );
    Set<DataFile> newDataFiles = new HashSet<>(dataFileBuilder.buildList(newFiles));

    RewriteFiles rewriteFiles = table.newRewrite();
    rewriteFiles.rewriteFiles(initDataFiles, newDataFiles);
    rewriteFiles.set(DELETE_UNTRACKED_HIVE_FILE, "true");
    rewriteFiles.commit();

    List<String> exceptedFiles = new ArrayList<>();
    exceptedFiles.add("data-a1.parquet");
    exceptedFiles.add("data-a3.parquet");
    Table hiveTable = TEST_HMS.getHiveClient().getTable(table.id().getDatabase(), table.name());
    List<String> fileNameList = table.io().list(hiveTable.getSd().
        getLocation()).stream().map(f -> f.getPath().getName()).sorted().collect(Collectors.toList());
    Assert.assertEquals(exceptedFiles, fileNameList);
  }

  @Test
  public void testCleanUnPartitionOrphanFileWhenRewritePartition() throws TException {
    List<Map.Entry<String, String>> orphanFiles = Lists.newArrayList(
        Maps.immutableEntry(null, "/test_path/hive_data_location/orphan-a1.parquet"),
        Maps.immutableEntry(null, "/test_path/hive_data_location/orphan-a2.parquet"),
        Maps.immutableEntry(null, "/test_path/hive_data_location/orphan-a3.parquet")
    );
    UnkeyedTable table = getArcticTable().asUnkeyedTable();
    AppendFiles appendFiles = table.newAppend();
    MockDataFileBuilder dataFileBuilder = new MockDataFileBuilder(table, TEST_HMS.getHiveClient());
    List<DataFile> orphanDataFiles = dataFileBuilder.buildList(orphanFiles);
    orphanDataFiles.forEach(appendFiles::appendFile);
    appendFiles.commit();

    List<Map.Entry<String, String>> files = Lists.newArrayList(
        Maps.immutableEntry(null, "/test_path/hive_data_location/data-a1.parquet"),
        Maps.immutableEntry(null, "/test_path/hive_data_location/data-a2.parquet"),
        Maps.immutableEntry(null, "/test_path/hive_data_location/data-a3.parquet")
    );
    List<DataFile> dataFiles = dataFileBuilder.buildList(files);

    ReplacePartitions replacePartitions = table.newReplacePartitions();
    dataFiles.forEach(replacePartitions::addFile);
    replacePartitions.commit();

    List<String> exceptedFiles = new ArrayList<>();
    exceptedFiles.add("data-a1.parquet");
    exceptedFiles.add("data-a2.parquet");
    exceptedFiles.add("data-a3.parquet");
    Table hiveTable = TEST_HMS.getHiveClient().getTable(table.id().getDatabase(), table.name());
    List<String> fileNameList = table.io().list(hiveTable.getSd().
        getLocation()).stream().map(f -> f.getPath().getName()).sorted().collect(Collectors.toList());
    Assert.assertEquals(exceptedFiles, fileNameList);
  }
}
