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

package org.apache.amoro.trace;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.Constants;
import org.apache.amoro.DataFileTestHelpers;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CommitMetaProducer;
import org.apache.amoro.api.DataFile;
import org.apache.amoro.api.TableChange;
import org.apache.amoro.api.TableCommitMeta;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.data.DataFileType;
import org.apache.amoro.io.writer.GenericTaskWriters;
import org.apache.amoro.io.writer.SortedPosDeleteWriter;
import org.apache.amoro.op.SnapshotSummary;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.DataOperations;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.data.Record;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** Table trace is disabled for version 0.5.0 */
@Disabled
public class TestTableTracer extends TableTestBase {

  private boolean onBaseTable;

  private UnkeyedTable operationTable;

  public static Stream<Arguments> parameters() {
    return Stream.of(
        Arguments.of(true, true, true),
        Arguments.of(true, true, false),
        Arguments.of(true, false, true),
        Arguments.of(true, false, false),
        Arguments.of(false, true, true),
        Arguments.of(false, true, false));
  }

  private void prepare(boolean keyedTable, boolean onBaseTable, boolean partitionedTable)
      throws IOException {
    setupTable(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(keyedTable, partitionedTable));
    this.onBaseTable = onBaseTable;
    getAmsHandler().getTableCommitMetas().remove(getOperationTable().id().buildTableIdentifier());
  }

  private UnkeyedTable getOperationTable() {
    if (operationTable == null) {
      MixedTable mixedTable = getMixedTable();
      if (isKeyedTable()) {
        if (onBaseTable) {
          operationTable = mixedTable.asKeyedTable().baseTable();
        } else {
          operationTable = mixedTable.asKeyedTable().changeTable();
        }
      } else {
        if (onBaseTable) {
          operationTable = mixedTable.asUnkeyedTable();
        } else {
          throw new IllegalArgumentException("Unkeyed table do not have change store");
        }
      }
    }
    return operationTable;
  }

  private org.apache.iceberg.DataFile getDataFile(int number) {
    if (isPartitionedTable()) {
      return DataFileTestHelpers.getFile(number, "op_time_day=2022-08-30");
    } else {
      return DataFileTestHelpers.getFile(number);
    }
  }

  @ParameterizedTest(name = "keyedTable = {0}, onBaseTable = {1}, partitionedTable = {2}")
  @MethodSource("parameters")
  public void testTraceAppendFiles(
      boolean keyedTable, boolean onBaseTable, boolean partitionedTable) throws IOException {
    prepare(keyedTable, onBaseTable, partitionedTable);
    UnkeyedTable operationTable = getOperationTable();
    operationTable.newAppend().appendFile(getDataFile(1)).appendFile(getDataFile(2)).commit();

    List<TableCommitMeta> tableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(0, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(0);
    validateCommitMeta(
        commitMeta,
        DataOperations.APPEND,
        new org.apache.iceberg.DataFile[] {getDataFile(1), getDataFile(2)},
        new org.apache.iceberg.DataFile[] {});
  }

  @ParameterizedTest(name = "keyedTable = {0}, onBaseTable = {1}, partitionedTable = {2}")
  @MethodSource("parameters")
  public void testTraceAppendFilesInTx(
      boolean keyedTable, boolean onBaseTable, boolean partitionedTable) throws IOException {
    prepare(keyedTable, onBaseTable, partitionedTable);
    UnkeyedTable operationTable = getOperationTable();
    Transaction transaction = operationTable.newTransaction();
    transaction.newAppend().appendFile(getDataFile(1)).appendFile(getDataFile(2)).commit();

    Assertions.assertFalse(
        getAmsHandler()
            .getTableCommitMetas()
            .containsKey(operationTable.id().buildTableIdentifier()));

    transaction.commitTransaction();

    List<TableCommitMeta> tableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(1, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(0);
    validateCommitMeta(
        commitMeta,
        DataOperations.APPEND,
        new org.apache.iceberg.DataFile[] {getDataFile(1), getDataFile(2)},
        new org.apache.iceberg.DataFile[] {});
  }

  @ParameterizedTest(name = "keyedTable = {0}, onBaseTable = {1}, partitionedTable = {2}")
  @MethodSource("parameters")
  public void testTraceAppendFilesByOptimize(
      boolean keyedTable, boolean onBaseTable, boolean partitionedTable) throws IOException {
    prepare(keyedTable, onBaseTable, partitionedTable);
    UnkeyedTable operationTable = getOperationTable();
    operationTable
        .newAppend()
        .appendFile(getDataFile(1))
        .appendFile(getDataFile(2))
        .set(SnapshotSummary.SNAPSHOT_PRODUCER, CommitMetaProducer.OPTIMIZE.name())
        .commit();

    List<TableCommitMeta> tableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(1, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(0);
    Assertions.assertSame(commitMeta.getCommitMetaProducer(), CommitMetaProducer.OPTIMIZE);
    validateCommitMeta(
        commitMeta,
        DataOperations.APPEND,
        new org.apache.iceberg.DataFile[] {getDataFile(1), getDataFile(2)},
        new org.apache.iceberg.DataFile[] {});
  }

  @ParameterizedTest(name = "keyedTable = {0}, onBaseTable = {1}, partitionedTable = {2}")
  @MethodSource("parameters")
  public void testTraceFastAppend(boolean keyedTable, boolean onBaseTable, boolean partitionedTable)
      throws IOException {
    prepare(keyedTable, onBaseTable, partitionedTable);
    UnkeyedTable operationTable = getOperationTable();
    operationTable.newFastAppend().appendFile(getDataFile(1)).appendFile(getDataFile(2)).commit();

    List<TableCommitMeta> tableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(1, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(0);
    validateCommitMeta(
        commitMeta,
        DataOperations.APPEND,
        new org.apache.iceberg.DataFile[] {getDataFile(1), getDataFile(2)},
        new org.apache.iceberg.DataFile[] {});
  }

  @ParameterizedTest(name = "keyedTable = {0}, onBaseTable = {1}, partitionedTable = {2}")
  @MethodSource("parameters")
  public void testTraceAppendNoneFiles(
      boolean keyedTable, boolean onBaseTable, boolean partitionedTable) throws IOException {
    prepare(keyedTable, onBaseTable, partitionedTable);
    UnkeyedTable operationTable = getOperationTable();
    operationTable.newAppend().commit();
    List<TableCommitMeta> appendTableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(1, appendTableCommitMetas.size());
    Assertions.assertNotNull(appendTableCommitMetas.get(0).getChanges());

    Transaction overwriteTransaction = operationTable.newTransaction();
    overwriteTransaction.newOverwrite().commit();
    overwriteTransaction.commitTransaction();
    List<TableCommitMeta> overwriteTableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(2, overwriteTableCommitMetas.size());
    Assertions.assertEquals(1, overwriteTableCommitMetas.get(1).getChanges().size());

    Transaction rewriteTransaction = operationTable.newTransaction();
    rewriteTransaction.newRewrite().commit();
    rewriteTransaction.commitTransaction();
    List<TableCommitMeta> rewriteTableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(3, rewriteTableCommitMetas.size());
    Assertions.assertEquals(1, rewriteTableCommitMetas.get(2).getChanges().size());

    getMixedTable().updateSchema().commit();
    List<TableCommitMeta> updateSchemaCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(3, updateSchemaCommitMetas.size());

    getMixedTable().updateProperties().commit();
    List<TableCommitMeta> updatePropertiesCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(4, updatePropertiesCommitMetas.size());
    Assertions.assertNull(rewriteTableCommitMetas.get(3).getChanges());
  }

  @ParameterizedTest(name = "keyedTable = {0}, onBaseTable = {1}, partitionedTable = {2}")
  @MethodSource("parameters")
  public void testTraceFastAppendInTx(
      boolean keyedTable, boolean onBaseTable, boolean partitionedTable) throws IOException {
    prepare(keyedTable, onBaseTable, partitionedTable);
    UnkeyedTable operationTable = getOperationTable();
    Transaction transaction = operationTable.newTransaction();
    transaction.newFastAppend().appendFile(getDataFile(1)).appendFile(getDataFile(2)).commit();

    Assertions.assertFalse(
        getAmsHandler()
            .getTableCommitMetas()
            .containsKey(operationTable.id().buildTableIdentifier()));

    transaction.commitTransaction();
    List<TableCommitMeta> tableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(1, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(0);
    validateCommitMeta(
        commitMeta,
        DataOperations.APPEND,
        new org.apache.iceberg.DataFile[] {getDataFile(1), getDataFile(2)},
        new org.apache.iceberg.DataFile[] {});
  }

  @ParameterizedTest(name = "keyedTable = {0}, onBaseTable = {1}, partitionedTable = {2}")
  @MethodSource("parameters")
  public void testTraceFastAppendByOptimize(
      boolean keyedTable, boolean onBaseTable, boolean partitionedTable) throws IOException {
    prepare(keyedTable, onBaseTable, partitionedTable);
    UnkeyedTable operationTable = getOperationTable();
    operationTable
        .newFastAppend()
        .appendFile(getDataFile(1))
        .appendFile(getDataFile(2))
        .set(SnapshotSummary.SNAPSHOT_PRODUCER, CommitMetaProducer.OPTIMIZE.name())
        .commit();

    List<TableCommitMeta> tableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(1, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(0);
    Assertions.assertSame(commitMeta.getCommitMetaProducer(), CommitMetaProducer.OPTIMIZE);
    validateCommitMeta(
        commitMeta,
        DataOperations.APPEND,
        new org.apache.iceberg.DataFile[] {getDataFile(1), getDataFile(2)},
        new org.apache.iceberg.DataFile[] {});
  }

  @ParameterizedTest(name = "keyedTable = {0}, onBaseTable = {1}, partitionedTable = {2}")
  @MethodSource("parameters")
  public void testTraceOverwrite(boolean keyedTable, boolean onBaseTable, boolean partitionedTable)
      throws IOException {
    prepare(keyedTable, onBaseTable, partitionedTable);
    UnkeyedTable operationTable = getOperationTable();
    operationTable.newFastAppend().appendFile(getDataFile(1)).appendFile(getDataFile(2)).commit();

    operationTable
        .newOverwrite()
        .deleteFile(getDataFile(1))
        .deleteFile(getDataFile(2))
        .addFile(getDataFile(3))
        .commit();

    List<TableCommitMeta> tableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(2, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(1);
    validateCommitMeta(
        commitMeta,
        DataOperations.OVERWRITE,
        new org.apache.iceberg.DataFile[] {getDataFile(3)},
        new org.apache.iceberg.DataFile[] {getDataFile(1), getDataFile(2)});
  }

  @ParameterizedTest(name = "keyedTable = {0}, onBaseTable = {1}, partitionedTable = {2}")
  @MethodSource("parameters")
  public void testTraceOverwriteInTx(
      boolean keyedTable, boolean onBaseTable, boolean partitionedTable) throws IOException {
    prepare(keyedTable, onBaseTable, partitionedTable);
    UnkeyedTable operationTable = getOperationTable();
    operationTable.newFastAppend().appendFile(getDataFile(1)).appendFile(getDataFile(2)).commit();

    Transaction transaction = operationTable.newTransaction();
    transaction
        .newOverwrite()
        .deleteFile(getDataFile(1))
        .deleteFile(getDataFile(2))
        .addFile(getDataFile(3))
        .commit();

    Assertions.assertEquals(
        1,
        getAmsHandler()
            .getTableCommitMetas()
            .get(operationTable.id().buildTableIdentifier())
            .size());

    transaction.commitTransaction();
    List<TableCommitMeta> tableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(2, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(1);
    validateCommitMeta(
        commitMeta,
        DataOperations.OVERWRITE,
        new org.apache.iceberg.DataFile[] {getDataFile(3)},
        new org.apache.iceberg.DataFile[] {getDataFile(1), getDataFile(2)});
  }

  @ParameterizedTest(name = "keyedTable = {0}, onBaseTable = {1}, partitionedTable = {2}")
  @MethodSource("parameters")
  public void testTraceOverwriteByOptimize(
      boolean keyedTable, boolean onBaseTable, boolean partitionedTable) throws IOException {
    prepare(keyedTable, onBaseTable, partitionedTable);
    UnkeyedTable operationTable = getOperationTable();
    operationTable.newFastAppend().appendFile(getDataFile(1)).appendFile(getDataFile(2)).commit();

    operationTable
        .newOverwrite()
        .deleteFile(getDataFile(1))
        .deleteFile(getDataFile(2))
        .addFile(getDataFile(3))
        .set(SnapshotSummary.SNAPSHOT_PRODUCER, CommitMetaProducer.OPTIMIZE.name())
        .commit();

    List<TableCommitMeta> tableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(2, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(1);
    Assertions.assertSame(commitMeta.getCommitMetaProducer(), CommitMetaProducer.OPTIMIZE);
    validateCommitMeta(
        commitMeta,
        DataOperations.OVERWRITE,
        new org.apache.iceberg.DataFile[] {getDataFile(3)},
        new org.apache.iceberg.DataFile[] {getDataFile(1), getDataFile(2)});
  }

  @ParameterizedTest(name = "keyedTable = {0}, onBaseTable = {1}, partitionedTable = {2}")
  @MethodSource("parameters")
  public void testTraceRewrite(boolean keyedTable, boolean onBaseTable, boolean partitionedTable)
      throws IOException {
    prepare(keyedTable, onBaseTable, partitionedTable);
    UnkeyedTable operationTable = getOperationTable();
    operationTable.newFastAppend().appendFile(getDataFile(1)).appendFile(getDataFile(2)).commit();

    operationTable
        .newRewrite()
        .rewriteFiles(
            Sets.newHashSet(getDataFile(1), getDataFile(2)), Sets.newHashSet(getDataFile(3)))
        .commit();

    List<TableCommitMeta> tableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(2, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(1);
    validateCommitMeta(
        commitMeta,
        DataOperations.REPLACE,
        new org.apache.iceberg.DataFile[] {getDataFile(3)},
        new org.apache.iceberg.DataFile[] {getDataFile(1), getDataFile(2)});
  }

  @ParameterizedTest(name = "keyedTable = {0}, onBaseTable = {1}, partitionedTable = {2}")
  @MethodSource("parameters")
  public void testTraceRewriteInTx(
      boolean keyedTable, boolean onBaseTable, boolean partitionedTable) throws IOException {
    prepare(keyedTable, onBaseTable, partitionedTable);
    UnkeyedTable operationTable = getOperationTable();
    operationTable.newFastAppend().appendFile(getDataFile(1)).appendFile(getDataFile(2)).commit();

    Transaction transaction = operationTable.newTransaction();
    transaction
        .newRewrite()
        .rewriteFiles(
            Sets.newHashSet(getDataFile(1), getDataFile(2)), Sets.newHashSet(getDataFile(3)))
        .commit();

    Assertions.assertEquals(
        1,
        getAmsHandler()
            .getTableCommitMetas()
            .get(operationTable.id().buildTableIdentifier())
            .size());

    transaction.commitTransaction();
    List<TableCommitMeta> tableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(2, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(1);
    validateCommitMeta(
        commitMeta,
        DataOperations.REPLACE,
        new org.apache.iceberg.DataFile[] {getDataFile(3)},
        new org.apache.iceberg.DataFile[] {getDataFile(1), getDataFile(2)});
  }

  @ParameterizedTest(name = "keyedTable = {0}, onBaseTable = {1}, partitionedTable = {2}")
  @MethodSource("parameters")
  public void testTraceRewriteByOptimize(
      boolean keyedTable, boolean onBaseTable, boolean partitionedTable) throws IOException {
    prepare(keyedTable, onBaseTable, partitionedTable);
    UnkeyedTable operationTable = getOperationTable();
    operationTable.newFastAppend().appendFile(getDataFile(1)).appendFile(getDataFile(2)).commit();

    operationTable
        .newRewrite()
        .rewriteFiles(
            Sets.newHashSet(getDataFile(1), getDataFile(2)), Sets.newHashSet(getDataFile(3)))
        .set(SnapshotSummary.SNAPSHOT_PRODUCER, CommitMetaProducer.OPTIMIZE.name())
        .commit();

    List<TableCommitMeta> tableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(2, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(1);
    Assertions.assertSame(commitMeta.getCommitMetaProducer(), CommitMetaProducer.OPTIMIZE);
    validateCommitMeta(
        commitMeta,
        DataOperations.REPLACE,
        new org.apache.iceberg.DataFile[] {getDataFile(3)},
        new org.apache.iceberg.DataFile[] {getDataFile(1), getDataFile(2)});
  }

  @ParameterizedTest(name = "keyedTable = {0}, onBaseTable = {1}, partitionedTable = {2}")
  @MethodSource("parameters")
  public void testMultipleOperationInTx(
      boolean keyedTable, boolean onBaseTable, boolean partitionedTable) throws IOException {
    prepare(keyedTable, onBaseTable, partitionedTable);
    UnkeyedTable operationTable = getOperationTable();
    Transaction transaction = operationTable.newTransaction();
    transaction.newAppend().appendFile(getDataFile(1)).appendFile(getDataFile(2)).commit();

    transaction.newOverwrite().deleteFile(getDataFile(1)).addFile(getDataFile(3)).commit();

    Assertions.assertFalse(
        getAmsHandler()
            .getTableCommitMetas()
            .containsKey(operationTable.id().buildTableIdentifier()));

    transaction.commitTransaction();

    List<Snapshot> snapshots = Lists.newArrayList(operationTable.snapshots());
    Assertions.assertEquals(2, snapshots.size());
    List<TableCommitMeta> tableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(1, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(0);
    Assertions.assertEquals(2, commitMeta.getChanges().size());
    validateTableChange(
        snapshots.get(0),
        commitMeta.getChanges().get(0),
        new org.apache.iceberg.DataFile[] {getDataFile(1), getDataFile(2)},
        new org.apache.iceberg.DataFile[] {});
    validateTableChange(
        snapshots.get(1),
        commitMeta.getChanges().get(1),
        new org.apache.iceberg.DataFile[] {getDataFile(3)},
        new org.apache.iceberg.DataFile[] {getDataFile(1)});
  }

  @ParameterizedTest(name = "keyedTable = {0}, onBaseTable = {1}, partitionedTable = {2}")
  @MethodSource("parameters")
  public void testMultipleOperationInTxByOptimize(
      boolean keyedTable, boolean onBaseTable, boolean partitionedTable) throws IOException {
    prepare(keyedTable, onBaseTable, partitionedTable);
    UnkeyedTable operationTable = getOperationTable();
    Transaction transaction = operationTable.newTransaction();
    transaction
        .newAppend()
        .appendFile(getDataFile(1))
        .appendFile(getDataFile(2))
        .set(SnapshotSummary.SNAPSHOT_PRODUCER, CommitMetaProducer.OPTIMIZE.name())
        .commit();

    transaction
        .newOverwrite()
        .deleteFile(getDataFile(1))
        .addFile(getDataFile(3))
        .set(SnapshotSummary.SNAPSHOT_PRODUCER, CommitMetaProducer.OPTIMIZE.name())
        .commit();

    Assertions.assertFalse(
        getAmsHandler()
            .getTableCommitMetas()
            .containsKey(operationTable.id().buildTableIdentifier()));

    transaction.commitTransaction();

    List<Snapshot> snapshots = Lists.newArrayList(operationTable.snapshots());
    Assertions.assertEquals(2, snapshots.size());
    List<TableCommitMeta> tableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(1, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(0);
    Assertions.assertSame(commitMeta.getCommitMetaProducer(), CommitMetaProducer.OPTIMIZE);
    Assertions.assertEquals(2, commitMeta.getChanges().size());
    validateTableChange(
        snapshots.get(0),
        commitMeta.getChanges().get(0),
        new org.apache.iceberg.DataFile[] {getDataFile(1), getDataFile(2)},
        new org.apache.iceberg.DataFile[] {});
    validateTableChange(
        snapshots.get(1),
        commitMeta.getChanges().get(1),
        new org.apache.iceberg.DataFile[] {getDataFile(3)},
        new org.apache.iceberg.DataFile[] {getDataFile(1)});
  }

  @ParameterizedTest(name = "keyedTable = {0}, onBaseTable = {1}, partitionedTable = {2}")
  @MethodSource("parameters")
  public void testTracedReplacePartitions(
      boolean keyedTable, boolean onBaseTable, boolean partitionedTable) throws IOException {
    prepare(keyedTable, onBaseTable, partitionedTable);
    Assumptions.assumeTrue(isPartitionedTable());
    UnkeyedTable operationTable = getOperationTable();
    operationTable
        .newFastAppend()
        .appendFile(DataFileTestHelpers.getFile(1, "op_time_day=2022-01-01"))
        .appendFile(DataFileTestHelpers.getFile(2, "op_time_day=2022-01-01"))
        .appendFile(DataFileTestHelpers.getFile(3, "op_time_day=2022-01-02"))
        .commit();

    operationTable
        .newReplacePartitions()
        .addFile(DataFileTestHelpers.getFile(4, "op_time_day=2022-01-02"))
        .commit();

    List<TableCommitMeta> tableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(operationTable.id().buildTableIdentifier());
    Assertions.assertEquals(2, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(1);
    validateCommitMeta(
        commitMeta,
        DataOperations.OVERWRITE,
        new org.apache.iceberg.DataFile[] {
          DataFileTestHelpers.getFile(4, "op_time_day=2022-01-02")
        },
        new org.apache.iceberg.DataFile[] {
          DataFileTestHelpers.getFile(3, "op_time_day=2022-01-02")
        });
  }

  @ParameterizedTest(name = "keyedTable = {0}, onBaseTable = {1}, partitionedTable = {2}")
  @MethodSource("parameters")
  public void testTraceRemovePosDeleteInternal(
      boolean keyedTable, boolean onBaseTable, boolean partitionedTable) throws Exception {
    prepare(keyedTable, onBaseTable, partitionedTable);
    Assumptions.assumeTrue(isKeyedTable() && this.onBaseTable);
    getMixedTable().asKeyedTable().baseTable().newAppend().appendFile(getDataFile(1)).commit();

    SortedPosDeleteWriter<Record> writer =
        GenericTaskWriters.builderFor(getMixedTable().asKeyedTable())
            .withTransactionId(1L)
            .buildBasePosDeleteWriter(2, 1, getDataFile(1).partition());
    writer.delete(getDataFile(1).path(), 1);
    writer.delete(getDataFile(1).path(), 3);
    writer.delete(getDataFile(1).path(), 5);
    List<DeleteFile> result = writer.complete();
    RowDelta rowDelta = getMixedTable().asKeyedTable().baseTable().newRowDelta();
    result.forEach(rowDelta::addDeletes);
    rowDelta.commit();

    getMixedTable().asKeyedTable().baseTable().newAppend().appendFile(getDataFile(3)).commit();
    OverwriteFiles overwriteFiles = getMixedTable().asKeyedTable().baseTable().newOverwrite();
    overwriteFiles.deleteFile(getDataFile(1));
    overwriteFiles.addFile(getDataFile(2));
    overwriteFiles.commit();

    List<TableCommitMeta> tableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(getMixedTable().id().buildTableIdentifier());
    Assertions.assertEquals(4, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(tableCommitMetas.size() - 1);
    Assertions.assertEquals(1, commitMeta.getChanges().size());
    TableChange tableChange = commitMeta.getChanges().get(0);
    Assertions.assertEquals(2, tableChange.deleteFiles.size());
  }

  @ParameterizedTest(name = "keyedTable = {0}, onBaseTable = {1}, partitionedTable = {2}")
  @MethodSource("parameters")
  public void testTraceRemovePosDeleteInternalInTransaction(
      boolean keyedTable, boolean onBaseTable, boolean partitionedTable) throws Exception {
    prepare(keyedTable, onBaseTable, partitionedTable);
    Assumptions.assumeTrue(isKeyedTable() && this.onBaseTable);
    getMixedTable().asKeyedTable().baseTable().newAppend().appendFile(getDataFile(1)).commit();

    SortedPosDeleteWriter<Record> writer =
        GenericTaskWriters.builderFor(getMixedTable().asKeyedTable())
            .withTransactionId(1L)
            .buildBasePosDeleteWriter(2, 1, getDataFile(1).partition());
    writer.delete(getDataFile(1).path(), 1);
    writer.delete(getDataFile(1).path(), 3);
    writer.delete(getDataFile(1).path(), 5);
    List<DeleteFile> result = writer.complete();
    RowDelta rowDelta = getMixedTable().asKeyedTable().baseTable().newRowDelta();
    result.forEach(rowDelta::addDeletes);
    rowDelta.commit();

    getMixedTable().asKeyedTable().baseTable().newAppend().appendFile(getDataFile(3)).commit();
    Transaction transaction = getMixedTable().asKeyedTable().baseTable().newTransaction();
    OverwriteFiles overwriteFiles = transaction.newOverwrite();
    overwriteFiles.deleteFile(getDataFile(1));
    overwriteFiles.addFile(getDataFile(2));
    overwriteFiles.commit();
    DeleteFiles deleteFiles = transaction.newDelete();
    deleteFiles.deleteFile(getDataFile(3));
    deleteFiles.commit();
    transaction.commitTransaction();

    List<TableCommitMeta> tableCommitMetas =
        getAmsHandler().getTableCommitMetas().get(getMixedTable().id().buildTableIdentifier());
    Assertions.assertEquals(4, tableCommitMetas.size());
    TableCommitMeta commitMeta = tableCommitMetas.get(tableCommitMetas.size() - 1);
    Assertions.assertEquals(2, commitMeta.getChanges().size());
    TableChange tableChange = commitMeta.getChanges().get(0);
    Assertions.assertEquals(2, tableChange.deleteFiles.size());
  }

  private String getExpectedInnerTable() {
    if (onBaseTable) {
      return Constants.INNER_TABLE_BASE;
    } else {
      return Constants.INNER_TABLE_CHANGE;
    }
  }

  private void validateTableChange(
      Snapshot snapshot,
      TableChange tableChange,
      org.apache.iceberg.DataFile[] addFiles,
      org.apache.iceberg.DataFile[] deleteFiles) {
    Assertions.assertEquals(getExpectedInnerTable(), tableChange.getInnerTable());
    Assertions.assertEquals(snapshot.snapshotId(), tableChange.getSnapshotId());
    Assertions.assertEquals(
        snapshot.parentId() == null ? -1 : getOperationTable().currentSnapshot().parentId(),
        tableChange.getParentSnapshotId());

    validateDataFile(tableChange.getAddFiles(), addFiles);
    validateDataFile(tableChange.getDeleteFiles(), deleteFiles);
  }

  private void validateCommitMeta(
      TableCommitMeta commitMeta,
      String operation,
      org.apache.iceberg.DataFile[] addFiles,
      org.apache.iceberg.DataFile[] deleteFiles) {

    Assertions.assertEquals(
        getMixedTable().id().buildTableIdentifier(), commitMeta.getTableIdentifier());
    Assertions.assertEquals(operation, commitMeta.getAction());
    Assertions.assertEquals(1, commitMeta.getChanges().size());

    TableChange tableChange = commitMeta.getChanges().get(0);
    Assertions.assertEquals(getExpectedInnerTable(), tableChange.getInnerTable());
    Assertions.assertEquals(
        getOperationTable().currentSnapshot().snapshotId(), tableChange.getSnapshotId());
    Assertions.assertEquals(
        getOperationTable().currentSnapshot().parentId() == null
            ? -1
            : getOperationTable().currentSnapshot().parentId(),
        tableChange.getParentSnapshotId());

    validateDataFile(tableChange.getAddFiles(), addFiles);
    validateDataFile(tableChange.getDeleteFiles(), deleteFiles);
  }

  private void validateDataFile(
      List<DataFile> dataFiles, org.apache.iceberg.DataFile... icebergFiles) {
    Assertions.assertEquals(icebergFiles.length, dataFiles.size());
    Map<String, org.apache.iceberg.DataFile> icebergFilesMap = Maps.newHashMap();
    Arrays.stream(icebergFiles).forEach(f -> icebergFilesMap.put(f.path().toString(), f));
    for (DataFile validateFile : dataFiles) {
      org.apache.iceberg.DataFile icebergFile = icebergFilesMap.get(validateFile.getPath());
      Assertions.assertEquals(icebergFile.path(), validateFile.getPath());
      Assertions.assertEquals(icebergFile.fileSizeInBytes(), validateFile.getFileSize());
      Assertions.assertEquals(icebergFile.recordCount(), validateFile.getRecordCount());
      Assertions.assertEquals(
          onBaseTable ? DataFileType.BASE_FILE.name() : DataFileType.INSERT_FILE.name(),
          validateFile.getFileType());
      Assertions.assertEquals(0, validateFile.getIndex());
      Assertions.assertEquals(0, validateFile.getMask());
      if (isPartitionedTable()) {
        Assertions.assertEquals(getMixedTable().spec().specId(), validateFile.getSpecId());
        Assertions.assertEquals(1, validateFile.getPartitionSize());
        Assertions.assertEquals(
            getMixedTable().spec().fields().get(0).name(),
            validateFile.getPartition().get(0).getName());
        Assertions.assertEquals(
            getMixedTable().spec().partitionToPath(icebergFile.partition()),
            getMixedTable().spec().fields().get(0).name()
                + "="
                + validateFile.getPartition().get(0).getValue());
      }
    }
  }
}
