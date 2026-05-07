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

package org.apache.amoro.flink;

import static org.apache.amoro.flink.catalog.factories.CatalogFactoryOptions.MIXED_ICEBERG_IDENTIFIER;
import static org.apache.amoro.flink.kafka.testutils.KafkaContainerTest.KAFKA_CONTAINER;
import static org.apache.flink.table.api.config.TableConfigOptions.TABLE_DYNAMIC_TABLE_OPTIONS_ENABLED;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.MockAmoroManagementServer;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.TestAms;
import org.apache.amoro.UnifiedCatalog;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.flink.catalog.factories.CatalogFactoryOptions;
import org.apache.amoro.flink.write.MixedFormatRowDataTaskWriterFactory;
import org.apache.amoro.io.reader.GenericKeyedDataReader;
import org.apache.amoro.mixed.CatalogLoader;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.scan.CombinedScanTask;
import org.apache.amoro.scan.KeyedTableScanTask;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableSet;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableBuilder;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.CatalogUtil;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.TimestampData;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;
import org.apache.flink.util.CloseableIterator;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * JUnit 5 base class for Flink integration tests in this module.
 *
 * <p>This class intentionally no longer extends {@code TableTestBase}/{@code CatalogTestBase}
 * (which remain on JUnit 4 until the closing PR of the umbrella migration). The catalog/table
 * lifecycle is re-implemented here against the same {@link CatalogTestHelper}/{@link
 * TableTestHelper} contracts so that children can stay clean Jupiter classes; for parameterized
 * children that cannot pass helpers through a constructor, call {@link
 * #initFlinkTestBase(CatalogTestHelper, TableTestHelper)} from the {@code @ParameterizedTest}
 * method body.
 */
public class FlinkTestBase {
  private static final Logger LOG = LoggerFactory.getLogger(FlinkTestBase.class);

  protected static final TestAms TEST_AMS = new TestAms();

  protected static final MiniClusterWithClientResource MINI_CLUSTER_RESOURCE =
      MiniClusterResource.createWithClassloaderCheckDisabled();

  public static String metastoreUri;

  protected static final int KAFKA_PARTITION_NUMS = 1;

  private volatile StreamTableEnvironment tEnv = null;
  protected Map<String, String> props;
  private volatile StreamExecutionEnvironment env = null;
  public static final Schema TABLE_SCHEMA = BasicTableTestHelper.TABLE_SCHEMA;
  public static final TableSchema FLINK_SCHEMA =
      TableSchema.builder()
          .field("id", DataTypes.INT())
          .field("name", DataTypes.STRING())
          .field("ts", DataTypes.BIGINT())
          .field("op_time", DataTypes.TIMESTAMP())
          .build();
  public static final RowType FLINK_ROW_TYPE =
      (RowType) FLINK_SCHEMA.toRowDataType().getLogicalType();

  public static InternalCatalogBuilder catalogBuilder;

  private CatalogTestHelper catalogTestHelper;
  private TableTestHelper tableTestHelper;
  private CatalogMeta catalogMeta;
  private MixedFormatCatalog mixedFormatCatalog;
  private UnifiedCatalog unifiedCatalog;
  private org.apache.iceberg.catalog.Catalog icebergCatalog;
  private MixedTable mixedTable;
  private TableMetaStore tableMetaStore;
  private File tempRoot;

  /**
   * No-arg constructor for parameterized children that pass helpers via {@link #initFlinkTestBase}.
   */
  public FlinkTestBase() {}

  public FlinkTestBase(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    this.catalogTestHelper = catalogTestHelper;
    this.tableTestHelper = tableTestHelper;
  }

  @BeforeAll
  public static void startFlinkBaseClassResources() throws Exception {
    TEST_AMS.before();
    MINI_CLUSTER_RESOURCE.before();
  }

  @AfterAll
  public static void stopFlinkBaseClassResources() {
    try {
      MINI_CLUSTER_RESOURCE.after();
    } finally {
      TEST_AMS.after();
    }
  }

  @BeforeEach
  public void setUpFlinkTestBaseLifecycle() throws Exception {
    if (catalogTestHelper == null) {
      // Parameterized child: the @ParameterizedTest body must call initFlinkTestBase(...).
      return;
    }
    initLifecycle();
  }

  @AfterEach
  public void tearDownFlinkTestBaseLifecycle() {
    teardownLifecycle();
  }

  /**
   * Initializer used by {@code @ParameterizedTest} children. Sets the helpers (since the parameters
   * are received by the test method, not the constructor) and runs the catalog/table setup.
   */
  protected void initFlinkTestBase(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws Exception {
    this.catalogTestHelper = catalogTestHelper;
    this.tableTestHelper = tableTestHelper;
    initLifecycle();
  }

  private void initLifecycle() throws Exception {
    tempRoot = Files.createTempDirectory("flink-test-base").toFile();
    String baseDir = tempRoot.getPath();
    if (!SystemUtils.IS_OS_UNIX) {
      baseDir = "file:/" + baseDir.replace("\\", "/");
    }
    catalogMeta = catalogTestHelper.buildCatalogMeta(baseDir);
    catalogMeta.putToCatalogProperties(CatalogMetaProperties.AMS_URI, TEST_AMS.getServerUrl());
    getAmsHandler().createCatalog(catalogMeta);
    metastoreUri = getCatalogUri();
    catalogBuilder = InternalCatalogBuilder.builder().amsUri(metastoreUri);
    if (tableTestHelper != null) {
      createTestTable();
    }
  }

  private void teardownLifecycle() {
    if (tableTestHelper != null && unifiedCatalog != null) {
      try {
        unifiedCatalog.dropTable(
            tableTestHelper.id().getDatabase(), tableTestHelper.id().getTableName(), true);
      } catch (Exception e) {
        LOG.warn("dropTable failed", e);
      }
      try {
        unifiedCatalog.dropDatabase(TableTestHelper.TEST_DB_NAME);
      } catch (Exception e) {
        // ignore
      }
    }
    if (catalogMeta != null) {
      try {
        getAmsHandler().dropCatalog(catalogMeta.getCatalogName());
      } catch (Exception e) {
        LOG.warn("dropCatalog failed", e);
      }
    }
    if (tempRoot != null) {
      try {
        FileUtils.deleteDirectory(tempRoot);
      } catch (Exception e) {
        LOG.warn("Failed to clean temp directory {}", tempRoot, e);
      }
    }
    catalogMeta = null;
    mixedFormatCatalog = null;
    unifiedCatalog = null;
    icebergCatalog = null;
    mixedTable = null;
    tableMetaStore = null;
    tempRoot = null;
  }

  private void createTestTable() {
    this.tableMetaStore = CatalogUtil.buildMetaStore(getCatalogMeta());
    getUnifiedCatalog().createDatabase(TableTestHelper.TEST_DB_NAME);
    TableFormat format = getTestFormat();
    if (format.in(TableFormat.MIXED_HIVE, TableFormat.MIXED_ICEBERG)) {
      createMixedFormatTable();
    } else if (TableFormat.ICEBERG.equals(format)) {
      createIcebergFormatTable();
    }
  }

  private void createMixedFormatTable() {
    TableBuilder tableBuilder =
        getMixedFormatCatalog()
            .newTableBuilder(TableTestHelper.TEST_TABLE_ID, tableTestHelper.tableSchema());
    tableBuilder.withProperties(tableTestHelper.tableProperties());
    if (isKeyedTable()) {
      tableBuilder.withPrimaryKeySpec(tableTestHelper.primaryKeySpec());
    }
    if (isPartitionedTable()) {
      tableBuilder.withPartitionSpec(tableTestHelper.partitionSpec());
    }
    mixedTable = tableBuilder.create();
  }

  private void createIcebergFormatTable() {
    getIcebergCatalog()
        .createTable(
            org.apache.iceberg.catalog.TableIdentifier.of(
                TableTestHelper.TEST_DB_NAME, TableTestHelper.TEST_TABLE_NAME),
            tableTestHelper.tableSchema(),
            tableTestHelper.partitionSpec(),
            tableTestHelper.tableProperties());
    mixedTable =
        (MixedTable)
            getUnifiedCatalog()
                .loadTable(TableTestHelper.TEST_DB_NAME, TableTestHelper.TEST_TABLE_NAME)
                .originalTable();
  }

  public static MockAmoroManagementServer.AmsHandler getAmsHandler() {
    return TEST_AMS.getAmsHandler();
  }

  protected MixedFormatCatalog getMixedFormatCatalog() {
    if (mixedFormatCatalog == null) {
      mixedFormatCatalog = CatalogLoader.load(getCatalogUri());
    }
    return mixedFormatCatalog;
  }

  protected void refreshMixedFormatCatalog() {
    this.mixedFormatCatalog = CatalogLoader.load(getCatalogUri());
  }

  protected String getCatalogUri() {
    return TEST_AMS.getServerUrl() + "/" + catalogMeta.getCatalogName();
  }

  protected CatalogMeta getCatalogMeta() {
    return catalogMeta;
  }

  protected TableFormat getTestFormat() {
    return catalogTestHelper.tableFormat();
  }

  protected org.apache.iceberg.catalog.Catalog getIcebergCatalog() {
    if (icebergCatalog == null) {
      icebergCatalog = catalogTestHelper.buildIcebergCatalog(catalogMeta);
    }
    return icebergCatalog;
  }

  protected UnifiedCatalog getUnifiedCatalog() {
    if (unifiedCatalog == null) {
      unifiedCatalog = catalogTestHelper.buildUnifiedCatalog(catalogMeta);
    }
    return unifiedCatalog;
  }

  protected MixedTable getMixedTable() {
    return mixedTable;
  }

  protected UnkeyedTable getBaseStore() {
    return MixedTableUtil.baseStore(mixedTable);
  }

  protected TableMetaStore getTableMetaStore() {
    return this.tableMetaStore;
  }

  protected boolean isKeyedTable() {
    return tableTestHelper.primaryKeySpec() != null
        && tableTestHelper.primaryKeySpec().primaryKeyExisted();
  }

  protected boolean isPartitionedTable() {
    return tableTestHelper.partitionSpec() != null
        && tableTestHelper.partitionSpec().isPartitioned();
  }

  protected TableTestHelper tableTestHelper() {
    return tableTestHelper;
  }

  protected CatalogTestHelper catalogTestHelper() {
    return catalogTestHelper;
  }

  public void config() {
    props = Maps.newHashMap();
    props.put("type", MIXED_ICEBERG_IDENTIFIER);
    props.put(CatalogFactoryOptions.AMS_URI.key(), metastoreUri);
  }

  protected int defaultParallelism() {
    return 1;
  }

  public static void prepare() throws Exception {
    KAFKA_CONTAINER.start();
  }

  public static void shutdown() throws Exception {
    KAFKA_CONTAINER.close();
  }

  protected StreamTableEnvironment getTableEnv() {
    if (tEnv == null) {
      synchronized (this) {
        if (tEnv == null) {
          this.tEnv =
              StreamTableEnvironment.create(
                  getEnv(), EnvironmentSettings.newInstance().inStreamingMode().build());
          Configuration configuration = tEnv.getConfig().getConfiguration();
          // set low-level key-value options
          configuration.setString(TABLE_DYNAMIC_TABLE_OPTIONS_ENABLED.key(), "true");
        }
      }
    }
    return tEnv;
  }

  protected StreamExecutionEnvironment getEnv() {
    if (env == null) {
      synchronized (this) {
        if (env == null) {
          StateBackend backend =
              new FsStateBackend(
                  "file:///" + System.getProperty("java.io.tmpdir") + "/flink/backend");
          env =
              StreamExecutionEnvironment.getExecutionEnvironment(
                  MiniClusterResource.DISABLE_CLASSLOADER_CHECK_CONFIG);
          env.setParallelism(defaultParallelism());
          env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
          env.getCheckpointConfig().setCheckpointInterval(300);
          env.getCheckpointConfig()
              .enableExternalizedCheckpoints(
                  CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
          env.setStateBackend(backend);
          env.setRestartStrategy(RestartStrategies.noRestart());
        }
      }
    }
    return env;
  }

  protected List<Row> sql(String query, Object... args) {
    TableResult tableResult = getTableEnv().executeSql(String.format(query, args));
    tableResult
        .getJobClient()
        .ifPresent(
            c -> {
              try {
                c.getJobExecutionResult().get();
              } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
              }
            });
    try (CloseableIterator<Row> iter = tableResult.collect()) {
      List<Row> results = Lists.newArrayList(iter);
      return results;
    } catch (Exception e) {
      LOG.warn("Failed to collect table result", e);
      return null;
    }
  }

  protected TableResult exec(String query, Object... args) {
    return exec(getTableEnv(), query, args);
  }

  protected static TableResult exec(TableEnvironment env, String query, Object... args) {
    return env.executeSql(String.format(query, args));
  }

  protected Set<Row> sqlSet(String query, Object... args) {
    return new HashSet<>(sql(query, args));
  }

  public static List<Record> read(KeyedTable table) {
    CloseableIterable<CombinedScanTask> combinedScanTasks = table.newScan().planTasks();
    Schema schema = table.schema();
    GenericKeyedDataReader genericKeyedDataReader =
        new GenericKeyedDataReader(
            table.io(),
            schema,
            schema,
            table.primaryKeySpec(),
            null,
            true,
            IdentityPartitionConverters::convertConstant);
    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    for (CombinedScanTask combinedScanTask : combinedScanTasks) {
      for (KeyedTableScanTask keyedTableScanTask : combinedScanTask.tasks()) {
        builder.addAll(genericKeyedDataReader.readData(keyedTableScanTask));
      }
    }
    return builder.build();
  }

  public static Set<Record> toRecords(Collection<Row> rows) {
    GenericRecord record = GenericRecord.create(TABLE_SCHEMA);
    ImmutableSet.Builder<Record> b = ImmutableSet.builder();
    rows.forEach(
        r ->
            b.add(
                record.copy(
                    ImmutableMap.of(
                        "id",
                        r.getField(0),
                        "name",
                        r.getField(1),
                        "ts",
                        r.getField(2),
                        "op_time",
                        r.getField(3)))));
    return b.build();
  }

  public static String toWithClause(Map<String, String> props) {
    StringBuilder builder = new StringBuilder();
    builder.append("(");
    int propCount = 0;
    for (Map.Entry<String, String> entry : props.entrySet()) {
      if (propCount > 0) {
        builder.append(",");
      }
      builder
          .append("'")
          .append(entry.getKey())
          .append("'")
          .append("=")
          .append("'")
          .append(entry.getValue())
          .append("'");
      propCount++;
    }
    builder.append(")");
    return builder.toString();
  }

  protected static RowData createRowData(
      Integer id, String name, String dateTime, RowKind rowKind) {
    return GenericRowData.ofKind(
        rowKind,
        id,
        StringData.fromString(name),
        LocalDateTime.parse(dateTime).toInstant(ZoneOffset.UTC).toEpochMilli(),
        TimestampData.fromLocalDateTime(LocalDateTime.parse(dateTime)));
  }

  protected static RowData createRowData(RowKind rowKind, Object... objects) {
    return GenericRowData.ofKind(
        rowKind,
        objects[0],
        StringData.fromString((String) objects[1]),
        objects[2],
        TimestampData.fromLocalDateTime((LocalDateTime) objects[3]));
  }

  protected static RowData createRowData(Integer id, String name, String dateTime) {
    return createRowData(id, name, dateTime, RowKind.INSERT);
  }

  protected static void commit(KeyedTable keyedTable, WriteResult result, boolean base) {
    if (base) {
      AppendFiles baseAppend = keyedTable.baseTable().newAppend();
      Arrays.stream(result.dataFiles()).forEach(baseAppend::appendFile);
      baseAppend.commit();
    } else {
      AppendFiles changeAppend = keyedTable.changeTable().newAppend();
      Arrays.stream(result.dataFiles()).forEach(changeAppend::appendFile);
      changeAppend.commit();
    }
  }

  /**
   * 3-arg variant of the keyed task writer factory with a default mask of 3. The 4-arg variant
   * lives under a different name ({@link #createKeyedTaskWriterWithMask}) to avoid the inherited
   * static signature clashing with the {@code FlinkTaskWriterBaseTest} interface's default method
   * in classes that {@code implements} that interface.
   */
  protected static TaskWriter<RowData> createKeyedTaskWriter(
      KeyedTable keyedTable, RowType rowType, boolean base) {
    return createKeyedTaskWriterWithMask(keyedTable, rowType, base, 3);
  }

  protected static TaskWriter<RowData> createKeyedTaskWriterWithMask(
      KeyedTable keyedTable, RowType rowType, boolean base, long mask) {
    MixedFormatRowDataTaskWriterFactory taskWriterFactory =
        new MixedFormatRowDataTaskWriterFactory(keyedTable, rowType, base);
    taskWriterFactory.setMask(mask);
    taskWriterFactory.initialize(0, 0);
    return taskWriterFactory.create();
  }
}
