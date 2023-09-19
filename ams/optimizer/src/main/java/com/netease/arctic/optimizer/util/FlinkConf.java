package com.netease.arctic.optimizer.util;

import org.apache.iceberg.relocated.com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class FlinkConf {

  public static final String FLINK_PARAMETER_PREFIX = "flink-conf.";

  public static class PropertyKeys {
    public static final String JOB_MANAGER_TOTAL_PROCESS_MEMORY = "jobmanager.memory.process.size";
    public static final String TASK_MANAGER_TOTAL_PROCESS_MEMORY = "taskmanager.memory.process.size";
    public static final String KUBERNETES_IMAGE_REF = "kubernetes.container.image";

    public static final String KUBERNETES_CLUSTER_ID = "kubernetes.cluster-id";
    public static final String KUBERNETES_TASKMANAGER_LABLES = "kubernetes.taskmanager.labels";
    public static final String KUBERNETES_JOBMANAGER_LABLES = "kubernetes.jobmanager.labels";

    public static final String KUBERNETES_NAMESPACE = "kubernetes.namespace";
  }

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
   * The properties with prefix "flink-conf." will be merged with the following priority
   * and transformed into Flink options.
   * 1. optimizing-group properties
   * 2. optimizing-container properties
   *
   * @return flink options, format is `-Dkey1=value1 -Dkey2=value2`
   */
  public String toCliOptions() {
    return flinkOptions.entrySet().stream()
        .map(entry -> "-D" + entry.getKey() + "=" + entry.getValue())
        .collect(Collectors.joining(" "));
  }

  public static FlinkConf.Builder buildFor(Map<String, String> flinkConf, Map<String, String> containerProperties) {
    return new Builder(flinkConf, containerProperties);
  }

  public static class Builder {
    final Map<String, String> flinkConf;

    Map<String, String> containerProperties;
    Map<String, String> groupProperties = Collections.emptyMap();

    public Builder(Map<String, String> flinkConf, Map<String, String> containerProperties) {
      this.flinkConf = Maps.newHashMap(flinkConf);
      this.containerProperties = containerProperties == null ? Collections.emptyMap() : containerProperties;
    }

    public Builder withGroupProperties(Map<String, String> groupProperties) {
      this.groupProperties = groupProperties;
      return this;
    }

    public FlinkConf build() {
      Map<String, String> options = Maps.newHashMap();
      this.containerProperties.entrySet().stream()
          .filter(entry -> entry.getKey().startsWith(FLINK_PARAMETER_PREFIX))
          .forEach(entry -> options.put(entry.getKey().substring(FLINK_PARAMETER_PREFIX.length()), entry.getValue()));

      this.groupProperties.entrySet().stream()
          .filter(entry -> entry.getKey().startsWith(FLINK_PARAMETER_PREFIX))
          .forEach(entry -> options.put(entry.getKey().substring(FLINK_PARAMETER_PREFIX.length()), entry.getValue()));

      return new FlinkConf(this.flinkConf, options);
    }
  }
}