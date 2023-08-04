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

package com.netease.arctic.optimizer;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class TestFlinkOptimizerContainer {
  FlinkOptimizerContainer container = new FlinkOptimizerContainer();

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
  public void testBuildFlinkArgs() {
    HashMap<String, String> prop = new HashMap<>();
    prop.put("taskmanager.memory", "100");
    prop.put("jobmanager.memory", "100");
    prop.put("jobmanager.memory.process.size", "100");
    prop.put("taskmanager.memory.process.size", "100");
    prop.put("key1", "value1");
    prop.put("key2", "value2");
    Assert.assertEquals("-yD key1=value1 -yD key2=value2", container.buildFlinkArgs(prop));
  }
}
