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

import org.apache.amoro.AmoroCatalog;
import org.apache.amoro.TestAms;
import org.apache.amoro.flink.MiniClusterResource;
import org.apache.amoro.formats.AmoroCatalogTestHelper;
import org.apache.amoro.hive.TestHMS;
import org.apache.commons.io.FileUtils;
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
 * JUnit 5 base class for Flink unified-catalog integration tests.
 *
 * <p>This class no longer extends the still-JUnit-4 {@code AmoroCatalogTestBase}; the catalog
 * lifecycle is re-implemented here against the {@link AmoroCatalogTestHelper} contract so children
 * can be clean Jupiter classes. Parameterized children construct without arguments and call {@link
 * #initAmoroCatalog(AmoroCatalogTestHelper)} from the {@code @ParameterizedTest} method body.
 */
public class AmoroCatalogITCaseBase {
  private static final Logger LOG = LoggerFactory.getLogger(AmoroCatalogITCaseBase.class);

  protected static final TestHMS TEST_HMS = new TestHMS();
  public static final String TEST_DB_NAME = "test_db";
  public static final String TEST_TABLE_NAME = "test_table";

  protected static final MiniClusterWithClientResource MINI_CLUSTER_RESOURCE =
      MiniClusterResource.createWithClassloaderCheckDisabled();

  protected static final TestAms TEST_AMS = new TestAms();

  protected AmoroCatalogTestHelper<?> catalogTestHelper;
  protected AmoroCatalog amoroCatalog;
  protected Object originalCatalog;

  private File tempRoot;

  private volatile StreamTableEnvironment tEnv = null;
  private volatile StreamExecutionEnvironment env = null;

  /** No-arg constructor for parameterized children. */
  public AmoroCatalogITCaseBase() {}

  public AmoroCatalogITCaseBase(AmoroCatalogTestHelper<?> catalogTestHelper) {
    this.catalogTestHelper = catalogTestHelper;
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
  public void setUpAmoroCatalog() throws Exception {
    if (catalogTestHelper == null) {
      // Parameterized child: must call initAmoroCatalog(...) inside the test body.
      return;
    }
    initLifecycle();
  }

  @AfterEach
  public void tearDownAmoroCatalog() {
    if (catalogTestHelper != null) {
      try {
        catalogTestHelper.clean();
      } catch (Throwable t) {
        LOG.warn("Failed to clean catalog helper", t);
      }
    }
    if (tempRoot != null) {
      try {
        FileUtils.deleteDirectory(tempRoot);
      } catch (Throwable t) {
        LOG.warn("Failed to delete temp dir {}", tempRoot, t);
      }
      tempRoot = null;
    }
  }

  protected void initAmoroCatalog(AmoroCatalogTestHelper<?> catalogTestHelper) throws Exception {
    this.catalogTestHelper = catalogTestHelper;
    initLifecycle();
  }

  private void initLifecycle() throws Exception {
    tempRoot = Files.createTempDirectory("amoro-catalog-it").toFile();
    catalogTestHelper.initWarehouse(tempRoot.getPath());
    this.amoroCatalog = catalogTestHelper.amoroCatalog();
    this.originalCatalog = catalogTestHelper.originalCatalog();
    catalogTestHelper.initHiveConf(TEST_HMS.getHiveConf());
    TEST_AMS.getAmsHandler().createCatalog(catalogTestHelper.getCatalogMeta());
  }

  protected String getCatalogUrl() {
    return TEST_AMS.getServerUrl() + "/" + catalogTestHelper.getCatalogMeta().getCatalogName();
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

  protected int defaultParallelism() {
    return 1;
  }
}
