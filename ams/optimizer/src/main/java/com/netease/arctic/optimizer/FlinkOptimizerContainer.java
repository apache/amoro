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

import com.netease.arctic.ams.api.PropertyNames;
import com.netease.arctic.ams.api.resource.Resource;
import com.netease.arctic.optimizer.flink.FlinkOptimizer;
import com.netease.arctic.optimizer.util.FlinkConf;
import com.netease.arctic.optimizer.util.PropertyUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.annotations.VisibleForTesting;
import org.apache.iceberg.relocated.com.google.common.base.Function;
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

public class FlinkOptimizerContainer extends AbstractResourceContainer {
  private static final Logger LOG = LoggerFactory.getLogger(FlinkOptimizerContainer.class);

  public static final String FLINK_HOME_PROPERTY = "flink-home";
  public static final String FLINK_CONFIG_PATH = "/conf";
  public static final String FLINK_CONFIG_YAML = "/flink-conf.yaml";
  public static final String ENV_FLINK_CONF_DIR = "FLINK_CONF_DIR";

  public static final String TASK_MANAGER_MEMORY_PROPERTY = "taskmanager.memory";

  public static final String JOB_MANAGER_MEMORY_PROPERTY = "jobmanager.memory";
  public static final String FLINK_RUN_TARGET = "target";
  public static final String FLINK_JOB_URI = "job-uri";

  public static final String YARN_APPLICATION_ID_PROPERTY = "yarn-application-id";
  public static final String KUBERNETES_CLUSTER_ID_PROPERTY = "kubernetes-cluster-id";

  private static final Pattern APPLICATION_ID_PATTERN = Pattern.compile("(.*)application_(\\d+)_(\\d+)");
  private static final int MAX_READ_APP_ID_TIME = 600000; //10 min

  private static final Function<String, String> yarnApplicationIdReader = readLine -> {
    Matcher matcher = APPLICATION_ID_PATTERN.matcher(readLine);
    if (matcher.matches()) {
      return String.format("application_%s_%s", matcher.group(2), matcher.group(3));
    }
    return null;
  };

  private Target target;
  private String flinkHome;
  private String flinkConfDir;
  private String jobUri;

  @Override
  public void init(String name, Map<String, String> containerProperties) {
    super.init(name, containerProperties);
    this.flinkHome = getFlinkHome();
    this.flinkConfDir = getFlinkConfDir();

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
      FlinkConf flinkConf = FlinkConf.buildFor(
          loadFlinkConfig(),
          containerProperties
      ).build();

      String imageRef = flinkConf.configValue(FlinkConf.PropertyKeys.KUBERNETES_IMAGE_REF);
      Preconditions.checkArgument(
          StringUtils.isNotEmpty(imageRef),
          "FlinkOptimizerContainer init error, The flink-conf: " +
              FlinkConf.PropertyKeys.KUBERNETES_IMAGE_REF + " is required if run target is " + target.value);
    }
  }

  @Override
  protected Map<String, String> doScaleOut(Resource resource) {
    String startUpArgs = this.buildOptimizerStartupArgsString(resource);
    Runtime runtime = Runtime.getRuntime();
    try {
      String exportCmd = String.join(" && ", exportSystemProperties());
      String startUpCmd = String.format("%s && %s", exportCmd, startUpArgs);
      String[] cmd = {"/bin/sh", "-c", startUpCmd};
      LOG.info("Starting flink optimizer using command : {}", startUpCmd);
      Process exec = runtime.exec(cmd);
      Map<String, String> startUpStatesMap = Maps.newHashMap();
      String applicationId = fetchCommandOutput(exec, yarnApplicationIdReader);
      switch (target) {
        case YarnPreJob:
        case YarnApplication:
          if (applicationId != null) {
            startUpStatesMap.put(YARN_APPLICATION_ID_PROPERTY, applicationId);
          }
          break;
        case KubernetesApplication:
          startUpStatesMap.put(KUBERNETES_CLUSTER_ID_PROPERTY, kubernetesClusterId(resource));
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

    FlinkConf resourceFlinkConf = FlinkConf.buildFor(flinkConfig, getContainerProperties())
        .withGroupProperties(resource.getProperties())
        .build();

    long jobManagerMemory = getMemorySizeValue(properties, resourceFlinkConf,
        JOB_MANAGER_MEMORY_PROPERTY,
        FlinkConf.PropertyKeys.JOB_MANAGER_TOTAL_PROCESS_MEMORY);
    long taskManagerMemory = getMemorySizeValue(properties, resourceFlinkConf,
        TASK_MANAGER_MEMORY_PROPERTY,
        FlinkConf.PropertyKeys.TASK_MANAGER_TOTAL_PROCESS_MEMORY);

    resourceFlinkConf.putToOptions(
        FlinkConf.PropertyKeys.JOB_MANAGER_TOTAL_PROCESS_MEMORY,
        jobManagerMemory + "m"
    );
    resourceFlinkConf.putToOptions(
        FlinkConf.PropertyKeys.TASK_MANAGER_TOTAL_PROCESS_MEMORY,
        taskManagerMemory + "m"
    );

    String flinkAction = target.applicationMode ? "run-application" : "run";
    if (Target.KubernetesApplication == target) {
      addKubernetesProperties(resource, resourceFlinkConf);
    }
    String flinkOptions = resourceFlinkConf.toCliOptions();

    String jobArgs = super.buildOptimizerStartupArgsString(resource);
    // ./bin/flink ACTION --target=TARGET OPTIONS -c <main-class> <job-file> <arguments>
    //  options: -D<property=value>
    return String.format("%s/bin/flink %s --target=%s %s -c %s %s %s",
        flinkHome, flinkAction, target.value, flinkOptions, FlinkOptimizer.class.getName(), jobUri, jobArgs);
  }

  private Map<String, String> loadFlinkConfig() {
    try {
      return new Yaml().load(Files.newInputStream(Paths.get(flinkConfDir + FLINK_CONFIG_YAML)));
    } catch (IOException e) {
      LOG.error("load flink conf yaml failed: {}", e.getMessage());
      return Collections.emptyMap();
    }
  }

  private void addKubernetesProperties(Resource resource, FlinkConf flinkConf) {
    String clusterId = kubernetesClusterId(resource);
    flinkConf.putToOptions(FlinkConf.PropertyKeys.KUBERNETES_CLUSTER_ID, clusterId);

    String[] labels = {
        "amoro.optimizing-group:" + resource.getGroupName(),
        "amoro.optimizer-implement:flink-native-kubernetes",
        "amoro.optimizer-id:" + resource.getResourceId()
    };
    String resourceLabel = Joiner.on(',').join(labels);
    flinkConf.putToOptions(FlinkConf.PropertyKeys.KUBERNETES_TASKMANAGER_LABLES, resourceLabel);
    flinkConf.putToOptions(FlinkConf.PropertyKeys.KUBERNETES_JOBMANAGER_LABLES, resourceLabel);
  }

  /**
   * get jobManager and taskManager memory.
   * An example of using Jobmanager memory parameters is as follows:
   * jobmanager.memory: 1024
   * flink-conf.jobmanager.memory.process.size: 1024M
   * flink-conf.yaml
   * Prioritize from high to low.
   */
  @VisibleForTesting
  protected long getMemorySizeValue(
      Map<String, String> resourceProperties, FlinkConf conf,
      String resourcePropertyKey, String flinkConfKey
  ) {
    String value = resourceProperties.get(resourcePropertyKey);
    if (value == null) {
      value = conf.configValue(flinkConfKey);
    }
    return parseMemorySize(value);
  }

  /**
   * memory conversion of units method, supporting m and g units
   */
  @VisibleForTesting
  protected long parseMemorySize(String memoryStr) {
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

  private <T> T fetchCommandOutput(Process exec, Function<String, T> commandReader) {
    T value = null;
    try (InputStreamReader inputStreamReader = new InputStreamReader(exec.getInputStream())) {
      try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < MAX_READ_APP_ID_TIME) {
          String readLine = bufferedReader.readLine();
          if (readLine == null) {
            break;
          }
          LOG.info("{}", readLine);
          if (commandReader != null) {
            value = commandReader.apply(readLine);
          }
        }
        return value;
      }
    } catch (IOException e) {
      LOG.error("Read application id from output failed", e);
      return null;
    }
  }

  @Override
  public void releaseOptimizer(Resource resource) {
    String releaseCommand;
    switch (target) {
      case YarnApplication:
      case YarnPreJob:
        releaseCommand = buildReleaseYarnCommand(resource);
        break;
      case KubernetesApplication:
        releaseCommand = buildReleaseKubernetesCommand(resource);
        break;
      default:
        throw new IllegalStateException("Unsupported running target: " + target.value);
    }

    try {
      String exportCmd = String.join(" && ", exportSystemProperties());
      String releaseCmd = exportCmd + " && " + releaseCommand;
      String[] cmd = {"/bin/sh", "-c", releaseCmd};
      LOG.info("Releasing flink optimizer using command: " + releaseCmd);
      Process exec = Runtime.getRuntime().exec(cmd);
      fetchCommandOutput(exec, null);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to release flink optimizer.", e);
    }
  }

  private String buildReleaseYarnCommand(Resource resource) {
    Preconditions.checkArgument(resource.getProperties().containsKey(YARN_APPLICATION_ID_PROPERTY),
        "Cannot find {} from optimizer start up stats", YARN_APPLICATION_ID_PROPERTY);
    String applicationId = resource.getProperties().get(YARN_APPLICATION_ID_PROPERTY);
    String options = "-Dyarn.application.id=" + applicationId;

    Preconditions.checkArgument(resource.getProperties().containsKey(FlinkOptimizer.JOB_ID_PROPERTY),
        "Cannot find {} from optimizer properties", FlinkOptimizer.JOB_ID_PROPERTY);
    String jobId = resource.getProperties().get(FlinkOptimizer.JOB_ID_PROPERTY);
    return String.format("%s/bin/flink cancel -t %s %s %s",
        flinkHome, target.value, options, jobId);
  }

  private String buildReleaseKubernetesCommand(Resource resource) {
    Preconditions.checkArgument(resource.getProperties().containsKey(KUBERNETES_CLUSTER_ID_PROPERTY),
        "Cannot find {} from optimizer start up stats.", KUBERNETES_CLUSTER_ID_PROPERTY);
    String clusterId = resource.getProperties().get(KUBERNETES_CLUSTER_ID_PROPERTY);


    FlinkConf conf = FlinkConf.buildFor(loadFlinkConfig(), getContainerProperties())
        .withGroupProperties(resource.getProperties()).build();
    conf.putToOptions(FlinkConf.PropertyKeys.KUBERNETES_CLUSTER_ID, clusterId);

    String options = conf.toCliOptions();

    // job-manager rest api can't be visited from k8s cluster outside, so using kubernetes-session command to kill it
    // echo 'stop' | $FLINK_HOME/bin/kubertnetes-session.sh
    return String.format(" echo 'stop' | %s/bin/kubernetes-session.sh %s -Dexecution.attached=true",
        this.flinkHome, options);
  }

  private String getFlinkHome() {
    String flinkHome = PropertyUtil.getRequiredNotNull(getContainerProperties(), FLINK_HOME_PROPERTY);
    return flinkHome.replaceAll("/$", "");
  }

  private String getFlinkConfDir() {
    String flinkConfDir = getContainerProperties().get(PropertyNames.EXPORT_PROPERTY_PREFIX + ENV_FLINK_CONF_DIR);
    if (StringUtils.isNotEmpty(flinkConfDir)) {
      return flinkConfDir;
    }
    return this.flinkHome + FLINK_CONFIG_PATH;
  }

  private String kubernetesClusterId(Resource resource) {
    return "amoro-optimizer-" + resource.getResourceId();
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
