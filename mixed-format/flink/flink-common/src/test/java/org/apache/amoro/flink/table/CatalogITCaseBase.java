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

import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
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
import org.apache.iceberg.flink.MiniClusterResource;
import org.junit.ClassRule;

public abstract class CatalogITCaseBase extends TableTestBase {

  @ClassRule
  public static final MiniClusterWithClientResource MINI_CLUSTER_RESOURCE =
      MiniClusterResource.createWithClassloaderCheckDisabled();

  private volatile StreamTableEnvironment tEnv = null;
  private volatile StreamExecutionEnvironment env = null;

  public CatalogITCaseBase(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
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
