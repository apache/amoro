package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.api.DataFileInfo;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.service.impl.FileInfoCacheService;
import com.netease.arctic.ams.server.service.impl.OrphanFilesCleanService;
import com.netease.arctic.ams.server.utils.JDBCSqlSessionFactoryProvider;
import org.apache.iceberg.io.OutputFile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static com.netease.arctic.ams.server.service.impl.OrphanFilesCleanService.DATA_FOLDER_NAME;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({
    ServiceContainer.class,
    JDBCSqlSessionFactoryProvider.class
})
@PowerMockIgnore({"org.apache.logging.log4j.*", "javax.management.*", "org.apache.http.conn.ssl.*",
    "com.amazonaws.http.conn.ssl.*",
    "javax.net.ssl.*", "org.apache.hadoop.*", "javax.*", "com.sun.org.apache.*", "org.apache.xerces.*"})
public class TestOrphanFileCleanSupportNative extends TestNativeIcebergBase {
  @Before
  public void mock() {
    mockStatic(JDBCSqlSessionFactoryProvider.class);
    mockStatic(ServiceContainer.class);
    when(JDBCSqlSessionFactoryProvider.get()).thenReturn(null);
    TestOrphanFileCleanSupportNative.FakeFileInfoCacheService fakeFileInfoCacheService =
        new TestOrphanFileCleanSupportNative.FakeFileInfoCacheService();
    when(ServiceContainer.getFileInfoCacheService()).thenReturn(fakeFileInfoCacheService);
  }

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

  private static class FakeFileInfoCacheService extends FileInfoCacheService {

    public FakeFileInfoCacheService() {
      super();
    }

    @Override
    public List<DataFileInfo> getOptimizeDatafiles(TableIdentifier tableIdentifier, String tableType) {
      return Collections.emptyList();
    }
  }
}
