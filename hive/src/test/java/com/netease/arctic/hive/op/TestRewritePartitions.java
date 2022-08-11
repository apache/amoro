package com.netease.arctic.hive.op;

import com.netease.arctic.hive.HiveTableTestBase;
import com.netease.arctic.op.RewritePartitions;
import java.util.List;
import java.util.Optional;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

public class TestRewritePartitions extends HiveTableTestBase {

  @Test
  public void testRewritePartitionTable() throws TException {
    // ====================== init table partition by overwrite =====================
    RewritePartitions rewritePartitions = testKeyedHiveTable.newRewritePartitions();

    rewritePartitions.addDataFile(
        DataFiles.builder(testKeyedHiveTable.spec())
            .withPath(temp.getRoot().getAbsolutePath() + "/test_path/partition1/data-a1.parquet")
            .withFileSizeInBytes(0)
            .withPartitionPath("name=aaa")
            .withRecordCount(2)
            .build()
    );
    rewritePartitions.addDataFile(
        DataFiles.builder(testKeyedHiveTable.spec())
            .withPath(temp.getRoot().getAbsolutePath() + "/test_path/partition1/data-a2.parquet")
            .withFileSizeInBytes(0)
            .withPartitionPath("name=aaa")
            .withRecordCount(2)
            .build()
    );
    rewritePartitions.addDataFile(
        DataFiles.builder(testKeyedHiveTable.spec())
            .withPath(temp.getRoot().getAbsolutePath() + "/test_path/partition2/data-a.parquet")
            .withFileSizeInBytes(0)
            .withPartitionPath("name=bbb")
            .withRecordCount(2)
            .build()
    );
    rewritePartitions.withTransactionId(testKeyedHiveTable.beginTransaction(""));
    rewritePartitions.commit();

    List<Partition> partitions = metastoreClient.listPartitions(testKeyedHiveTable.id().getDatabase(),
        testKeyedHiveTable.id().getTableName(),
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
    rewritePartitions = testKeyedHiveTable.newRewritePartitions();

    rewritePartitions.addDataFile(
        DataFiles.builder(testKeyedHiveTable.spec())
            .withPath(temp.getRoot().getAbsolutePath() + "/test_path/partition3/data-a3.parquet")
            .withFileSizeInBytes(0)
            .withPartitionPath("name=aaa")
            .withRecordCount(2)
            .build()
    );
    rewritePartitions.addDataFile(
        DataFiles.builder(testKeyedHiveTable.spec())
            .withPath(temp.getRoot().getAbsolutePath() + "/test_path/partition4/data-c.parquet")
            .withFileSizeInBytes(0)
            .withPartitionPath("name=ccc")
            .withRecordCount(2)
            .build()
    );
    rewritePartitions.withTransactionId(testKeyedHiveTable.beginTransaction(""));
    rewritePartitions.commit();



    partitions = metastoreClient.listPartitions(testKeyedHiveTable.id().getDatabase(),
        testKeyedHiveTable.id().getTableName(),
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
