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

import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.resource.Resource;
import org.apache.amoro.resource.ResourceType;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
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

  @Test
  public void testKubernetesWaitAppCompletionDefaultsToFalse() {
    SparkOptimizerContainer container = createContainer("k8s://https://127.0.0.1:6443");

    String startupArgs =
        container.buildOptimizerStartupArgsString(createResource(Maps.newHashMap()));

    Assert.assertTrue(
        startupArgs.contains(
            "--conf "
                + SparkOptimizerContainer.SparkConfKeys.KUBERNETES_SUBMISSION_WAIT_APP_COMPLETION
                + "=false"));
  }

  @Test
  public void testKubernetesWaitAppCompletionCanBeOverridden() {
    SparkOptimizerContainer container = createContainer("k8s://https://127.0.0.1:6443");
    Map<String, String> resourceProperties = Maps.newHashMap();
    resourceProperties.put(
        SparkOptimizerContainer.SparkConf.SPARK_PARAMETER_PREFIX
            + SparkOptimizerContainer.SparkConfKeys.KUBERNETES_SUBMISSION_WAIT_APP_COMPLETION,
        "true");

    String startupArgs =
        container.buildOptimizerStartupArgsString(createResource(resourceProperties));

    Assert.assertTrue(
        startupArgs.contains(
            "--conf "
                + SparkOptimizerContainer.SparkConfKeys.KUBERNETES_SUBMISSION_WAIT_APP_COMPLETION
                + "=true"));
    Assert.assertFalse(
        startupArgs.contains(
            "--conf "
                + SparkOptimizerContainer.SparkConfKeys.KUBERNETES_SUBMISSION_WAIT_APP_COMPLETION
                + "=false"));
  }

  @Test
  public void testYarnDoesNotSetKubernetesWaitAppCompletion() {
    SparkOptimizerContainer container = createContainer("yarn");

    String startupArgs =
        container.buildOptimizerStartupArgsString(createResource(Maps.newHashMap()));

    Assert.assertFalse(
        startupArgs.contains(
            SparkOptimizerContainer.SparkConfKeys.KUBERNETES_SUBMISSION_WAIT_APP_COMPLETION));
  }

  private SparkOptimizerContainer createContainer(String master) {
    Map<String, String> properties = Maps.newHashMap(containerProperties);
    properties.put(SparkOptimizerContainer.SPARK_MASTER, master);
    if (master.startsWith("k8s://")) {
      properties.put(
          SparkOptimizerContainer.SparkConf.SPARK_PARAMETER_PREFIX
              + SparkOptimizerContainer.SparkConfKeys.KUBERNETES_IMAGE_REF,
          "apache/amoro-spark-optimizer:test");
    }
    SparkOptimizerContainer container = new SparkOptimizerContainer();
    container.init("spark", properties);
    return container;
  }

  private Resource createResource(Map<String, String> properties) {
    return new Resource.Builder("spark", "test-group", ResourceType.OPTIMIZER)
        .setThreadCount(1)
        .setProperties(properties)
        .build();
  }
}
