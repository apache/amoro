package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import org.apache.commons.collections.IteratorUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.TableProperties;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

public class TestIcebergMajorOptimizePlan extends TestIcebergBase {
  @Test
  public void testNoPartitionMajorOptimize() throws Exception {
    icebergNoPartitionTable.asUnkeyedTable().updateProperties()
        .set(com.netease.arctic.table.TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD, "1000")
        .set(com.netease.arctic.table.TableProperties.MAJOR_OPTIMIZE_TRIGGER_DUPLICATE_SIZE_BYTES_THRESHOLD, "10")
        .commit();
    List<DataFile> dataFiles = insertDataFiles(icebergNoPartitionTable.asUnkeyedTable(), 10);
    insertEqDeleteFiles(icebergNoPartitionTable.asUnkeyedTable(), 5);
    insertPosDeleteFiles(icebergNoPartitionTable.asUnkeyedTable(), dataFiles);
    IcebergMajorOptimizePlan optimizePlan = new IcebergMajorOptimizePlan(icebergNoPartitionTable,
        new TableOptimizeRuntime(icebergNoPartitionTable.id()),
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = optimizePlan.plan();
    Assert.assertEquals(1, tasks.size());
  }

  @Test
  public void testPartitionMajorOptimize() throws Exception {
    icebergPartitionTable.asUnkeyedTable().updateProperties()
        .set(com.netease.arctic.table.TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD, "1000")
        .set(com.netease.arctic.table.TableProperties.MAJOR_OPTIMIZE_TRIGGER_DUPLICATE_SIZE_BYTES_THRESHOLD, "10")
        .commit();
    List<DataFile> dataFiles = insertDataFiles(icebergPartitionTable.asUnkeyedTable(), 10);
    insertEqDeleteFiles(icebergPartitionTable.asUnkeyedTable(), 5);
    insertPosDeleteFiles(icebergPartitionTable.asUnkeyedTable(), dataFiles);
    IcebergMajorOptimizePlan optimizePlan = new IcebergMajorOptimizePlan(icebergPartitionTable,
        new TableOptimizeRuntime(icebergPartitionTable.id()),
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = optimizePlan.plan();
    Assert.assertEquals(1, tasks.size());
  }

  @Test
  public void testBinPackPlan() throws Exception {
    icebergNoPartitionTable.asUnkeyedTable().updateProperties()
        .set(com.netease.arctic.table.TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD, "1000")
        .set(com.netease.arctic.table.TableProperties.MAJOR_OPTIMIZE_TRIGGER_DUPLICATE_SIZE_BYTES_THRESHOLD, "10")
        .set(TableProperties.WRITE_TARGET_FILE_SIZE_BYTES, "1500")
        .commit();
    List<DataFile> dataFiles = insertDataFiles(icebergNoPartitionTable.asUnkeyedTable(), 10);
    insertEqDeleteFiles(icebergNoPartitionTable.asUnkeyedTable(), 5);
    insertPosDeleteFiles(icebergNoPartitionTable.asUnkeyedTable(), dataFiles);
    IcebergMajorOptimizePlan optimizePlan = new IcebergMajorOptimizePlan(icebergNoPartitionTable,
        new TableOptimizeRuntime(icebergNoPartitionTable.id()),
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = optimizePlan.plan();
    Assert.assertEquals(50, tasks.size());
  }
}
