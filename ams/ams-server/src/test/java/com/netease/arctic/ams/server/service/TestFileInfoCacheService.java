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

import com.netease.arctic.ams.api.CommitMetaProducer;
import com.netease.arctic.ams.api.DataFile;
import com.netease.arctic.ams.api.DataFileInfo;
import com.netease.arctic.ams.api.MetaException;
import com.netease.arctic.ams.api.SchemaUpdateMeta;
import com.netease.arctic.ams.api.TableChange;
import com.netease.arctic.ams.api.TableCommitMeta;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.server.model.TransactionsOfTable;

import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestFileInfoCacheService {

  TableIdentifier tableIdentifier = new TableIdentifier("test", "test", "test");
  Random random = new Random();

  @Before
  public void setUp() {
    ServiceContainer.getFileInfoCacheService()
        .deleteTableCache(com.netease.arctic.table.TableIdentifier.of(tableIdentifier));
  }

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
        ServiceContainer.getFileInfoCacheService().getTransactions(tableIdentifier);
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
    ServiceContainer.getFileInfoCacheService().getTransactions(tableIdentifier).forEach(transactionsOfTable -> {
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
        ServiceContainer.getFileInfoCacheService().getTransactions(tableIdentifier);
    Assert.assertEquals(2, transactionsOfTables.size());
    Assert.assertEquals(snapshotId1, transactionsOfTables.get(0).getTransactionId());
    Assert.assertEquals(snapshotId, transactionsOfTables.get(1).getTransactionId());

    List<DataFileInfo> dataFileInfos = ServiceContainer.getFileInfoCacheService().getOptimizeDatafiles(tableIdentifier,
        "base");
    Assert.assertEquals(dataFiles1.size(), dataFileInfos.size());
    Assert.assertEquals(dataFiles1.get(0).getPath(), dataFileInfos.get(0).getPath());
  }

  private DataFile genDatafile() {
    DataFile dataFile = new DataFile();
    dataFile.setFileSize(1);
    dataFile.setFileType("INSERT_FILE");
    dataFile.setIndex(0);
    dataFile.setMask(0);
    dataFile.setPath("/tmp/test"+ random.nextInt() +".file");
    Map<String, ByteBuffer> upperBounds = new HashMap<>();
    byte[] bytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(1000L).array();
    upperBounds.put("eventTime", ByteBuffer.wrap(bytes));
    dataFile.setUpperBounds(upperBounds);
    dataFile.setPartition(new ArrayList<>());
    return dataFile;
  }

  public static void checkCache(ArcticTable testTable, List<DataFileInfo> actual) {
    ServiceContainer.getFileInfoCacheService().syncTableFileInfo(
        testTable.id().buildTableIdentifier(),
        "base");
    ServiceContainer.getFileInfoCacheService().syncTableFileInfo(
        testTable.id().buildTableIdentifier(),
        "change");
    List<TransactionsOfTable> transactionsOfTables =
        ServiceContainer.getFileInfoCacheService().getTransactions(testTable.id().buildTableIdentifier());
    List<DataFileInfo> except = new ArrayList<>();
    transactionsOfTables.forEach(transaction -> except.addAll(ServiceContainer.getFileInfoCacheService()
        .getDatafilesInfo(testTable.id().buildTableIdentifier(), transaction.getTransactionId())));
    Assert.assertEquals(actual, except);
  }
}
