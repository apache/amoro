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
package com.netease.arctic.optimizer.container;

import com.netease.arctic.ams.api.PropertyNames;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestFlinkOptimizerContainer {
  FlinkOptimizerContainer container = new FlinkOptimizerContainer();

  Map<String, String> containerProperties = Maps.newHashMap();

  public TestFlinkOptimizerContainer() {
    containerProperties.put(PropertyNames.AMS_HOME, "/home/ams");
    containerProperties.put(FlinkOptimizerContainer.FLINK_HOME_PROPERTY, "/home/ams");
    containerProperties.put(PropertyNames.AMS_OPTIMIZER_URI, "thrift://127.0.0.1:1261");
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
  public void testBuildFlinkOptions() {
    Map<String, String> containerProperties = Maps.newHashMap(this.containerProperties);
    containerProperties.put("flink-conf.key1", "value1");
    containerProperties.put("flink-conf.key2", "value2");
    containerProperties.put("key3", "value3"); // non "flink-conf." property


    // Create some optimizing group properties
    Map<String, String> groupProperties = new HashMap<>();
    groupProperties.put("flink-conf.key2", "value4");
    groupProperties.put("flink-conf.key5", "value5");

    FlinkConf conf = FlinkConf.buildFor(Maps.newHashMap(), containerProperties)
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
    prop.put("taskmanager.memory", "100");
    prop.put("jobmanager.memory", "100");

    FlinkConf conf = FlinkConf.buildFor(prop, Maps.newHashMap()).build();

    Assert.assertEquals(100L, container.getMemorySizeValue(prop, conf,
        "taskmanager.memory",
        "taskmanager.memory.process.size"));
    Assert.assertEquals(100L, container.getMemorySizeValue(prop, conf,
        "jobmanager.memory",
        "jobmanager.memory.process.size"));


    Map<String, String> containerProperties = Maps.newHashMap();
    containerProperties.put("flink-conf.jobmanager.memory.process.size", "200 M");
    containerProperties.put("flink-conf.taskmanager.memory.process.size", "200");
    conf = FlinkConf.buildFor(prop, containerProperties).build();
    prop.clear();
    Assert.assertEquals(200L, container.getMemorySizeValue(prop, conf,
        "taskmanager.memory",
        "taskmanager.memory.process.size"));
    Assert.assertEquals(200L, container.getMemorySizeValue(prop, conf,
        "jobmanager.memory",
        "jobmanager.memory.process.size"));

    prop.clear();
    containerProperties = Maps.newHashMap();
    conf = FlinkConf.buildFor(prop, containerProperties).build();

    prop.put("taskmanager.memory", "300 M");
    prop.put("jobmanager.memory", "300");
    Assert.assertEquals(300L, container.getMemorySizeValue(prop, conf,
        "taskmanager.memory",
        "taskmanager.memory.process.size"));
    Assert.assertEquals(300L, container.getMemorySizeValue(prop, conf,
        "jobmanager.memory",
        "jobmanager.memory.process.size"));

    conf = FlinkConf.buildFor(Maps.newHashMap(), Maps.newHashMap()).build();
    prop.clear();
    Assert.assertEquals(0L, container.getMemorySizeValue(prop, conf,
        "taskmanager.memory",
        "taskmanager.memory.process.size"));
    Assert.assertEquals(0L, container.getMemorySizeValue(prop, conf,
        "jobmanager.memory",
        "jobmanager.memory.process.size"));
  }
}
