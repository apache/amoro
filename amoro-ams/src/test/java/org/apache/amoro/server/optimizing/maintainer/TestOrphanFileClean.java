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

import static org.apache.amoro.server.optimizing.maintainer.IcebergTableMaintainer.DATA_FOLDER_NAME;
import static org.apache.amoro.server.optimizing.maintainer.IcebergTableMaintainer.FLINK_JOB_ID;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.server.table.TableConfigurations;
import org.apache.amoro.server.table.TableOrphanFilesCleaningMetrics;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.server.table.executor.ExecutorTestBase;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.GenericBlobMetadata;
import org.apache.iceberg.GenericStatisticsFile;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StatisticsFile;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.puffin.BlobMetadata;
import org.apache.iceberg.puffin.Puffin;
import org.apache.iceberg.puffin.PuffinWriter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class TestOrphanFileClean extends ExecutorTestBase {

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, true)},
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, false)
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(false, true)
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(false, false)
      }
    };
  }

  public TestOrphanFileClean(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Test
  public void orphanDataFileClean() throws IOException {
    if (isKeyedTable()) {
      writeAndCommitBaseAndChange(getMixedTable());
    } else {
      writeAndCommitBaseStore(getMixedTable());
    }

    UnkeyedTable baseTable =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().baseTable()
            : getMixedTable().asUnkeyedTable();
    String baseOrphanFileDir =
        baseTable.location() + File.separator + DATA_FOLDER_NAME + File.separator + "testLocation";
    String baseOrphanFilePath = baseOrphanFileDir + File.separator + "orphan.parquet";
    OutputFile baseOrphanDataFile = getMixedTable().io().newOutputFile(baseOrphanFilePath);
    baseOrphanDataFile.createOrOverwrite().close();
    Assert.assertTrue(getMixedTable().io().exists(baseOrphanFileDir));
    Assert.assertTrue(getMixedTable().io().exists(baseOrphanFilePath));

    String changeOrphanFilePath =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().changeTable().location()
                + File.separator
                + DATA_FOLDER_NAME
                + File.separator
                + "orphan.parquet"
            : "";
    if (isKeyedTable()) {
      OutputFile changeOrphanDataFile = getMixedTable().io().newOutputFile(changeOrphanFilePath);
      changeOrphanDataFile.createOrOverwrite().close();
      Assert.assertTrue(getMixedTable().io().exists(changeOrphanFilePath));
    }
    TableIdentifier tableIdentifier = getMixedTable().id();
    TableOrphanFilesCleaningMetrics orphanFilesCleaningMetrics =
        new TableOrphanFilesCleaningMetrics(
            ServerTableIdentifier.of(
                tableIdentifier.getCatalog(),
                tableIdentifier.getDatabase(),
                tableIdentifier.getTableName(),
                getTestFormat()));
    MixedTableMaintainer maintainer = new MixedTableMaintainer(getMixedTable());
    maintainer.cleanContentFiles(
        System.currentTimeMillis()
            - TableProperties.MIN_ORPHAN_FILE_EXISTING_TIME_DEFAULT * 60 * 1000,
        orphanFilesCleaningMetrics);
    maintainer.cleanMetadata(
        System.currentTimeMillis()
            - TableProperties.MIN_ORPHAN_FILE_EXISTING_TIME_DEFAULT * 60 * 1000,
        orphanFilesCleaningMetrics);

    Assert.assertTrue(getMixedTable().io().exists(baseOrphanFileDir));
    Assert.assertTrue(getMixedTable().io().exists(baseOrphanFilePath));

    if (isKeyedTable()) {
      Assert.assertTrue(getMixedTable().io().exists(changeOrphanFilePath));
    }

    maintainer.cleanContentFiles(System.currentTimeMillis(), orphanFilesCleaningMetrics);
    maintainer.cleanMetadata(System.currentTimeMillis(), orphanFilesCleaningMetrics);

    Assert.assertFalse(getMixedTable().io().exists(baseOrphanFileDir));
    Assert.assertFalse(getMixedTable().io().exists(baseOrphanFilePath));
    baseTable
        .newScan()
        .planFiles()
        .forEach(
            task -> Assert.assertTrue(getMixedTable().io().exists(task.file().path().toString())));
    if (isKeyedTable()) {
      Assert.assertFalse(getMixedTable().io().exists(changeOrphanFilePath));
      getMixedTable()
          .asKeyedTable()
          .changeTable()
          .newScan()
          .planFiles()
          .forEach(
              task ->
                  Assert.assertTrue(getMixedTable().io().exists(task.file().path().toString())));
    }
  }

  @Test
  public void orphanMetadataFileClean() throws IOException {
    if (isKeyedTable()) {
      writeAndCommitBaseAndChange(getMixedTable());
    } else {
      writeAndCommitBaseStore(getMixedTable());
    }

    UnkeyedTable baseTable =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().baseTable()
            : getMixedTable().asUnkeyedTable();
    String baseOrphanFilePath =
        baseTable.location() + File.separator + "metadata" + File.separator + "orphan.avro";

    String changeOrphanFilePath =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().changeTable().location()
                + File.separator
                + "metadata"
                + File.separator
                + "orphan.avro"
            : "";

    OutputFile baseOrphanDataFile = getMixedTable().io().newOutputFile(baseOrphanFilePath);
    baseOrphanDataFile.createOrOverwrite().close();
    Assert.assertTrue(getMixedTable().io().exists(baseOrphanFilePath));

    String changeInvalidMetadataJson =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().changeTable().location()
                + File.separator
                + "metadata"
                + File.separator
                + "v0.metadata.json"
            : "";
    if (isKeyedTable()) {
      OutputFile changeOrphanDataFile = getMixedTable().io().newOutputFile(changeOrphanFilePath);
      changeOrphanDataFile.createOrOverwrite().close();
      getMixedTable().io().newOutputFile(changeInvalidMetadataJson).createOrOverwrite().close();
      Assert.assertTrue(getMixedTable().io().exists(changeOrphanFilePath));
      Assert.assertTrue(getMixedTable().io().exists(changeInvalidMetadataJson));
    }

    MixedTableMaintainer maintainer = new MixedTableMaintainer(getMixedTable());
    TableIdentifier tableIdentifier = getMixedTable().id();
    TableOrphanFilesCleaningMetrics orphanFilesCleaningMetrics =
        new TableOrphanFilesCleaningMetrics(
            ServerTableIdentifier.of(
                tableIdentifier.getCatalog(),
                tableIdentifier.getDatabase(),
                tableIdentifier.getTableName(),
                getTestFormat()));
    maintainer.cleanMetadata(System.currentTimeMillis(), orphanFilesCleaningMetrics);

    Assert.assertFalse(getMixedTable().io().exists(baseOrphanFilePath));
    if (isKeyedTable()) {
      Assert.assertFalse(getMixedTable().io().exists(changeOrphanFilePath));
      Assert.assertFalse(getMixedTable().io().exists(changeInvalidMetadataJson));
    }
    ExecutorTestBase.assertMetadataExists(getMixedTable());
  }

  @Test
  public void notDeleteFlinkTemporaryFile() throws IOException {
    if (isKeyedTable()) {
      writeAndCommitBaseAndChange(getMixedTable());
    } else {
      writeAndCommitBaseStore(getMixedTable());
    }
    String flinkJobId = "flinkJobTest";
    String fakeFlinkJobId = "fakeFlinkJobTest";

    UnkeyedTable baseTable =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().baseTable()
            : getMixedTable().asUnkeyedTable();
    String baseOrphanFilePath =
        baseTable.location()
            + File.separator
            + "metadata"
            + File.separator
            + flinkJobId
            + "orphan.avro";

    String changeOrphanFilePath =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().changeTable().location()
                + File.separator
                + "metadata"
                + File.separator
                + flinkJobId
                + "orphan.avro"
            : "";
    String fakeChangeOrphanFilePath =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().changeTable().location()
                + File.separator
                + "metadata"
                + File.separator
                + fakeFlinkJobId
                + "orphan.avro"
            : "";
    String changeInvalidMetadataJson =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().changeTable().location()
                + File.separator
                + "metadata"
                + File.separator
                + "v0.metadata.json"
            : "";

    OutputFile baseOrphanDataFile = getMixedTable().io().newOutputFile(baseOrphanFilePath);
    baseOrphanDataFile.createOrOverwrite().close();
    Assert.assertTrue(getMixedTable().io().exists(baseOrphanFilePath));

    if (isKeyedTable()) {
      OutputFile changeOrphanDataFile = getMixedTable().io().newOutputFile(changeOrphanFilePath);
      changeOrphanDataFile.createOrOverwrite().close();
      OutputFile fakeChangeOrphanDataFile =
          getMixedTable().io().newOutputFile(fakeChangeOrphanFilePath);
      fakeChangeOrphanDataFile.createOrOverwrite().close();

      getMixedTable().io().newOutputFile(changeInvalidMetadataJson).createOrOverwrite().close();
      AppendFiles appendFiles = getMixedTable().asKeyedTable().changeTable().newAppend();
      appendFiles.set(FLINK_JOB_ID, fakeFlinkJobId);
      appendFiles.commit();
      // set flink.job-id to change table
      AppendFiles appendFiles2 = getMixedTable().asKeyedTable().changeTable().newAppend();
      appendFiles2.set(FLINK_JOB_ID, flinkJobId);
      appendFiles2.commit();

      Assert.assertTrue(getMixedTable().io().exists(changeOrphanFilePath));
      Assert.assertTrue(getMixedTable().io().exists(fakeChangeOrphanFilePath));
      Assert.assertTrue(getMixedTable().io().exists(changeInvalidMetadataJson));
    }

    MixedTableMaintainer tableMaintainer = new MixedTableMaintainer(getMixedTable());
    TableIdentifier tableIdentifier = getMixedTable().id();
    TableOrphanFilesCleaningMetrics orphanFilesCleaningMetrics =
        new TableOrphanFilesCleaningMetrics(
            ServerTableIdentifier.of(
                tableIdentifier.getCatalog(),
                tableIdentifier.getDatabase(),
                tableIdentifier.getTableName(),
                getTestFormat()));

    tableMaintainer.cleanMetadata(System.currentTimeMillis(), orphanFilesCleaningMetrics);
    Assert.assertFalse(getMixedTable().io().exists(baseOrphanFilePath));
    if (isKeyedTable()) {
      // files whose file name starts with flink.job-id should not be deleted
      Assert.assertTrue(getMixedTable().io().exists(changeOrphanFilePath));
      Assert.assertFalse(getMixedTable().io().exists(fakeChangeOrphanFilePath));
      Assert.assertFalse(getMixedTable().io().exists(changeInvalidMetadataJson));
    }

    ExecutorTestBase.assertMetadataExists(getMixedTable());
  }

  @Test
  public void notDeleteStatisticsFile() {
    UnkeyedTable unkeyedTable;
    if (isKeyedTable()) {
      unkeyedTable = getMixedTable().asKeyedTable().baseTable();
    } else {
      unkeyedTable = getMixedTable().asUnkeyedTable();
    }
    StatisticsFile file1 =
        commitStatisticsFile(unkeyedTable, unkeyedTable.location() + "/metadata/test1.puffin");
    StatisticsFile file2 =
        commitStatisticsFile(unkeyedTable, unkeyedTable.location() + "/data/test2.puffin");
    StatisticsFile file3 =
        commitStatisticsFile(unkeyedTable, unkeyedTable.location() + "/data/puffin/test3.puffin");

    TableIdentifier tableIdentifier = getMixedTable().id();
    TableOrphanFilesCleaningMetrics orphanFilesCleaningMetrics =
        new TableOrphanFilesCleaningMetrics(
            ServerTableIdentifier.of(
                tableIdentifier.getCatalog(),
                tableIdentifier.getDatabase(),
                tableIdentifier.getTableName(),
                getTestFormat()));

    Assert.assertTrue(unkeyedTable.io().exists(file1.path()));
    Assert.assertTrue(unkeyedTable.io().exists(file2.path()));
    Assert.assertTrue(unkeyedTable.io().exists(file3.path()));
    new MixedTableMaintainer(getMixedTable())
        .cleanContentFiles(System.currentTimeMillis() + 1, orphanFilesCleaningMetrics);
    new MixedTableMaintainer(getMixedTable())
        .cleanMetadata(System.currentTimeMillis() + 1, orphanFilesCleaningMetrics);
    Assert.assertTrue(unkeyedTable.io().exists(file1.path()));
    Assert.assertTrue(unkeyedTable.io().exists(file2.path()));
    Assert.assertTrue(unkeyedTable.io().exists(file3.path()));
  }

  @Test
  public void testGcDisabled() throws IOException {
    if (isKeyedTable()) {
      writeAndCommitBaseAndChange(getMixedTable());
    } else {
      writeAndCommitBaseStore(getMixedTable());
    }

    UnkeyedTable baseTable =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().baseTable()
            : getMixedTable().asUnkeyedTable();
    baseTable
        .updateProperties()
        .set(TableProperties.ENABLE_ORPHAN_CLEAN, "true")
        .set(TableProperties.MIN_ORPHAN_FILE_EXISTING_TIME, "0")
        .set("gc.enabled", "false")
        .commit();

    String baseOrphanFileDir =
        baseTable.location() + File.separator + DATA_FOLDER_NAME + File.separator + "testLocation";
    String baseOrphanFilePath = baseOrphanFileDir + File.separator + "orphan.parquet";
    OutputFile baseOrphanDataFile = getMixedTable().io().newOutputFile(baseOrphanFilePath);
    baseOrphanDataFile.createOrOverwrite().close();
    Assert.assertTrue(getMixedTable().io().exists(baseOrphanFileDir));
    Assert.assertTrue(getMixedTable().io().exists(baseOrphanFilePath));

    String changeOrphanFilePath =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().changeTable().location()
                + File.separator
                + DATA_FOLDER_NAME
                + File.separator
                + "orphan.parquet"
            : "";
    if (isKeyedTable()) {
      OutputFile changeOrphanDataFile = getMixedTable().io().newOutputFile(changeOrphanFilePath);
      changeOrphanDataFile.createOrOverwrite().close();
      Assert.assertTrue(getMixedTable().io().exists(changeOrphanFilePath));
    }

    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Mockito.when(tableRuntime.getTableIdentifier())
        .thenReturn(ServerTableIdentifier.of(baseTable.id(), getTestFormat()));
    Mockito.when(tableRuntime.getTableConfiguration())
        .thenReturn(TableConfigurations.parseTableConfig(baseTable.properties()));

    Mockito.when(tableRuntime.getOrphanFilesCleaningMetrics())
        .thenReturn(
            new TableOrphanFilesCleaningMetrics(
                ServerTableIdentifier.of(baseTable.id(), getTestFormat())));

    MixedTableMaintainer maintainer = new MixedTableMaintainer(getMixedTable());
    maintainer.cleanOrphanFiles(tableRuntime);

    Assert.assertTrue(getMixedTable().io().exists(baseOrphanFileDir));
    Assert.assertTrue(getMixedTable().io().exists(baseOrphanFilePath));

    baseTable.updateProperties().set("gc.enabled", "true").commit();
    Mockito.when(tableRuntime.getTableConfiguration())
        .thenReturn(TableConfigurations.parseTableConfig((baseTable.properties())));
    maintainer.cleanOrphanFiles(tableRuntime);

    Assert.assertFalse(getMixedTable().io().exists(baseOrphanFileDir));
    Assert.assertFalse(getMixedTable().io().exists(baseOrphanFilePath));
  }

  private StatisticsFile commitStatisticsFile(UnkeyedTable table, String fileLocation) {
    table.newAppend().commit();
    Snapshot snapshot = table.currentSnapshot();
    OutputFile outputFile = table.io().newOutputFile(fileLocation);
    List<BlobMetadata> blobMetadata;
    long fileSize;
    long footerSize;
    try (PuffinWriter puffinWriter = Puffin.write(outputFile).build()) {
      puffinWriter.finish();
      blobMetadata = puffinWriter.writtenBlobsMetadata();
      fileSize = puffinWriter.fileSize();
      footerSize = puffinWriter.footerSize();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    List<org.apache.iceberg.BlobMetadata> collect =
        blobMetadata.stream().map(GenericBlobMetadata::from).collect(Collectors.toList());
    GenericStatisticsFile statisticsFile =
        new GenericStatisticsFile(
            snapshot.snapshotId(), outputFile.location(), fileSize, footerSize, collect);
    table.updateStatistics().setStatistics(snapshot.snapshotId(), statisticsFile).commit();

    return statisticsFile;
  }
}
