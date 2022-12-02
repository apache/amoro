package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.DataFile;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

public class TestIcebergFullOptimizePlan extends TestIcebergBase {
  @Test
  public void testNoPartitionFullOptimize() throws Exception {
    icebergNoPartitionTable.asUnkeyedTable().updateProperties()
        .set(com.netease.arctic.table.TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO,
            com.netease.arctic.table.TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT / 1000 + "")
        .set(com.netease.arctic.table.TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO, "0")
        .commit();
    List<DataFile> dataFiles = insertDataFiles(icebergNoPartitionTable.asUnkeyedTable(), 10);
    insertEqDeleteFiles(icebergNoPartitionTable.asUnkeyedTable(), 5);
    insertPosDeleteFiles(icebergNoPartitionTable.asUnkeyedTable(), dataFiles);
    IcebergFullOptimizePlan optimizePlan = new IcebergFullOptimizePlan(icebergNoPartitionTable,
        new TableOptimizeRuntime(icebergNoPartitionTable.id()),
        icebergNoPartitionTable.asUnkeyedTable().newScan().planFiles(),
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = optimizePlan.plan();
    Assert.assertEquals(1, tasks.size());
  }

  @Test
  public void testPartitionFullOptimize() throws Exception {
    icebergPartitionTable.asUnkeyedTable().updateProperties()
        .set(com.netease.arctic.table.TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO,
            com.netease.arctic.table.TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT / 1000 + "")
        .set(com.netease.arctic.table.TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO, "0")
        .commit();
    List<DataFile> dataFiles = insertDataFiles(icebergPartitionTable.asUnkeyedTable(), 10);
    insertEqDeleteFiles(icebergPartitionTable.asUnkeyedTable(), 5);
    insertPosDeleteFiles(icebergPartitionTable.asUnkeyedTable(), dataFiles);
    IcebergFullOptimizePlan optimizePlan = new IcebergFullOptimizePlan(icebergPartitionTable,
        new TableOptimizeRuntime(icebergPartitionTable.id()),
        icebergPartitionTable.asUnkeyedTable().newScan().planFiles(),
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = optimizePlan.plan();
    Assert.assertEquals(1, tasks.size());
  }

  @Test
  public void testBinPackPlan() throws Exception {
    // small file size 1000, target size 3000
    int fragmentRatio = 3;
    icebergNoPartitionTable.asUnkeyedTable().updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO, fragmentRatio + "")
        .set(TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO, "0")
        .set(TableProperties.SELF_OPTIMIZING_TARGET_SIZE, "3000")
        .commit();
    // write 50 data files with size =~ 1000
    List<DataFile> dataFiles = insertDataFiles(icebergNoPartitionTable.asUnkeyedTable(), 10);
    insertEqDeleteFiles(icebergNoPartitionTable.asUnkeyedTable(), 5);
    insertPosDeleteFiles(icebergNoPartitionTable.asUnkeyedTable(), dataFiles);
    IcebergFullOptimizePlan optimizePlan = new IcebergFullOptimizePlan(icebergNoPartitionTable,
        new TableOptimizeRuntime(icebergNoPartitionTable.id()),
        icebergNoPartitionTable.asUnkeyedTable().newScan().planFiles(),
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = optimizePlan.plan();
    Assert.assertEquals((int) Math.ceil(1.0 * dataFiles.size() / fragmentRatio), tasks.size());
  }
}
