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

package org.apache.amoro.server.manager;

import org.apache.amoro.api.OptimizerProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TestFlinkOptimizerContainer {
  FlinkOptimizerContainer container = new FlinkOptimizerContainer();

  Map<String, String> containerProperties = Maps.newHashMap();

  public TestFlinkOptimizerContainer() {
    containerProperties.put(OptimizerProperties.AMS_HOME, "/home/ams");
    containerProperties.put(FlinkOptimizerContainer.FLINK_HOME_PROPERTY, "/home/ams");
    containerProperties.put(OptimizerProperties.AMS_OPTIMIZER_URI, "thrift://127.0.0.1:1261");
  }

  @Test
  public void testParseMemorySize() {
    Assert.assertEquals(100, container.parseMemorySize("100"));
    Assert.assertEquals(100, container.parseMemorySize("100m"));
    Assert.assertEquals(100, container.parseMemorySize("100 m"));
    Assert.assertEquals(100, container.parseMemorySize("100M"));
    Assert.assertEquals(100, container.parseMemorySize("100 M"));
    Assert.assertEquals(102400, container.parseMemorySize("100g"));
    Assert.assertEquals(102400, container.parseMemorySize("100 g"));
    Assert.assertEquals(102400, container.parseMemorySize("100G"));
    Assert.assertEquals(102400, container.parseMemorySize("100 G"));
    Assert.assertEquals(0, container.parseMemorySize("G100G"));
    Assert.assertEquals(0, container.parseMemorySize("100kb"));
  }

  @Test
  public void testReadFlinkConfigFile() {
    ClassLoader classLoader = getClass().getClassLoader();
    URL flinkConfResourceUrl = classLoader.getResource("flink-conf.yaml");
    Assert.assertEquals(
        Paths.get(Objects.requireNonNull(flinkConfResourceUrl).getPath()).getFileName().toString(),
        "flink-conf.yaml");
    URL newFlinkConfResourceUrl = classLoader.getResource("config.yaml");
    Assert.assertEquals(
        Paths.get(Objects.requireNonNull(newFlinkConfResourceUrl).getPath())
            .getFileName()
            .toString(),
        "config.yaml");
    Map<String, String> flinkConfig = container.loadFlinkConfigForYAML(newFlinkConfResourceUrl);
    Assert.assertEquals(
        flinkConfig.get(FlinkOptimizerContainer.FlinkConfKeys.TASK_MANAGER_TOTAL_PROCESS_MEMORY),
        "1728m");
    Assert.assertEquals(
        flinkConfig.get(FlinkOptimizerContainer.FlinkConfKeys.JOB_MANAGER_TOTAL_PROCESS_MEMORY),
        "1600m");
  }

  @Test
  public void testBuildFlinkOptions() {
    Map<String, String> containerProperties = Maps.newHashMap(this.containerProperties);
    containerProperties.put("flink-conf.key1", "value1");
    containerProperties.put("flink-conf.key2", "value2");
    containerProperties.put("key3", "value3"); // non "flink-conf." property

    // Create some optimizing group properties
    Map<String, String> groupProperties = new HashMap<>();
    groupProperties.put("flink-conf.key2", "value4");
    groupProperties.put("flink-conf.key5", "value5");

    FlinkOptimizerContainer.FlinkConf conf =
        FlinkOptimizerContainer.FlinkConf.buildFor(Maps.newHashMap(), containerProperties)
            .withGroupProperties(groupProperties)
            .build();
    String flinkOptions = conf.toCliOptions();
    Assert.assertEquals(3, flinkOptions.split(" ").length);
    Assert.assertTrue(flinkOptions.contains("-Dkey1=value1"));
    Assert.assertTrue(flinkOptions.contains("-Dkey2=value4"));
    Assert.assertTrue(flinkOptions.contains("-Dkey5=value5"));
  }

  @Test
  public void testGetMemorySizeValue() {
    HashMap<String, String> prop = new HashMap<>();
    prop.put(FlinkOptimizerContainer.FlinkConfKeys.TASK_MANAGER_TOTAL_PROCESS_MEMORY, "100");
    prop.put(FlinkOptimizerContainer.FlinkConfKeys.JOB_MANAGER_TOTAL_PROCESS_MEMORY, "100");

    FlinkOptimizerContainer.FlinkConf conf =
        FlinkOptimizerContainer.FlinkConf.buildFor(prop, Maps.newHashMap()).build();

    Assert.assertEquals(
        100L,
        container.getMemorySizeValue(
            prop, conf, FlinkOptimizerContainer.FlinkConfKeys.TASK_MANAGER_TOTAL_PROCESS_MEMORY));
    Assert.assertEquals(
        100L,
        container.getMemorySizeValue(
            prop, conf, FlinkOptimizerContainer.FlinkConfKeys.JOB_MANAGER_TOTAL_PROCESS_MEMORY));

    Map<String, String> containerProperties = Maps.newHashMap();
    containerProperties.put("flink-conf.jobmanager.memory.process.size", "200 M");
    containerProperties.put("flink-conf.taskmanager.memory.process.size", "200");
    conf = FlinkOptimizerContainer.FlinkConf.buildFor(prop, containerProperties).build();
    prop.clear();
    Assert.assertEquals(
        200L,
        container.getMemorySizeValue(
            prop, conf, FlinkOptimizerContainer.FlinkConfKeys.TASK_MANAGER_TOTAL_PROCESS_MEMORY));
    Assert.assertEquals(
        200L,
        container.getMemorySizeValue(
            prop, conf, FlinkOptimizerContainer.FlinkConfKeys.JOB_MANAGER_TOTAL_PROCESS_MEMORY));

    prop.clear();
    containerProperties = Maps.newHashMap();
    conf = FlinkOptimizerContainer.FlinkConf.buildFor(prop, containerProperties).build();

    prop.put(FlinkOptimizerContainer.FlinkConfKeys.TASK_MANAGER_TOTAL_PROCESS_MEMORY, "300 M");
    prop.put(FlinkOptimizerContainer.FlinkConfKeys.JOB_MANAGER_TOTAL_PROCESS_MEMORY, "300");
    Assert.assertEquals(
        300L,
        container.getMemorySizeValue(
            prop, conf, FlinkOptimizerContainer.FlinkConfKeys.TASK_MANAGER_TOTAL_PROCESS_MEMORY));
    Assert.assertEquals(
        300L,
        container.getMemorySizeValue(
            prop, conf, FlinkOptimizerContainer.FlinkConfKeys.JOB_MANAGER_TOTAL_PROCESS_MEMORY));

    conf = FlinkOptimizerContainer.FlinkConf.buildFor(Maps.newHashMap(), Maps.newHashMap()).build();
    prop.clear();
    Assert.assertEquals(
        0L,
        container.getMemorySizeValue(
            prop, conf, FlinkOptimizerContainer.FlinkConfKeys.TASK_MANAGER_TOTAL_PROCESS_MEMORY));
    Assert.assertEquals(
        0L,
        container.getMemorySizeValue(
            prop, conf, FlinkOptimizerContainer.FlinkConfKeys.JOB_MANAGER_TOTAL_PROCESS_MEMORY));
  }
}
