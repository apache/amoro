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

package org.apache.amoro.optimizer.spark;

import org.apache.amoro.TableFormat;
import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.optimizer.common.OptimizerConfig;
import org.apache.amoro.optimizing.OptimizingExecutor;
import org.apache.amoro.optimizing.OptimizingExecutorFactory;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.optimizing.TableOptimizing;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.utils.SerializationUtil;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.UpdateSchema;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestSparkOptimizerLocalMode {

  @Test
  public void testExecuteOptimizingTaskInLocalMode() {
    SparkConf sparkConf =
        new SparkConf()
            .setMaster("local[1]")
            .setAppName("amoro-spark-optimizer-local-mode-test")
            .set("spark.driver.bindAddress", "127.0.0.1")
            .set("spark.driver.host", "127.0.0.1")
            .set("spark.ui.enabled", "false");

    try (JavaSparkContext sparkContext = new JavaSparkContext(sparkConf)) {
      OptimizerConfig optimizerConfig = new OptimizerConfig();
      optimizerConfig.setAmsUrl("thrift://127.0.0.1:1261");
      optimizerConfig.setExecutionParallel(1);
      optimizerConfig.setGroupName("local-test");
      optimizerConfig.setResourceId("local-test");
      optimizerConfig.setDiskStoragePath(System.getProperty("java.io.tmpdir"));

      SparkOptimizerExecutor executor =
          new SparkOptimizerExecutor(sparkContext, optimizerConfig, 0);
      OptimizingTask task = new OptimizingTask(new OptimizingTaskId(1L, 1));
      RewriteFilesInput input =
          new RewriteFilesInput(
              new org.apache.iceberg.DataFile[0],
              new org.apache.iceberg.DataFile[0],
              new org.apache.iceberg.ContentFile<?>[0],
              new org.apache.iceberg.ContentFile<?>[0],
              new TestMixedTable());
      task.setTaskInput(SerializationUtil.simpleSerialize(input));
      Map<String, String> properties = new HashMap<>();
      properties.put(
          TaskProperties.TASK_EXECUTOR_FACTORY_IMPL, TestOptimizingExecutorFactory.class.getName());
      task.setProperties(properties);

      OptimizingTaskResult result = executor.executeTask(task);

      Assertions.assertNull(result.getErrorMessage());
      Assertions.assertEquals("spark-local", result.getSummary().get("engine"));
    }
  }

  public static class TestOptimizingExecutorFactory
      implements OptimizingExecutorFactory<RewriteFilesInput> {

    @Override
    public void initialize(Map<String, String> properties) {}

    @Override
    public OptimizingExecutor<?> createExecutor(RewriteFilesInput input) {
      return () -> new TestOptimizingOutput();
    }
  }

  public static class TestOptimizingOutput implements TableOptimizing.OptimizingOutput {

    @Override
    public Map<String, String> summary() {
      return Collections.singletonMap("engine", "spark-local");
    }
  }

  private static class TestMixedTable implements MixedTable {

    @Override
    public TableIdentifier id() {
      return null;
    }

    @Override
    public TableFormat format() {
      return TableFormat.ICEBERG;
    }

    @Override
    public Schema schema() {
      return null;
    }

    @Override
    public String name() {
      return "local_test_table";
    }

    @Override
    public PartitionSpec spec() {
      return null;
    }

    @Override
    public Map<String, String> properties() {
      return Collections.emptyMap();
    }

    @Override
    public String location() {
      return null;
    }

    @Override
    public AuthenticatedFileIO io() {
      return null;
    }

    @Override
    public void refresh() {}

    @Override
    public UpdateSchema updateSchema() {
      return null;
    }

    @Override
    public UpdateProperties updateProperties() {
      return null;
    }
  }
}
