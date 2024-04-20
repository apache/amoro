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
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestSparkOptimizerContainer {
  Map<String, String> containerProperties = Maps.newHashMap();

  public TestSparkOptimizerContainer() {
    containerProperties.put(OptimizerProperties.AMS_HOME, "/home/ams");
    containerProperties.put(SparkOptimizerContainer.SPARK_HOME_PROPERTY, "/home/ams");
    containerProperties.put(OptimizerProperties.AMS_OPTIMIZER_URI, "thrift://127.0.0.1:1261");
  }

  @Test
  public void testBuildSparkOptions() {
    Map<String, String> containerProperties = Maps.newHashMap(this.containerProperties);
    containerProperties.put("spark-conf.key1", "value1");
    containerProperties.put("spark-conf.key2", "value2");
    containerProperties.put("key3", "value3"); // non "spark-conf." property

    // Create some optimizing group properties
    Map<String, String> groupProperties = new HashMap<>();
    groupProperties.put("spark-conf.key2", "value4");
    groupProperties.put("spark-conf.key5", "value5");

    SparkOptimizerContainer.SparkConf conf =
        SparkOptimizerContainer.SparkConf.buildFor(Maps.newHashMap(), containerProperties)
            .withGroupProperties(groupProperties)
            .build();
    String sparkOptions = conf.toConfOptions();
    System.out.println(sparkOptions);
    Assert.assertEquals(3, sparkOptions.split(" --conf").length);
    Assert.assertTrue(sparkOptions.contains("--conf key1=value1"));
    Assert.assertTrue(sparkOptions.contains("--conf key2=value4"));
    Assert.assertTrue(sparkOptions.contains("--conf key5=value5"));
  }
}
