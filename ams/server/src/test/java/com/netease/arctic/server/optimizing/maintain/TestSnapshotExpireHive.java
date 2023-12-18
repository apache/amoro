/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.optimizing.maintain;

import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.hive.catalog.HiveCatalogTestHelper;
import com.netease.arctic.hive.catalog.HiveTableTestHelper;
import com.netease.arctic.hive.io.HiveDataTestHelpers;
import com.netease.arctic.hive.utils.HiveTableUtil;
import com.netease.arctic.server.process.maintain.MixedTableMaintainer;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
public class TestSnapshotExpireHive extends TestSnapshotExpire {

  @ClassRule public static TestHMS TEST_HMS = new TestHMS();

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, true)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, false)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(false, true)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(false, false)
      }
    };
  }

  public TestSnapshotExpireHive(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Test
  public void testExpireTableFiles() {
    List<DataFile> hiveFiles = writeAndReplaceHivePartitions(getArcticTable());
    List<DataFile> s2Files = writeAndCommitBaseStore(getArcticTable());

    DeleteFiles deleteHiveFiles =
        isKeyedTable()
            ? getArcticTable().asKeyedTable().baseTable().newDelete()
            : getArcticTable().asUnkeyedTable().newDelete();

    getArcticTable()
        .updateProperties()
        .set(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES, "0")
        .commit();
    getArcticTable().updateProperties().set(TableProperties.CHANGE_DATA_TTL, "0").commit();

    for (DataFile hiveFile : hiveFiles) {
      Assert.assertTrue(getArcticTable().io().exists(hiveFile.path().toString()));
      deleteHiveFiles.deleteFile(hiveFile);
    }
    deleteHiveFiles.commit();

    DeleteFiles deleteIcebergFiles =
        isKeyedTable()
            ? getArcticTable().asKeyedTable().baseTable().newDelete()
            : getArcticTable().asUnkeyedTable().newDelete();
    for (DataFile s2File : s2Files) {
      Assert.assertTrue(getArcticTable().io().exists(s2File.path().toString()));
      deleteIcebergFiles.deleteFile(s2File);
    }
    deleteIcebergFiles.commit();

    List<DataFile> s3Files = writeAndCommitBaseStore(getArcticTable());
    s3Files.forEach(
        file -> Assert.assertTrue(getArcticTable().io().exists(file.path().toString())));

    UnkeyedTable unkeyedTable =
        isKeyedTable()
            ? getArcticTable().asKeyedTable().baseTable()
            : getArcticTable().asUnkeyedTable();
    MixedTableMaintainer mixedTableMaintainer = new MixedTableMaintainer(getArcticTable());
    mixedTableMaintainer.getBaseMaintainer().expireSnapshots(System.currentTimeMillis());
    Assert.assertEquals(1, Iterables.size(unkeyedTable.snapshots()));

    hiveFiles.forEach(
        file -> Assert.assertTrue(getArcticTable().io().exists(file.path().toString())));
    s2Files.forEach(
        file -> Assert.assertFalse(getArcticTable().io().exists(file.path().toString())));
    s3Files.forEach(
        file -> Assert.assertTrue(getArcticTable().io().exists(file.path().toString())));
  }

  public List<DataFile> writeAndReplaceHivePartitions(ArcticTable table) {
    String hiveSubDir = HiveTableUtil.newHiveSubdirectory();
    HiveDataTestHelpers.WriterHelper writerHelper =
        HiveDataTestHelpers.writerOf(table).customHiveLocation(hiveSubDir).transactionId(0L);
    List<Record> records = createRecords(1, 100);
    List<DataFile> dataFiles;
    dataFiles = writerHelper.writeHive(records);

    // Using replace partitions to alter hive table or partitions to new location
    UnkeyedTable baseTable =
        table.isKeyedTable() ? table.asKeyedTable().baseTable() : table.asUnkeyedTable();
    ReplacePartitions replacePartitions = baseTable.newReplacePartitions();
    dataFiles.forEach(replacePartitions::addFile);
    replacePartitions.commit();
    // The dataFiles have the prefix '.' in file name, but it will be removed after commit
    return Lists.newArrayList(baseTable.currentSnapshot().addedDataFiles(table.io()));
  }
}
