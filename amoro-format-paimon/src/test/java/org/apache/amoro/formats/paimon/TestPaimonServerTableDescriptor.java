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

package org.apache.amoro.formats.paimon;

import org.apache.amoro.formats.AmoroCatalogTestHelper;
import org.apache.amoro.table.descriptor.FormatTableDescriptor;
import org.apache.amoro.table.descriptor.ServerTableMeta;
import org.apache.amoro.table.descriptor.TestServerTableDescriptor;
import org.apache.paimon.FileStore;
import org.apache.paimon.Snapshot;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.data.BinaryString;
import org.apache.paimon.data.GenericRow;
import org.apache.paimon.manifest.ManifestFileMeta;
import org.apache.paimon.schema.SchemaChange;
import org.apache.paimon.table.FileStoreTable;
import org.apache.paimon.table.Table;
import org.apache.paimon.table.sink.BatchTableCommit;
import org.apache.paimon.table.sink.BatchTableWrite;
import org.apache.paimon.table.sink.BatchWriteBuilder;
import org.apache.paimon.table.sink.CommitMessage;
import org.apache.paimon.types.DataTypes;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
public class TestPaimonServerTableDescriptor extends TestServerTableDescriptor {

  public TestPaimonServerTableDescriptor(AmoroCatalogTestHelper<?> amoroCatalogTestHelper) {
    super(amoroCatalogTestHelper);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Object[] parameters() {
    return new Object[] {
      PaimonHadoopCatalogTestHelper.defaultHelper(), PaimonHiveCatalogTestHelper.defaultHelper()
    };
  }

  @Override
  protected void tableOperationsAddColumns() {
    try {
      getCatalog()
          .alterTable(
              Identifier.create(
                  TestServerTableDescriptor.TEST_DB, TestServerTableDescriptor.TEST_TABLE),
              SchemaChange.addColumn("new_col", DataTypes.INT()),
              false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void tableOperationsRenameColumns() {
    try {
      getCatalog()
          .alterTable(
              Identifier.create(
                  TestServerTableDescriptor.TEST_DB, TestServerTableDescriptor.TEST_TABLE),
              SchemaChange.renameColumn("new_col", "renamed_col"),
              false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void tableOperationsChangeColumnType() {
    try {
      getCatalog()
          .alterTable(
              Identifier.create(
                  TestServerTableDescriptor.TEST_DB, TestServerTableDescriptor.TEST_TABLE),
              SchemaChange.updateColumnType("renamed_col", DataTypes.BIGINT()),
              false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void tableOperationsChangeColumnComment() {
    try {
      getCatalog()
          .alterTable(
              Identifier.create(
                  TestServerTableDescriptor.TEST_DB, TestServerTableDescriptor.TEST_TABLE),
              SchemaChange.updateColumnComment("renamed_col", "new comment"),
              false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void tableOperationsChangeColumnRequired() {
    try {
      getCatalog()
          .alterTable(
              Identifier.create(
                  TestServerTableDescriptor.TEST_DB, TestServerTableDescriptor.TEST_TABLE),
              SchemaChange.updateColumnNullability("renamed_col", false),
              false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void tableOperationsDropColumn() {
    try {
      getCatalog()
          .alterTable(
              Identifier.create(
                  TestServerTableDescriptor.TEST_DB, TestServerTableDescriptor.TEST_TABLE),
              SchemaChange.dropColumn("renamed_col"),
              false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected FormatTableDescriptor getTableDescriptor() {
    return new PaimonTableDescriptor();
  }

  @Test
  public void getTableDetailUsesSnapshotSummaryAndManifestMetaOnly() throws Exception {
    Identifier identifier = Identifier.create(TEST_DB, TEST_TABLE);
    Table table = getCatalog().getTable(identifier);
    writeRecords(table);

    FileStore<?> store = ((FileStoreTable) table).store();
    Snapshot snapshot = store.snapshotManager().latestSnapshot();
    long expectedRecords = snapshot.totalRecordCount() == null ? 0L : snapshot.totalRecordCount();
    List<ManifestFileMeta> manifestFileMetas =
        store.manifestListFactory().create().readDataManifests(snapshot);
    long expectedFileCount =
        manifestFileMetas.stream().mapToLong(ManifestFileMeta::numAddedFiles).sum()
            - manifestFileMetas.stream().mapToLong(ManifestFileMeta::numDeletedFiles).sum();

    ServerTableMeta tableMeta =
        getTableDescriptor().getTableDetail(getAmoroCatalog().loadTable(TEST_DB, TEST_TABLE));

    Assert.assertEquals(expectedRecords, tableMeta.getTableSummary().getRecords());
    Assert.assertEquals(expectedFileCount, tableMeta.getTableSummary().getFile());
    Assert.assertEquals(
        expectedFileCount, ((Number) tableMeta.getBaseMetrics().get("fileCount")).longValue());
    Assert.assertNull(tableMeta.getBaseMetrics().get("totalSize"));
    Assert.assertNull(tableMeta.getBaseMetrics().get("averageFileSize"));
    Assert.assertNull(tableMeta.getTableSummary().getSize());
    Assert.assertNull(tableMeta.getTableSummary().getAverageFile());
  }

  private void writeRecords(Table table) throws Exception {
    BatchWriteBuilder builder = table.newBatchWriteBuilder();
    try (BatchTableWrite write = builder.newWrite()) {
      write.write(GenericRow.of(1, BinaryString.fromString("alice"), 10), 0);
      write.write(GenericRow.of(2, BinaryString.fromString("bob"), 20), 0);
      List<CommitMessage> messages = write.prepareCommit();
      try (BatchTableCommit commit = builder.newCommit()) {
        commit.commit(messages);
      }
    }
  }

  private Catalog getCatalog() {
    return (Catalog) getOriginalCatalog();
  }
}
