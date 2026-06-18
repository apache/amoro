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

package org.apache.amoro.formats.paimon.optimizing.primary;

import org.apache.paimon.utils.TimeUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class PaimonPrimaryKeyOptions {

  public static final String ENABLED = "paimon-optimizer.primary-key.enabled";
  public static final String MAX_BUCKETS_PER_TASK =
      "paimon-optimizer.primary-key.max-buckets-per-task";
  public static final String PARTITION_IDLE_TIME =
      "paimon-optimizer.primary-key.partition-idle-time";
  public static final String MAJOR_FILE_COUNT_THRESHOLD =
      "paimon-optimizer.primary-key.major.file-count-threshold";

  private final boolean enabled;
  private final int maxBucketsPerTask;
  private final Duration partitionIdleTime;
  private final Long majorFileCountThreshold;

  private PaimonPrimaryKeyOptions(
      boolean enabled,
      int maxBucketsPerTask,
      Duration partitionIdleTime,
      Long majorFileCountThreshold) {
    if (maxBucketsPerTask <= 0) {
      throw new IllegalArgumentException(MAX_BUCKETS_PER_TASK + " must be greater than 0.");
    }
    this.enabled = enabled;
    this.maxBucketsPerTask = maxBucketsPerTask;
    this.partitionIdleTime = partitionIdleTime;
    this.majorFileCountThreshold = majorFileCountThreshold;
  }

  public static PaimonPrimaryKeyOptions from(Map<String, String> properties) {
    Map<String, String> props = properties == null ? Collections.emptyMap() : properties;
    boolean enabled = enabled(properties);
    int maxBucketsPerTask = Integer.parseInt(props.getOrDefault(MAX_BUCKETS_PER_TASK, "16"));
    Duration partitionIdleTime =
        props.containsKey(PARTITION_IDLE_TIME)
            ? parseDuration(props.get(PARTITION_IDLE_TIME))
            : null;
    Long majorFileCountThreshold =
        props.containsKey(MAJOR_FILE_COUNT_THRESHOLD)
            ? Long.parseLong(props.get(MAJOR_FILE_COUNT_THRESHOLD))
            : null;

    return new PaimonPrimaryKeyOptions(
        enabled, maxBucketsPerTask, partitionIdleTime, majorFileCountThreshold);
  }

  public static boolean enabled(Map<String, String> properties) {
    Map<String, String> props = properties == null ? Collections.emptyMap() : properties;
    return Boolean.parseBoolean(props.getOrDefault(ENABLED, "false"));
  }

  private static Duration parseDuration(String value) {
    try {
      return TimeUtils.parseDuration(value);
    } catch (RuntimeException paimonStyleFailure) {
      try {
        return Duration.parse(value);
      } catch (RuntimeException isoFailure) {
        paimonStyleFailure.addSuppressed(isoFailure);
        throw paimonStyleFailure;
      }
    }
  }

  public boolean enabled() {
    return enabled;
  }

  public int maxBucketsPerTask() {
    return maxBucketsPerTask;
  }

  public Optional<Duration> partitionIdleTime() {
    return Optional.ofNullable(partitionIdleTime);
  }

  public Optional<Long> majorFileCountThreshold() {
    return Optional.ofNullable(majorFileCountThreshold);
  }
}
