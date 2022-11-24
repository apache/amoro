package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import org.apache.iceberg.DataFile;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

public class TestIcebergMinorOptimizePlan extends TestIcebergBase {
  @Test
  public void testMinorOptimize() throws Exception {
    icebergTable.asUnkeyedTable().updateProperties()
        .set(com.netease.arctic.table.TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD, "1000")
        .commit();
    List<DataFile> dataFiles = insertDataFiles(icebergTable.asUnkeyedTable(), 10);
    insertEqDeleteFiles(icebergTable.asUnkeyedTable(), 5);
    insertPosDeleteFiles(icebergTable.asUnkeyedTable(), dataFiles);
    IcebergMinorOptimizePlan optimizePlan = new IcebergMinorOptimizePlan(icebergTable,
        new TableOptimizeRuntime(icebergTable.id()),
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = optimizePlan.plan();
    Assert.assertEquals(2, tasks.size());
  }
}
