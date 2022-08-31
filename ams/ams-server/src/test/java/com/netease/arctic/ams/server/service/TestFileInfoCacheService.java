/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.server.service;

import com.clearspring.analytics.util.Lists;
import com.netease.arctic.TableTestBase;
import com.netease.arctic.ams.api.CommitMetaProducer;
import com.netease.arctic.ams.api.DataFile;
import com.netease.arctic.ams.api.DataFileInfo;
import com.netease.arctic.ams.api.MetaException;
import com.netease.arctic.ams.api.PartitionFieldData;
import com.netease.arctic.ams.api.TableChange;
import com.netease.arctic.ams.api.TableCommitMeta;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.server.model.TransactionsOfTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.netease.arctic.ams.server.AmsTestBase.AMS_TEST_CATALOG_NAME;
import static com.netease.arctic.ams.server.AmsTestBase.AMS_TEST_DB_NAME;
import static com.netease.arctic.ams.server.AmsTestBase.catalog;

public class TestFileInfoCacheService extends TableTestBase {

  TableIdentifier tableIdentifier = new TableIdentifier("test", "test", "test");
  Random random = new Random();

  @Test
  public void testAppendCommit() throws MetaException {
    TableCommitMeta meta = new TableCommitMeta();
    meta.setAction("append");
    meta.setCommitTime(System.currentTimeMillis());
    meta.setCommitMetaProducer(CommitMetaProducer.INGESTION);
    meta.setTableIdentifier(tableIdentifier);
    List<TableChange> changes = new ArrayList<>();
    TableChange change = new TableChange();
    change.setParentSnapshotId(-1);
    change.setInnerTable("base");
    List<DataFile> dataFiles = new ArrayList<>();
    dataFiles.add(genDatafile());
    change.setAddFiles(dataFiles);
    long snapshotId = 1L;
    change.setSnapshotId(snapshotId);
    TableChange change1 = new TableChange();
    change1.setParentSnapshotId(snapshotId);
    change1.setInnerTable("base");
    List<DataFile> dataFiles1 = new ArrayList<>();
    dataFiles1.add(genDatafile());
    change1.setAddFiles(dataFiles1);
    long snapshotId1 = 2L;
    change1.setSnapshotId(snapshotId1);

    changes.add(change);
    changes.add(change1);
    meta.setChanges(changes);
    Map<String, String> properties = new HashMap<>();
    properties.put(TableProperties.TABLE_EVENT_TIME_FIELD, "eventTime");
    meta.setProperties(properties);
    ServiceContainer.getFileInfoCacheService().commitCacheFileInfo(meta);

    List<TransactionsOfTable> transactionsOfTables =
        ServiceContainer.getFileInfoCacheService().getTxExcludeOptimize(tableIdentifier);
    Assert.assertEquals(2, transactionsOfTables.size());
    Assert.assertEquals(snapshotId1, transactionsOfTables.get(0).getTransactionId());
    Assert.assertEquals(snapshotId, transactionsOfTables.get(1).getTransactionId());
  }

  @Test
  public void testNeedFixCacheWhenParentNotCached() throws MetaException {
    TableCommitMeta meta = new TableCommitMeta();
    meta.setAction("append");
    meta.setCommitTime(System.currentTimeMillis());
    meta.setCommitMetaProducer(CommitMetaProducer.INGESTION);
    meta.setTableIdentifier(tableIdentifier);
    List<TableChange> changes = new ArrayList<>();
    TableChange change = new TableChange();
    change.setParentSnapshotId(1);
    change.setInnerTable("base");
    List<DataFile> dataFiles = new ArrayList<>();
    dataFiles.add(genDatafile());
    change.setAddFiles(dataFiles);
    long snapshotId = 2L;
    change.setSnapshotId(snapshotId);
    ServiceContainer.getFileInfoCacheService().commitCacheFileInfo(meta);
    meta.setChanges(changes);

    AtomicBoolean isCached = new AtomicBoolean(false);
    ServiceContainer.getFileInfoCacheService().getTxExcludeOptimize(tableIdentifier).forEach(transactionsOfTable -> {
      if (transactionsOfTable.getTransactionId() == snapshotId) {
        isCached.set(true);
      }
    });
    Assert.assertFalse(isCached.get());
  }

  @Test
  public void testUpdateDeleteCommit() throws MetaException {
    TableCommitMeta meta = new TableCommitMeta();
    meta.setAction("append");
    meta.setCommitTime(System.currentTimeMillis());
    meta.setCommitMetaProducer(CommitMetaProducer.INGESTION);
    meta.setTableIdentifier(tableIdentifier);
    List<TableChange> changes = new ArrayList<>();
    TableChange change = new TableChange();
    change.setParentSnapshotId(-1);
    change.setInnerTable("base");
    List<DataFile> dataFiles = new ArrayList<>();
    dataFiles.add(genDatafile());
    change.setAddFiles(dataFiles);
    long snapshotId = 1L;
    change.setSnapshotId(snapshotId);

    TableChange change1 = new TableChange();
    change1.setParentSnapshotId(snapshotId);
    change1.setInnerTable("base");
    List<DataFile> dataFiles1 = new ArrayList<>();
    dataFiles1.add(genDatafile());
    change1.setDeleteFiles(dataFiles);
    change1.setAddFiles(dataFiles1);
    long snapshotId1 = 2L;
    change1.setSnapshotId(snapshotId1);

    changes.add(change);
    changes.add(change1);
    meta.setChanges(changes);
    Map<String, String> properties = new HashMap<>();
    properties.put(TableProperties.TABLE_EVENT_TIME_FIELD, "eventTime");
    meta.setProperties(properties);
    ServiceContainer.getFileInfoCacheService().commitCacheFileInfo(meta);

    List<TransactionsOfTable> transactionsOfTables =
        ServiceContainer.getFileInfoCacheService().getTxExcludeOptimize(tableIdentifier);
    Assert.assertEquals(2, transactionsOfTables.size());
    Assert.assertEquals(snapshotId1, transactionsOfTables.get(0).getTransactionId());
    Assert.assertEquals(snapshotId, transactionsOfTables.get(1).getTransactionId());

    List<DataFileInfo> dataFileInfos = ServiceContainer.getFileInfoCacheService().getOptimizeDatafiles(
        tableIdentifier,
        "base");
    Assert.assertEquals(dataFiles1.size(), dataFileInfos.size());
    Assert.assertEquals(dataFiles1.get(0).getPath(), dataFileInfos.get(0).getPath());
  }

  @Test
  public void testUnkeyedTableSyncFileCache() {
    com.netease.arctic.table.TableIdentifier tableId =
        com.netease.arctic.table.TableIdentifier.of(AMS_TEST_CATALOG_NAME, AMS_TEST_DB_NAME,
            "file_sync_test_unkeyed_table");
    UnkeyedTable fileSyncUnkeyedTable = catalog
        .newTableBuilder(
            tableId,
            TABLE_SCHEMA).withPartitionSpec(SPEC).create().asUnkeyedTable();
    testSyncFileCache(fileSyncUnkeyedTable, tableId);
  }

  @Test
  public void testKeyedTableSyncFileCache() {
    com.netease.arctic.table.TableIdentifier tableId =
        com.netease.arctic.table.TableIdentifier.of(AMS_TEST_CATALOG_NAME, AMS_TEST_DB_NAME,
            "file_sync_test_keyed_table");
    KeyedTable fileSyncKeyedTable = catalog
        .newTableBuilder(
            tableId,
            TABLE_SCHEMA).withPrimaryKeySpec(PRIMARY_KEY_SPEC).withPartitionSpec(SPEC).create().asKeyedTable();
    testSyncFileCache(fileSyncKeyedTable.baseTable(), tableId);
  }

  public void testSyncFileCache(UnkeyedTable fileSyncUnkeyedTable, com.netease.arctic.table.TableIdentifier tableId) {
    fileSyncUnkeyedTable.newFastAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .commit();
    List<DataFileInfo> commitDataFileInfos = ServiceContainer.getFileInfoCacheService().getOptimizeDatafiles(
        fileSyncUnkeyedTable.id().buildTableIdentifier(),
        "base");
    List<TransactionsOfTable> commitSnapInfos = ServiceContainer.getFileInfoCacheService().getTxExcludeOptimize(
        fileSyncUnkeyedTable.id().buildTableIdentifier());
    ServiceContainer.getFileInfoCacheService().deleteTableCache(tableId);
    List<DataFileInfo> cacheDataFileInfos = ServiceContainer.getFileInfoCacheService().getOptimizeDatafiles(
        fileSyncUnkeyedTable.id().buildTableIdentifier(),
        "base");
    Assert.assertEquals(0, cacheDataFileInfos.size());
    ServiceContainer.getFileInfoCacheService()
        .syncTableFileInfo(fileSyncUnkeyedTable.id().buildTableIdentifier(), "base");
    List<DataFileInfo> syncDataFileInfos = ServiceContainer.getFileInfoCacheService().getOptimizeDatafiles(
        fileSyncUnkeyedTable.id().buildTableIdentifier(),
        "base");
    List<TransactionsOfTable> syncSnapInfos = ServiceContainer.getFileInfoCacheService().getTxExcludeOptimize(
        fileSyncUnkeyedTable.id().buildTableIdentifier());
    Assert.assertEquals(commitDataFileInfos.size(), syncDataFileInfos.size());
    for (DataFileInfo commitDataFileInfo : commitDataFileInfos) {
      boolean isCached = false;
      for (DataFileInfo syncDataFileInfo : syncDataFileInfos) {
        if (commitDataFileInfo.getPath().equals(syncDataFileInfo.getPath())) {
          isCached = true;
          break;
        }
      }
      Assert.assertTrue(isCached);
    }
    Assert.assertEquals(commitSnapInfos.size(), syncSnapInfos.size());
    for (int i = 0; i < commitSnapInfos.size(); i++) {
      Assert.assertEquals(commitSnapInfos.get(i).getTransactionId(), syncSnapInfos.get(i).getTransactionId());
    }

    fileSyncUnkeyedTable.newOverwrite()
        .deleteFile(FILE_A)
        .deleteFile(FILE_B)
        .addFile(FILE_C)
        .commit();
    List<TransactionsOfTable> overwriteCommitSnapInfos =
        ServiceContainer.getFileInfoCacheService().getTxExcludeOptimize(
            fileSyncUnkeyedTable.id().buildTableIdentifier());
    ServiceContainer.getFileInfoCacheService().deleteTableCache(tableId);
    ServiceContainer.getFileInfoCacheService()
        .syncTableFileInfo(fileSyncUnkeyedTable.id().buildTableIdentifier(), "base");
    List<DataFileInfo> overwriteSyncDataFileInfos = ServiceContainer.getFileInfoCacheService().getOptimizeDatafiles(
        fileSyncUnkeyedTable.id().buildTableIdentifier(),
        "base");
    List<TransactionsOfTable> overwriteSnapInfos = ServiceContainer.getFileInfoCacheService().getTxExcludeOptimize(
        fileSyncUnkeyedTable.id().buildTableIdentifier());
    Assert.assertEquals(1, overwriteSyncDataFileInfos.size());
    Assert.assertEquals(FILE_C.path(), overwriteSyncDataFileInfos.get(0).getPath());
    Assert.assertEquals(1, overwriteSnapInfos.size());
    Assert.assertEquals(
        overwriteCommitSnapInfos.get(0).getTransactionId(),
        overwriteSnapInfos.get(0).getTransactionId());
  }

  private DataFile genDatafile() {
    DataFile dataFile = new DataFile();
    dataFile.setFileSize(1);
    dataFile.setFileType("INSERT_FILE");
    dataFile.setIndex(0);
    dataFile.setMask(0);
    dataFile.setPath("/tmp/test" + random.nextInt() + ".file");
    Map<String, ByteBuffer> upperBounds = new HashMap<>();
    byte[] bytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(1000L).array();
    upperBounds.put("eventTime", ByteBuffer.wrap(bytes));
    dataFile.setUpperBounds(upperBounds);
    PartitionFieldData partitionFieldData = new PartitionFieldData();
    partitionFieldData.setName("pt");
    partitionFieldData.setValue("2022-08-31");
    List<PartitionFieldData> partitionFieldDataList = new ArrayList<>();
    partitionFieldDataList.add(partitionFieldData);
    dataFile.setPartition(partitionFieldDataList);
    return dataFile;
  }
}
