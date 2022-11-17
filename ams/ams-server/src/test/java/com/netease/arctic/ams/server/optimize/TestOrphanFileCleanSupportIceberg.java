package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.server.service.impl.OrphanFilesCleanService;
import org.apache.iceberg.io.OutputFile;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static com.netease.arctic.ams.server.service.impl.OrphanFilesCleanService.DATA_FOLDER_NAME;

public class TestOrphanFileCleanSupportIceberg extends TestIcebergBase {
  @Test
  public void orphanDataFileClean() {
    String orphanFilePath = icebergTable.asUnkeyedTable().location() +
        File.separator + DATA_FOLDER_NAME + File.separator + "orphan.parquet";
    OutputFile baseOrphanDataFile = icebergTable.io().newOutputFile(orphanFilePath);
    baseOrphanDataFile.createOrOverwrite();
    Assert.assertTrue(icebergTable.io().exists(orphanFilePath));
    OrphanFilesCleanService.clean(icebergTable, System.currentTimeMillis(), true, "all", false);
    Assert.assertFalse(icebergTable.io().exists(orphanFilePath));
  }

  @Test
  public void orphanMetadataFileClean() {
    String orphanFilePath = icebergTable.asUnkeyedTable().location() + File.separator + "metadata" +
        File.separator + "orphan.avro";
    OutputFile baseOrphanDataFile = icebergTable.io().newOutputFile(orphanFilePath);
    baseOrphanDataFile.createOrOverwrite();
    Assert.assertTrue(icebergTable.io().exists(orphanFilePath));
    OrphanFilesCleanService.clean(icebergTable, System.currentTimeMillis(), true, "all", true);
    Assert.assertFalse(icebergTable.io().exists(orphanFilePath));
  }
}
