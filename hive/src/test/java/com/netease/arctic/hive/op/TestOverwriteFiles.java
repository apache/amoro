package com.netease.arctic.hive.op;

import com.netease.arctic.hive.HiveTableTestBase;
import com.netease.arctic.hive.table.UnkeyedHiveTable;
import com.netease.arctic.op.OverwriteBaseFiles;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.FileUtil;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

public class TestOverwriteFiles extends HiveTableTestBase {

  @Test
  public void testOverwriteUnkeyedPartitionTable() throws TException {
    UnkeyedTable table = testHiveTable;
    Map<String, String> partitionAndLocations = Maps.newHashMap();

    List<Map.Entry<String, String>> files = Lists.newArrayList(
        Maps.immutableEntry("name=aaa", "/test_path/partition1/data-a1.parquet"),
        Maps.immutableEntry("name=aaa", "/test_path/partition1/data-a2.parquet"),
        Maps.immutableEntry("name=bbb", "/test_path/partition2/data-a2.parquet")
    );
    DataFileBuilder dataFileBuilder = new DataFileBuilder(table);
    List<DataFile> dataFiles = dataFileBuilder.buildList(files);

    OverwriteFiles overwriteFiles = table.newOverwrite();
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();

    applyOverwrite(partitionAndLocations, s -> false, files);
    assertHivePartitionLocations(partitionAndLocations, table);

    // ================== test overwrite all partition
    files = Lists.newArrayList(
        Maps.immutableEntry("name=aaa", "/test_path/partition3/data-a3.parquet"),
        Maps.immutableEntry("name=ccc", "/test_path/partition4/data-c.parquet")
    );
    dataFiles = dataFileBuilder.buildList(files);
    overwriteFiles = table.newOverwrite();
    overwriteFiles.overwriteByRowFilter(Expressions.alwaysTrue());
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();

    partitionAndLocations.clear();
    applyOverwrite(partitionAndLocations, s -> false, files);
    assertHivePartitionLocations(partitionAndLocations, table);
  }


  @Test
  public void testOverwriteKeyedPartitionTable() throws TException {
    KeyedTable table = testKeyedHiveTable;
    Map<String, String> partitionAndLocations = Maps.newHashMap();

    List<Map.Entry<String, String>> files = Lists.newArrayList(
        Maps.immutableEntry("name=aaa", "/test_path/partition1/data-a1.parquet"),
        Maps.immutableEntry("name=aaa", "/test_path/partition1/data-a2.parquet"),
        Maps.immutableEntry("name=bbb", "/test_path/partition2/data-a2.parquet")
    );
    DataFileBuilder dataFileBuilder = new DataFileBuilder(table);
    List<DataFile> dataFiles = dataFileBuilder.buildList(files);

    OverwriteBaseFiles overwriteBaseFiles = table.newOverwriteBaseFiles();
    dataFiles.forEach(overwriteBaseFiles::addFile);
    overwriteBaseFiles.withTransactionId(table.beginTransaction(""));
    overwriteBaseFiles.commit();

    applyOverwrite(partitionAndLocations, s -> false, files);
    assertHivePartitionLocations(partitionAndLocations, table);

    // ================== test overwrite all partition
    files = Lists.newArrayList(
        Maps.immutableEntry("name=aaa", "/test_path/partition3/data-a3.parquet"),
        Maps.immutableEntry("name=ccc", "/test_path/partition4/data-c.parquet")
    );
    dataFiles = dataFileBuilder.buildList(files);

    overwriteBaseFiles = table.newOverwriteBaseFiles();
    dataFiles.forEach(overwriteBaseFiles::addFile);
    overwriteBaseFiles.overwriteByRowFilter(Expressions.alwaysTrue());
    overwriteBaseFiles.withTransactionId(table.beginTransaction(""));
    overwriteBaseFiles.commit();

    partitionAndLocations.clear();
    applyOverwrite(partitionAndLocations, s -> false, files);
    assertHivePartitionLocations(partitionAndLocations, table);
  }


  @Test
  public void testOverwriteOperationTransaction() throws TException {
    UnkeyedTable table = testHiveTable;
    Map<String, String> partitionAndLocations = Maps.newHashMap();

    List<Map.Entry<String, String>> files = Lists.newArrayList(
        Maps.immutableEntry("name=aaa", "/test_path/partition1/data-a1.parquet"),
        Maps.immutableEntry("name=aaa", "/test_path/partition1/data-a2.parquet"),
        Maps.immutableEntry("name=bbb", "/test_path/partition2/data-a2.parquet")
    );
    DataFileBuilder dataFileBuilder = new DataFileBuilder(table);
    List<DataFile> dataFiles = dataFileBuilder.buildList(files);

    Transaction tx = table.newTransaction();

    OverwriteFiles overwriteFiles = tx.newOverwrite();
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();

    String key = "test-overwrite-transaction";
    UpdateProperties updateProperties = tx.updateProperties();
    updateProperties.set(key, "true");
    updateProperties.commit();

    table.refresh();
    Assert.assertFalse("table properties should not update", table.properties().containsKey(key));
    assertHivePartitionLocations(partitionAndLocations, table);

    tx.commitTransaction();
    table.refresh();

    Assert.assertTrue("table properties should update", table.properties().containsKey(key));
    Assert.assertEquals("true", table.properties().get(key));

    applyOverwrite(partitionAndLocations, s -> false, files);
    assertHivePartitionLocations(partitionAndLocations, table);
  }

  @Test
  public void testNonPartitionTable() throws TException {
    TableIdentifier identifier = TableIdentifier.of(HIVE_CATALOG_NAME, HIVE_DB_NAME, "test_un_partition");
    UnkeyedHiveTable table = (UnkeyedHiveTable) hiveCatalog.newTableBuilder(identifier, TABLE_SCHEMA)
        .create();

    Map<String, String> partitionAndLocations = Maps.newHashMap();

    List<Map.Entry<String, String>> files = Lists.newArrayList(
        Maps.immutableEntry(null, "/test_path/hive_data_location/data-a1.parquet"),
        Maps.immutableEntry(null, "/test_path/hive_data_location/data-a2.parquet"),
        Maps.immutableEntry(null, "/test_path/hive_data_location/data-a2.parquet")
    );
    DataFileBuilder dataFileBuilder = new DataFileBuilder(table);
    List<DataFile> dataFiles = dataFileBuilder.buildList(files);

    OverwriteFiles overwriteFiles = table.newOverwrite();
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();

    // assert no hive partition
    assertHivePartitionLocations(partitionAndLocations, table);
    Table hiveTable = hms.getClient().getTable(table.id().getDatabase(), table.name());
    Assert.assertTrue("table location to new path",
        hiveTable.getSd().getLocation().endsWith("/test_path/hive_data_location"));

    // =================== test delete all files and add no file to un-partitioned table ===================
    overwriteFiles = table.newOverwrite();
    overwriteFiles.overwriteByRowFilter(Expressions.alwaysTrue());
    overwriteFiles.commit();

    assertHivePartitionLocations(partitionAndLocations, table);
    hiveTable = hms.getClient().getTable(table.id().getDatabase(), table.name());
    Assert.assertFalse("table location to new path",
        hiveTable.getSd().getLocation().endsWith("/test_path/hive_data_location"));

    String hiveLocation = hiveTable.getSd().getLocation();
    Assert.assertTrue("table location change to hive location",
        hiveLocation.startsWith(table.hiveLocation()));
    System.out.println(hiveLocation);

    hiveCatalog.dropTable(identifier, true);
    AMS.handler().getTableCommitMetas().remove(identifier.buildTableIdentifier());
  }

  @Test
  public void testDeleteByExpr() throws TException {
    UnkeyedTable table = testHiveTable;
    Map<String, String> partitionAndLocations = Maps.newHashMap();

    List<Map.Entry<String, String>> files = Lists.newArrayList(
        Maps.immutableEntry("name=aaa", "/test_path/partition1/data-a1.parquet"),
        Maps.immutableEntry("name=aaa", "/test_path/partition1/data-a2.parquet"),
        Maps.immutableEntry("name=bbb", "/test_path/partition2/data-a2.parquet")
    );
    DataFileBuilder dataFileBuilder = new DataFileBuilder(table);
    List<DataFile> dataFiles = dataFileBuilder.buildList(files);

    OverwriteFiles overwriteFiles = table.newOverwrite();
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();

    applyOverwrite(partitionAndLocations, s -> false, files);
    assertHivePartitionLocations(partitionAndLocations, table);

    // ================== test overwrite all partition
    files = Lists.newArrayList(
        Maps.immutableEntry("name=aaa", "/test_path/partition3/data-a3.parquet"),
        Maps.immutableEntry("name=ccc", "/test_path/partition4/data-c.parquet")
    );
    dataFiles = dataFileBuilder.buildList(files);
    overwriteFiles = table.newOverwrite();
    overwriteFiles.overwriteByRowFilter(Expressions.equal("name", "aaa"));
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();

    applyOverwrite(partitionAndLocations, "name=aaa"::equalsIgnoreCase, files);
    assertHivePartitionLocations(partitionAndLocations, table);
  }


  private void applyOverwrite(
      Map<String, String> partitionAndLocations,
      Predicate<String> deleteFunc,
      List<Map.Entry<String, String>> addFiles){
    Set<String> deleteLocations = partitionAndLocations.keySet()
        .stream().filter(deleteFunc).collect(Collectors.toSet());

    deleteLocations.forEach(partitionAndLocations::remove);

    addFiles.forEach(kv -> {
      String partLocation = FileUtil.getFileDir(kv.getValue());
      partitionAndLocations.put(
          kv.getKey(),
          partLocation
      );
    });

  }
}
