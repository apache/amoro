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

package com.netease.arctic.hive.op;

import com.google.common.base.Joiner;
import com.netease.arctic.hive.HiveTableTestBase;
import com.netease.arctic.op.RewritePartitions;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.FileUtil;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

public class TestRewritePartitions extends HiveTableTestBase {

  @Test
  public void testUnKeyedTableRewritePartitions() throws TException {
    UnkeyedTable table = testHiveTable;
    Map<String, String> partitionAndLocations = Maps.newHashMap();

    List<Map.Entry<String, String>> overwriteFiles = Lists.newArrayList(
        Maps.immutableEntry("name=aaa", "/test_path/partition1/data-a1.parquet"),
        Maps.immutableEntry("name=aaa", "/test_path/partition1/data-a2.parquet"),
        Maps.immutableEntry("name=bbb", "/test_path/partition2/data-a2.parquet")
    );
    DataFileBuilder dataFileBuilder = new DataFileBuilder(table);
    List<DataFile> initDataFiles = dataFileBuilder.buildList(overwriteFiles);

    ReplacePartitions replacePartitions = table.newReplacePartitions();
    initDataFiles.forEach(replacePartitions::addFile);
    replacePartitions.commit();

    applyOverwrite(partitionAndLocations, overwriteFiles);
    //assert hive partition equal with expect.
    assertPartitionInfo(partitionAndLocations, table);



    // =================== overwrite table ========================
    overwriteFiles = Lists.newArrayList(
        Maps.immutableEntry("name=aaa", "/test_path/partition3/data-a3.parquet"),
        Maps.immutableEntry("name=ccc", "/test_path/partition4/data-c.parquet")
    );

    List<DataFile> overwriteDataFiles = dataFileBuilder.buildList(overwriteFiles);
    replacePartitions = table.newReplacePartitions();
    overwriteDataFiles.forEach(replacePartitions::addFile);
    replacePartitions.commit();

    applyOverwrite(partitionAndLocations, overwriteFiles);
    //assert hive partition equal with expect.
    assertPartitionInfo(partitionAndLocations, table);
  }

  @Test
  public void testKeyedTableRewritePartitions() throws Exception {
    KeyedTable table = testKeyedHiveTable;
    // ====================== init table partition by overwrite =====================
    Map<String, String> partitionAndLocations = Maps.newHashMap();

    List<Map.Entry<String, String>> overwriteFiles = Lists.newArrayList(
        Maps.immutableEntry("name=aaa", "/test_path/partition1/data-a1.parquet"),
        Maps.immutableEntry("name=aaa", "/test_path/partition1/data-a2.parquet"),
        Maps.immutableEntry("name=bbb", "/test_path/partition2/data-a2.parquet")
    );
    DataFileBuilder dataFileBuilder = new DataFileBuilder(table);
    List<DataFile> initDataFiles = dataFileBuilder.buildList(overwriteFiles);

    RewritePartitions rewritePartitions = table.newRewritePartitions();
    rewritePartitions.withTransactionId(table.beginTransaction(""));
    initDataFiles.forEach(rewritePartitions::addDataFile);
    rewritePartitions.commit();

    applyOverwrite(partitionAndLocations, overwriteFiles);
    //assert hive partition equal with expect.
    assertPartitionInfo(partitionAndLocations, table);


    // =================== overwrite table ========================
    overwriteFiles = Lists.newArrayList(
        Maps.immutableEntry("name=aaa", "/test_path/partition3/data-a3.parquet"),
        Maps.immutableEntry("name=ccc", "/test_path/partition4/data-c.parquet")
    );

    List<DataFile> overwriteDataFiles = dataFileBuilder.buildList(overwriteFiles);
    rewritePartitions = table.newRewritePartitions();
    rewritePartitions.withTransactionId(table.beginTransaction(""));
    overwriteDataFiles.forEach(rewritePartitions::addDataFile);
    rewritePartitions.commit();

    applyOverwrite(partitionAndLocations, overwriteFiles);
    //assert hive partition equal with expect.
    assertPartitionInfo(partitionAndLocations, table);

  }

  @Test
  public void testRewritePartitionInTransaction() throws Exception {
    UnkeyedTable table = testHiveTable;
    Map<String, String> partitionAndLocations = Maps.newHashMap();

    List<Map.Entry<String, String>> overwriteFiles = Lists.newArrayList(
        Maps.immutableEntry("name=aaa", "/test_path/partition1/data-a1.parquet"),
        Maps.immutableEntry("name=aaa", "/test_path/partition1/data-a2.parquet"),
        Maps.immutableEntry("name=bbb", "/test_path/partition2/data-a2.parquet")
    );
    DataFileBuilder dataFileBuilder = new DataFileBuilder(table);
    List<DataFile> initDataFiles = dataFileBuilder.buildList(overwriteFiles);

    Transaction tx = table.newTransaction();
    ReplacePartitions replacePartitions = tx.newReplacePartitions();
    initDataFiles.forEach(replacePartitions::addFile);
    replacePartitions.commit();

    UpdateProperties updateProperties = tx.updateProperties();
    updateProperties.set("test-rewrite-partition", "test-rewrite-partition-value");
    updateProperties.commit();

    table.refresh();

    assertPartitionInfo(partitionAndLocations, table);
    Assert.assertFalse(table.properties().containsKey("test-rewrite-partition"));

    tx.commitTransaction();
    applyOverwrite(partitionAndLocations, overwriteFiles);


    assertPartitionInfo(partitionAndLocations, table);
    Assert.assertTrue(table.properties().containsKey("test-rewrite-partition"));
    Assert.assertEquals("test-rewrite-partition-value", table.properties().get("test-rewrite-partition"));

  }

  private static class DataFileBuilder {
    final TableIdentifier identifier;
    final Table hiveTable;
    final ArcticTable table;

    public DataFileBuilder(ArcticTable table) throws TException {
      identifier = table.id();
      this.table = table;
      hiveTable = hms.getClient().getTable(identifier.getDatabase(), identifier.getTableName());
    }

    public DataFile build(String valuePath, String path) {
      return DataFiles.builder(table.spec())
          .withPath(hiveTable.getSd().getLocation() + path)
          .withFileSizeInBytes(0)
          .withPartitionPath(valuePath)
          .withRecordCount(2)
          .build();
    }



    public List<DataFile> buildList(List<Map.Entry<String, String>> partValueFiles){
      return partValueFiles.stream().map(
          kv -> this.build(kv.getKey(), kv.getValue())
      ).collect(Collectors.toList());
    }
  }

  private String partitionPath(List<String> values, PartitionSpec spec) {
    List<String> nameValues = Lists.newArrayList();
    for (int i = 0; i < values.size(); i++) {
      String field = spec.fields().get(i).name();
      String value = values.get(i);
      nameValues.add(field + "=" + value);
    }
    return Joiner.on("/").join(nameValues);
  }


  private void applyOverwrite(Map<String, String> partitionLocations,
      List<Map.Entry<String, String>> overwriteFiles){
    overwriteFiles.forEach(kv -> {
      String partLocation = FileUtil.getFileDir(kv.getValue());
      partitionLocations.put(kv.getKey(), partLocation);
    });
  }

  private void assertPartitionInfo(Map<String, String> partitionLocations, ArcticTable table) throws TException {
    TableIdentifier identifier = table.id();
    final String database = identifier.getDatabase();
    final String tableName = identifier.getTableName();

    List<Partition> partitions = hms.getClient().listPartitions(
        database,
        tableName,
        (short) -1);
    Assert.assertEquals("expect " + partitionLocations.size() + " partition after first rewrite partition",
        partitionLocations.size(), partitions.size());

    for (Partition p : partitions) {
      String valuePath = partitionPath(p.getValues(), table.spec());
      Assert.assertTrue("partition " + valuePath + " is not expected",
          partitionLocations.containsKey(valuePath));

      String locationExpect = partitionLocations.get(valuePath);
      String actualLocation = p.getSd().getLocation();
      Assert.assertTrue(
          "partition location is not expected, expect " + actualLocation + " end-with " + locationExpect,
          actualLocation.contains(locationExpect));
    }
  }

}
