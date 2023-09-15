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

import com.netease.arctic.ams.api.resource.Resource;
import com.netease.arctic.optimizer.flink.FlinkOptimizer;
import com.netease.arctic.optimizer.util.PropertyUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.relocated.com.google.common.base.Joiner;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FlinkOptimizerContainer extends AbstractResourceContainer {
  private static final Logger LOG = LoggerFactory.getLogger(FlinkOptimizerContainer.class);

  public static final String FLINK_HOME_PROPERTY = "flink-home";
  public static final String FLINK_CONFIG_PATH = "/conf/flink-conf.yaml";

  public static final String TASK_MANAGER_MEMORY_PROPERTY = "taskmanager.memory";

  public static final String JOB_MANAGER_MEMORY_PROPERTY = "jobmanager.memory";
  public static final String FLINK_PARAMETER_PREFIX = "flink-conf.";
  public static final String FLINK_RUN_TARGET = "target";
  public static final String FLINK_JOB_URI = "job-uri";
  public static final String FLINK_KUBERNETES_IMAGE_REF = "kubernetes.container.image";
  public static final String JOB_MANAGER_TOTAL_PROCESS_MEMORY = "jobmanager.memory.process.size";
  public static final String TASK_MANAGER_TOTAL_PROCESS_MEMORY = "taskmanager.memory.process.size";
  public static final String YARN_APPLICATION_ID_PROPERTY = "yarn-application-id";
  public static final String KUBERNETES_CLUSTER_ID_PROPERTY = "kubernetes-cluster-id";

  private static final Pattern APPLICATION_ID_PATTERN = Pattern.compile("(.*)application_(\\d+)_(\\d+)");
  private static final int MAX_READ_APP_ID_TIME = 600000; //10 min

  private Target target;
  private String flinkHome;
  private String jobUri;

  @Override
  public void init(String name, Map<String, String> containerProperties) {
    super.init(name, containerProperties);
    String runTarget = Optional.ofNullable(containerProperties.get(FLINK_RUN_TARGET))
        .orElse(Target.YarnPreJob.value);
    this.target = Target.valueToEnum(runTarget);
    String jobUri = containerProperties.get(FLINK_JOB_URI);
    if (target.applicationMode) {
      Preconditions.checkArgument(
          StringUtils.isNotEmpty(jobUri),
          "FlinkOptimizerContainer init error, the property " +
              FLINK_JOB_URI + " is required if run target in " + "application mode.");
    }
    if (StringUtils.isEmpty(jobUri)) {
      jobUri = amsHome + "/plugin/optimize/OptimizeJob.jar";
    }
    this.jobUri = jobUri;

    if (Target.KubernetesApplication == target) {
      String imageRef = containerProperties.get(FLINK_PARAMETER_PREFIX + FLINK_KUBERNETES_IMAGE_REF);
      Preconditions.checkArgument(
          StringUtils.isNotEmpty(imageRef),
          "FlinkOptimizerContainer init error, The property " +
              FLINK_PARAMETER_PREFIX + FLINK_KUBERNETES_IMAGE_REF +
              " is required if run target is " + target.value);
    }

    String flinkHome = PropertyUtil.getRequiredNotNull(getContainerProperties(), FLINK_HOME_PROPERTY);
    this.flinkHome = flinkHome.replaceAll("/$", "");
  }

  @Override
  protected Map<String, String> doScaleOut(String startUpArgs) {
    Runtime runtime = Runtime.getRuntime();
    try {
      String exportCmd = String.join(" && ", exportSystemProperties());
      String startUpCmd = String.format("%s && %s", exportCmd, startUpArgs);
      String[] cmd = {"/bin/sh", "-c", startUpCmd};
      LOG.info("Starting flink optimizer using command : {}", startUpCmd);
      Process exec = runtime.exec(cmd);
      Map<String, String> startUpStatesMap = Maps.newHashMap();

      switch (target) {
        case YarnPreJob:
        case YarnApplication:
          String applicationId = readApplicationId(exec);
          if (applicationId != null) {
            startUpStatesMap.put(YARN_APPLICATION_ID_PROPERTY, applicationId);
          }
          break;
      }
      return startUpStatesMap;
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to scale out flink optimizer.", e);
    }
  }

  @Override
  protected String buildOptimizerStartupArgsString(Resource resource) {
    Map<String, String> properties = resource.getProperties();
    Map<String, String> flinkConfig = loadFlinkConfig();
    Preconditions.checkState(
        properties != null && !flinkConfig.isEmpty(),
        "resource properties is null or load flink-conf.yaml failed");
    long jobManagerMemory = getMemorySizeValue(properties, flinkConfig, JOB_MANAGER_MEMORY_PROPERTY,
        JOB_MANAGER_TOTAL_PROCESS_MEMORY);
    long taskManagerMemory = getMemorySizeValue(properties, flinkConfig, TASK_MANAGER_MEMORY_PROPERTY,
        TASK_MANAGER_TOTAL_PROCESS_MEMORY);

    resource.getProperties().put(
        FLINK_PARAMETER_PREFIX + JOB_MANAGER_TOTAL_PROCESS_MEMORY,
        jobManagerMemory + "m");
    resource.getProperties().put(
        FLINK_PARAMETER_PREFIX + TASK_MANAGER_TOTAL_PROCESS_MEMORY,
        taskManagerMemory + "m");

    String flinkAction = target.applicationMode ? "run-application" : "run";
    String flinkOptions;
    switch (target) {
      case YarnPreJob:
      case YarnApplication:
        flinkOptions = buildFlinkOptions(resource.getProperties());
        break;
      case KubernetesApplication:
        addKubernetesProperties(resource);
        flinkOptions = buildFlinkOptions(resource.getProperties());
        break;
      default:
        throw new IllegalStateException("flink run target is not supported: " + target.value);
    }

    String jobArgs = super.buildOptimizerStartupArgsString(resource);
    // ./bin/flink ACTION --target=TARGET OPTIONS <job-file> <arguments>
    //  options: -D<property=value>
    return String.format("%s/bin/flink %s --target=%s %s -c %s %s %s",
        flinkHome, flinkAction, target.value, flinkOptions, FlinkOptimizer.class.getName(), jobUri, jobArgs);
  }

  private Map<String, String> loadFlinkConfig() {
    try {
      return new Yaml().load(Files.newInputStream(Paths.get(flinkHome + FLINK_CONFIG_PATH)));
    } catch (IOException e) {
      LOG.error("load flink conf yaml failed: {}", e.getMessage());
      return Collections.emptyMap();
    }
  }

  /**
   * get jobManager and taskManager memory.
   * An example of using Jobmanager memory parameters is as follows:
   * jobmanager.memory: 1024
   * flink-conf.jobmanager.memory.process.size: 1024M
   * flink-conf.yaml
   * Prioritize from high to low.
   */
  protected long getMemorySizeValue(
      Map<String, String> properties, Map<String, String> flinkConfig,
      String propertyKey, String finkConfigKey) {
    return Optional.ofNullable(properties.get(propertyKey))
        .map(this::parseMemorySize)
        .orElseGet(() -> Optional.ofNullable(properties.get(FLINK_PARAMETER_PREFIX + finkConfigKey))
            .map(this::parseMemorySize)
            .orElseGet(() -> Optional.ofNullable(flinkConfig.get(finkConfigKey))
                .map(this::parseMemorySize)
                .orElse(0L)));
  }

  /**
   * The properties with prefix "flink-conf." will be merged with the following priority
   * and transformed into Flink options.
   * 1. optimizing-group properties
   * 2. optimizing-container properties
   *
   * @param groupProperties - optimizing group properties
   * @return flink options, format is `-Dkey1=value1 -Dkey2=value2`
   */
  protected String buildFlinkOptions(Map<String, String> groupProperties) {
    Map<String, String> properties = Maps.newHashMap();
    getContainerProperties().entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(FLINK_PARAMETER_PREFIX))
        .forEach(entry -> properties.put(entry.getKey(), entry.getValue()));

    groupProperties.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(FLINK_PARAMETER_PREFIX))
        .forEach(entry -> properties.put(entry.getKey(), entry.getValue()));

    return properties.entrySet().stream()
        .map(entry -> "-D" + entry.getKey().substring(FLINK_PARAMETER_PREFIX.length()) + "=" + entry.getValue())
        .collect(Collectors.joining(" "));
  }

  /**
   * memory conversion of units method, supporting m and g units
   */
  public long parseMemorySize(String memoryStr) {
    if (memoryStr == null || memoryStr.isEmpty()) {
      return 0;
    }
    memoryStr = memoryStr.toLowerCase().trim().replaceAll("\\s", "");
    Matcher matcher = Pattern.compile("(\\d+)([mg])").matcher(memoryStr);
    if (matcher.matches()) {
      long size = Long.parseLong(matcher.group(1));
      String unit = matcher.group(2);
      switch (unit) {
        case "m":
          return size;
        case "g":
          return size * 1024;
        default:
          LOG.error("Invalid memory size unit: {}, Please use m or g as the unit", unit);
          return 0;
      }
    } else {
      try {
        return Long.parseLong(memoryStr);
      } catch (NumberFormatException e) {
        LOG.error("Invalid memory size format: {}", memoryStr);
        return 0;
      }
    }
  }

  private String readApplicationId(Process exec) {
    StringBuilder outputBuilder = new StringBuilder();
    String applicationId = null;
    try (InputStreamReader inputStreamReader = new InputStreamReader(exec.getInputStream())) {
      try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < MAX_READ_APP_ID_TIME) {
          String readLine = bufferedReader.readLine();
          if (readLine == null) {
            break;
          }
          outputBuilder.append(readLine).append("\n");
          Matcher matcher = APPLICATION_ID_PATTERN.matcher(readLine);
          if (matcher.matches()) {
            applicationId = String.format("application_%s_%s", matcher.group(2), matcher.group(3));
            break;
          }
        }
        LOG.info("Started flink optimizer log:\n{}", outputBuilder);
        return applicationId;
      }
    } catch (IOException e) {
      LOG.error("Read application id from output failed", e);
      return null;
    }
  }

  @Override
  public void releaseOptimizer(Resource resource) {
    String options;
    switch (target) {
      case YarnApplication:
      case YarnPreJob:
        Preconditions.checkArgument(resource.getProperties().containsKey(YARN_APPLICATION_ID_PROPERTY),
            "Cannot find {} from optimizer start up stats", YARN_APPLICATION_ID_PROPERTY);
        String applicationId = resource.getProperties().get(YARN_APPLICATION_ID_PROPERTY);
        options = "-Dyarn.application.id=" + applicationId;
        break;
      case KubernetesApplication:
        Preconditions.checkArgument(resource.getProperties().containsKey(
            FLINK_PARAMETER_PREFIX + KUBERNETES_CLUSTER_ID_PROPERTY
        ), "Cannot find {} from optimizer start up stats.", KUBERNETES_CLUSTER_ID_PROPERTY);
        String clusterId = resource.getProperties().get(FLINK_PARAMETER_PREFIX + KUBERNETES_CLUSTER_ID_PROPERTY);
        options = "-D" + KUBERNETES_CLUSTER_ID_PROPERTY + "=" + clusterId;
        break;
      default:
        throw new IllegalStateException("Unsupported running target: " + target.value);
    }

    Preconditions.checkArgument(resource.getProperties().containsKey(FlinkOptimizer.JOB_ID_PROPERTY),
        "Cannot find {} from optimizer properties", FlinkOptimizer.JOB_ID_PROPERTY);
    String jobId = resource.getProperties().get(FlinkOptimizer.JOB_ID_PROPERTY);
    try {
      String exportCmd = String.join(" && ", exportSystemProperties());
      String releaseCmd = String.format("%s && %s/bin/flink cancel -t %s %s %s",
          exportCmd, flinkHome, target.value, options, jobId);
      String[] cmd = {"/bin/sh", "-c", releaseCmd};
      LOG.info("Releasing flink optimizer using command:" + releaseCmd);
      Runtime.getRuntime().exec(cmd);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to release flink optimizer.", e);
    }
  }

  protected void addKubernetesProperties(Resource resource) {
    String clusterId = "amoro-" + resource.getGroupName() + "-optimizer-" + resource.getResourceId();
    resource.getProperties().put(FLINK_PARAMETER_PREFIX + "kubernetes.cluster-id", clusterId);

    String[] labels = {
        "amoro.optimizing-group=" + resource.getGroupName(),
        "amoro.optimizer-implement=flink/kubernetes-native",
        "amoro.optimizer-id=" + resource.getResourceId()
    };
    String resourceLabel = Joiner.on(',').join(labels);
    resource.getProperties().put(FLINK_PARAMETER_PREFIX + "kubernetes.taskmanager.labels", resourceLabel);
    resource.getProperties().put(FLINK_PARAMETER_PREFIX + "kubernetes.jobmanager.labels", resourceLabel);
  }

  private enum Target {
    YarnPreJob("yarn-per-job", false),
    YarnApplication("yarn-application", true),
    KubernetesApplication("kubernetes-application", true);

    final String value;
    final boolean applicationMode;

    Target(String value, boolean applicationMode) {
      this.value = value;
      this.applicationMode = applicationMode;
    }

    public static Target valueToEnum(String value) {
      return Arrays.stream(values()).filter(t -> t.value.equalsIgnoreCase(value)).findFirst()
          .orElseThrow(() -> new IllegalArgumentException("can't parse value: " + value));
    }
  }
}
