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
import org.apache.amoro.shade.guava32.com.google.common.base.Function;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
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
  public static final String ENV_HADOOP_USER_NAME = "HADOOP_USER_NAME";
  private static final String DEFAULT_JOB_URI = "/plugin/optimizer/spark/optimizer-job.jar";
  private static final String SPARK_JOB_MAIN_CLASS =
      "org.apache.amoro.optimizer.spark.SparkOptimizer";
  public static final String SPARK_MASTER = "master";
  public static final String SPARK_DEPLOY_MODE = "deploy-mode";
  public static final String SPARK_JOB_URI = "job-uri";
  public static final String YARN_APPLICATION_ID_PROPERTY = "yarn-application-id";
  public static final String KUBERNETES_SUBMISSION_ID_PROPERTY = "kubernetes-submission-id";
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
  private String sparkMaster;
  private DeployMode deployMode;
  private String sparkHome;
  private String jobUri;

  @Override
  public void init(String name, Map<String, String> containerProperties) {
    super.init(name, containerProperties);
    this.sparkHome = getSparkHome();
    this.sparkMaster = containerProperties.getOrDefault(SPARK_MASTER, "yarn");
    Preconditions.checkArgument(
        StringUtils.isNotEmpty(sparkMaster), "The property: %s is required", sparkMaster);
    String runMode =
        Optional.ofNullable(containerProperties.get(SPARK_DEPLOY_MODE))
            .orElse(DeployMode.CLIENT.getValue());
    this.deployMode = DeployMode.valueToEnum(runMode);
    String jobUri = containerProperties.get(SPARK_JOB_URI);
    if (deployMode.equals(DeployMode.CLUSTER.name())) {
      Preconditions.checkArgument(
          StringUtils.isNotEmpty(jobUri),
          "The property: %s is required if running mode in cluster mode.",
          SPARK_JOB_URI);
    }
    if (StringUtils.isEmpty(jobUri)) {
      jobUri = amsHome + DEFAULT_JOB_URI;
    }
    this.jobUri = jobUri;
    SparkOptimizerContainer.SparkConf sparkConf =
        SparkOptimizerContainer.SparkConf.buildFor(loadSparkConfig(), containerProperties).build();
    if (deployedOnKubernetes()) {
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
    try {
      String exportCmd = String.join(" && ", exportSystemProperties());
      String startUpCmd = String.format("%s && %s", exportCmd, startUpArgs);
      String[] cmd = {"/bin/sh", "-c", startUpCmd};
      LOG.info("Starting spark optimizer using command : {}", startUpCmd);
      Process exec = Runtime.getRuntime().exec(cmd);
      Map<String, String> startUpStatesMap = Maps.newHashMap();
      if (deployedOnKubernetes()) {
        SparkOptimizerContainer.SparkConf sparkConf =
            SparkOptimizerContainer.SparkConf.buildFor(loadSparkConfig(), getContainerProperties())
                .withGroupProperties(resource.getProperties())
                .build();
        String namespace =
            StringUtils.defaultIfEmpty(
                sparkConf.configValue(SparkConfKeys.KUBERNETES_NAMESPACE), "default");
        startUpStatesMap.put(
            KUBERNETES_SUBMISSION_ID_PROPERTY,
            String.format("%s:%s", namespace, kubernetesDriverName(resource)));
      } else {
        String applicationId = fetchCommandOutput(exec, yarnApplicationIdReader);
        if (applicationId != null) {
          startUpStatesMap.put(YARN_APPLICATION_ID_PROPERTY, applicationId);
        }
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

    // Default enable the spark DRA(dynamic resource allocation)
    resourceSparkConf.putToOptions(
        SparkConfKeys.KUBERNETES_DRA_ENABLED,
        StringUtils.defaultIfEmpty(
            resourceSparkConf.configValue(SparkConfKeys.KUBERNETES_DRA_ENABLED), "true"));
    resourceSparkConf.putToOptions(
        SparkConfKeys.KUBERNETES_DRA_MAX_EXECUTORS,
        StringUtils.defaultIfEmpty(
            resourceSparkConf.configValue(SparkConfKeys.KUBERNETES_DRA_MAX_EXECUTORS),
            String.valueOf(resource.getThreadCount())));

    if (deployedOnKubernetes()) {
      addKubernetesProperties(resource, resourceSparkConf);
    }
    String sparkOptions = resourceSparkConf.toConfOptions();
    String proxyUser =
        getContainerProperties()
            .getOrDefault(
                OptimizerProperties.EXPORT_PROPERTY_PREFIX + ENV_HADOOP_USER_NAME, "hadoop");
    String jobArgs = super.buildOptimizerStartupArgsString(resource);
    // ./bin/spark-submit --master <master> --deploy-mode=<sparkMode> <options> --proxy-user <user>
    // --class
    // <main-class>
    // <job-file>
    // <arguments>
    //  options: --conf <property=value>
    return String.format(
        "%s/bin/spark-submit --master %s --deploy-mode=%s %s --proxy-user %s --class %s %s %s",
        sparkHome,
        sparkMaster,
        deployMode.getValue(),
        sparkOptions,
        proxyUser,
        SPARK_JOB_MAIN_CLASS,
        jobUri,
        jobArgs);
  }

  private Map<String, String> loadSparkConfig() {
    try {
      return Arrays.stream(new org.apache.spark.SparkConf().getAll())
          .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
    } catch (Exception e) {
      LOG.error("Load spark conf failed.", e);
      return Collections.emptyMap();
    }
  }

  private boolean deployedOnKubernetes() {
    return sparkMaster.startsWith("k8s://");
  }

  private void addKubernetesProperties(
      Resource resource, SparkOptimizerContainer.SparkConf sparkConf) {
    String driverName = kubernetesDriverName(resource);
    sparkConf.putToOptions(
        SparkOptimizerContainer.SparkConfKeys.KUBERNETES_DRIVER_NAME, driverName);

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
    if (deployedOnKubernetes()) {
      releaseCommand = buildReleaseKubernetesCommand(resource);
    } else {
      releaseCommand = buildReleaseYarnCommand(resource);
    }
    try {
      String exportCmd = String.join(" && ", exportSystemProperties());
      String releaseCmd = exportCmd + " && " + releaseCommand;
      String[] cmd = {"/bin/sh", "-c", releaseCmd};
      LOG.info("Releasing spark optimizer using command: {}", releaseCmd);
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
    return String.format(
        "%s/bin/spark-submit --kill %s --master %s", sparkHome, applicationId, sparkMaster);
  }

  private String buildReleaseKubernetesCommand(Resource resource) {
    Map<String, String> sparkConfig = loadSparkConfig();
    Preconditions.checkArgument(
        resource.getProperties().containsKey(KUBERNETES_SUBMISSION_ID_PROPERTY),
        "Cannot find {} from optimizer start up stats.",
        KUBERNETES_SUBMISSION_ID_PROPERTY);
    SparkOptimizerContainer.SparkConf resourceSparkConf =
        SparkOptimizerContainer.SparkConf.buildFor(sparkConfig, getContainerProperties())
            .withGroupProperties(resource.getProperties())
            .build();
    String sparkOptions = resourceSparkConf.toConfOptions();
    String submissionId = resource.getProperties().get(KUBERNETES_SUBMISSION_ID_PROPERTY);
    return String.format(
        "%s/bin/spark-submit --kill %s --master %s %s",
        sparkHome, submissionId, sparkMaster, sparkOptions);
  }

  private String getSparkHome() {
    String sparkHome = getContainerProperties().get(SPARK_HOME_PROPERTY);
    Preconditions.checkNotNull(
        sparkHome, "Container property: %s is required", SPARK_HOME_PROPERTY);
    return sparkHome.replaceAll("/$", "");
  }

  private String kubernetesDriverName(Resource resource) {
    return "amoro-optimizer-" + resource.getResourceId();
  }

  private enum DeployMode {
    CLIENT("client"),
    CLUSTER("cluster");

    private final String value;

    DeployMode(String value) {
      this.value = value;
    }

    public static DeployMode valueToEnum(String value) {
      return Arrays.stream(values())
          .filter(t -> t.value.equalsIgnoreCase(value))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("can't parse value: " + value));
    }

    public String getValue() {
      return value;
    }
  }

  public static class SparkConfKeys {
    public static final String KUBERNETES_IMAGE_REF = "spark.kubernetes.container.image";
    public static final String KUBERNETES_DRIVER_NAME = "spark.kubernetes.driver.pod.name";
    public static final String KUBERNETES_NAMESPACE = "spark.kubernetes.namespace";
    public static final String KUBERNETES_EXECUTOR_LABEL_PREFIX = "spark.kubernetes.driver.label.";
    public static final String KUBERNETES_DRIVER_LABEL_PREFIX = "spark.kubernetes.driver.label.";
    public static final String KUBERNETES_DRA_ENABLED = "spark.dynamicAllocation.enabled";
    public static final String KUBERNETES_DRA_MAX_EXECUTORS =
        "spark.dynamicAllocation.maxExecutors";
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
