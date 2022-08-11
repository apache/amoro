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

package com.netease.arctic.trace;

import com.google.common.collect.Lists;
import com.netease.arctic.TableTestBase;
import com.netease.arctic.ams.api.Constants;
import com.netease.arctic.ams.api.DataFile;
import com.netease.arctic.ams.api.TableChange;
import com.netease.arctic.ams.api.TableCommitMeta;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.io.writer.GenericTaskWriters;
import com.netease.arctic.io.writer.SortedPosDeleteWriter;
import org.apache.iceberg.DataOperations;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TableTracerTest extends TableTestBase {

  @Test
  public void testTraceAppendFiles() {
    testTable.newAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .commit();

    List<TableCommitMeta> TableCommitMetas = AMS.handler().getTableCommitMetas().get(TABLE_ID.buildTableIdentifier());
    Assert.assertEquals(1, TableCommitMetas.size());
    TableCommitMeta commitMeta = TableCommitMetas.get(0);
    validateCommitMeta(commitMeta, DataOperations.APPEND, new org.apache.iceberg.DataFile[]{FILE_A, FILE_B},
        new org.apache.iceberg.DataFile[]{});
  }

  @Test
  public void testTraceAppendFilesInTx() {
    Transaction transaction = testTable.newTransaction();
    transaction.newAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .commit();

    Assert.assertFalse(AMS.handler().getTableCommitMetas().containsKey(TABLE_ID.buildTableIdentifier()));

    transaction.commitTransaction();

    List<TableCommitMeta> TableCommitMetas = AMS.handler().getTableCommitMetas().get(TABLE_ID.buildTableIdentifier());
    Assert.assertEquals(1, TableCommitMetas.size());
    TableCommitMeta commitMeta = TableCommitMetas.get(0);
    validateCommitMeta(commitMeta, DataOperations.APPEND, new org.apache.iceberg.DataFile[]{FILE_A, FILE_B},
        new org.apache.iceberg.DataFile[]{});
  }

  @Test
  public void testTraceAppendFilesByOptimize() {
    testTable.newAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .set(SnapshotSummary.OPTIMIZE_PRODUCED, "true")
        .commit();

    List<TableCommitMeta> TableCommitMetas = AMS.handler().getTableCommitMetas().get(TABLE_ID.buildTableIdentifier());
    Assert.assertEquals(1, TableCommitMetas.size());
    TableCommitMeta commitMeta = TableCommitMetas.get(0);
    Assert.assertTrue(commitMeta.isOptimizeProduced());
    validateCommitMeta(commitMeta, DataOperations.APPEND, new org.apache.iceberg.DataFile[]{FILE_A, FILE_B},
        new org.apache.iceberg.DataFile[]{});
  }

  @Test
  public void testTraceFastAppend() {
    testTable.newFastAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .commit();

    List<TableCommitMeta> TableCommitMetas = AMS.handler().getTableCommitMetas().get(TABLE_ID.buildTableIdentifier());
    Assert.assertEquals(1, TableCommitMetas.size());
    TableCommitMeta commitMeta = TableCommitMetas.get(0);
    validateCommitMeta(commitMeta, DataOperations.APPEND, new org.apache.iceberg.DataFile[]{FILE_A, FILE_B},
        new org.apache.iceberg.DataFile[]{});
  }

  @Test
  public void testTraceFastAppendInTx() {
    Transaction transaction = testTable.newTransaction();
    transaction.newFastAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .commit();

    Assert.assertFalse(AMS.handler().getTableCommitMetas().containsKey(TABLE_ID.buildTableIdentifier()));

    transaction.commitTransaction();
    List<TableCommitMeta> TableCommitMetas = AMS.handler().getTableCommitMetas().get(TABLE_ID.buildTableIdentifier());
    Assert.assertEquals(1, TableCommitMetas.size());
    TableCommitMeta commitMeta = TableCommitMetas.get(0);
    validateCommitMeta(commitMeta, DataOperations.APPEND, new org.apache.iceberg.DataFile[]{FILE_A, FILE_B},
        new org.apache.iceberg.DataFile[]{});
  }

  @Test
  public void testTraceFastAppendByOptimize() {
    testTable.newFastAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .set(SnapshotSummary.OPTIMIZE_PRODUCED, "true")
        .commit();

    List<TableCommitMeta> TableCommitMetas = AMS.handler().getTableCommitMetas().get(TABLE_ID.buildTableIdentifier());
    Assert.assertEquals(1, TableCommitMetas.size());
    TableCommitMeta commitMeta = TableCommitMetas.get(0);
    Assert.assertTrue(commitMeta.isOptimizeProduced());
    validateCommitMeta(commitMeta, DataOperations.APPEND, new org.apache.iceberg.DataFile[]{FILE_A, FILE_B},
        new org.apache.iceberg.DataFile[]{});
  }

  @Test
  public void testTraceOverwrite() {
    testTable.newFastAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .commit();

    testTable.newOverwrite()
        .deleteFile(FILE_A)
        .deleteFile(FILE_B)
        .addFile(FILE_C)
        .commit();

    List<TableCommitMeta> TableCommitMetas = AMS.handler().getTableCommitMetas().get(TABLE_ID.buildTableIdentifier());
    Assert.assertEquals(2, TableCommitMetas.size());
    TableCommitMeta commitMeta = TableCommitMetas.get(1);
    validateCommitMeta(commitMeta, DataOperations.OVERWRITE, new org.apache.iceberg.DataFile[]{FILE_C},
        new org.apache.iceberg.DataFile[]{FILE_A, FILE_B});
  }

  @Test
  public void testTraceOverwriteInTx() {
    testTable.newFastAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .commit();

    Transaction transaction = testTable.newTransaction();
    transaction.newOverwrite()
        .deleteFile(FILE_A)
        .deleteFile(FILE_B)
        .addFile(FILE_C)
        .commit();

    Assert.assertEquals(1, AMS.handler().getTableCommitMetas().get(TABLE_ID.buildTableIdentifier()).size());

    transaction.commitTransaction();
    List<TableCommitMeta> TableCommitMetas = AMS.handler().getTableCommitMetas().get(TABLE_ID.buildTableIdentifier());
    Assert.assertEquals(2, TableCommitMetas.size());
    TableCommitMeta commitMeta = TableCommitMetas.get(1);
    validateCommitMeta(commitMeta, DataOperations.OVERWRITE, new org.apache.iceberg.DataFile[]{FILE_C},
        new org.apache.iceberg.DataFile[]{FILE_A, FILE_B});
  }

  @Test
  public void testTraceOverwriteByOptimize() {
    testTable.newFastAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .commit();

    testTable.newOverwrite()
        .deleteFile(FILE_A)
        .deleteFile(FILE_B)
        .addFile(FILE_C)
        .set(SnapshotSummary.OPTIMIZE_PRODUCED, "true")
        .commit();

    List<TableCommitMeta> TableCommitMetas = AMS.handler().getTableCommitMetas().get(TABLE_ID.buildTableIdentifier());
    Assert.assertEquals(2, TableCommitMetas.size());
    TableCommitMeta commitMeta = TableCommitMetas.get(1);
    Assert.assertTrue(commitMeta.isOptimizeProduced());
    validateCommitMeta(commitMeta, DataOperations.OVERWRITE, new org.apache.iceberg.DataFile[]{FILE_C},
        new org.apache.iceberg.DataFile[]{FILE_A, FILE_B});
  }

  @Test
  public void testTraceRewrite() {
    testTable.newFastAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .commit();

    testTable.newRewrite()
        .rewriteFiles(Sets.newHashSet(FILE_A, FILE_B), Sets.newHashSet(FILE_C))
        .commit();

    List<TableCommitMeta> TableCommitMetas = AMS.handler().getTableCommitMetas().get(TABLE_ID.buildTableIdentifier());
    Assert.assertEquals(2, TableCommitMetas.size());
    TableCommitMeta commitMeta = TableCommitMetas.get(1);
    validateCommitMeta(commitMeta, DataOperations.REPLACE, new org.apache.iceberg.DataFile[]{FILE_C},
        new org.apache.iceberg.DataFile[]{FILE_A, FILE_B});
  }

  @Test
  public void testTraceRewriteInTx() {
    testTable.newFastAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .commit();

    Transaction transaction = testTable.newTransaction();
    transaction.newRewrite()
        .rewriteFiles(Sets.newHashSet(FILE_A, FILE_B), Sets.newHashSet(FILE_C))
        .commit();

    Assert.assertEquals(1, AMS.handler().getTableCommitMetas().get(TABLE_ID.buildTableIdentifier()).size());

    transaction.commitTransaction();
    List<TableCommitMeta> TableCommitMetas = AMS.handler().getTableCommitMetas().get(TABLE_ID.buildTableIdentifier());
    Assert.assertEquals(2, TableCommitMetas.size());
    TableCommitMeta commitMeta = TableCommitMetas.get(1);
    validateCommitMeta(commitMeta, DataOperations.REPLACE, new org.apache.iceberg.DataFile[]{FILE_C},
        new org.apache.iceberg.DataFile[]{FILE_A, FILE_B});
  }

  @Test
  public void testTraceRewriteByOptimize() {
    testTable.newFastAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .commit();

    testTable.newRewrite()
        .rewriteFiles(Sets.newHashSet(FILE_A, FILE_B), Sets.newHashSet(FILE_C))
        .set(SnapshotSummary.OPTIMIZE_PRODUCED, "true")
        .commit();

    List<TableCommitMeta> TableCommitMetas = AMS.handler().getTableCommitMetas().get(TABLE_ID.buildTableIdentifier());
    Assert.assertEquals(2, TableCommitMetas.size());
    TableCommitMeta commitMeta = TableCommitMetas.get(1);
    Assert.assertTrue(commitMeta.isOptimizeProduced());
    validateCommitMeta(commitMeta, DataOperations.REPLACE, new org.apache.iceberg.DataFile[]{FILE_C},
        new org.apache.iceberg.DataFile[]{FILE_A, FILE_B});
  }

  @Test
  public void testMultipleOperationInTx() {
    Transaction transaction = testTable.newTransaction();
    transaction.newAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .commit();

    transaction.newOverwrite()
        .deleteFile(FILE_A)
        .addFile(FILE_C)
        .commit();

    Assert.assertFalse(AMS.handler().getTableCommitMetas().containsKey(TABLE_ID.buildTableIdentifier()));

    transaction.commitTransaction();

    List<Snapshot> snapshots = Lists.newArrayList(testTable.snapshots());
    Assert.assertEquals(2, snapshots.size());
    List<TableCommitMeta> TableCommitMetas = AMS.handler().getTableCommitMetas().get(TABLE_ID.buildTableIdentifier());
    Assert.assertEquals(1, TableCommitMetas.size());
    TableCommitMeta commitMeta = TableCommitMetas.get(0);
    Assert.assertEquals(2, commitMeta.getChanges().size());
    validateTableChange(snapshots.get(0), commitMeta.getChanges().get(0),
        new org.apache.iceberg.DataFile[]{FILE_A, FILE_B}, new org.apache.iceberg.DataFile[]{});
    validateTableChange(snapshots.get(1), commitMeta.getChanges().get(1),
        new org.apache.iceberg.DataFile[]{FILE_C}, new org.apache.iceberg.DataFile[]{FILE_A});
  }

  @Test
  public void testMultipleOperationInTxByOptimize() {
    Transaction transaction = testTable.newTransaction();
    transaction.newAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .set(SnapshotSummary.OPTIMIZE_PRODUCED, "true")
        .commit();

    transaction.newOverwrite()
        .deleteFile(FILE_A)
        .addFile(FILE_C)
        .set(SnapshotSummary.OPTIMIZE_PRODUCED, "true")
        .commit();

    Assert.assertFalse(AMS.handler().getTableCommitMetas().containsKey(TABLE_ID.buildTableIdentifier()));

    transaction.commitTransaction();

    List<Snapshot> snapshots = Lists.newArrayList(testTable.snapshots());
    Assert.assertEquals(2, snapshots.size());
    List<TableCommitMeta> TableCommitMetas = AMS.handler().getTableCommitMetas().get(TABLE_ID.buildTableIdentifier());
    Assert.assertEquals(1, TableCommitMetas.size());
    TableCommitMeta commitMeta = TableCommitMetas.get(0);
    Assert.assertTrue(commitMeta.isOptimizeProduced());
    Assert.assertEquals(2, commitMeta.getChanges().size());
    validateTableChange(snapshots.get(0), commitMeta.getChanges().get(0),
        new org.apache.iceberg.DataFile[]{FILE_A, FILE_B}, new org.apache.iceberg.DataFile[]{});
    validateTableChange(snapshots.get(1), commitMeta.getChanges().get(1),
        new org.apache.iceberg.DataFile[]{FILE_C}, new org.apache.iceberg.DataFile[]{FILE_A});
  }

  @Test
  public void testTraceRemovePosDeleteInternal() throws Exception {
    testKeyedTable.baseTable().newAppend().appendFile(FILE_A).commit();

    SortedPosDeleteWriter<Record> writer = GenericTaskWriters.builderFor(testKeyedTable)
        .withTransactionId(1).buildBasePosDeleteWriter(2, 1, FILE_A.partition());
    writer.delete(FILE_A.path(), 1);
    writer.delete(FILE_A.path(), 3);
    writer.delete(FILE_A.path(), 5);
    List<DeleteFile> result = writer.complete();
    RowDelta rowDelta = testKeyedTable.baseTable().newRowDelta();
    result.forEach(rowDelta::addDeletes);
    rowDelta.commit();

    testKeyedTable.baseTable().newAppend().appendFile(FILE_C).commit();
    OverwriteFiles overwriteFiles = testKeyedTable.baseTable().newOverwrite();
    overwriteFiles.deleteFile(FILE_A);
    overwriteFiles.addFile(FILE_B);
    overwriteFiles.commit();

    List<TableCommitMeta> tableCommitMetas = AMS.handler().getTableCommitMetas().get(PK_TABLE_ID.buildTableIdentifier());
    Assert.assertEquals(4, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(tableCommitMetas.size() - 1);
    Assert.assertEquals(1, commitMeta.getChanges().size());
    TableChange tableChange = commitMeta.getChanges().get(0);
    Assert.assertEquals(2, tableChange.deleteFiles.size());
  }

  @Test
  public void testTraceRemovePosDeleteInternalInTransaction() throws Exception {
    testKeyedTable.baseTable().newAppend().appendFile(FILE_A).commit();

    SortedPosDeleteWriter<Record> writer = GenericTaskWriters.builderFor(testKeyedTable)
        .withTransactionId(1).buildBasePosDeleteWriter(2, 1, FILE_A.partition());
    writer.delete(FILE_A.path(), 1);
    writer.delete(FILE_A.path(), 3);
    writer.delete(FILE_A.path(), 5);
    List<DeleteFile> result = writer.complete();
    RowDelta rowDelta = testKeyedTable.baseTable().newRowDelta();
    result.forEach(rowDelta::addDeletes);
    rowDelta.commit();

    testKeyedTable.baseTable().newAppend().appendFile(FILE_C).commit();
    Transaction transaction = testKeyedTable.baseTable().newTransaction();
    OverwriteFiles overwriteFiles = transaction.newOverwrite();
    overwriteFiles.deleteFile(FILE_A);
    overwriteFiles.addFile(FILE_B);
    overwriteFiles.commit();
    DeleteFiles deleteFiles = transaction.newDelete();
    deleteFiles.deleteFile(FILE_C);
    deleteFiles.commit();
    transaction.commitTransaction();

    List<TableCommitMeta> tableCommitMetas = AMS.handler().getTableCommitMetas().get(PK_TABLE_ID.buildTableIdentifier());
    Assert.assertEquals(4, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(tableCommitMetas.size() - 1);
    Assert.assertEquals(2, commitMeta.getChanges().size());
    TableChange tableChange = commitMeta.getChanges().get(0);
    Assert.assertEquals(2, tableChange.deleteFiles.size());
  }

  private void validateTableChange(Snapshot snapshot, TableChange tableChange,
                                   org.apache.iceberg.DataFile[] addFiles,
                                   org.apache.iceberg.DataFile[] deleteFiles) {
    Assert.assertEquals(Constants.INNER_TABLE_BASE, tableChange.getInnerTable());
    Assert.assertEquals(snapshot.snapshotId(), tableChange.getSnapshotId());
    Assert.assertEquals(snapshot.parentId() == null ? -1 :
        testTable.currentSnapshot().parentId(), tableChange.getParentSnapshotId());

    validateDataFile(tableChange.getAddFiles(), addFiles);
    validateDataFile(tableChange.getDeleteFiles(), deleteFiles);
  }

  private void validateCommitMeta(TableCommitMeta commitMeta, String operation,
                                  org.apache.iceberg.DataFile[] addFiles,
                                  org.apache.iceberg.DataFile[] deleteFiles) {

    Assert.assertEquals(TABLE_ID.buildTableIdentifier(), commitMeta.getTableIdentifier());
    Assert.assertEquals(operation, commitMeta.getAction());
    Assert.assertEquals(1, commitMeta.getChanges().size());

    TableChange tableChange = commitMeta.getChanges().get(0);
    Assert.assertEquals(Constants.INNER_TABLE_BASE, tableChange.getInnerTable());
    Assert.assertEquals(testTable.currentSnapshot().snapshotId(), tableChange.getSnapshotId());
    Assert.assertEquals(testTable.currentSnapshot().parentId() == null ? -1 :
        testTable.currentSnapshot().parentId(), tableChange.getParentSnapshotId());

    validateDataFile(tableChange.getAddFiles(), addFiles);
    validateDataFile(tableChange.getDeleteFiles(), deleteFiles);
  }

  private void validateDataFile(List<DataFile> dataFiles, org.apache.iceberg.DataFile... icebergFiles) {
    Assert.assertEquals(icebergFiles.length, dataFiles.size());
    Map<String, org.apache.iceberg.DataFile> icebergFilesMap = Maps.newHashMap();
    Arrays.stream(icebergFiles).forEach(f -> icebergFilesMap.put(f.path().toString(), f));
    for (DataFile validateFile : dataFiles) {
      org.apache.iceberg.DataFile icebergFile = icebergFilesMap.get(validateFile.getPath());
      Assert.assertEquals(icebergFile.path(), validateFile.getPath());
      Assert.assertEquals(icebergFile.fileSizeInBytes(), validateFile.getFileSize());
      Assert.assertEquals(icebergFile.recordCount(), validateFile.getRecordCount());
      Assert.assertEquals(DataFileType.BASE_FILE.name(), validateFile.getFileType());
      Assert.assertEquals(0, validateFile.getIndex());
      Assert.assertEquals(0, validateFile.getMask());
      Assert.assertEquals(testTable.spec().specId(), validateFile.getSpecId());
      Assert.assertEquals(1, validateFile.getPartitionSize());
      Assert.assertEquals(SPEC.fields().get(0).name(), validateFile.getPartition().get(0).getName());
      Assert.assertEquals(SPEC.partitionToPath(icebergFile.partition()),
          SPEC.fields().get(0).name() + "=" + validateFile.getPartition().get(0).getValue());
    }
  }

}
