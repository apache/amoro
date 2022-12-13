package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

public class TestIcebergMinorOptimizePlan extends TestIcebergBase {
  @Test
  public void testUnPartitionMinorOptimize() throws Exception {
    icebergNoPartitionTable.asUnkeyedTable().updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO,
            TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT / 1000 + "")
        .commit();
    List<DataFile> dataFiles = insertDataFiles(icebergNoPartitionTable.asUnkeyedTable(), 10);
    insertEqDeleteFiles(icebergNoPartitionTable.asUnkeyedTable(), 5);
    insertPosDeleteFiles(icebergNoPartitionTable.asUnkeyedTable(), dataFiles);
    List<FileScanTask> fileScanTasks;
    try (CloseableIterable<FileScanTask> filesIterable = icebergNoPartitionTable.asUnkeyedTable().newScan()
        .planFiles()) {
      fileScanTasks = Lists.newArrayList(filesIterable);
    }
    IcebergMinorOptimizePlan optimizePlan = new IcebergMinorOptimizePlan(icebergNoPartitionTable,
        new TableOptimizeRuntime(icebergNoPartitionTable.id()),
        fileScanTasks,
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = optimizePlan.plan();
    Assert.assertEquals(2, tasks.size());
  }

  @Test
  public void testPartitionMinorOptimize() throws Exception {
    icebergPartitionTable.asUnkeyedTable().updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO,
            TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT / 1000 + "")
        .commit();
    List<DataFile> dataFiles = insertDataFiles(icebergPartitionTable.asUnkeyedTable(), 10);
    insertEqDeleteFiles(icebergPartitionTable.asUnkeyedTable(), 5);
    insertPosDeleteFiles(icebergPartitionTable.asUnkeyedTable(), dataFiles);
    List<FileScanTask> fileScanTasks;
    try (CloseableIterable<FileScanTask> filesIterable = icebergPartitionTable.asUnkeyedTable().newScan().planFiles()) {
      fileScanTasks = Lists.newArrayList(filesIterable);
    }
    IcebergMinorOptimizePlan optimizePlan = new IcebergMinorOptimizePlan(icebergPartitionTable,
        new TableOptimizeRuntime(icebergPartitionTable.id()),
        fileScanTasks,
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = optimizePlan.plan();
    Assert.assertEquals(2, tasks.size());
  }
}
