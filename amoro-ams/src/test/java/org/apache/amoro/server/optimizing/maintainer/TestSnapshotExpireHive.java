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

package org.apache.amoro.server.optimizing.maintainer;

import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.amoro.hive.io.HiveDataTestHelpers;
import org.apache.amoro.hive.utils.HiveTableUtil;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.data.Record;
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
    List<DataFile> hiveFiles = writeAndReplaceHivePartitions(getMixedTable());
    List<DataFile> s2Files = writeAndCommitBaseStore(getMixedTable());

    DeleteFiles deleteHiveFiles =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().baseTable().newDelete()
            : getMixedTable().asUnkeyedTable().newDelete();

    getMixedTable()
        .updateProperties()
        .set(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES, "0")
        .commit();
    getMixedTable().updateProperties().set(TableProperties.CHANGE_DATA_TTL, "0").commit();

    for (DataFile hiveFile : hiveFiles) {
      Assert.assertTrue(getMixedTable().io().exists(hiveFile.path().toString()));
      deleteHiveFiles.deleteFile(hiveFile);
    }
    deleteHiveFiles.commit();

    DeleteFiles deleteIcebergFiles =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().baseTable().newDelete()
            : getMixedTable().asUnkeyedTable().newDelete();
    for (DataFile s2File : s2Files) {
      Assert.assertTrue(getMixedTable().io().exists(s2File.path().toString()));
      deleteIcebergFiles.deleteFile(s2File);
    }
    deleteIcebergFiles.commit();

    List<DataFile> s3Files = writeAndCommitBaseStore(getMixedTable());
    s3Files.forEach(file -> Assert.assertTrue(getMixedTable().io().exists(file.path().toString())));

    UnkeyedTable unkeyedTable =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().baseTable()
            : getMixedTable().asUnkeyedTable();
    MixedTableMaintainer mixedTableMaintainer = new MixedTableMaintainer(getMixedTable());
    mixedTableMaintainer.getBaseMaintainer().expireSnapshots(System.currentTimeMillis(), 1);
    Assert.assertEquals(1, Iterables.size(unkeyedTable.snapshots()));

    hiveFiles.forEach(
        file -> Assert.assertTrue(getMixedTable().io().exists(file.path().toString())));
    s2Files.forEach(
        file -> Assert.assertFalse(getMixedTable().io().exists(file.path().toString())));
    s3Files.forEach(file -> Assert.assertTrue(getMixedTable().io().exists(file.path().toString())));
  }

  public List<DataFile> writeAndReplaceHivePartitions(MixedTable table) {
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
