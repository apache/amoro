package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import org.apache.commons.collections.IteratorUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileScanTask;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

public class TestIcebergMajorOptimizePlan extends TestIcebergBase {
  @Test
  public void testMajorOptimize() throws Exception {
    icebergTable.asUnkeyedTable().updateProperties()
        .set(com.netease.arctic.table.TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD, "1000")
        .set(com.netease.arctic.table.TableProperties.MAJOR_OPTIMIZE_TRIGGER_DUPLICATE_SIZE_BYTES_THRESHOLD, "10")
        .commit();
    List<DataFile> dataFiles = insertDataFiles(icebergTable.asUnkeyedTable(), 10);
    insertEqDeleteFiles(icebergTable.asUnkeyedTable(), 5);
    insertPosDeleteFiles(icebergTable.asUnkeyedTable(), dataFiles);
    List<FileScanTask> fileScanTasks = IteratorUtils.toList(icebergTable.asUnkeyedTable().newScan().planFiles().iterator());
    IcebergMajorOptimizePlan optimizePlan = new IcebergMajorOptimizePlan(icebergTable,
        new TableOptimizeRuntime(icebergTable.id()), fileScanTasks,
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = optimizePlan.plan();
    Assert.assertEquals(1, tasks.size());
    Assert.assertEquals(dataFiles.size(), tasks.get(0).getIcebergFileScanTasksSize());
  }
}
