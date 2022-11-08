package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.api.properties.OptimizeTaskProperties;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileScanTask;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestNativeMajorOptimizePlan extends TestNativeOptimizeBase {
  @Test
  public void testMajorOptimize() throws Exception {
    icebergTable.asUnkeyedTable().updateProperties()
        .set(com.netease.arctic.table.TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD, "1000")
        .set(com.netease.arctic.table.TableProperties.MAJOR_OPTIMIZE_TRIGGER_DUPLICATE_SIZE_BYTES_THRESHOLD, "100")
        .commit();
    List<DataFile> dataFiles = insertDataFiles(icebergTable.asUnkeyedTable());
    insertEqDeleteFiles(icebergTable.asUnkeyedTable());
    insertPosDeleteFiles(icebergTable.asUnkeyedTable(), dataFiles);
    Map<DataFile, List<DeleteFile>> dataFileListMap = new HashMap<>();
    for (FileScanTask fileScanTask : icebergTable.asUnkeyedTable().newScan().planFiles()) {
      dataFileListMap.put(fileScanTask.file(), fileScanTask.deletes());
    }
    NativeMajorOptimizePlan optimizePlan = new NativeMajorOptimizePlan(icebergTable,
        new TableOptimizeRuntime(icebergTable.id()), dataFileListMap,
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = optimizePlan.plan();
    Assert.assertEquals(1, tasks.size());
    Assert.assertNotNull(tasks.get(0).getProperties().get(OptimizeTaskProperties.EQ_DELETE_FILES_INDEX));
    Assert.assertNotNull(tasks.get(0).getProperties().get(OptimizeTaskProperties.POS_DELETE_FILES_INDEX));
    Assert.assertEquals(dataFiles.size(), tasks.get(0).getBaseFiles().size());
    Assert.assertNotEquals(0, tasks.get(0).getEqDeleteFiles().size());
    Assert.assertNotEquals(0, tasks.get(0).getPosDeleteFiles().size());
  }
}
