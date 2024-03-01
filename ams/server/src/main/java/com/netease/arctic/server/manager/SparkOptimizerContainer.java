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

package com.netease.arctic.server.manager;

import com.netease.arctic.ams.api.OptimizerProperties;
import com.netease.arctic.ams.api.resource.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.relocated.com.google.common.base.Function;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SparkOptimizerContainer extends AbstractResourceContainer {
  private static final Logger LOG = LoggerFactory.getLogger(SparkOptimizerContainer.class);

  public static final String SPARK_HOME_PROPERTY = "spark-home";
  public static final String SPARK_CONFIG_PATH = "/conf";
  public static final String ENV_SPARK_CONF_DIR = "SPARK_CONF_DIR";

  private static final String DEFAULT_JOB_URI = "/plugin/optimizer/spark/optimizer-job.jar";
  private static final String SPARK_JOB_MAIN_CLASS =
      "com.netease.arctic.optimizer.spark.SparkOptimizer";

  public static final String SPARK_DEPLOY_MODE = "deploy-mode";
  public static final String SPARK_JOB_URI = "job-uri";

  public static final String YARN_APPLICATION_ID_PROPERTY = "yarn-application-id";
  public static final String KUBERNETES_APPLICATION_ID_PROPERTY = "kubernetes-application-id";

  private static final Pattern APPLICATION_ID_PATTERN =
      Pattern.compile("(.*)application_(\\d+)_(\\d+)");
  private static final int MAX_READ_APP_ID_TIME = 600000; // 10 min

  private static final Function<String, String> yarnApplicationIdReader =
      readLine -> {
        Matcher matcher = APPLICATION_ID_PATTERN.matcher(readLine);
        if (matcher.matches()) {
          return String.format("application_%s_%s", matcher.group(2), matcher.group(3));
        }
        return null;
      };

  private DeployMode deployMode;
  private String sparkHome;

  private String sparkConfDir;
  private String jobUri;

  @Override
  public void init(String name, Map<String, String> containerProperties) {
    super.init(name, containerProperties);
    this.sparkHome = getSparkHome();
    this.sparkConfDir = getSparkConfDir();

    String runMode =
        Optional.ofNullable(containerProperties.get(SPARK_DEPLOY_MODE))
            .orElse(DeployMode.YARN_CLIENT.getValue());
    this.deployMode = DeployMode.valueToEnum(runMode);
    String jobUri = containerProperties.get(SPARK_JOB_URI);
    if (deployMode.isClusterMode()) {
      Preconditions.checkArgument(
          StringUtils.isNotEmpty(jobUri),
          "The property: %s is required if running mode in cluster mode.",
          SPARK_JOB_URI);
    }
    if (StringUtils.isEmpty(jobUri)) {
      jobUri = amsHome + DEFAULT_JOB_URI;
    }
    this.jobUri = jobUri;

    if (DeployMode.KUBERNETES_CLUSTER == deployMode) {
      SparkOptimizerContainer.SparkConf sparkConf =
          SparkOptimizerContainer.SparkConf.buildFor(loadSparkConfig(), containerProperties)
              .build();

      String imageRef =
          sparkConf.configValue(SparkOptimizerContainer.SparkConfKeys.KUBERNETES_IMAGE_REF);
      Preconditions.checkArgument(
          StringUtils.isNotEmpty(imageRef),
          "The spark-conf: %s is required if running mode is %s",
          SparkOptimizerContainer.SparkConfKeys.KUBERNETES_IMAGE_REF,
          deployMode.getValue());
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
      LOG.info("Starting spark optimizer using command : {}", startUpCmd);
      Process exec = runtime.exec(cmd);
      Map<String, String> startUpStatesMap = Maps.newHashMap();
      String applicationId = fetchCommandOutput(exec, yarnApplicationIdReader);
      switch (deployMode) {
        case YARN_CLIENT:
        case YARN_CLUSTER:
          if (applicationId != null) {
            startUpStatesMap.put(YARN_APPLICATION_ID_PROPERTY, applicationId);
          }
          break;
        case KUBERNETES_CLUSTER:
          startUpStatesMap.put(KUBERNETES_APPLICATION_ID_PROPERTY, kubernetesAppId(resource));
          break;
      }
      return startUpStatesMap;
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to scale out spark optimizer.", e);
    }
  }

  @Override
  protected String buildOptimizerStartupArgsString(Resource resource) {
    Map<String, String> sparkConfig = loadSparkConfig();

    SparkOptimizerContainer.SparkConf resourceSparkConf =
        SparkOptimizerContainer.SparkConf.buildFor(sparkConfig, getContainerProperties())
            .withGroupProperties(resource.getProperties())
            .build();

    String sparkMode = deployMode.isClusterMode() ? "cluster" : "client";
    if (DeployMode.KUBERNETES_CLUSTER == deployMode) {
      addKubernetesProperties(resource, resourceSparkConf);
    } else if (DeployMode.YARN_CLIENT == deployMode || DeployMode.YARN_CLUSTER == deployMode) {
      addYarnProperties(resourceSparkConf);
    }
    String sparkOptions = resourceSparkConf.toConfOptions();

    String jobArgs = super.buildOptimizerStartupArgsString(resource);
    // ./bin/spark-submit ACTION --deploy-mode=<sparkMode> OPTIONS --class <main-class> <job-file>
    // <arguments>
    //  options: --conf <property=value>
    return String.format(
        "%s/bin/spark-submit --deploy-mode=%s %s --class %s %s %s",
        sparkHome, sparkMode, sparkOptions, SPARK_JOB_MAIN_CLASS, jobUri, jobArgs);
  }

  private Map<String, String> loadSparkConfig() {
    try {
      return Arrays.stream(new org.apache.spark.SparkConf().getAll())
          .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
    } catch (Exception e) {
      LOG.error("load spark conf failed: {}", e.getMessage());
      return Collections.emptyMap();
    }
  }

  private void addKubernetesProperties(
      Resource resource, SparkOptimizerContainer.SparkConf sparkConf) {
    String appId = kubernetesAppId(resource);
    sparkConf.putToOptions(SparkOptimizerContainer.SparkConfKeys.KUBERNETES_DRIVER_NAME, appId);

    // add labels to the driver pod
    sparkConf.putToOptions(
        SparkConfKeys.KUBERNETES_DRIVER_LABEL_PREFIX + "optimizer-group", resource.getGroupName());
    sparkConf.putToOptions(
        SparkConfKeys.KUBERNETES_DRIVER_LABEL_PREFIX + "optimizer-implementation",
        "spark-native-kubernetes");
    sparkConf.putToOptions(
        SparkConfKeys.KUBERNETES_DRIVER_LABEL_PREFIX + "optimizer-id", resource.getResourceId());

    // add labels to the executor pod
    sparkConf.putToOptions(
        SparkConfKeys.KUBERNETES_EXECUTOR_LABEL_PREFIX + "optimizer-group",
        resource.getGroupName());
    sparkConf.putToOptions(
        SparkConfKeys.KUBERNETES_EXECUTOR_LABEL_PREFIX + "optimizer-implementation",
        "spark-native-kubernetes");
    sparkConf.putToOptions(
        SparkConfKeys.KUBERNETES_EXECUTOR_LABEL_PREFIX + "optimizer-id", resource.getResourceId());
  }

  private void addYarnProperties(SparkOptimizerContainer.SparkConf sparkConf) {
    // Load optimizer jar first
    sparkConf.putToOptions(
        SparkConfKeys.DRIVER_CLASSPATH_INCLUDE_USER_JAR,
        SparkOptimizerContainer.SparkConfKeys.CLASSPATH_INCLUDE_USER_JAR_DEFAULT);
    sparkConf.putToOptions(
        SparkConfKeys.EXECUTOR_CLASSPATH_INCLUDE_USER_JAR,
        SparkOptimizerContainer.SparkConfKeys.CLASSPATH_INCLUDE_USER_JAR_DEFAULT);
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
          if ((value = commandReader.apply(readLine)) != null) {
            break;
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
    switch (deployMode) {
      case YARN_CLIENT:
      case YARN_CLUSTER:
        releaseCommand = buildReleaseYarnCommand(resource);
        break;
      case KUBERNETES_CLUSTER:
        releaseCommand = buildReleaseKubernetesCommand(resource);
        break;
      default:
        throw new IllegalStateException("Unsupported running mode: " + deployMode.getValue());
    }

    try {
      String exportCmd = String.join(" && ", exportSystemProperties());
      String releaseCmd = exportCmd + " && " + releaseCommand;
      String[] cmd = {"/bin/sh", "-c", releaseCmd};
      LOG.info("Releasing spark optimizer using command: " + releaseCmd);
      Runtime.getRuntime().exec(cmd);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to release spark optimizer.", e);
    }
  }

  private String buildReleaseYarnCommand(Resource resource) {
    Preconditions.checkArgument(
        resource.getProperties().containsKey(YARN_APPLICATION_ID_PROPERTY),
        "Cannot find {} from optimizer start up stats",
        YARN_APPLICATION_ID_PROPERTY);
    String applicationId = resource.getProperties().get(YARN_APPLICATION_ID_PROPERTY);
    return String.format("%s/bin/spark-submit --kill %s", sparkHome, applicationId);
  }

  private String buildReleaseKubernetesCommand(Resource resource) {
    Preconditions.checkArgument(
        resource.getProperties().containsKey(KUBERNETES_APPLICATION_ID_PROPERTY),
        "Cannot find {} from optimizer start up stats.",
        KUBERNETES_APPLICATION_ID_PROPERTY);
    String appId = resource.getProperties().get(KUBERNETES_APPLICATION_ID_PROPERTY);
    return String.format("%s/bin/spark-submit --kill %s", this.sparkHome, appId);
  }

  private String getSparkHome() {
    String sparkHome = getContainerProperties().get(SPARK_HOME_PROPERTY);
    Preconditions.checkNotNull(
        sparkHome, "Container property: %s is required", SPARK_HOME_PROPERTY);
    return sparkHome.replaceAll("/$", "");
  }

  private String getSparkConfDir() {
    String sparkConfDir =
        getContainerProperties()
            .get(OptimizerProperties.EXPORT_PROPERTY_PREFIX + ENV_SPARK_CONF_DIR);
    if (StringUtils.isNotEmpty(sparkConfDir)) {
      return sparkConfDir;
    }
    return this.sparkHome + SPARK_CONFIG_PATH;
  }

  private String kubernetesAppId(Resource resource) {
    return "amoro-optimizer-" + resource.getResourceId();
  }

  private enum DeployMode {
    YARN_CLIENT("yarn-client", false),
    YARN_CLUSTER("yarn-cluster", true),
    KUBERNETES_CLUSTER("kubernetes-cluster", true);

    private final String value;
    private final boolean isClusterMode;

    DeployMode(String value, boolean mode) {
      this.value = value;
      this.isClusterMode = mode;
    }

    public static DeployMode valueToEnum(String value) {
      return Arrays.stream(values())
          .filter(t -> t.value.equalsIgnoreCase(value))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("can't parse value: " + value));
    }

    public boolean isClusterMode() {
      return isClusterMode;
    }

    public String getValue() {
      return value;
    }
  }

  public static class SparkConfKeys {
    public static final String DRIVER_CLASSPATH_INCLUDE_USER_JAR =
        "spark.driver.userClassPathFirst";

    public static final String EXECUTOR_CLASSPATH_INCLUDE_USER_JAR =
        "spark.executor.userClassPathFirst";
    public static final String CLASSPATH_INCLUDE_USER_JAR_DEFAULT = "true";

    public static final String KUBERNETES_IMAGE_REF = "spark.kubernetes.container.image";
    public static final String KUBERNETES_DRIVER_NAME = "spark.kubernetes.driver.pod.name";
    public static final String KUBERNETES_EXECUTOR_LABEL_PREFIX = "spark.kubernetes.driver.label.";
    public static final String KUBERNETES_DRIVER_LABEL_PREFIX = "spark.kubernetes.driver.label.";
  }

  public static class SparkConf {

    public static final String SPARK_PARAMETER_PREFIX = "spark-conf.";

    final Map<String, String> sparkConf;
    final Map<String, String> sparkOptions;

    public SparkConf(Map<String, String> sparkConf, Map<String, String> sparkOptions) {
      this.sparkConf = sparkConf;
      this.sparkOptions = sparkOptions;
    }

    public String configValue(String key) {
      if (sparkOptions.containsKey(key)) {
        return sparkOptions.get(key);
      }
      return sparkConf.get(key);
    }

    public void putToOptions(String key, String value) {
      this.sparkOptions.put(key, value);
    }

    /**
     * The properties with prefix "spark-conf." will be merged with the following priority and
     * transformed into Spark options. 1. optimizing-group properties 2. optimizing-container
     * properties
     *
     * @return spark options, format is `--conf key1=value1 --conf key2=value2`
     */
    public String toConfOptions() {
      return sparkOptions.entrySet().stream()
          .map(entry -> "--conf " + entry.getKey() + "=" + entry.getValue())
          .collect(Collectors.joining(" "));
    }

    public static SparkOptimizerContainer.SparkConf.Builder buildFor(
        Map<String, String> sparkConf, Map<String, String> containerProperties) {
      return new SparkOptimizerContainer.SparkConf.Builder(sparkConf, containerProperties);
    }

    public static class Builder {
      final Map<String, String> sparkConf;

      Map<String, String> containerProperties;
      Map<String, String> groupProperties = Collections.emptyMap();

      public Builder(Map<String, String> sparkConf, Map<String, String> containerProperties) {
        this.sparkConf = Maps.newHashMap(sparkConf);
        this.containerProperties =
            containerProperties == null ? Collections.emptyMap() : containerProperties;
      }

      public SparkOptimizerContainer.SparkConf.Builder withGroupProperties(
          Map<String, String> groupProperties) {
        this.groupProperties = groupProperties;
        return this;
      }

      public SparkOptimizerContainer.SparkConf build() {
        Map<String, String> options = Maps.newHashMap();
        this.containerProperties.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(SPARK_PARAMETER_PREFIX))
            .forEach(
                entry ->
                    options.put(
                        entry.getKey().substring(SPARK_PARAMETER_PREFIX.length()),
                        entry.getValue()));

        this.groupProperties.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(SPARK_PARAMETER_PREFIX))
            .forEach(
                entry ->
                    options.put(
                        entry.getKey().substring(SPARK_PARAMETER_PREFIX.length()),
                        entry.getValue()));

        return new SparkOptimizerContainer.SparkConf(this.sparkConf, options);
      }
    }
  }
}
