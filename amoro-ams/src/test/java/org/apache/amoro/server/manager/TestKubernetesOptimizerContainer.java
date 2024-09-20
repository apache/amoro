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

import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.LocalObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.PodTemplate;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.resource.Resource;
import org.apache.amoro.resource.ResourceType;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.amoro.utils.JacksonUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestKubernetesOptimizerContainer {
  private KubernetesOptimizerContainer kubernetesOptimizerContainer;
  private Map<String, String> containerProperties;
  private Map<String, String> groupProperties;
  public static final String MEMORY_PROPERTY = "memory";
  public static final String CPU_FACTOR_PROPERTY = "cpu.factor";
  public static final String IMAGE = "image";
  public static final String PULL_POLICY = "pullPolicy";
  public static final String PULL_SECRETS = "imagePullSecrets";

  @Before
  public void setup() throws IOException {
    // generating configuration files
    kubernetesOptimizerContainer = new KubernetesOptimizerContainer();
    groupProperties = Maps.newHashMap();
    containerProperties = Maps.newHashMap();

    containerProperties.put(OptimizerProperties.AMS_HOME, "/home/ams");
    containerProperties.put(OptimizerProperties.AMS_OPTIMIZER_URI, "thrift://127.0.0.1:1261");

    URL resource = getClass().getClassLoader().getResource("config.yaml");

    JsonNode yamlConfig =
        JacksonUtil.fromObjects(
            new Yaml().loadAs(Files.newInputStream(Paths.get(resource.getPath())), Map.class));
    JsonNode containers = yamlConfig.get(AmoroManagementConf.CONTAINER_LIST);
    for (JsonNode container : containers) {
      if (container.get("name").asText().equals("KubernetesContainer")) {
        ObjectMapper mapper = new ObjectMapper();
        containerProperties.putAll(mapper.convertValue(container.get("properties"), Map.class));
      }
    }
    groupProperties.putAll(this.containerProperties);
  }

  private static String checkAndGetProperty(Map<String, String> properties, String key) {
    Preconditions.checkState(
        properties != null && properties.containsKey(key), "Cannot find %s in properties", key);
    return properties.get(key);
  }

  @Test
  public void testBuildPodTemplateFromLocal() {
    PodTemplate podTemplate =
        kubernetesOptimizerContainer.initPodTemplateFromLocal(groupProperties);

    Assert.assertEquals(1, podTemplate.getTemplate().getSpec().getContainers().size());
    // read the image version from the podTemplate config and assert it
    Assert.assertEquals(
        "apache/amoro:0.6", podTemplate.getTemplate().getSpec().getContainers().get(0).getImage());
  }

  @Test
  public void testBuildPodTemplateWithResourceSetMemoryMb() {
    PodTemplate podTemplate =
        kubernetesOptimizerContainer.initPodTemplateFromLocal(groupProperties);

    ResourceType resourceType = ResourceType.OPTIMIZER;
    Map<String, String> properties = Maps.newHashMap();
    properties.put("memory", "1024");
    Resource resource =
        new Resource.Builder("KubernetesContainer", "k8s", resourceType)
            .setMemoryMb(1025) // It's not 0 here
            .setThreadCount(1)
            .setProperties(properties)
            .build();
    groupProperties.putAll(resource.getProperties());

    Map<String, Object> argsList =
        kubernetesOptimizerContainer.generatePodStartArgs(resource, groupProperties);
    String image = argsList.get(IMAGE).toString();
    String pullPolicy = argsList.get(PULL_POLICY).toString();
    List<LocalObjectReference> imagePullSecretsList =
        (List<LocalObjectReference>) argsList.get(PULL_SECRETS);
    int cpuLimit = (int) argsList.get("cpuLimit");
    long memory = (long) argsList.get(MEMORY_PROPERTY);
    String groupName = argsList.get("groupName").toString();
    String resourceId = argsList.get("resourceId").toString();
    String startUpArgs = argsList.get("startUpArgs").toString();

    Deployment deployment =
        kubernetesOptimizerContainer.initPodTemplateFromFrontEnd(
            podTemplate,
            image,
            pullPolicy,
            cpuLimit,
            groupName,
            resourceId,
            startUpArgs,
            memory,
            imagePullSecretsList);

    Assert.assertEquals(1, deployment.getSpec().getReplicas().intValue());
    Assert.assertNotEquals(
        "1024Mi",
        deployment
            .getSpec()
            .getTemplate()
            .getSpec()
            .getContainers()
            .get(0)
            .getResources()
            .getLimits()
            .get("memory")
            .toString());
    Assert.assertEquals(
        "1025Mi",
        deployment
            .getSpec()
            .getTemplate()
            .getSpec()
            .getContainers()
            .get(0)
            .getResources()
            .getLimits()
            .get("memory")
            .toString());
  }

  @Test
  public void testBuildPodTemplateConfig() {
    // before parameter merging
    PodTemplate podTemplate =
        kubernetesOptimizerContainer.initPodTemplateFromLocal(groupProperties);

    // after parameter merging
    ResourceType resourceType = ResourceType.OPTIMIZER;
    Map<String, String> properties = Maps.newHashMap();
    properties.put("memory", "1024");
    Resource resource =
        new Resource.Builder("KubernetesContainer", "k8s", resourceType)
            .setMemoryMb(0)
            .setThreadCount(1)
            .setProperties(properties)
            .build();
    groupProperties.putAll(resource.getProperties());

    // generate pod start args
    long memoryPerThread;
    long memory;

    if (resource.getMemoryMb() > 0) {
      memory = resource.getMemoryMb();
    } else {
      memoryPerThread = Long.parseLong(checkAndGetProperty(groupProperties, MEMORY_PROPERTY));
      memory = memoryPerThread * resource.getThreadCount();
    }
    String startUpArgs =
        String.format(
            "/entrypoint.sh optimizer %s %s",
            memory, kubernetesOptimizerContainer.buildOptimizerStartupArgsString(resource));
    // read the image version from config and assert it , but not from podTemplate
    String image = checkAndGetProperty(groupProperties, IMAGE);
    String pullPolicy = checkAndGetProperty(groupProperties, PULL_POLICY);
    String pullSecrets = groupProperties.getOrDefault(PULL_SECRETS, "");
    String cpuLimitFactorString = groupProperties.getOrDefault(CPU_FACTOR_PROPERTY, "1.0");
    double cpuLimitFactor = Double.parseDouble(cpuLimitFactorString);
    int cpuLimit = (int) (Math.ceil(cpuLimitFactor * resource.getThreadCount()));

    List<LocalObjectReference> imagePullSecretsList =
        Arrays.stream(pullSecrets.split(";"))
            .map(secret -> new LocalObjectReferenceBuilder().withName(secret).build())
            .collect(Collectors.toList());

    String resourceId = resource.getResourceId();
    String groupName = resource.getGroupName();

    Assert.assertEquals(1, podTemplate.getTemplate().getSpec().getContainers().size());

    // read the image version from the podTemplate config and assert it
    Assert.assertEquals(
        "apache/amoro:0.6", podTemplate.getTemplate().getSpec().getContainers().get(0).getImage());

    Deployment deployment =
        kubernetesOptimizerContainer.initPodTemplateFromFrontEnd(
            podTemplate,
            image,
            pullPolicy,
            cpuLimit,
            groupName,
            resourceId,
            startUpArgs,
            memory,
            imagePullSecretsList);

    Assert.assertEquals("amoro-optimizer-" + resourceId, deployment.getMetadata().getName());
    Assert.assertEquals(
        "k8s",
        deployment.getSpec().getTemplate().getMetadata().getLabels().get("AmoroOptimizerGroup"));
    Assert.assertEquals(1, deployment.getSpec().getReplicas().intValue());
    Assert.assertEquals(
        "IfNotPresent",
        deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImagePullPolicy());

    Assert.assertEquals(1, deployment.getSpec().getTemplate().getSpec().getContainers().size());

    // read the image version from the podTemplate config and assert it
    // the final version is still apache/amoro:0.7-SNAPSHOT
    Assert.assertEquals(
        "apache/amoro:0.7-SNAPSHOT",
        deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());
    Assert.assertEquals(
        String.valueOf(cpuLimit),
        deployment
            .getSpec()
            .getTemplate()
            .getSpec()
            .getContainers()
            .get(0)
            .getResources()
            .getLimits()
            .get("cpu")
            .toString());
    Assert.assertEquals(
        memory + "Mi",
        deployment
            .getSpec()
            .getTemplate()
            .getSpec()
            .getContainers()
            .get(0)
            .getResources()
            .getLimits()
            .get("memory")
            .toString());
  }
}
