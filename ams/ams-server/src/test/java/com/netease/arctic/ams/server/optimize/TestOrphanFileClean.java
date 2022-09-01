/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.TableTestBase;
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
public class TestOrphanFileClean extends TableTestBase {

  @Before
  public void mock() {
    mockStatic(JDBCSqlSessionFactoryProvider.class);
    mockStatic(ServiceContainer.class);
    when(JDBCSqlSessionFactoryProvider.get()).thenReturn(null);
    FakeFileInfoCacheService fakeFileInfoCacheService = new FakeFileInfoCacheService();
    when(ServiceContainer.getFileInfoCacheService()).thenReturn(fakeFileInfoCacheService);
  }

  @Test
  public void orphanDataFileClean() {
    String baseOrphanFilePath = testKeyedTable.baseTable().location() +
        File.separator + DATA_FOLDER_NAME + File.separator + "orphan.parquet";
    String changeOrphanFilePath = testKeyedTable.changeTable().location() +
        File.separator + DATA_FOLDER_NAME + File.separator + "orphan.parquet";
    OutputFile baseOrphanDataFile = testKeyedTable.io().newOutputFile(baseOrphanFilePath);
    baseOrphanDataFile.createOrOverwrite();
    OutputFile changeOrphanDataFile = testKeyedTable.io().newOutputFile(changeOrphanFilePath);
    changeOrphanDataFile.createOrOverwrite();
    Assert.assertTrue(testKeyedTable.io().exists(baseOrphanFilePath));
    Assert.assertTrue(testKeyedTable.io().exists(changeOrphanFilePath));
    OrphanFilesCleanService.clean(testKeyedTable, System.currentTimeMillis(), true, "all", false);
    Assert.assertFalse(testKeyedTable.io().exists(baseOrphanFilePath));
    Assert.assertFalse(testKeyedTable.io().exists(changeOrphanFilePath));
  }

  @Test
  public void orphanMetadataFileClean() {
    String baseOrphanFilePath = testKeyedTable.baseTable().location() + File.separator + "metadata" +
        File.separator + "orphan.avro";
    String changeOrphanFilePath = testKeyedTable.changeTable().location() + File.separator + "metadata" +
        File.separator + "orphan.avro";
    OutputFile baseOrphanDataFile = testKeyedTable.io().newOutputFile(baseOrphanFilePath);
    baseOrphanDataFile.createOrOverwrite();
    OutputFile changeOrphanDataFile = testKeyedTable.io().newOutputFile(changeOrphanFilePath);
    changeOrphanDataFile.createOrOverwrite();
    Assert.assertTrue(testKeyedTable.io().exists(baseOrphanFilePath));
    Assert.assertTrue(testKeyedTable.io().exists(changeOrphanFilePath));
    OrphanFilesCleanService.clean(testKeyedTable, System.currentTimeMillis(), true, "all", true);
    Assert.assertFalse(testKeyedTable.io().exists(baseOrphanFilePath));
    Assert.assertFalse(testKeyedTable.io().exists(changeOrphanFilePath));
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
