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

import com.netease.arctic.hive.HiveTableTestBase;
import com.netease.arctic.op.RewritePartitions;
import com.netease.arctic.table.TableIdentifier;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

public class TestRewritePartitions extends HiveTableTestBase {

  @Test
  public void testUnKeyedTableRewritePartitions() throws TException {
    testReplacePartition(files -> {
      ReplacePartitions replacePartitions = testHiveTable.newReplacePartitions();
      files.forEach(replacePartitions::addFile);
      replacePartitions.commit();
    }, testHiveTable.id());
  }


  @Test
  public void testKeyedTableRewritePartitions() throws Exception {
    testReplacePartition(files -> {
      RewritePartitions rewritePartitions = testKeyedHiveTable.newRewritePartitions();
      rewritePartitions.withTransactionId(testKeyedHiveTable.beginTransaction(""));
      files.forEach(rewritePartitions::addDataFile);
      rewritePartitions.commit();
    }, testKeyedHiveTable.id());
  }

  private void testReplacePartition( Consumer<List<DataFile>>  rewriteConsumer, TableIdentifier identifier) throws TException {
    // ====================== init table partition by overwrite =====================
    final String database = identifier.getDatabase();
    final String tableName = identifier.getTableName();
    Table hiveTable = hms.getClient().getTable(identifier.getDatabase(), identifier.getTableName());

    List<DataFile> initDataFiles = Lists.newArrayList(
        DataFiles.builder(testKeyedHiveTable.spec())
            .withPath(hiveTable.getSd().getLocation() + "/test_path/partition1/data-a1.parquet")
            .withFileSizeInBytes(0)
            .withPartitionPath("name=aaa")
            .withRecordCount(2)
            .build(),
        DataFiles.builder(testKeyedHiveTable.spec())
            .withPath(hiveTable.getSd().getLocation() + "/test_path/partition1/data-a2.parquet")
            .withFileSizeInBytes(0)
            .withPartitionPath("name=aaa")
            .withRecordCount(2)
            .build(),
        DataFiles.builder(testKeyedHiveTable.spec())
            .withPath(hiveTable.getSd().getLocation() + "/test_path/partition2/data-a.parquet")
            .withFileSizeInBytes(0)
            .withPartitionPath("name=bbb")
            .withRecordCount(2)
            .build()
    );
    rewriteConsumer.accept(initDataFiles);


    List<Partition> partitions = hms.getClient().listPartitions(database,
        tableName,
        (short) -1);

    Assert.assertEquals("expect 2 partition after first rewrite partition",
        2, partitions.size());
    Optional<Partition> partition = partitions.stream().filter(
        p -> p.getValues().size() == 1 && "aaa".equals(p.getValues().get(0))
    ).findAny();
    Assert.assertTrue("expect partition name=aaa exists", partition.isPresent());
    Assert.assertTrue(partition.get().getSd().getLocation().contains("partition1"));

    partition = partitions.stream().filter(
        p -> p.getValues().size() == 1 && "bbb".equals(p.getValues().get(0))
    ).findAny();
    Assert.assertTrue("expect partition name=bbb exists", partition.isPresent());
    Assert.assertTrue(partition.get().getSd().getLocation().contains("partition2"));

    // =================== overwrite table ========================
    List<DataFile> overwriteFiles = Lists.newArrayList(
        DataFiles.builder(testKeyedHiveTable.spec())
            .withPath(hiveTable.getSd().getLocation() + "/test_path/partition3/data-a3.parquet")
            .withFileSizeInBytes(0)
            .withPartitionPath("name=aaa")
            .withRecordCount(2)
            .build(),
        DataFiles.builder(testKeyedHiveTable.spec())
            .withPath(hiveTable.getSd().getLocation() + "/test_path/partition4/data-c.parquet")
            .withFileSizeInBytes(0)
            .withPartitionPath("name=ccc")
            .withRecordCount(2)
            .build()
    );
    rewriteConsumer.accept(overwriteFiles);

    partitions = hms
        .getClient().listPartitions(database,
        tableName,
        (short) -1);

    Assert.assertEquals("expect 3 partition after second rewrite partition",
        3, partitions.size());
    partition = partitions.stream().filter(
        p -> p.getValues().size() == 1 && "aaa".equals(p.getValues().get(0))
    ).findAny();
    Assert.assertTrue("expect partition name=aaa exists", partition.isPresent());
    Assert.assertTrue(partition.get().getSd().getLocation().contains("partition3"));

    partition = partitions.stream().filter(
        p -> p.getValues().size() == 1 && "bbb".equals(p.getValues().get(0))
    ).findAny();
    Assert.assertTrue("expect partition name=bbb exists", partition.isPresent());
    Assert.assertTrue(partition.get().getSd().getLocation().contains("partition2"));

    partition = partitions.stream().filter(
        p -> p.getValues().size() == 1 && "ccc".equals(p.getValues().get(0))
    ).findAny();
    Assert.assertTrue("expect partition name=bbb exists", partition.isPresent());
    Assert.assertTrue(partition.get().getSd().getLocation().contains("partition4"));
  }
}
