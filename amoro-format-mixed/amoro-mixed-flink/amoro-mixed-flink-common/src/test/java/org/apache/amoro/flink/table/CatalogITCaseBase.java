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

package org.apache.amoro.flink.table;

import static org.apache.flink.table.api.config.TableConfigOptions.TABLE_DYNAMIC_TABLE_OPTIONS_ENABLED;

import org.apache.amoro.MockAmoroManagementServer;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.TestAms;
import org.apache.amoro.UnifiedCatalog;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.flink.MiniClusterResource;
import org.apache.amoro.mixed.CatalogLoader;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.properties.CatalogMetaProperties;
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
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;

/**
 * JUnit 5 base class for Flink catalog integration tests that exercise the mixed-format catalog
 * lifecycle but do not need the streaming-write helpers from FlinkTestBase. Implementations of
 * FlinkTaskWriterBaseTest extend this directly so that no static {@code createKeyedTaskWriter}
 * inherited from FlinkTestBase clashes with the interface's default methods.
 */
public abstract class CatalogITCaseBase {
  private static final Logger LOG = LoggerFactory.getLogger(CatalogITCaseBase.class);

  protected static final TestAms TEST_AMS = new TestAms();

  protected static final MiniClusterWithClientResource MINI_CLUSTER_RESOURCE =
      MiniClusterResource.createWithClassloaderCheckDisabled();

  private CatalogTestHelper catalogTestHelper;
  private TableTestHelper tableTestHelper;
  private CatalogMeta catalogMeta;
  private MixedFormatCatalog mixedFormatCatalog;
  private UnifiedCatalog unifiedCatalog;
  private org.apache.iceberg.catalog.Catalog icebergCatalog;
  private MixedTable mixedTable;
  private TableMetaStore tableMetaStore;
  private File tempRoot;

  private volatile StreamTableEnvironment tEnv = null;
  private volatile StreamExecutionEnvironment env = null;

  /** No-arg constructor for parameterized children that supply helpers in the test method body. */
  public CatalogITCaseBase() {}

  public CatalogITCaseBase(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    this.catalogTestHelper = catalogTestHelper;
    this.tableTestHelper = tableTestHelper;
  }

  @BeforeAll
  public static void startBaseClassResources() throws Exception {
    TEST_AMS.before();
    MINI_CLUSTER_RESOURCE.before();
  }

  @AfterAll
  public static void stopBaseClassResources() {
    try {
      MINI_CLUSTER_RESOURCE.after();
    } finally {
      TEST_AMS.after();
    }
  }

  @BeforeEach
  public void setUpCatalogITCase() throws Exception {
    if (catalogTestHelper == null) {
      return;
    }
    initLifecycle();
  }

  @AfterEach
  public void tearDownCatalogITCase() {
    teardownLifecycle();
  }

  protected void initCatalogITCase(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws Exception {
    this.catalogTestHelper = catalogTestHelper;
    this.tableTestHelper = tableTestHelper;
    initLifecycle();
  }

  private void initLifecycle() throws Exception {
    tempRoot = Files.createTempDirectory("catalog-it").toFile();
    String baseDir = tempRoot.getPath();
    if (!SystemUtils.IS_OS_UNIX) {
      baseDir = "file:/" + baseDir.replace("\\", "/");
    }
    catalogMeta = catalogTestHelper.buildCatalogMeta(baseDir);
    catalogMeta.putToCatalogProperties(CatalogMetaProperties.AMS_URI, TEST_AMS.getServerUrl());
    getAmsHandler().createCatalog(catalogMeta);
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

  protected TableResult exec(String query, Object... args) {
    return exec(getTableEnv(), query, args);
  }

  protected static TableResult exec(TableEnvironment env, String query, Object... args) {
    return env.executeSql(String.format(query, args));
  }

  protected StreamTableEnvironment getTableEnv() {
    if (tEnv == null) {
      synchronized (this) {
        if (tEnv == null) {
          this.tEnv =
              StreamTableEnvironment.create(
                  getEnv(), EnvironmentSettings.newInstance().inStreamingMode().build());
          Configuration configuration = tEnv.getConfig().getConfiguration();
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

  protected int defaultParallelism() {
    return 1;
  }
}
