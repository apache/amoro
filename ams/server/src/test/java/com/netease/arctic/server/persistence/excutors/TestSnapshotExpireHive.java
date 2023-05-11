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

package com.netease.arctic.server.persistence.excutors;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.hive.catalog.HiveCatalogTestHelper;
import com.netease.arctic.hive.catalog.HiveTableTestHelper;
import com.netease.arctic.server.table.executor.SnapshotsExpiringExecutor;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.TableFileUtil;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(Parameterized.class)
public class TestSnapshotExpireHive extends TableTestBase {

  @ClassRule
  public static TestHMS TEST_HMS = new TestHMS();

  public TestSnapshotExpireHive(boolean ifKeyed, boolean ifPartitioned) {
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

  @Test
  public void testExpireTableFiles() {
    List<DataFile> hiveFiles = ExecutorTestUtil.writeAndCommitBaseAndHive(getArcticTable(), 1, true);
    List<DataFile> s2Files = ExecutorTestUtil.writeAndCommitBaseAndHive(getArcticTable(), 1, false);

    DeleteFiles deleteHiveFiles = isKeyedTable() ?
        getArcticTable().asKeyedTable().baseTable().newDelete() : getArcticTable().asUnkeyedTable().newDelete();
    for (DataFile hiveFile : hiveFiles) {
      Assert.assertTrue(getArcticTable().io().exists(hiveFile.path().toString()));
      deleteHiveFiles.deleteFile(hiveFile);
    }
    deleteHiveFiles.commit();

    DeleteFiles deleteIcebergFiles = isKeyedTable() ?
        getArcticTable().asKeyedTable().baseTable().newDelete() : getArcticTable().asUnkeyedTable().newDelete();
    for (DataFile s2File : s2Files) {
      Assert.assertTrue(getArcticTable().io().exists(s2File.path().toString()));
      deleteIcebergFiles.deleteFile(s2File);
    }
    deleteIcebergFiles.commit();

    List<DataFile> s3Files = ExecutorTestUtil.writeAndCommitBaseAndHive(getArcticTable(), 1, false);
    s3Files.forEach(file -> Assert.assertTrue(getArcticTable().io().exists(file.path().toString())));

    Set<String> hiveLocation = new HashSet<>();
    String partitionHiveLocation = hiveFiles.get(0).path().toString();
    hiveLocation.add(TableFileUtil.getUriPath(TableFileUtil.getFileDir(partitionHiveLocation)));
    if (isPartitionedTable()) {
      String anotherHiveLocation = partitionHiveLocation.contains("op_time_day=2022-01-01") ?
          partitionHiveLocation.replace("op_time_day=2022-01-01", "op_time_day=2022-01-02") :
          partitionHiveLocation.replace("op_time_day=2022-01-02", "op_time_day=2022-01-01");
      hiveLocation.add(TableFileUtil.getUriPath(TableFileUtil.getFileDir(anotherHiveLocation)));
    }
    UnkeyedTable unkeyedTable = isKeyedTable() ?
        getArcticTable().asKeyedTable().baseTable() : getArcticTable().asUnkeyedTable();
    SnapshotsExpiringExecutor.expireSnapshots(unkeyedTable, System.currentTimeMillis(), hiveLocation);
    Assert.assertEquals(1, Iterables.size(unkeyedTable.snapshots()));

    hiveFiles.forEach(file -> Assert.assertTrue(getArcticTable().io().exists(file.path().toString())));
    s2Files.forEach(file -> Assert.assertFalse(getArcticTable().io().exists(file.path().toString())));
    s3Files.forEach(file -> Assert.assertTrue(getArcticTable().io().exists(file.path().toString())));
  }

}
