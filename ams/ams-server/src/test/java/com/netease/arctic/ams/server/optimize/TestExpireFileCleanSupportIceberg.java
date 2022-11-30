package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.server.service.impl.TableExpireService;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;

public class TestExpireFileCleanSupportIceberg extends TestIcebergBase {
  @Test
  public void testExpireTableFiles() throws Exception {
    List<DataFile> dataFiles = insertDataFiles(icebergNoPartitionTable.asUnkeyedTable(), 1);

    DeleteFiles deleteFiles = icebergNoPartitionTable.asUnkeyedTable().newDelete();
    for (DataFile dataFile : dataFiles) {
      Assert.assertTrue(icebergNoPartitionTable.io().exists(dataFile.path().toString()));
      deleteFiles.deleteFile(dataFile);
    }
    deleteFiles.commit();

    List<DataFile> newDataFiles = insertDataFiles(icebergNoPartitionTable.asUnkeyedTable(), 1);
    TableExpireService.expireSnapshots(icebergNoPartitionTable.asUnkeyedTable(), System.currentTimeMillis(), new HashSet<>());
    Assert.assertEquals(1, Iterables.size(icebergNoPartitionTable.asUnkeyedTable().snapshots()));

    for (DataFile dataFile : dataFiles) {
      Assert.assertFalse(icebergNoPartitionTable.io().exists(dataFile.path().toString()));
    }
    for (DataFile dataFile : newDataFiles) {
      Assert.assertTrue(icebergNoPartitionTable.io().exists(dataFile.path().toString()));
    }
  }
}
