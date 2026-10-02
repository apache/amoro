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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestSparkOptimizerLocalMode {

  private static final String TEST_PROPERTY_KEY = "test.property";
  private static final String TEST_PROPERTY_VALUE = "property-from-task";
  private static final String EXPECTED_FAILURE_MESSAGE = "expected optimizer failure";

  private static JavaSparkContext sparkContext;
  private static OptimizerConfig optimizerConfig;

  @BeforeAll
  public static void startSparkContext() {
    SparkConf sparkConf =
        new SparkConf()
            .setMaster("local[1]")
            .setAppName("amoro-spark-optimizer-local-mode-test")
            .set("spark.driver.bindAddress", "127.0.0.1")
            .set("spark.driver.host", "127.0.0.1")
            .set("spark.ui.enabled", "false");

    sparkContext = new JavaSparkContext(sparkConf);
    optimizerConfig = new OptimizerConfig();
    optimizerConfig.setAmsUrl("thrift://127.0.0.1:1261");
    optimizerConfig.setExecutionParallel(1);
    optimizerConfig.setGroupName("local-test");
    optimizerConfig.setResourceId("local-test");
    optimizerConfig.setDiskStoragePath(System.getProperty("java.io.tmpdir"));
  }

  @AfterAll
  public static void stopSparkContext() {
    if (sparkContext != null) {
      sparkContext.close();
    }
  }

  @Test
  public void testExecuteOptimizingTaskInLocalMode() {
    int threadId = 0;
    OptimizingTask task = newRewriteTask(1, factoryProperties(TestOptimizingExecutorFactory.class));

    OptimizingTaskResult result = executeTask(task, threadId);

    assertSuccessfulResult(result, task, threadId);
    Assertions.assertEquals("spark-local", result.getSummary().get("engine"));
  }

  @Test
  public void testExecuteOptimizingTaskWithFactoryProperties() {
    int threadId = 1;
    Map<String, String> properties = factoryProperties(PropertyEchoExecutorFactory.class);
    properties.put(TEST_PROPERTY_KEY, TEST_PROPERTY_VALUE);
    OptimizingTask task = newRewriteTask(2, properties);

    OptimizingTaskResult result = executeTask(task, threadId);

    assertSuccessfulResult(result, task, threadId);
    Assertions.assertEquals(TEST_PROPERTY_VALUE, result.getSummary().get(TEST_PROPERTY_KEY));
    Assertions.assertEquals("1", result.getSummary().get(TaskProperties.PROCESS_ID));
    Assertions.assertEquals("local_test_table", result.getSummary().get("table"));
    Assertions.assertEquals(TableFormat.ICEBERG.name(), result.getSummary().get("format"));
  }

  @Test
  public void testExecuteOptimizingTaskWithFailingExecutor() {
    int threadId = 2;
    OptimizingTask task = newRewriteTask(3, factoryProperties(FailingExecutorFactory.class));

    OptimizingTaskResult result = executeTask(task, threadId);

    assertFailedResult(result, task, threadId);
    Assertions.assertTrue(result.getErrorMessage().contains(EXPECTED_FAILURE_MESSAGE));
  }

  @Test
  public void testExecuteOptimizingTaskWithMissingFactoryProperty() {
    int threadId = 3;
    OptimizingTask task = newRewriteTask(4, Collections.emptyMap());

    OptimizingTaskResult result = executeTask(task, threadId);

    assertFailedResult(result, task, threadId);
  }

  @Test
  public void testExecuteOptimizingTaskWithMalformedInput() {
    int threadId = 4;
    OptimizingTask task = new OptimizingTask(new OptimizingTaskId(1L, 5));
    task.setTaskInput(ByteBuffer.wrap(new byte[] {0x1, 0x2, 0x3, 0x4}));
    task.setProperties(factoryProperties(TestOptimizingExecutorFactory.class));

    OptimizingTaskResult result = executeTask(task, threadId);

    assertFailedResult(result, task, threadId);
    Assertions.assertTrue(result.getErrorMessage().contains("deserialization error"));
  }

  private OptimizingTaskResult executeTask(OptimizingTask task, int threadId) {
    SparkOptimizerExecutor executor =
        new SparkOptimizerExecutor(sparkContext, optimizerConfig, threadId);
    return executor.executeTask(task);
  }

  private static OptimizingTask newRewriteTask(int taskId, Map<String, String> properties) {
    OptimizingTask task = new OptimizingTask(new OptimizingTaskId(1L, taskId));
    RewriteFilesInput input =
        new RewriteFilesInput(
            new org.apache.iceberg.DataFile[0],
            new org.apache.iceberg.DataFile[0],
            new org.apache.iceberg.ContentFile<?>[0],
            new org.apache.iceberg.ContentFile<?>[0],
            new TestMixedTable());
    task.setTaskInput(SerializationUtil.simpleSerialize(input));
    task.setProperties(properties);
    return task;
  }

  private static Map<String, String> factoryProperties(Class<?> factoryClass) {
    Map<String, String> properties = new HashMap<>();
    properties.put(TaskProperties.TASK_EXECUTOR_FACTORY_IMPL, factoryClass.getName());
    return properties;
  }

  private static void assertSuccessfulResult(
      OptimizingTaskResult result, OptimizingTask task, int threadId) {
    Assertions.assertEquals(task.getTaskId(), result.getTaskId());
    Assertions.assertEquals(threadId, result.getThreadId());
    Assertions.assertNull(result.getErrorMessage());
    Assertions.assertNotNull(result.getTaskOutput());
    Assertions.assertNotNull(result.getSummary());
  }

  private static void assertFailedResult(
      OptimizingTaskResult result, OptimizingTask task, int threadId) {
    Assertions.assertEquals(task.getTaskId(), result.getTaskId());
    Assertions.assertEquals(threadId, result.getThreadId());
    Assertions.assertNotNull(result.getErrorMessage());
    Assertions.assertFalse(result.getErrorMessage().isBlank());
    Assertions.assertNull(result.getTaskOutput());
  }

  public static class TestOptimizingExecutorFactory
      implements OptimizingExecutorFactory<RewriteFilesInput> {

    @Override
    public void initialize(Map<String, String> properties) {}

    @Override
    public OptimizingExecutor<?> createExecutor(RewriteFilesInput input) {
      return () -> new TestOptimizingOutput(Collections.singletonMap("engine", "spark-local"));
    }
  }

  public static class PropertyEchoExecutorFactory
      implements OptimizingExecutorFactory<RewriteFilesInput> {

    private Map<String, String> properties;

    @Override
    public void initialize(Map<String, String> properties) {
      this.properties = new HashMap<>(properties);
    }

    @Override
    public OptimizingExecutor<?> createExecutor(RewriteFilesInput input) {
      Map<String, String> summary = new HashMap<>();
      summary.put(TEST_PROPERTY_KEY, properties.get(TEST_PROPERTY_KEY));
      summary.put(TaskProperties.PROCESS_ID, properties.get(TaskProperties.PROCESS_ID));
      summary.put("table", input.getTable().name());
      summary.put("format", input.getTable().format().name());
      return () -> new TestOptimizingOutput(summary);
    }
  }

  public static class FailingExecutorFactory
      implements OptimizingExecutorFactory<RewriteFilesInput> {

    @Override
    public void initialize(Map<String, String> properties) {}

    @Override
    public OptimizingExecutor<?> createExecutor(RewriteFilesInput input) {
      return () -> {
        throw new IllegalStateException(EXPECTED_FAILURE_MESSAGE);
      };
    }
  }

  public static class TestOptimizingOutput implements TableOptimizing.OptimizingOutput {

    private final Map<String, String> summary;

    public TestOptimizingOutput(Map<String, String> summary) {
      this.summary = summary;
    }

    @Override
    public Map<String, String> summary() {
      return summary;
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
