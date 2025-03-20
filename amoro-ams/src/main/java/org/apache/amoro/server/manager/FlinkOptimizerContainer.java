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
import org.apache.amoro.server.utils.FlinkClientUtil;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.Function;
import org.apache.amoro.shade.guava32.com.google.common.base.Joiner;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.base.Supplier;
import org.apache.amoro.shade.guava32.com.google.common.base.Suppliers;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.amoro.utils.MemorySize;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.JobID;
import org.apache.flink.client.program.rest.RestClusterClient;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.YamlParserUtils;
import org.apache.flink.core.execution.RestoreMode;
import org.apache.flink.runtime.messages.Acknowledge;
import org.apache.flink.runtime.rest.FileUpload;
import org.apache.flink.runtime.rest.RestClient;
import org.apache.flink.runtime.rest.handler.legacy.messages.ClusterOverviewWithVersion;
import org.apache.flink.runtime.rest.messages.EmptyMessageParameters;
import org.apache.flink.runtime.rest.messages.EmptyRequestBody;
import org.apache.flink.runtime.rest.util.RestConstants;
import org.apache.flink.runtime.webmonitor.handlers.JarListHeaders;
import org.apache.flink.runtime.webmonitor.handlers.JarListInfo;
import org.apache.flink.runtime.webmonitor.handlers.JarRunHeaders;
import org.apache.flink.runtime.webmonitor.handlers.JarRunMessageParameters;
import org.apache.flink.runtime.webmonitor.handlers.JarRunRequestBody;
import org.apache.flink.runtime.webmonitor.handlers.JarRunResponseBody;
import org.apache.flink.runtime.webmonitor.handlers.JarUploadHeaders;
import org.apache.flink.runtime.webmonitor.handlers.JarUploadResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FlinkOptimizerContainer extends AbstractResourceContainer {
  private static final Logger LOG = LoggerFactory.getLogger(FlinkOptimizerContainer.class);

  public static final String FLINK_HOME_PROPERTY = "flink-home";
  public static final String FLINK_CONFIG_PATH = "/conf";
  public static final String LEGACY_FLINK_CONFIG_YAML = "/flink-conf.yaml";
  // flink version >= 1.20 use it first
  public static final String FLINK_CONFIG_YAML = "/config.yaml";
  public static final String ENV_FLINK_CONF_DIR = "FLINK_CONF_DIR";
  public static final String FLINK_CLIENT_TIMEOUT_SECOND = "flink-client-timeout-second";

  private static final String DEFAULT_JOB_URI = "/plugin/optimizer/flink/optimizer-job.jar";
  private static final String FLINK_JOB_MAIN_CLASS =
      "org.apache.amoro.optimizer.flink.FlinkOptimizer";

  public static final String FLINK_RUN_TARGET = "target";
  public static final String FLINK_JOB_URI = "job-uri";

  public static final String YARN_APPLICATION_ID_PROPERTY = "yarn-application-id";
  public static final String KUBERNETES_CLUSTER_ID_PROPERTY = "kubernetes-cluster-id";
  public static final String SESSION_CLUSTER_JOB_ID = "session-cluster-job-id";

  private static final Pattern APPLICATION_ID_PATTERN =
      Pattern.compile("(.*)application_(\\d+)_(\\d+)");
  private static final int MAX_READ_APP_ID_TIME = 600000; // 10 min
  private static final long FLINK_CLIENT_TIMEOUT_SECOND_DEFAULT = 30; // 30s

  private static final Function<String, String> yarnApplicationIdReader =
      readLine -> {
        Matcher matcher = APPLICATION_ID_PATTERN.matcher(readLine);
        if (matcher.matches()) {
          return String.format("application_%s_%s", matcher.group(2), matcher.group(3));
        }
        return null;
      };

  private final Supplier<ExecutorService> clientExecutorServiceSupplier =
      Suppliers.memoize(
          () ->
              Executors.newFixedThreadPool(
                  5,
                  new ThreadFactoryBuilder()
                      .setNameFormat("flink-rest-cluster-client-io-%d")
                      .setDaemon(true)
                      .build()));

  private Target target;
  private String flinkHome;
  private String flinkConfDir;
  private String jobUri;
  private long flinkClientTimeoutSecond;

  @Override
  public void init(String name, Map<String, String> containerProperties) {
    super.init(name, containerProperties);
    this.flinkHome = getFlinkHome();
    this.flinkConfDir = getFlinkConfDir();
    this.flinkClientTimeoutSecond = getFlinkClientTimeoutSecond();

    String runTarget =
        Optional.ofNullable(containerProperties.get(FLINK_RUN_TARGET))
            .orElse(Target.YARN_PER_JOB.getValue());
    this.target = Target.valueToEnum(runTarget);
    String jobUri = containerProperties.get(FLINK_JOB_URI);
    if (target.isApplicationMode()) {
      Preconditions.checkArgument(
          StringUtils.isNotEmpty(jobUri),
          "The property: %s  is required if running target in application mode.",
          FLINK_JOB_URI);
    }
    if (StringUtils.isEmpty(jobUri)) {
      jobUri = amsHome + DEFAULT_JOB_URI;
    }
    this.jobUri = jobUri;

    if (Target.KUBERNETES_APPLICATION == target) {
      FlinkConf flinkConf = FlinkConf.buildFor(loadFlinkConfig(), containerProperties).build();

      String imageRef = flinkConf.configValue(FlinkConfKeys.KUBERNETES_IMAGE_REF);
      Preconditions.checkArgument(
          StringUtils.isNotEmpty(imageRef),
          "The flink-conf: %s is required if running target is %s",
          FlinkConfKeys.KUBERNETES_IMAGE_REF,
          target.getValue());
    }
  }

  @Override
  protected Map<String, String> doScaleOut(Resource resource) {
    if (target.runByFlinkRestClient()) {
      try {
        Configuration configuration =
            FlinkConf.buildFor(loadFlinkConfig(), getContainerProperties())
                .withGroupProperties(resource.getProperties())
                .build()
                .toConfiguration();
        JobID jobID = runFlinkOptimizerJob(resource, configuration);
        Map<String, String> startUpStatesMap = Maps.newHashMap();
        startUpStatesMap.put(SESSION_CLUSTER_JOB_ID, jobID.toString());
        return startUpStatesMap;
      } catch (Exception e) {
        throw new RuntimeException("Failed to scale out flink optimizer in session cluster.", e);
      }
    } else {
      String startUpArgs = this.buildOptimizerStartupArgsString(resource);
      Runtime runtime = Runtime.getRuntime();
      try {
        String exportCmd = String.join(" && ", exportSystemProperties());
        String startUpCmd = String.format("%s && %s", exportCmd, startUpArgs);
        String[] cmd = {"/bin/sh", "-c", startUpCmd};
        LOG.info("Starting flink optimizer using command : {}", startUpCmd);
        Process exec = runtime.exec(cmd);
        Map<String, String> startUpStatesMap = Maps.newHashMap();
        switch (target) {
          case YARN_PER_JOB:
          case YARN_APPLICATION:
            String applicationId = fetchCommandOutput(exec, yarnApplicationIdReader);
            if (applicationId != null) {
              startUpStatesMap.put(YARN_APPLICATION_ID_PROPERTY, applicationId);
            }
            break;
          case KUBERNETES_APPLICATION:
            startUpStatesMap.put(KUBERNETES_CLUSTER_ID_PROPERTY, kubernetesClusterId(resource));
            break;
        }
        return startUpStatesMap;
      } catch (IOException e) {
        throw new UncheckedIOException("Failed to scale out flink optimizer.", e);
      }
    }
  }

  @Override
  protected String buildOptimizerStartupArgsString(Resource resource) {
    Map<String, String> properties = resource.getProperties();
    Map<String, String> flinkConfig = loadFlinkConfig();

    FlinkConf resourceFlinkConf =
        FlinkConf.buildFor(flinkConfig, getContainerProperties())
            .withGroupProperties(resource.getProperties())
            .build();

    long jobManagerMemory =
        getMemorySizeValue(
            properties, resourceFlinkConf, FlinkConfKeys.JOB_MANAGER_TOTAL_PROCESS_MEMORY);
    long taskManagerMemory =
        getMemorySizeValue(
            properties, resourceFlinkConf, FlinkConfKeys.TASK_MANAGER_TOTAL_PROCESS_MEMORY);

    resourceFlinkConf.putToOptions(
        FlinkConfKeys.JOB_MANAGER_TOTAL_PROCESS_MEMORY,
        MemorySize.ofMebiBytes(jobManagerMemory).toString());
    resourceFlinkConf.putToOptions(
        FlinkConfKeys.TASK_MANAGER_TOTAL_PROCESS_MEMORY,
        MemorySize.ofMebiBytes(taskManagerMemory).toString());
    resourceFlinkConf.putToOptions(
        FlinkConfKeys.YARN_APPLICATION_JOB_NAME,
        String.join(
            "-", "Amoro-flink-optimizer", resource.getGroupName(), resource.getResourceId()));

    String flinkAction = target.isApplicationMode() ? "run-application" : "run";
    if (Target.KUBERNETES_APPLICATION == target) {
      addKubernetesProperties(resource, resourceFlinkConf);
    } else if (Target.YARN_PER_JOB == target || Target.YARN_APPLICATION == target) {
      addYarnProperties(resourceFlinkConf);
    }
    String flinkOptions = resourceFlinkConf.toCliOptions();

    String jobArgs = super.buildOptimizerStartupArgsString(resource);
    // ./bin/flink ACTION --target=TARGET OPTIONS -c <main-class> <job-file> <arguments>
    //  options: -D<property=value>
    return String.format(
        "%s/bin/flink %s --target=%s %s -c %s %s %s",
        flinkHome,
        flinkAction,
        target.getValue(),
        flinkOptions,
        FLINK_JOB_MAIN_CLASS,
        jobUri,
        jobArgs);
  }

  @VisibleForTesting
  protected Map<String, String> loadFlinkConfigForYAML(URL path) {
    this.flinkConfDir = Paths.get(path.getPath()).getParent().toString();
    return loadFlinkConfig();
  }

  /**
   * get flink config with config.yaml or flink-conf.yaml see <a
   * href="https://nightlies.apache.org/flink/flink-docs-release-1.20/docs/deployment/config/#flink-configuration-file"></a>
   *
   * @return flink config map
   */
  private Map<String, String> loadFlinkConfig() {
    try {
      Path flinkConfPath = Paths.get(flinkConfDir + FLINK_CONFIG_YAML);
      if (!Files.exists(flinkConfPath, LinkOption.NOFOLLOW_LINKS)) {
        flinkConfPath = Paths.get(flinkConfDir + LEGACY_FLINK_CONFIG_YAML);
        return new Yaml().load(Files.newInputStream(flinkConfPath));
      }
      Map<String, Object> configDocument =
          YamlParserUtils.loadYamlFile(new File(flinkConfPath.toUri()));
      return Maps.transformValues(
          flatten(configDocument, ""), value -> value == null ? null : value.toString());
    } catch (Exception e) {
      LOG.error("load flink conf yaml failed: {}", e.getMessage());
      return Collections.emptyMap();
    }
  }

  /**
   * Copy from flink 1.20 GlobalConfiguration.flatten Utils
   *
   * @param config
   * @param keyPrefix
   * @return
   */
  private static Map<String, Object> flatten(Map<String, Object> config, String keyPrefix) {
    final Map<String, Object> flattenedMap = new HashMap<>();
    config.forEach(
        (key, value) -> {
          String flattenedKey = keyPrefix + key;
          if (value instanceof Map) {
            Map<String, Object> e = (Map<String, Object>) value;
            flattenedMap.putAll(flatten(e, flattenedKey + "."));
          } else {
            if (value instanceof List) {
              flattenedMap.put(flattenedKey, YamlParserUtils.toYAMLString(value));
            } else {
              flattenedMap.put(flattenedKey, value);
            }
          }
        });

    return flattenedMap;
  }

  private void addKubernetesProperties(Resource resource, FlinkConf flinkConf) {
    String clusterId = kubernetesClusterId(resource);
    flinkConf.putToOptions(FlinkConfKeys.KUBERNETES_CLUSTER_ID, clusterId);

    String[] labels = {
      "amoro.optimizing-group:" + resource.getGroupName(),
      "amoro.optimizer-implement:flink-native-kubernetes",
      "amoro.optimizer-id:" + resource.getResourceId()
    };
    String resourceLabel = Joiner.on(',').join(labels);
    flinkConf.putToOptions(FlinkConfKeys.KUBERNETES_TASKMANAGER_LABELS, resourceLabel);
    flinkConf.putToOptions(FlinkConfKeys.KUBERNETES_JOBMANAGER_LABELS, resourceLabel);
  }

  private void addYarnProperties(FlinkConf flinkConf) {
    // Load optimizer jar first
    flinkConf.putToOptions(
        FlinkConfKeys.CLASSPATH_INCLUDE_USER_JAR, FlinkConfKeys.CLASSPATH_INCLUDE_USER_JAR_DEFAULT);
  }

  /**
   * get jobManager and taskManager memory. An example of using Jobmanager memory parameters is as
   * flink-conf.jobmanager.memory.process.size: 1024M flink-conf.yaml Prioritize from high to low.
   */
  @VisibleForTesting
  protected long getMemorySizeValue(
      Map<String, String> resourceProperties, FlinkConf conf, String flinkConfKey) {
    String value = resourceProperties.get(flinkConfKey);
    if (value == null) {
      value = conf.configValue(flinkConfKey);
    }
    return parseMemorySize(value);
  }

  /** memory conversion of units method, supporting m and g units */
  @VisibleForTesting
  protected long parseMemorySize(String memoryStr) {
    if (memoryStr == null || memoryStr.isEmpty()) {
      return 0;
    }

    return MemorySize.parse(memoryStr).getMebiBytes();
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
    if (target.runByFlinkRestClient()) {
      Preconditions.checkArgument(
          resource.getProperties().containsKey(SESSION_CLUSTER_JOB_ID),
          "Cannot find {} from optimizer start up stats",
          SESSION_CLUSTER_JOB_ID);
      String jobId = resource.getProperties().get(SESSION_CLUSTER_JOB_ID);
      Configuration configuration =
          FlinkConf.buildFor(loadFlinkConfig(), getContainerProperties())
              .withGroupProperties(resource.getProperties())
              .build()
              .toConfiguration();

      try (RestClusterClient<String> restClusterClient =
          FlinkClientUtil.getRestClusterClient(configuration)) {
        Acknowledge ignore =
            restClusterClient
                .cancel(JobID.fromHexString(jobId))
                .get(flinkClientTimeoutSecond, TimeUnit.SECONDS);
      } catch (TimeoutException timeoutException) {
        throw new RuntimeException(
            String.format(
                "Timed out stopping the optimizer in Flink cluster, "
                    + "please configure a larger timeout via '%s'",
                FLINK_CLIENT_TIMEOUT_SECOND));
      } catch (Exception e) {
        throw new RuntimeException("Failed to release flink optimizer", e);
      }
    } else {
      String releaseCommand;
      switch (target) {
        case YARN_APPLICATION:
        case YARN_PER_JOB:
          releaseCommand = buildReleaseYarnCommand(resource);
          break;
        case KUBERNETES_APPLICATION:
          releaseCommand = buildReleaseKubernetesCommand(resource);
          break;
        default:
          throw new IllegalStateException("Unsupported running target: " + target.getValue());
      }

      try {
        String exportCmd = String.join(" && ", exportSystemProperties());
        String releaseCmd = exportCmd + " && " + releaseCommand;
        String[] cmd = {"/bin/sh", "-c", releaseCmd};
        LOG.info("Releasing flink optimizer using command: {}", releaseCmd);
        Runtime.getRuntime().exec(cmd);
      } catch (IOException e) {
        throw new UncheckedIOException("Failed to release flink optimizer.", e);
      }
    }
  }

  private String buildReleaseYarnCommand(Resource resource) {
    Preconditions.checkArgument(
        resource.getProperties().containsKey(YARN_APPLICATION_ID_PROPERTY),
        "Cannot find {} from optimizer start up stats",
        YARN_APPLICATION_ID_PROPERTY);
    String applicationId = resource.getProperties().get(YARN_APPLICATION_ID_PROPERTY);
    String options = "-Dyarn.application.id=" + applicationId;

    Preconditions.checkArgument(
        resource.getProperties().containsKey(Resource.PROPERTY_JOB_ID),
        "Cannot find {} from optimizer properties",
        Resource.PROPERTY_JOB_ID);
    String jobId = resource.getProperties().get(Resource.PROPERTY_JOB_ID);
    return String.format(
        "%s/bin/flink cancel -t %s %s %s", flinkHome, target.getValue(), options, jobId);
  }

  private String buildReleaseKubernetesCommand(Resource resource) {
    Preconditions.checkArgument(
        resource.getProperties().containsKey(KUBERNETES_CLUSTER_ID_PROPERTY),
        "Cannot find {} from optimizer start up stats.",
        KUBERNETES_CLUSTER_ID_PROPERTY);
    String clusterId = resource.getProperties().get(KUBERNETES_CLUSTER_ID_PROPERTY);

    FlinkConf conf =
        FlinkConf.buildFor(loadFlinkConfig(), getContainerProperties())
            .withGroupProperties(resource.getProperties())
            .build();
    conf.putToOptions(FlinkConfKeys.KUBERNETES_CLUSTER_ID, clusterId);

    String options = conf.toCliOptions();

    // job-manager rest api can't be visited from k8s cluster outside, so using kubernetes-session
    // command to kill it
    // echo 'stop' | $FLINK_HOME/bin/kubertnetes-session.sh
    return String.format(
        " echo 'stop' | %s/bin/kubernetes-session.sh %s -Dexecution.attached=true",
        this.flinkHome, options);
  }

  /** Submit the OptimizerJob to the flink cluster through Flink RestClient. */
  protected JobID runFlinkOptimizerJob(Resource resource, Configuration configuration)
      throws Exception {
    try (RestClusterClient<String> client = FlinkClientUtil.getRestClusterClient(configuration)) {
      ClusterOverviewWithVersion overviewWithVersion =
          client.getClusterOverview().get(flinkClientTimeoutSecond, TimeUnit.SECONDS);
      LOG.debug("cluster overview: {}", overviewWithVersion);
      File jarFile = new File(jobUri);
      Preconditions.checkArgument(
          jarFile.exists(), String.format("The jar file %s not exists", jarFile.getAbsolutePath()));

      URL url = new URL(client.getWebInterfaceURL());
      return runJar(
          uploadJar(jarFile, url.getHost(), url.getPort(), configuration), configuration, resource);
    }
  }

  @VisibleForTesting
  protected String uploadJar(File jarFile, String host, int port, Configuration configuration)
      throws Exception {
    Preconditions.checkArgument(
        jarFile.exists(), String.format("The jar file %s not exists", jarFile.getAbsolutePath()));
    try (RestClient restClient =
        FlinkClientUtil.getRestClient(configuration, clientExecutorServiceSupplier.get())) {
      // First check whether the jar already exists in the cluster. If it exists, there is no need
      // to upload it multiple times. If it does not exist, upload and return jarId.
      JarListHeaders jarListHeaders = JarListHeaders.getInstance();
      JarListInfo jarListInfo =
          restClient
              .sendRequest(
                  host,
                  port,
                  jarListHeaders,
                  EmptyMessageParameters.getInstance(),
                  EmptyRequestBody.getInstance())
              .get(flinkClientTimeoutSecond, TimeUnit.SECONDS);
      Optional<JarListInfo.JarFileInfo> optimizerJarOptional =
          jarListInfo.jarFileList.stream()
              .filter(jarFileInfo -> jarFile.getName().equals(jarFileInfo.name))
              .findFirst();
      if (optimizerJarOptional.isPresent()) {
        return optimizerJarOptional.get().id;
      }
      JarUploadHeaders jarUploadHeaders = JarUploadHeaders.getInstance();
      JarUploadResponseBody jarUploadResponseBody =
          restClient
              .sendRequest(
                  host,
                  port,
                  jarUploadHeaders,
                  EmptyMessageParameters.getInstance(),
                  EmptyRequestBody.getInstance(),
                  Collections.singletonList(
                      new FileUpload(jarFile.toPath(), RestConstants.CONTENT_TYPE_JAR)))
              .get(flinkClientTimeoutSecond, TimeUnit.SECONDS);
      return jarUploadResponseBody
          .getFilename()
          .substring(jarUploadResponseBody.getFilename().lastIndexOf("/") + 1);
    }
  }

  @VisibleForTesting
  protected JobID runJar(String jarId, Configuration configuration, Resource resource)
      throws Exception {
    JarRunHeaders headers = JarRunHeaders.getInstance();
    JarRunMessageParameters parameters = headers.getUnresolvedMessageParameters();
    parameters.jarIdPathParameter.resolve(jarId);
    String args = super.buildOptimizerStartupArgsString(resource);
    JobID jobID = JobID.generate();
    JarRunRequestBody runRequestBody =
        new JarRunRequestBody(
            FLINK_JOB_MAIN_CLASS, args, null, null, jobID, true, null, RestoreMode.NO_CLAIM, null);
    LOG.info("Submitting job: {} to session cluster, args: {}", jobID, args);
    try (RestClusterClient<String> restClusterClient =
        FlinkClientUtil.getRestClusterClient(configuration)) {
      JarRunResponseBody jarRunResponseBody =
          restClusterClient
              .sendRequest(headers, parameters, runRequestBody)
              .get(flinkClientTimeoutSecond, TimeUnit.SECONDS);
      return jarRunResponseBody.getJobId();
    }
  }

  private String getFlinkHome() {
    String flinkHome = getContainerProperties().get(FLINK_HOME_PROPERTY);
    Preconditions.checkNotNull(
        flinkHome, "Container property: %s is required", FLINK_HOME_PROPERTY);
    return flinkHome.replaceAll("/$", "");
  }

  private String getFlinkConfDir() {
    String flinkConfDir =
        getContainerProperties()
            .get(OptimizerProperties.EXPORT_PROPERTY_PREFIX + ENV_FLINK_CONF_DIR);
    if (StringUtils.isNotEmpty(flinkConfDir)) {
      return flinkConfDir;
    }
    return this.flinkHome + FLINK_CONFIG_PATH;
  }

  private long getFlinkClientTimeoutSecond() {
    return getContainerProperties().get(FLINK_CLIENT_TIMEOUT_SECOND) != null
        ? Long.parseLong(getContainerProperties().get(FLINK_CLIENT_TIMEOUT_SECOND))
        : FLINK_CLIENT_TIMEOUT_SECOND_DEFAULT;
  }

  private String kubernetesClusterId(Resource resource) {
    return "amoro-optimizer-" + resource.getResourceId();
  }

  private enum Target {
    YARN_PER_JOB("yarn-per-job", false, false),
    YARN_APPLICATION("yarn-application", true, false),
    KUBERNETES_APPLICATION("kubernetes-application", true, false),
    SESSION("session", false, true);

    private final String value;
    private final boolean applicationMode;
    private final boolean runByFlinkRestClient;

    Target(String value, boolean applicationMode, boolean runByFlinkRestClient) {
      this.value = value;
      this.applicationMode = applicationMode;
      this.runByFlinkRestClient = runByFlinkRestClient;
    }

    public static Target valueToEnum(String value) {
      return Arrays.stream(values())
          .filter(t -> t.value.equalsIgnoreCase(value))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("can't parse value: " + value));
    }

    public boolean isApplicationMode() {
      return applicationMode;
    }

    public boolean runByFlinkRestClient() {
      return runByFlinkRestClient;
    }

    public String getValue() {
      return value;
    }
  }

  public static class FlinkConfKeys {
    public static final String JOB_MANAGER_TOTAL_PROCESS_MEMORY = "jobmanager.memory.process.size";
    public static final String TASK_MANAGER_TOTAL_PROCESS_MEMORY =
        "taskmanager.memory.process.size";

    public static final String CLASSPATH_INCLUDE_USER_JAR = "yarn.per-job-cluster.include-user-jar";
    public static final String CLASSPATH_INCLUDE_USER_JAR_DEFAULT = "FIRST";

    public static final String KUBERNETES_IMAGE_REF = "kubernetes.container.image";
    public static final String KUBERNETES_CLUSTER_ID = "kubernetes.cluster-id";
    public static final String KUBERNETES_TASKMANAGER_LABELS = "kubernetes.taskmanager.labels";
    public static final String KUBERNETES_JOBMANAGER_LABELS = "kubernetes.jobmanager.labels";

    public static final String YARN_APPLICATION_JOB_NAME = "yarn.application.name";
  }

  public static class FlinkConf {

    public static final String FLINK_PARAMETER_PREFIX = "flink-conf.";

    final Map<String, String> flinkConf;
    final Map<String, String> flinkOptions;

    public FlinkConf(Map<String, String> flinkConf, Map<String, String> flinkOptions) {
      this.flinkConf = flinkConf;
      this.flinkOptions = flinkOptions;
    }

    public String configValue(String key) {
      if (flinkOptions.containsKey(key)) {
        return flinkOptions.get(key);
      }
      return flinkConf.get(key);
    }

    public void putToOptions(String key, String value) {
      this.flinkOptions.put(key, value);
    }

    /**
     * The properties with prefix "flink-conf." will be merged with the following priority and
     * transformed into Flink options. 1. optimizing-group properties 2. optimizing-container
     * properties
     *
     * @return flink options, format is `-Dkey1="value1" -Dkey2="value2"`
     */
    public String toCliOptions() {
      return flinkOptions.entrySet().stream()
          .map(entry -> "-D" + entry.getKey() + "=\"" + entry.getValue() + "\"")
          .collect(Collectors.joining(" "));
    }

    public static Builder buildFor(
        Map<String, String> flinkConf, Map<String, String> containerProperties) {
      return new Builder(flinkConf, containerProperties);
    }

    public Configuration toConfiguration() {
      HashMap<String, String> config = new HashMap<>();
      config.putAll(flinkConf);
      config.putAll(flinkOptions);
      return Configuration.fromMap(config);
    }

    public static class Builder {
      final Map<String, String> flinkConf;

      Map<String, String> containerProperties;
      Map<String, String> groupProperties = Collections.emptyMap();

      public Builder(Map<String, String> flinkConf, Map<String, String> containerProperties) {
        this.flinkConf = Maps.newHashMap(flinkConf);
        this.containerProperties =
            containerProperties == null ? Collections.emptyMap() : containerProperties;
      }

      public Builder withGroupProperties(Map<String, String> groupProperties) {
        this.groupProperties = groupProperties;
        return this;
      }

      public FlinkConf build() {
        Map<String, String> options = Maps.newHashMap();
        this.containerProperties.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(FLINK_PARAMETER_PREFIX))
            .forEach(
                entry ->
                    options.put(
                        entry.getKey().substring(FLINK_PARAMETER_PREFIX.length()),
                        entry.getValue()));

        this.groupProperties.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(FLINK_PARAMETER_PREFIX))
            .forEach(
                entry ->
                    options.put(
                        entry.getKey().substring(FLINK_PARAMETER_PREFIX.length()),
                        entry.getValue()));

        return new FlinkConf(this.flinkConf, options);
      }
    }
  }
}
