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

package org.apache.amoro.server.optimizing.dra;

import org.apache.amoro.Constants;
import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.config.ConfigHelpers;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.utils.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

/**
 * Dynamic resource allocation (DRA) configuration of a resource group (AIP-5). Parsed from the
 * group's properties; {@link #validate()} enforces the AIP-5 constraints.
 */
public class DynamicAllocationConfig {

  private static final Logger LOG = LoggerFactory.getLogger(DynamicAllocationConfig.class);

  private final String groupName;
  private final String container;
  private final boolean enabled;
  private final Integer minParallelism;
  private final Integer maxParallelism;
  private final Duration schedulerBacklogTimeout;
  private final Duration sustainedBacklogTimeout;
  private final Duration executorIdleTimeout;
  private final Duration scaleDownCooldown;
  private final Duration drainTimeout;

  private DynamicAllocationConfig(
      String groupName,
      String container,
      boolean enabled,
      Integer minParallelism,
      Integer maxParallelism,
      Duration schedulerBacklogTimeout,
      Duration sustainedBacklogTimeout,
      Duration executorIdleTimeout,
      Duration scaleDownCooldown,
      Duration drainTimeout) {
    this.groupName = groupName;
    this.container = container;
    this.enabled = enabled;
    this.minParallelism = minParallelism;
    this.maxParallelism = maxParallelism;
    this.schedulerBacklogTimeout = schedulerBacklogTimeout;
    this.sustainedBacklogTimeout = sustainedBacklogTimeout;
    this.executorIdleTimeout = executorIdleTimeout;
    this.scaleDownCooldown = scaleDownCooldown;
    this.drainTimeout = drainTimeout;
  }

  /**
   * Parse the DRA configuration of a resource group. Malformed numeric or duration values throw
   * {@link IllegalArgumentException}; semantic constraints are checked by {@link #validate()}.
   */
  public static DynamicAllocationConfig parse(ResourceGroup group) {
    Map<String, String> properties = group.getProperties();
    boolean enabled =
        PropertyUtil.propertyAsBoolean(
            properties,
            OptimizerProperties.DYNAMIC_ALLOCATION_ENABLED,
            OptimizerProperties.DYNAMIC_ALLOCATION_ENABLED_DEFAULT);
    Integer minParallelism = parseMinParallelism(group);

    Integer maxParallelism =
        PropertyUtil.propertyAsNullableInt(
            properties, OptimizerProperties.DYNAMIC_ALLOCATION_MAX_PARALLELISM);

    return new DynamicAllocationConfig(
        group.getName(),
        group.getContainer(),
        enabled,
        minParallelism,
        maxParallelism,
        parseDuration(
            properties,
            OptimizerProperties.DYNAMIC_ALLOCATION_SCHEDULER_BACKLOG_TIMEOUT,
            OptimizerProperties.DYNAMIC_ALLOCATION_SCHEDULER_BACKLOG_TIMEOUT_DEFAULT),
        parseDuration(
            properties,
            OptimizerProperties.DYNAMIC_ALLOCATION_SUSTAINED_BACKLOG_TIMEOUT,
            OptimizerProperties.DYNAMIC_ALLOCATION_SUSTAINED_BACKLOG_TIMEOUT_DEFAULT),
        parseDuration(
            properties,
            OptimizerProperties.DYNAMIC_ALLOCATION_EXECUTOR_IDLE_TIMEOUT,
            OptimizerProperties.DYNAMIC_ALLOCATION_EXECUTOR_IDLE_TIMEOUT_DEFAULT),
        parseDuration(
            properties,
            OptimizerProperties.DYNAMIC_ALLOCATION_SCALE_DOWN_COOLDOWN,
            OptimizerProperties.DYNAMIC_ALLOCATION_SCALE_DOWN_COOLDOWN_DEFAULT),
        parseDuration(
            properties,
            OptimizerProperties.DYNAMIC_ALLOCATION_DRAIN_TIMEOUT,
            OptimizerProperties.DYNAMIC_ALLOCATION_DRAIN_TIMEOUT_DEFAULT));
  }

  /**
   * Resolve the effective min-parallelism of a group, honoring the deprecated flat {@code
   * min-parallelism} as a fallback. Resolution order: {@link
   * OptimizerProperties#DYNAMIC_ALLOCATION_MIN_PARALLELISM} → {@link
   * OptimizerProperties#OPTIMIZER_GROUP_MIN_PARALLELISM} → {@code 0}. Lenient: an unparsable value
   * falls back to {@code 0} rather than throwing, preserving legacy behavior. This is on the keeper
   * hot path and therefore stays silent; deprecation is reported by {@link
   * #warnDeprecatedMinParallelism(ResourceGroup)} at config-entry points instead.
   */
  public static int resolveMinParallelism(ResourceGroup group) {
    Map<String, String> properties = group.getProperties();
    String namespaced = properties.get(OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM);
    if (namespaced != null) {
      return parseIntOrDefault(group.getName(), namespaced);
    }
    String legacy = properties.get(OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM);
    if (legacy != null) {
      return parseIntOrDefault(group.getName(), legacy);
    }
    return OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM_DEFAULT;
  }

  /**
   * Strictly parse the configured min-parallelism, or {@code null} when unset. Follows the
   * resolution order of {@link #resolveMinParallelism(ResourceGroup)} ({@link
   * OptimizerProperties#DYNAMIC_ALLOCATION_MIN_PARALLELISM} → {@link
   * OptimizerProperties#OPTIMIZER_GROUP_MIN_PARALLELISM}). Mirrors {@code max-parallelism}: a
   * malformed value throws at parse time (honoring {@link #parse}'s contract) rather than being
   * silently degraded to {@code 0} by the lenient {@link #resolveMinParallelism(ResourceGroup)} on
   * the keeper hot path. Shares its trim semantics so the value {@link #validate()} accepts is the
   * one the keeper resolves; the error cites the key the value actually came from.
   */
  private static Integer parseMinParallelism(ResourceGroup group) {
    Map<String, String> properties = group.getProperties();
    String key = OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM;
    String raw = properties.get(key);
    if (raw == null) {
      key = OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM;
      raw = properties.get(key);
    }
    if (raw == null) {
      return null;
    }
    try {
      return Integer.parseInt(raw.trim());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          String.format(
              "Resource group:%s '%s'(%s) is not a valid integer.", group.getName(), key, raw));
    }
  }

  /**
   * Log a one-off deprecation warning when a group still relies on the flat {@code
   * min-parallelism}. Intended for config-entry points (startup load, REST create/update), not the
   * keeper hot path.
   */
  public static void warnDeprecatedMinParallelism(ResourceGroup group) {
    Map<String, String> properties = group.getProperties();
    boolean hasNamespaced =
        properties.containsKey(OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM);
    boolean hasLegacy = properties.containsKey(OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM);
    if (hasNamespaced && hasLegacy) {
      LOG.warn(
          "Resource group:{} sets both '{}' and the deprecated '{}'; the namespaced value wins.",
          group.getName(),
          OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM,
          OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM);
    } else if (hasLegacy) {
      LOG.warn(
          "Resource group:{} uses the deprecated '{}'; please migrate to '{}'.",
          group.getName(),
          OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM,
          OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM);
    }
  }

  /**
   * Whether dynamic allocation is effectively enabled for the group: opted in and the configuration
   * is valid. An invalid configuration counts as disabled, mirroring the startup fail-safe
   * behavior.
   */
  public static boolean isEffectivelyEnabled(ResourceGroup group) {
    try {
      DynamicAllocationConfig config = parse(group);
      config.validate();
      return config.isEnabled();
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * The min-parallelism property key that {@link #resolveMinParallelism(ResourceGroup)} actually
   * reads for this group. Writers updating the effective value (e.g. the keeper's auto-reset) must
   * target this key; writing the deprecated flat key while the namespaced one is present would be
   * shadowed.
   */
  public static String effectiveMinParallelismKey(ResourceGroup group) {
    return group.getProperties().containsKey(OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM)
        ? OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM
        : OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM;
  }

  /** Enforce the AIP-5 constraints. No-op when DRA is disabled. */
  public void validate() {
    if (!enabled) {
      return;
    }
    if (Constants.EXTERNAL_RESOURCE_CONTAINER.equals(container)) {
      throw new IllegalArgumentException(
          String.format(
              "Resource group:%s cannot enable dynamic allocation on an externally-registered "
                  + "optimizer (container '%s'); AMS cannot scale optimizers it did not launch.",
              groupName, container));
    }
    if (maxParallelism == null) {
      throw new IllegalArgumentException(
          String.format(
              "Resource group:%s enables dynamic allocation but '%s' is not set; it is required.",
              groupName, OptimizerProperties.DYNAMIC_ALLOCATION_MAX_PARALLELISM));
    }
    // min-parallelism: a malformed value already threw in parse(); an opted-in group must also
    // reject a negative floor. resolveMinParallelism() stays lenient on the keeper hot path.
    int minParallelism =
        this.minParallelism == null
            ? OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM_DEFAULT
            : this.minParallelism;
    if (minParallelism < 0) {
      throw new IllegalArgumentException(
          String.format(
              "Resource group:%s '%s'(%d) must not be negative.",
              groupName, OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM, minParallelism));
    }
    if (maxParallelism < minParallelism) {
      throw new IllegalArgumentException(
          String.format(
              "Resource group:%s '%s'(%d) must be >= '%s'(%d).",
              groupName,
              OptimizerProperties.DYNAMIC_ALLOCATION_MAX_PARALLELISM,
              maxParallelism,
              OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM,
              minParallelism));
    }
    if (maxParallelism > OptimizerProperties.DYNAMIC_ALLOCATION_MAX_PARALLELISM_LIMIT) {
      throw new IllegalArgumentException(
          String.format(
              "Resource group:%s '%s'(%d) must not exceed the hard limit %d.",
              groupName,
              OptimizerProperties.DYNAMIC_ALLOCATION_MAX_PARALLELISM,
              maxParallelism,
              OptimizerProperties.DYNAMIC_ALLOCATION_MAX_PARALLELISM_LIMIT));
    }
    Duration idleMin =
        ConfigHelpers.TimeUtils.parseDuration(
            OptimizerProperties.DYNAMIC_ALLOCATION_EXECUTOR_IDLE_TIMEOUT_MIN);
    if (executorIdleTimeout.compareTo(idleMin) < 0) {
      throw new IllegalArgumentException(
          String.format(
              "Resource group:%s '%s'(%s) must be >= %s.",
              groupName,
              OptimizerProperties.DYNAMIC_ALLOCATION_EXECUTOR_IDLE_TIMEOUT,
              executorIdleTimeout,
              OptimizerProperties.DYNAMIC_ALLOCATION_EXECUTOR_IDLE_TIMEOUT_MIN));
    }
    // TODO AIP-5 Phase 2: these polling-oriented timeouts only enforce a positive lower bound; a
    // value like "1ns" passes here but would busy-spin the scale loop. Add realistic minimums once
    // the scaling loop that consumes them lands.
    requirePositive(
        OptimizerProperties.DYNAMIC_ALLOCATION_SCHEDULER_BACKLOG_TIMEOUT, schedulerBacklogTimeout);
    requirePositive(
        OptimizerProperties.DYNAMIC_ALLOCATION_SUSTAINED_BACKLOG_TIMEOUT, sustainedBacklogTimeout);
    requirePositive(OptimizerProperties.DYNAMIC_ALLOCATION_SCALE_DOWN_COOLDOWN, scaleDownCooldown);
    requirePositive(OptimizerProperties.DYNAMIC_ALLOCATION_DRAIN_TIMEOUT, drainTimeout);
  }

  private void requirePositive(String property, Duration value) {
    if (value.isZero() || value.isNegative()) {
      throw new IllegalArgumentException(
          String.format(
              "Resource group:%s '%s'(%s) must be a positive duration.",
              groupName, property, value));
    }
  }

  private static Duration parseDuration(
      Map<String, String> properties, String property, String defaultValue) {
    String value = properties.getOrDefault(property, defaultValue);
    return ConfigHelpers.TimeUtils.parseDuration(value);
  }

  private static int parseIntOrDefault(String groupName, String value) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      LOG.warn(
          "Resource group:{} has an illegal min-parallelism value '{}', using default {}.",
          groupName,
          value,
          OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM_DEFAULT);
      return OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM_DEFAULT;
    }
  }

  public boolean isEnabled() {
    return enabled;
  }

  public int getMinParallelism() {
    return minParallelism == null
        ? OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM_DEFAULT
        : minParallelism;
  }

  /**
   * The configured max-parallelism. Precondition: only call on a config that has passed {@link
   * #validate()} with DRA enabled — {@code max-parallelism} is nullable and only mandatory when
   * enabled, so calling this on a disabled or unvalidated config may throw {@link
   * NullPointerException} on unboxing.
   */
  public int getMaxParallelism() {
    return maxParallelism;
  }

  public Duration getSchedulerBacklogTimeout() {
    return schedulerBacklogTimeout;
  }

  public Duration getSustainedBacklogTimeout() {
    return sustainedBacklogTimeout;
  }

  public Duration getExecutorIdleTimeout() {
    return executorIdleTimeout;
  }

  public Duration getScaleDownCooldown() {
    return scaleDownCooldown;
  }

  public Duration getDrainTimeout() {
    return drainTimeout;
  }
}
