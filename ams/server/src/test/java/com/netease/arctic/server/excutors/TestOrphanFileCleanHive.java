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

package com.netease.arctic.server.excutors;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.hive.catalog.HiveCatalogTestHelper;
import com.netease.arctic.hive.catalog.HiveTableTestHelper;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.server.dashboard.utils.AmsUtil;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.executor.OrphanFilesCleaningExecutor;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.io.OutputFile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

import static com.netease.arctic.server.table.executor.OrphanFilesCleaningExecutor.DATA_FOLDER_NAME;

@RunWith(Parameterized.class)
public class TestOrphanFileCleanHive extends TableTestBase {

  @ClassRule
  public static TestHMS TEST_HMS = new TestHMS();

  public TestOrphanFileCleanHive(boolean ifKeyed, boolean ifPartitioned) {
    super(new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(ifKeyed, ifPartitioned));
  }

  @Parameterized.Parameters(name = "ifKeyed = {0}, ifPartitioned = {1}")
  public static Object[][] parameters() {
    return new Object[][]{
        {true, true},
        {true, false},
        {false, true},
        {false, false}};
  }

  private static OrphanFilesCleaningExecutor orphanFilesCleaningExecutor;

  @Before
  public void mock() {
    orphanFilesCleaningExecutor = Mockito.mock(OrphanFilesCleaningExecutor.class);
    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Mockito.when(tableRuntime.loadTable()).thenReturn(getArcticTable());
    Mockito.when(tableRuntime.getTableIdentifier()).thenReturn(
        ServerTableIdentifier.of(AmsUtil.toTableIdentifier(getArcticTable().id())));
    Mockito.doCallRealMethod().when(orphanFilesCleaningExecutor).execute(tableRuntime);
  }

  @Test
  public void orphanDataFileClean() throws IOException {
    UnkeyedTable baseTable = isKeyedTable() ?
        getArcticTable().asKeyedTable().baseTable() : getArcticTable().asUnkeyedTable();
    String baseOrphanFilePath = baseTable.location() +
        File.separator + DATA_FOLDER_NAME + File.separator + "orphan.parquet";
    String hiveOrphanFilePath = ((SupportHive) getArcticTable()).hiveLocation() +
        File.separator + DATA_FOLDER_NAME + File.separator + "orphan.parquet";
    OutputFile baseOrphanDataFile = getArcticTable().io().newOutputFile(baseOrphanFilePath);
    baseOrphanDataFile.createOrOverwrite().close();
    OutputFile changeOrphanDataFile = getArcticTable().io().newOutputFile(hiveOrphanFilePath);
    changeOrphanDataFile.createOrOverwrite().close();
    Assert.assertTrue(getArcticTable().io().exists(baseOrphanFilePath));
    Assert.assertTrue(getArcticTable().io().exists(hiveOrphanFilePath));
    orphanFilesCleaningExecutor.cleanContentFiles(getArcticTable(), System.currentTimeMillis());
    Assert.assertFalse(getArcticTable().io().exists(baseOrphanFilePath));
    Assert.assertTrue(getArcticTable().io().exists(hiveOrphanFilePath));
  }

  @Test
  public void orphanMetadataFileClean() throws IOException {
    UnkeyedTable baseTable = isKeyedTable() ?
        getArcticTable().asKeyedTable().baseTable() : getArcticTable().asUnkeyedTable();
    String orphanFilePath = baseTable.location() + File.separator + "metadata" +
        File.separator + "orphan.avro";
    OutputFile baseOrphanDataFile = getArcticTable().io().newOutputFile(orphanFilePath);
    baseOrphanDataFile.createOrOverwrite().close();
    Assert.assertTrue(getArcticTable().io().exists(orphanFilePath));
    orphanFilesCleaningExecutor.cleanMetadata(getArcticTable(), System.currentTimeMillis());
    Assert.assertFalse(getArcticTable().io().exists(orphanFilePath));
    ExecutorTestUtil.assertMetadataExists(getArcticTable());
  }

}
