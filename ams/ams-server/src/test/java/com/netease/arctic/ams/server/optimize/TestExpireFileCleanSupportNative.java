package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.server.service.impl.TableExpireService;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;

public class TestExpireFileCleanSupportNative extends TestNativeIcebergBase {
  @Test
  public void testExpireTableFiles() throws Exception {
    List<DataFile> dataFiles = insertDataFiles(icebergTable.asUnkeyedTable());

    DeleteFiles deleteFiles = icebergTable.asUnkeyedTable().newDelete();
    for (DataFile dataFile : dataFiles) {
      Assert.assertTrue(icebergTable.io().exists(dataFile.path().toString()));
      deleteFiles.deleteFile(dataFile);
    }
    deleteFiles.commit();

    List<DataFile> newDataFiles = insertDataFiles(icebergTable.asUnkeyedTable());
    TableExpireService.expireSnapshots(icebergTable.asUnkeyedTable(), System.currentTimeMillis(), new HashSet<>());
    Assert.assertEquals(1, Iterables.size(icebergTable.asUnkeyedTable().snapshots()));

    for (DataFile dataFile : dataFiles) {
      Assert.assertFalse(icebergTable.io().exists(dataFile.path().toString()));
    }
    for (DataFile dataFile : newDataFiles) {
      Assert.assertTrue(icebergTable.io().exists(dataFile.path().toString()));
    }
  }
}
