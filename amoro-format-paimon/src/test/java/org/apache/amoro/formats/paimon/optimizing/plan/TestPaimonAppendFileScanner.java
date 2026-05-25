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

package org.apache.amoro.formats.paimon.optimizing.plan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.formats.paimon.PaimonCatalogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.paimon.CoreOptions;
import org.apache.paimon.append.AppendCompactTask;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.data.BinaryRow;
import org.apache.paimon.data.BinaryString;
import org.apache.paimon.data.GenericRow;
import org.apache.paimon.io.DataFileMeta;
import org.apache.paimon.operation.BaseAppendFileStoreWrite;
import org.apache.paimon.options.CatalogOptions;
import org.apache.paimon.predicate.Predicate;
import org.apache.paimon.predicate.PredicateBuilder;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.table.AppendOnlyFileStoreTable;
import org.apache.paimon.table.Table;
import org.apache.paimon.table.sink.BatchTableCommit;
import org.apache.paimon.table.sink.BatchTableWrite;
import org.apache.paimon.table.sink.BatchWriteBuilder;
import org.apache.paimon.table.sink.CommitMessage;
import org.apache.paimon.table.sink.TableCommitImpl;
import org.apache.paimon.types.DataTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@DisplayName("PaimonAppendFileScanner")
class TestPaimonAppendFileScanner {

  @Test
  void returnsEmptyWhenTableHasNoSnapshot(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    AppendOnlyFileStoreTable table =
        (AppendOnlyFileStoreTable)
            createAppendOnlyTable(catalog, "t_empty", new HashMap<>(), false);

    PaimonAppendFileScanner.ScanResult result =
        new PaimonAppendFileScanner(table, context(table, 100_000), null).scan();

    assertEquals(-1L, result.snapshotId());
    assertTrue(result.files().isEmpty());
  }

  @Test
  void scansAddFilesByPartition(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Table table = createAppendOnlyTable(catalog, "t_scan", new HashMap<>(), true);
    writeRecords(
        table,
        Collections.singletonList(
            GenericRow.of(1, BinaryString.fromString("a"), BinaryString.fromString("dt=1"))));
    writeRecords(
        table,
        Collections.singletonList(
            GenericRow.of(2, BinaryString.fromString("b"), BinaryString.fromString("dt=2"))));

    AppendOnlyFileStoreTable appendTable =
        (AppendOnlyFileStoreTable) catalog.getTable(Identifier.create("db1", "t_scan"));
    PaimonAppendFileScanner.ScanResult result =
        new PaimonAppendFileScanner(appendTable, context(appendTable, 100_000), null).scan();

    assertTrue(result.snapshotId() > 0);
    assertFalse(result.files().isEmpty());
    assertEquals(2, result.files().size());
    assertEquals(2, countFiles(result.files()));
  }

  @Test
  void respectsFileNumLimit(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Table table = createAppendOnlyTable(catalog, "t_limit", new HashMap<>(), false);
    for (int i = 0; i < 5; i++) {
      writeRecords(
          table, Collections.singletonList(GenericRow.of(i, BinaryString.fromString("name-" + i))));
    }

    AppendOnlyFileStoreTable appendTable =
        (AppendOnlyFileStoreTable) catalog.getTable(Identifier.create("db1", "t_limit"));
    PaimonAppendFileScanner.ScanResult result =
        new PaimonAppendFileScanner(appendTable, context(appendTable, 2), null).scan();

    assertEquals(2, countFiles(result.files()));
  }

  @Test
  void healthyAddFilesDoNotConsumeFileNumLimit(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> options = new HashMap<>();
    options.put("target-file-size", "1 kb");
    Table table = createAppendOnlyTable(catalog, "t_candidate_limit", options, false);
    writeRecords(table, Collections.singletonList(GenericRow.of(1, largeName())));
    writeRecords(
        table, Collections.singletonList(GenericRow.of(2, BinaryString.fromString("small"))));

    AppendOnlyFileStoreTable appendTable =
        (AppendOnlyFileStoreTable) catalog.getTable(Identifier.create("db1", "t_candidate_limit"));
    PaimonAppendFileScanner.ScanResult result =
        new PaimonAppendFileScanner(appendTable, context(appendTable, 1), null).scan();

    assertEquals(1, countFiles(result.files()));
    assertTrue(
        result.files().values().stream()
            .flatMap(List::stream)
            .allMatch(PaimonFileCandidate::isProblemFile));
  }

  @Test
  void fullRewriteAllFilesScansAllFilesInProblemPartition(@TempDir Path warehouse)
      throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> options = new HashMap<>();
    options.put("target-file-size", "1 kb");
    Table table = createAppendOnlyTable(catalog, "t_full", options, false);
    writeRecords(table, Collections.singletonList(GenericRow.of(1, largeName())));
    writeRecords(table, Collections.singletonList(GenericRow.of(2, BinaryString.fromString("b"))));

    AppendOnlyFileStoreTable appendTable =
        (AppendOnlyFileStoreTable) catalog.getTable(Identifier.create("db1", "t_full"));
    PaimonPlanContext context =
        context(
            appendTable,
            1,
            new OptimizingConfig()
                .setMaxTaskSize(64L * 1024 * 1024)
                .setFullTriggerInterval(1)
                .setFullRewriteAllFiles(true),
            10_000L);

    PaimonAppendFileScanner.ScanResult result =
        new PaimonAppendFileScanner(appendTable, context, null).scan();

    assertEquals(2, countFiles(result.files()));
    assertTrue(
        result.files().values().stream()
            .flatMap(List::stream)
            .anyMatch(candidate -> !candidate.isProblemFile()));
  }

  @Test
  void scanModeAllDoesNotReturnFilesDeletedByPreviousCompact(@TempDir Path warehouse)
      throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Table table = createAppendOnlyTable(catalog, "t_compacted_scan", new HashMap<>(), false);
    for (int i = 0; i < 5; i++) {
      writeRecords(
          table, Collections.singletonList(GenericRow.of(i, BinaryString.fromString("name-" + i))));
    }

    Identifier id = Identifier.create("db1", "t_compacted_scan");
    AppendOnlyFileStoreTable appendTable = (AppendOnlyFileStoreTable) catalog.getTable(id);
    PaimonAppendFileScanner.ScanResult beforeCompact =
        new PaimonAppendFileScanner(appendTable, context(appendTable, 100_000), null).scan();
    Set<String> compactedFileNames = fileNames(beforeCompact);
    assertTrue(compactedFileNames.size() > 1);

    compactFirstPartition(appendTable, beforeCompact);

    AppendOnlyFileStoreTable afterCompact = (AppendOnlyFileStoreTable) catalog.getTable(id);
    PaimonAppendFileScanner.ScanResult afterCompactScan =
        new PaimonAppendFileScanner(afterCompact, context(afterCompact, 100_000), null).scan();
    Set<String> scannedFileNames = fileNames(afterCompactScan);

    assertTrue(
        Collections.disjoint(compactedFileNames, scannedFileNames),
        "ScanMode.ALL must merge compact DELETE entries before Amoro filters ADD candidates");
  }

  @Test
  void appliesPartitionFilter(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Table table = createAppendOnlyTable(catalog, "t_filter", new HashMap<>(), true);
    writeRecords(
        table,
        Collections.singletonList(
            GenericRow.of(1, BinaryString.fromString("a"), BinaryString.fromString("dt=1"))));
    writeRecords(
        table,
        Collections.singletonList(
            GenericRow.of(2, BinaryString.fromString("b"), BinaryString.fromString("dt=2"))));

    AppendOnlyFileStoreTable appendTable =
        (AppendOnlyFileStoreTable) catalog.getTable(Identifier.create("db1", "t_filter"));
    PredicateBuilder builder = new PredicateBuilder(appendTable.schema().logicalPartitionType());
    Predicate partitionFilter =
        builder.equal(builder.indexOf("dt"), BinaryString.fromString("dt=1"));

    PaimonAppendFileScanner.ScanResult result =
        new PaimonAppendFileScanner(appendTable, context(appendTable, 100_000), partitionFilter)
            .scan();

    assertEquals(1, result.files().size());
    assertEquals(1, countFiles(result.files()));
    BinaryRow partition = result.files().keySet().iterator().next();
    assertEquals("dt=1", partition.getString(0).toString());
  }

  @Test
  void skipsBucketedAppendTable(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> options = new HashMap<>();
    options.put("bucket", "2");
    options.put("bucket-key", "id");
    Table table = createAppendOnlyTable(catalog, "t_bucketed", options, false);
    writeRecords(table, Collections.singletonList(GenericRow.of(1, BinaryString.fromString("a"))));

    AppendOnlyFileStoreTable appendTable =
        (AppendOnlyFileStoreTable) catalog.getTable(Identifier.create("db1", "t_bucketed"));

    PaimonAppendFileScanner.ScanResult result =
        new PaimonAppendFileScanner(appendTable, context(appendTable, 100_000), null).scan();

    assertNotNull(result);
    assertTrue(result.files().isEmpty());
  }

  private static Catalog fsCatalog(Path warehouse) {
    Map<String, String> props = new HashMap<>();
    props.put(CatalogOptions.WAREHOUSE.key(), warehouse.toUri().toString());
    return PaimonCatalogFactory.paimonCatalog(props, new Configuration());
  }

  private static Table createAppendOnlyTable(
      Catalog catalog, String tableName, Map<String, String> extraOptions, boolean partitioned)
      throws Exception {
    catalog.createDatabase("db1", true);
    Schema.Builder builder =
        Schema.newBuilder()
            .column("id", DataTypes.INT())
            .column("name", DataTypes.STRING())
            .option("bucket", "-1")
            .option("target-file-size", "1 kb")
            .option("compaction.min.file-num", "2");
    if (partitioned) {
      builder.column("dt", DataTypes.STRING()).partitionKeys("dt");
    }
    for (Map.Entry<String, String> e : extraOptions.entrySet()) {
      builder.option(e.getKey(), e.getValue());
    }
    Identifier id = Identifier.create("db1", tableName);
    catalog.createTable(id, builder.build(), true);
    return catalog.getTable(id);
  }

  private static PaimonPlanContext context(AppendOnlyFileStoreTable table, int fileNumLimit) {
    return context(
        table,
        fileNumLimit,
        new OptimizingConfig().setMaxTaskSize(64L * 1024 * 1024),
        System.currentTimeMillis());
  }

  private static PaimonPlanContext context(
      AppendOnlyFileStoreTable table,
      int fileNumLimit,
      OptimizingConfig optimizingConfig,
      long planTime) {
    Map<String, String> options = new HashMap<>(table.options());
    options.put("compaction.file-num-limit", String.valueOf(fileNumLimit));
    return PaimonPlanContext.forOptions(
        CoreOptions.fromMap(options),
        optimizingConfig,
        0L,
        0L,
        0L,
        1.0,
        64L * 1024 * 1024,
        planTime);
  }

  private static void writeRecords(Table table, List<GenericRow> rowsInOneCommit) throws Exception {
    BatchWriteBuilder builder = table.newBatchWriteBuilder();
    try (BatchTableWrite write = builder.newWrite()) {
      for (GenericRow row : rowsInOneCommit) {
        write.write(row);
      }
      List<CommitMessage> messages = write.prepareCommit();
      try (BatchTableCommit commit = builder.newCommit()) {
        commit.commit(messages);
      }
    }
  }

  private static void compactFirstPartition(
      AppendOnlyFileStoreTable table, PaimonAppendFileScanner.ScanResult scanResult)
      throws Exception {
    Map.Entry<BinaryRow, List<PaimonFileCandidate>> partitionFiles =
        scanResult.files().entrySet().iterator().next();
    List<DataFileMeta> compactBefore =
        partitionFiles.getValue().stream()
            .map(PaimonFileCandidate::file)
            .collect(Collectors.toList());
    AppendCompactTask task = new AppendCompactTask(partitionFiles.getKey(), compactBefore);
    String commitUser = "test-compact-scan";
    BaseAppendFileStoreWrite write = table.store().newWrite(commitUser);
    CommitMessage message;
    try {
      message = task.doCompact(table, write);
    } finally {
      write.close();
    }
    try (TableCommitImpl commit = table.newCommit(commitUser)) {
      commit.commit(Collections.singletonList(message));
    }
  }

  private static Set<String> fileNames(PaimonAppendFileScanner.ScanResult scanResult) {
    return scanResult.files().values().stream()
        .flatMap(List::stream)
        .map(PaimonFileCandidate::fileName)
        .collect(Collectors.toSet());
  }

  private static int countFiles(Map<BinaryRow, List<PaimonFileCandidate>> files) {
    return files.values().stream().mapToInt(List::size).sum();
  }

  private static BinaryString largeName() {
    StringBuilder builder = new StringBuilder(512 * 1024);
    for (int i = 0; i < 512 * 1024; i++) {
      int value = i * 1103515245 + 12345;
      builder.append((char) (33 + Math.floorMod(value >>> 16, 94)));
    }
    return BinaryString.fromString(builder.toString());
  }
}
