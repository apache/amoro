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

package org.apache.amoro.formats.paimon.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.formats.paimon.PaimonCatalogFactory;
import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionInput;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionTask;
import org.apache.amoro.formats.paimon.optimizing.commit.PaimonTableCommit;
import org.apache.amoro.formats.paimon.optimizing.plan.PaimonOptimizingPlanner;
import org.apache.amoro.optimizing.OptimizationContext;
import org.apache.amoro.optimizing.OptimizingPlanResult;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.optimizing.TableOptimizingCommitter;
import org.apache.amoro.optimizing.TableOptimizingPlanner;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.StagedTaskDescriptor;
import org.apache.amoro.table.TableIdentifier;
import org.apache.hadoop.conf.Configuration;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.data.BinaryString;
import org.apache.paimon.data.GenericRow;
import org.apache.paimon.options.CatalogOptions;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.table.Table;
import org.apache.paimon.table.sink.BatchTableCommit;
import org.apache.paimon.table.sink.BatchTableWrite;
import org.apache.paimon.table.sink.BatchWriteBuilder;
import org.apache.paimon.table.sink.CommitMessage;
import org.apache.paimon.types.DataTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@DisplayName("PaimonProcessFactory")
public class TestPaimonProcessFactory {

  private static PaimonTable buildAppendTable(Path warehouse, String tableName) throws Exception {
    return buildAppendTable(warehouse, tableName, Collections.emptyMap());
  }

  private static PaimonTable buildAppendTable(
      Path warehouse, String tableName, Map<String, String> options) throws Exception {
    Map<String, String> props = new HashMap<>();
    props.put(CatalogOptions.WAREHOUSE.key(), warehouse.toUri().toString());
    Catalog catalog = PaimonCatalogFactory.paimonCatalog(props, new Configuration());
    catalog.createDatabase("db1", true);
    Schema schema =
        Schema.newBuilder()
            .column("id", DataTypes.INT())
            .column("name", DataTypes.STRING())
            .option("bucket", "-1")
            .options(options)
            .build();
    Identifier id = Identifier.create("db1", tableName);
    catalog.createTable(id, schema, true);
    Table table = catalog.getTable(id);
    return new PaimonTable(TableIdentifier.of("test_catalog", "db1", tableName), table);
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

  @Test
  @DisplayName("Default (no properties) → optimizer disabled → empty supportedFormats/Actions")
  void testDisabledByDefault() {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    factory.open(null);
    assertTrue(factory.supportedFormats().isEmpty());
    assertTrue(factory.supportedActions().isEmpty());
    assertEquals(PaimonProcessFactory.PLUGIN_NAME, factory.name());
  }

  @Test
  @DisplayName("When paimon-optimizer.enabled=true → supports PAIMON format")
  void testEnabledReturnsPaimon() {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    Map<String, String> props = new HashMap<>();
    props.put(PaimonProcessFactory.OPTIMIZER_ENABLED.key(), "true");
    factory.open(props);
    assertEquals(Collections.singleton(TableFormat.PAIMON), factory.supportedFormats());
    assertTrue(factory.supportedActions().containsKey(TableFormat.PAIMON));
  }

  @Test
  @DisplayName("createPlanner rejects non-PaimonTable and accepts PaimonTable")
  void testCreatePlanner(@TempDir Path warehouse) throws Exception {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    factory.open(enabledProps());
    assertThrows(IllegalStateException.class, () -> factory.createPlanner(null, null, 1.0, 1024L));
    PaimonTable table = buildAppendTable(warehouse, "t_plan");
    TableOptimizingPlanner planner = factory.createPlanner(null, table, 1.0, 1024L);
    assertNotNull(planner);
    assertTrue(planner instanceof PaimonOptimizingPlanner);
  }

  @Test
  @DisplayName("createPlanner propagates table optimizing config to Paimon planner")
  void testCreatePlannerPropagatesOptimizingConfig(@TempDir Path warehouse) throws Exception {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    factory.open(enabledProps());
    Map<String, String> options = new HashMap<>();
    options.put("target-file-size", "1 kb");
    options.put("compaction.min.file-num", "2");
    PaimonTable table = buildAppendTable(warehouse, "t_full_config", options);
    writeRecords(
        table.originalTable(),
        Collections.singletonList(GenericRow.of(1, BinaryString.fromString("a"))));
    writeRecords(
        table.originalTable(),
        Collections.singletonList(GenericRow.of(2, BinaryString.fromString("b"))));

    TableRuntime runtime =
        runtimeWithConfig(
            new OptimizingConfig()
                .setEnabled(true)
                .setMinorLeastInterval(3600000)
                .setFullTriggerInterval(1)
                .setFullRewriteAllFiles(true)
                .setMaxTaskSize(64L * 1024 * 1024),
            0L,
            0L,
            0L);

    OptimizingPlanResult<?> result =
        factory.createPlanner(runtime, table, 1.0, 64L * 1024 * 1024).plan();

    assertEquals(OptimizingType.FULL, result.getOptimizingType());
    assertFalse(result.getTasks().isEmpty());
  }

  @Test
  @DisplayName("createPlanner falls back to OptimizationContext optimizing config")
  void testCreatePlannerUsesOptimizationContextConfigFallback(@TempDir Path warehouse)
      throws Exception {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    factory.open(enabledProps());
    Map<String, String> options = new HashMap<>();
    options.put("target-file-size", "1 kb");
    options.put("compaction.min.file-num", "2");
    PaimonTable table = buildAppendTable(warehouse, "t_context_config", options);
    writeRecords(
        table.originalTable(),
        Collections.singletonList(GenericRow.of(1, BinaryString.fromString("a"))));
    writeRecords(
        table.originalTable(),
        Collections.singletonList(GenericRow.of(2, BinaryString.fromString("b"))));

    TableRuntime runtime =
        runtimeWithOptimizationContextConfig(
            new OptimizingConfig()
                .setEnabled(true)
                .setMinorLeastInterval(3600000)
                .setFullTriggerInterval(1)
                .setFullRewriteAllFiles(true)
                .setMaxTaskSize(64L * 1024 * 1024),
            0L,
            0L,
            0L);

    OptimizingPlanResult<?> result =
        factory.createPlanner(runtime, table, 1.0, 64L * 1024 * 1024).plan();

    assertEquals(OptimizingType.FULL, result.getOptimizingType());
    assertFalse(result.getTasks().isEmpty());
  }

  @Test
  @DisplayName("createPlanner propagates last optimizing times from OptimizationContext")
  void testCreatePlannerPropagatesLastOptimizingTimes(@TempDir Path warehouse) throws Exception {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    factory.open(enabledProps());
    Map<String, String> options = new HashMap<>();
    options.put("target-file-size", "64 kb");
    options.put("compaction.small-file-ratio", "1.0");
    options.put("compaction.min.file-num", "3");
    options.put("source.split.open-file-cost", "1 b");
    PaimonTable table = buildAppendTable(warehouse, "t_last_times", options);
    writeRecords(
        table.originalTable(),
        Collections.singletonList(GenericRow.of(1, BinaryString.fromString("a"))));
    writeRecords(
        table.originalTable(),
        Collections.singletonList(GenericRow.of(2, BinaryString.fromString("b"))));

    TableRuntime runtime =
        runtimeWithConfig(
            new OptimizingConfig()
                .setEnabled(true)
                .setMinorLeastInterval(1_000_000)
                .setFullTriggerInterval(-1)
                .setFullRewriteAllFiles(false)
                .setMaxTaskSize(64L * 1024 * 1024),
            System.currentTimeMillis(),
            0L,
            0L);

    OptimizingPlanResult<?> result =
        factory.createPlanner(runtime, table, 1.0, 64L * 1024 * 1024).plan();

    assertTrue(result.getTasks().isEmpty());
  }

  @Test
  @DisplayName("createCommitter picks commitUser from a task input and returns PaimonTableCommit")
  void testCreateCommitter(@TempDir Path warehouse) throws Exception {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    factory.open(enabledProps());
    PaimonTable table = buildAppendTable(warehouse, "t_commit");
    PaimonCompactionInput input =
        new PaimonCompactionInput(table, new byte[] {1}, 2, "user-abc", "p", 0L);
    PaimonCompactionTask task = new PaimonCompactionTask(1L, "p", input, new HashMap<>());

    TableOptimizingCommitter committer =
        factory.createCommitter(
            table,
            /* targetSnapshotId */ 7L,
            /* targetChangeSnapshotId */ -1L,
            Collections.singletonList((StagedTaskDescriptor<?, ?, ?>) task),
            Collections.emptyMap(),
            Collections.emptyMap());
    assertNotNull(committer);
    assertTrue(committer instanceof PaimonTableCommit);
  }

  @Test
  @DisplayName("createCommitter fails fast when no success task carries a commitUser")
  void testCreateCommitterRejectsEmptyCommitUser(@TempDir Path warehouse) throws Exception {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    factory.open(enabledProps());
    PaimonTable table = buildAppendTable(warehouse, "t_nouser");
    assertThrows(
        IllegalStateException.class,
        () ->
            factory.createCommitter(
                table,
                1L,
                -1L,
                Collections.emptyList(),
                Collections.emptyMap(),
                Collections.emptyMap()));
  }

  @Test
  @DisplayName("SPI registration: PaimonProcessFactory is discoverable via ServiceLoader")
  void testSpiDiscovery() throws Exception {
    // Confirm the META-INF/services/org.apache.amoro.process.ProcessFactory file is on the
    // classpath and names this class.
    boolean foundInFile = false;
    Enumeration<URL> urls =
        Thread.currentThread()
            .getContextClassLoader()
            .getResources("META-INF/services/" + ProcessFactory.class.getName());
    while (urls.hasMoreElements()) {
      URL url = urls.nextElement();
      try (BufferedReader r =
          new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = r.readLine()) != null) {
          if (line.trim().equals(PaimonProcessFactory.class.getName())) {
            foundInFile = true;
          }
        }
      }
    }
    assertTrue(foundInFile, "SPI file must list PaimonProcessFactory");

    boolean foundByLoader = false;
    for (ProcessFactory f : ServiceLoader.load(ProcessFactory.class)) {
      if (f instanceof PaimonProcessFactory) {
        foundByLoader = true;
      }
    }
    assertTrue(foundByLoader, "ServiceLoader must load PaimonProcessFactory");
    assertFalse(
        Thread.currentThread()
                .getContextClassLoader()
                .getResources("META-INF/services/" + ProcessFactory.class.getName())
                .hasMoreElements()
            && !foundInFile);
  }

  @Test
  @DisplayName(
      "generateProcessId is strictly positive and monotonically non-decreasing over 1k calls")
  void testProcessIdMonotonic() {
    long prev = 0L;
    for (int i = 0; i < 1000; i++) {
      long id = PaimonProcessFactory.generateProcessId();
      assertTrue(id > 0L, "processId must be strictly positive, got " + id);
      assertTrue(
          id > prev,
          "processId must be strictly increasing across single-threaded calls: prev="
              + prev
              + " curr="
              + id);
      prev = id;
    }
  }

  @Test
  @DisplayName("generateProcessId has no collisions under 2 threads x 1k calls")
  void testProcessIdNoCollision() throws Exception {
    int threads = 2;
    int perThread = 1000;
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threads);
    List<Long> all = new CopyOnWriteArrayList<>();
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    try {
      for (int t = 0; t < threads; t++) {
        pool.submit(
            () -> {
              try {
                start.await();
                for (int i = 0; i < perThread; i++) {
                  all.add(PaimonProcessFactory.generateProcessId());
                }
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              } finally {
                done.countDown();
              }
            });
      }
      start.countDown();
      assertTrue(done.await(30, TimeUnit.SECONDS), "Threads did not finish in time");
    } finally {
      pool.shutdownNow();
    }
    assertEquals(threads * perThread, all.size(), "Unexpected number of generated ids");
    Set<Long> unique = new HashSet<>(all);
    assertEquals(
        all.size(),
        unique.size(),
        "Expected no collisions across "
            + threads
            + " threads x "
            + perThread
            + " calls, but saw "
            + (all.size() - unique.size())
            + " duplicates");
    for (Long id : all) {
      assertTrue(id > 0L, "all ids must stay strictly positive, got " + id);
    }
  }

  private static Map<String, String> enabledProps() {
    Map<String, String> props = new HashMap<>();
    props.put(PaimonProcessFactory.OPTIMIZER_ENABLED.key(), "true");
    return props;
  }

  private static TableRuntime runtimeWithConfig(
      OptimizingConfig optimizingConfig, long lastMinor, long lastMajor, long lastFull) {
    TableRuntime runtime =
        mock(TableRuntime.class, withSettings().extraInterfaces(OptimizationContext.class));
    when(runtime.getTableConfiguration())
        .thenReturn(new TableConfiguration().setOptimizingConfig(optimizingConfig));
    OptimizationContext context = (OptimizationContext) runtime;
    when(context.getLastMinorOptimizingTime()).thenReturn(lastMinor);
    when(context.getLastMajorOptimizingTime()).thenReturn(lastMajor);
    when(context.getLastFullOptimizingTime()).thenReturn(lastFull);
    return runtime;
  }

  private static TableRuntime runtimeWithOptimizationContextConfig(
      OptimizingConfig optimizingConfig, long lastMinor, long lastMajor, long lastFull) {
    TableRuntime runtime =
        mock(TableRuntime.class, withSettings().extraInterfaces(OptimizationContext.class));
    when(runtime.getTableConfiguration()).thenReturn(null);
    OptimizationContext context = (OptimizationContext) runtime;
    when(context.getOptimizingConfig()).thenReturn(optimizingConfig);
    when(context.getLastMinorOptimizingTime()).thenReturn(lastMinor);
    when(context.getLastMajorOptimizingTime()).thenReturn(lastMajor);
    when(context.getLastFullOptimizingTime()).thenReturn(lastFull);
    return runtime;
  }
}
