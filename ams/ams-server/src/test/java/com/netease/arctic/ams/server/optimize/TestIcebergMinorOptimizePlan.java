package com.netease.arctic.ams.server.optimize;

import com.google.common.collect.Maps;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.catalog.Catalog;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

public class TestIcebergMinorOptimizePlan extends TestIcebergBase {
  @Test
  public void testUnPartitionMinorOptimize() throws Exception {
    icebergNoPartitionTable.asUnkeyedTable().updateProperties()
        .set(com.netease.arctic.table.TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD, "1000")
        .commit();
    List<DataFile> dataFiles = insertDataFiles(icebergNoPartitionTable.asUnkeyedTable(), 10);
    insertEqDeleteFiles(icebergNoPartitionTable.asUnkeyedTable(), 5);
    insertPosDeleteFiles(icebergNoPartitionTable.asUnkeyedTable(), dataFiles);
    IcebergMinorOptimizePlan optimizePlan = new IcebergMinorOptimizePlan(icebergNoPartitionTable,
        new TableOptimizeRuntime(icebergNoPartitionTable.id()),
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = optimizePlan.plan();
    Assert.assertEquals(2, tasks.size());
  }

  @Test
  public void testPartitionMinorOptimize() throws Exception {
    icebergPartitionTable.asUnkeyedTable().updateProperties()
        .set(com.netease.arctic.table.TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD, "1000")
        .commit();
    List<DataFile> dataFiles = insertDataFiles(icebergPartitionTable.asUnkeyedTable(), 10);
    insertEqDeleteFiles(icebergPartitionTable.asUnkeyedTable(), 5);
    insertPosDeleteFiles(icebergPartitionTable.asUnkeyedTable(), dataFiles);
    IcebergFullOptimizePlan optimizePlan = new IcebergFullOptimizePlan(icebergPartitionTable,
        new TableOptimizeRuntime(icebergPartitionTable.id()),
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = optimizePlan.plan();
    Assert.assertEquals(2, tasks.size());
  }
}
