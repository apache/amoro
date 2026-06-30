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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.resource.ResourceGroup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class TestDynamicAllocationConfig {

  private static final String CONTAINER = "flink";

  private ResourceGroup group(Map<String, String> properties) {
    return new ResourceGroup.Builder("group1", CONTAINER).addProperties(properties).build();
  }

  private Map<String, String> enabledProps() {
    Map<String, String> props = new HashMap<>();
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_ENABLED, "true");
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_MAX_PARALLELISM, "16");
    return props;
  }

  private void parseAndValidate(ResourceGroup group) {
    DynamicAllocationConfig.parse(group).validate();
  }

  @Test
  void enabledWithoutMaxParallelismIsRejected() {
    Map<String, String> props = new HashMap<>();
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_ENABLED, "true");
    Assertions.assertThrows(IllegalArgumentException.class, () -> parseAndValidate(group(props)));
  }

  @Test
  void maxParallelismBelowMinIsRejected() {
    Map<String, String> props = enabledProps();
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM, "32");
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_MAX_PARALLELISM, "16");
    Assertions.assertThrows(IllegalArgumentException.class, () -> parseAndValidate(group(props)));
  }

  @Test
  void maxParallelismAboveHardLimitIsRejected() {
    Map<String, String> props = enabledProps();
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_MAX_PARALLELISM, "2048");
    Assertions.assertThrows(IllegalArgumentException.class, () -> parseAndValidate(group(props)));
  }

  @Test
  void enabledWithUnparsableMinParallelismIsRejected() {
    // resolveMinParallelism() is lenient (legacy/keeper path), but an opted-in group must not
    // silently degrade an unparsable min-parallelism to 0.
    Map<String, String> props = enabledProps();
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM, "abc");
    Assertions.assertThrows(IllegalArgumentException.class, () -> parseAndValidate(group(props)));
  }

  @Test
  void enabledWithNegativeMinParallelismIsRejected() {
    Map<String, String> props = enabledProps();
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM, "-3");
    Assertions.assertThrows(IllegalArgumentException.class, () -> parseAndValidate(group(props)));
  }

  @Test
  void malformedMinParallelismIsRejectedAtParseEvenWhenDisabled() {
    // Mirrors max-parallelism: a malformed numeric is rejected at parse() regardless of enabled,
    // honoring parse()'s documented contract rather than being silently degraded to 0.
    Map<String, String> props = new HashMap<>();
    props.put(OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM, "abc");
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> DynamicAllocationConfig.parse(group(props)));
  }

  @Test
  void malformedMinParallelismErrorCitesTheKeyActuallyUsed() {
    // Value comes only from the legacy key; the error must name that key, not the namespaced one
    // the user never set.
    Map<String, String> props = new HashMap<>();
    props.put(OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM, "abc");
    IllegalArgumentException e =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> DynamicAllocationConfig.parse(group(props)));
    Assertions.assertTrue(
        e.getMessage().contains("'" + OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM + "'"),
        "error should cite the legacy key actually used: " + e.getMessage());
  }

  @Test
  void whitespacePaddedMinParallelismIsResolvedConsistently() {
    // validate() trims, so it accepts " 5 " as 5; the keeper's resolveMinParallelism() must agree.
    // Otherwise a config validate() accepts silently degrades to a floor of 0 at runtime.
    Map<String, String> props = enabledProps();
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM, " 5 ");
    ResourceGroup g = group(props);

    DynamicAllocationConfig config = DynamicAllocationConfig.parse(g);
    assertDoesNotThrow(config::validate);
    Assertions.assertEquals(5, config.getMinParallelism());
    Assertions.assertEquals(5, DynamicAllocationConfig.resolveMinParallelism(g));
  }

  @Test
  void executorIdleTimeoutBelowMinimumIsRejected() {
    Map<String, String> props = enabledProps();
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_EXECUTOR_IDLE_TIMEOUT, "10s");
    Assertions.assertThrows(IllegalArgumentException.class, () -> parseAndValidate(group(props)));
  }

  @Test
  void unparsableDurationIsRejected() {
    Map<String, String> props = enabledProps();
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_SCHEDULER_BACKLOG_TIMEOUT, "not-a-duration");
    Assertions.assertThrows(IllegalArgumentException.class, () -> parseAndValidate(group(props)));
  }

  @Test
  void zeroDurationIsRejected() {
    Map<String, String> props = enabledProps();
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_SCHEDULER_BACKLOG_TIMEOUT, "0s");
    Assertions.assertThrows(IllegalArgumentException.class, () -> parseAndValidate(group(props)));
  }

  @Test
  void externalContainerWithEnabledIsRejected() {
    ResourceGroup external =
        new ResourceGroup.Builder("group1").addProperties(enabledProps()).build();
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> DynamicAllocationConfig.parse(external).validate());
  }

  @Test
  void validConfigIsParsedCorrectly() {
    Map<String, String> props = enabledProps();
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM, "4");
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_SCHEDULER_BACKLOG_TIMEOUT, "2min");
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_EXECUTOR_IDLE_TIMEOUT, "10min");

    DynamicAllocationConfig config = DynamicAllocationConfig.parse(group(props));
    assertDoesNotThrow(config::validate);

    Assertions.assertTrue(config.isEnabled());
    Assertions.assertEquals(4, config.getMinParallelism());
    Assertions.assertEquals(16, config.getMaxParallelism());
    Assertions.assertEquals(Duration.ofMinutes(2), config.getSchedulerBacklogTimeout());
    Assertions.assertEquals(Duration.ofMinutes(10), config.getExecutorIdleTimeout());
    Assertions.assertEquals(Duration.ofSeconds(30), config.getSustainedBacklogTimeout());
    Assertions.assertEquals(Duration.ofMinutes(1), config.getScaleDownCooldown());
    Assertions.assertEquals(Duration.ofMinutes(15), config.getDrainTimeout());
  }

  @Test
  void minParallelismResolutionPrefersNamespacedValue() {
    Map<String, String> props = enabledProps();
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM, "8");
    props.put(OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM, "2");
    Assertions.assertEquals(8, DynamicAllocationConfig.resolveMinParallelism(group(props)));
  }

  @Test
  void minParallelismResolutionFallsBackToLegacyValue() {
    Map<String, String> props = new HashMap<>();
    props.put(OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM, "5");
    Assertions.assertEquals(5, DynamicAllocationConfig.resolveMinParallelism(group(props)));
  }

  @Test
  void minParallelismResolutionDefaultsToZero() {
    Assertions.assertEquals(
        0, DynamicAllocationConfig.resolveMinParallelism(group(new HashMap<>())));
  }

  @Test
  void disabledConfigSkipsValidation() {
    Map<String, String> props = new HashMap<>();
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_ENABLED, "false");
    // max-parallelism absent and an external container would fail if validated, but disabled.
    ResourceGroup external = new ResourceGroup.Builder("group1").addProperties(props).build();
    assertDoesNotThrow(() -> DynamicAllocationConfig.parse(external).validate());
  }

  @Test
  void effectivelyEnabledWhenValidConfig() {
    Assertions.assertTrue(DynamicAllocationConfig.isEffectivelyEnabled(group(enabledProps())));
  }

  @Test
  void notEffectivelyEnabledWhenDisabled() {
    Assertions.assertFalse(DynamicAllocationConfig.isEffectivelyEnabled(group(new HashMap<>())));
  }

  @Test
  void notEffectivelyEnabledWhenConfigInvalid() {
    Map<String, String> props = new HashMap<>();
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_ENABLED, "true");
    // enabled without max-parallelism is invalid, so DRA falls back to disabled.
    Assertions.assertFalse(DynamicAllocationConfig.isEffectivelyEnabled(group(props)));
  }

  @Test
  void effectiveMinParallelismKeyPrefersNamespacedWhenPresent() {
    Map<String, String> props = new HashMap<>();
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM, "2");
    props.put(OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM, "2");
    Assertions.assertEquals(
        OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM,
        DynamicAllocationConfig.effectiveMinParallelismKey(group(props)));
  }

  @Test
  void effectiveMinParallelismKeyFallsBackToLegacy() {
    Map<String, String> props = new HashMap<>();
    props.put(OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM, "2");
    Assertions.assertEquals(
        OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM,
        DynamicAllocationConfig.effectiveMinParallelismKey(group(props)));
    Assertions.assertEquals(
        OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM,
        DynamicAllocationConfig.effectiveMinParallelismKey(group(new HashMap<>())));
  }
}
