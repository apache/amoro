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

package org.apache.amoro.server;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.metrics.Counter;
import org.apache.amoro.metrics.Metric;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.metrics.MetricKey;
import org.apache.amoro.resource.ResourceContainer;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.optimizing.dra.DynamicAllocationMetrics;
import org.apache.amoro.server.resource.ContainerMetadata;
import org.apache.amoro.server.resource.Containers;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.table.AMSTableTestBase;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.iceberg.common.DynFields;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Integration tests for the dynamic-allocation scale keeper: DRA-enabled groups are taken over from
 * the legacy floor keeper and scaled in executor-parallelism-thread instance units, with
 * requested-but-unregistered capacity preventing duplicate scale-outs during the pod boot window.
 */
@RunWith(Parameterized.class)
public class TestOptimizerScaleKeeper extends AMSTableTestBase {

  private static final String TEST_GROUP_NAME = "test-scale-keeper-group";
  private static final String MOCK_CONTAINER_NAME = "mock-scale-container";

  private final AtomicBoolean resourceAvailable = new AtomicBoolean(true);
  private final AtomicInteger scaleOutCallCount = new AtomicInteger(0);
  // Null simulates a pod that was requested but never boots far enough to self-register.
  private volatile Function<org.apache.amoro.api.OptimizerRegisterInfo, String> optimizerRegistrar;
  private static boolean originIsInitialized = false;
  private String currentGroupName;
  private TestOptimizerGroupKeeper.MockOptimizerContainer mockContainer;

  public TestOptimizerScaleKeeper(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper, false);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(true, false)}
    };
  }

  @Before
  public void prepare() throws Exception {
    optimizerRegistrar = registerInfo -> optimizingService().authenticate(registerInfo);
    setupMockContainer(() -> currentGroupName);
  }

  @After
  public void clear() {
    if (currentGroupName == null) {
      return;
    }
    try {
      optimizerManager()
          .listOptimizers(currentGroupName)
          .forEach(
              optimizer ->
                  optimizingService()
                      .deleteOptimizer(optimizer.getGroupName(), optimizer.getResourceId()));
      try {
        optimizingService().deleteResourceGroup(currentGroupName);
      } catch (Exception ignored) {
      }
      try {
        optimizerManager().deleteResourceGroup(currentGroupName);
      } catch (Exception ignored) {
      }
    } catch (Exception e) {
      // ignore
    } finally {
      currentGroupName = null;
    }
  }

  @AfterClass
  public static void cleanup() {
    if (!originIsInitialized) {
      DynFields.UnboundField<Boolean> initializedField =
          DynFields.builder().hiddenImpl(Containers.class, "isInitialized").build();
      initializedField.asStatic().set(false);
    }
  }

  private void setupMockContainer(Supplier<String> targetGroupNameSupplier) throws Exception {
    mockContainer =
        new TestOptimizerGroupKeeper.MockOptimizerContainer(
            resourceAvailable,
            scaleOutCallCount,
            registerInfo -> {
              Function<org.apache.amoro.api.OptimizerRegisterInfo, String> registrar =
                  optimizerRegistrar;
              return registrar == null ? null : registrar.apply(registerInfo);
            },
            targetGroupNameSupplier);

    DynFields.UnboundField<Boolean> initializedField =
        DynFields.builder().hiddenImpl(Containers.class, "isInitialized").build();
    if (!initializedField.asStatic().get()) {
      originIsInitialized = false;
      initializedField.asStatic().set(true);
    }

    DynFields.UnboundField<Map<String, Object>> containersField =
        DynFields.builder().hiddenImpl(Containers.class, "globalContainers").build();
    Map<String, Object> globalContainers = containersField.asStatic().get();

    ContainerMetadata metadata =
        new ContainerMetadata(
            MOCK_CONTAINER_NAME, TestOptimizerGroupKeeper.MockOptimizerContainer.class.getName());
    Map<String, String> properties = Maps.newHashMap();
    properties.put(OptimizerProperties.AMS_HOME, "/tmp");
    properties.put(OptimizerProperties.AMS_OPTIMIZER_URI, "thrift://localhost:1261");
    properties.put("memory", "1024");
    metadata.setProperties(properties);

    Class<?> wrapperClass =
        Class.forName("org.apache.amoro.server.resource.Containers$ContainerWrapper");
    java.lang.reflect.Constructor<?> constructor =
        wrapperClass.getDeclaredConstructor(ContainerMetadata.class, ResourceContainer.class);
    constructor.setAccessible(true);
    Object wrapper = constructor.newInstance(metadata, mockContainer);
    globalContainers.put(MOCK_CONTAINER_NAME, wrapper);
  }

  private ResourceGroup buildDraResourceGroup(String groupName, int minParallelism, int k) {
    this.currentGroupName = groupName;
    Map<String, String> properties = Maps.newHashMap();
    properties.put(OptimizerProperties.DYNAMIC_ALLOCATION_ENABLED, "true");
    properties.put(
        OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM, String.valueOf(minParallelism));
    properties.put(OptimizerProperties.DYNAMIC_ALLOCATION_MAX_PARALLELISM, "8");
    properties.put(OptimizerProperties.DYNAMIC_ALLOCATION_EXECUTOR_PARALLELISM, String.valueOf(k));
    // Fast timings so the scale keeper runs several rounds within a short sleep.
    properties.put(OptimizerProperties.DYNAMIC_ALLOCATION_SCHEDULER_BACKLOG_TIMEOUT, "1ms");
    properties.put(OptimizerProperties.DYNAMIC_ALLOCATION_SUSTAINED_BACKLOG_TIMEOUT, "50ms");
    properties.put("memory", "1024");
    return new ResourceGroup.Builder(groupName, MOCK_CONTAINER_NAME)
        .addProperties(properties)
        .build();
  }

  /**
   * The floor of a DRA group is satisfied in executor-parallelism-thread instance units by the
   * scale keeper, not by the legacy keeper's single deficit-sized instance: min-parallelism=2 with
   * executor-parallelism=1 must produce two 1-thread instances, not one 2-thread instance.
   */
  @Test
  public void testDraFloorSatisfiedInExecutorParallelismUnits() throws InterruptedException {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    ResourceGroup group = buildDraResourceGroup(TEST_GROUP_NAME + "-1", 2, 1);

    optimizerManager().createResourceGroup(group);
    optimizingService().createResourceGroup(group);

    Thread.sleep(500);

    List<OptimizerInstance> optimizers = optimizerManager().listOptimizers(group.getName());
    Assertions.assertEquals(
        2, optimizers.size(), "floor should be satisfied by K-thread instances");
    optimizers.forEach(
        optimizer ->
            Assertions.assertEquals(
                1,
                optimizer.getThreadCount(),
                "each instance should have executor-parallelism threads"));
    Assertions.assertEquals(
        0,
        optimizingService().pendingScaleThreads(group.getName()),
        "registration must clear the boot-window accounting, or registered capacity would be "
            + "double-counted and suppress demand scaling");
  }

  /**
   * Requested-but-unregistered capacity counts toward the effective threads: while pods are booting
   * (never registering here), the scale keeper must not re-request the same deficit every round the
   * way the legacy keeper would.
   */
  @Test
  public void testBootWindowPreventsDuplicateScaleOuts() throws InterruptedException {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    optimizerRegistrar = null; // pods are requested but never self-register
    ResourceGroup group = buildDraResourceGroup(TEST_GROUP_NAME + "-2", 2, 1);

    optimizerManager().createResourceGroup(group);
    optimizingService().createResourceGroup(group);

    // ~10 scale rounds at the 50ms cadence; without boot-window accounting each round would
    // re-request the full deficit.
    Thread.sleep(500);

    Assertions.assertEquals(
        2,
        scaleOutCallCount.get(),
        "the deficit must be requested exactly once while the pods are still booting");
    @SuppressWarnings("unchecked")
    org.apache.amoro.metrics.Gauge<Integer> effectiveThreads =
        (org.apache.amoro.metrics.Gauge<Integer>)
            draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_EFFECTIVE_THREADS, group.getName());
    Assertions.assertEquals(
        2,
        effectiveThreads.getValue().intValue(),
        "requested-but-unregistered threads must count as effective");
  }

  /**
   * A synchronous scale-out failure must not stick as phantom pending capacity: the failed request
   * is dropped immediately and retried on a later round.
   */
  @Test
  public void testFailedScaleOutIsRetriedNextRound() throws InterruptedException {
    resourceAvailable.set(false);
    scaleOutCallCount.set(0);
    ResourceGroup group = buildDraResourceGroup(TEST_GROUP_NAME + "-3", 1, 1);

    optimizerManager().createResourceGroup(group);
    optimizingService().createResourceGroup(group);

    Thread.sleep(500);

    Assertions.assertTrue(
        scaleOutCallCount.get() >= 2,
        "failed requests should be retried instead of freezing scale-up: "
            + scaleOutCallCount.get());
  }

  /** Enabling DRA on an existing group at runtime brings it under the scale keeper. */
  @Test
  public void testEnablingDraAtRuntimeBringsGroupUnderScaleKeeper() throws InterruptedException {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    this.currentGroupName = TEST_GROUP_NAME + "-4";
    Map<String, String> legacyProps = Maps.newHashMap();
    legacyProps.put("memory", "1024");
    ResourceGroup legacyGroup =
        new ResourceGroup.Builder(currentGroupName, MOCK_CONTAINER_NAME)
            .addProperties(legacyProps)
            .build();

    optimizerManager().createResourceGroup(legacyGroup);
    optimizingService().createResourceGroup(legacyGroup);
    Thread.sleep(100);
    Assertions.assertEquals(0, scaleOutCallCount.get(), "no demand and no floor: no scale-out");

    ResourceGroup draGroup = buildDraResourceGroup(currentGroupName, 2, 1);
    optimizerManager().updateResourceGroup(draGroup);
    optimizingService().updateResourceGroup(draGroup);

    Thread.sleep(500);

    List<OptimizerInstance> optimizers = optimizerManager().listOptimizers(currentGroupName);
    Assertions.assertEquals(
        2, optimizers.size(), "runtime-enabled DRA group should reach its floor in K units");
  }

  private OptimizerInstance awaitSingleOptimizer(String groupName) throws InterruptedException {
    Thread.sleep(500);
    List<OptimizerInstance> optimizers = optimizerManager().listOptimizers(groupName);
    Assertions.assertEquals(1, optimizers.size(), "floor of 1 should register one optimizer");
    return optimizers.get(0);
  }

  /** Removal releases the container resource, deletes the persisted row, and unregisters. */
  @Test
  public void testExecuteRemovalReleasesResourceAndUnregisters() throws InterruptedException {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    ResourceGroup group = buildDraResourceGroup(TEST_GROUP_NAME + "-5", 1, 1);
    optimizerManager().createResourceGroup(group);
    optimizingService().createResourceGroup(group);
    OptimizerInstance optimizer = awaitSingleOptimizer(group.getName());

    // Keep the keeper from instantly re-filling the floor while we assert emptiness.
    resourceAvailable.set(false);
    optimizingService().executeRemoval(optimizer.getToken());

    Assertions.assertTrue(optimizerManager().listOptimizers(group.getName()).isEmpty());
    Assertions.assertNull(optimizerManager().getResource(optimizer.getResourceId()));
    Assertions.assertTrue(
        mockContainer.getReleasedResources().stream()
            .anyMatch(r -> optimizer.getResourceId().equals(r.getResourceId())),
        "the container resource must be released");
  }

  /**
   * A registered optimizer whose resource row is missing (the pod self-registered after a persist
   * failure, or a manual release raced the row away) must still be removable: the instance itself
   * carries the container-side identity, so release through it and only skip the row delete.
   */
  @Test
  public void testExecuteRemovalFallsBackWhenResourceRowMissing() throws InterruptedException {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    ResourceGroup group = buildDraResourceGroup(TEST_GROUP_NAME + "-6", 1, 1);
    optimizerManager().createResourceGroup(group);
    optimizingService().createResourceGroup(group);
    OptimizerInstance optimizer = awaitSingleOptimizer(group.getName());

    resourceAvailable.set(false);
    optimizerManager().deleteResource(optimizer.getResourceId());
    optimizingService().executeRemoval(optimizer.getToken());

    Assertions.assertTrue(
        optimizerManager().listOptimizers(group.getName()).isEmpty(),
        "a row-less optimizer must not become an unremovable zombie");
    Assertions.assertTrue(
        mockContainer.getReleasedResources().stream()
            .anyMatch(r -> optimizer.getResourceId().equals(r.getResourceId())),
        "the container side must still be released via the instance");
  }

  /**
   * A transient container release failure keeps the drain state so a later round retries the
   * idempotent deletion; the optimizer must not be unregistered while its pod may still exist.
   */
  @Test
  public void testExecuteRemovalKeepsDrainStateOnReleaseFailure() throws InterruptedException {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    ResourceGroup group = buildDraResourceGroup(TEST_GROUP_NAME + "-7", 1, 1);
    optimizerManager().createResourceGroup(group);
    optimizingService().createResourceGroup(group);
    OptimizerInstance optimizer = awaitSingleOptimizer(group.getName());

    resourceAvailable.set(false);
    optimizingService().beginGracefulDrain(optimizer.getToken(), Long.MAX_VALUE);
    mockContainer.setReleaseAvailable(false);
    optimizingService().executeRemoval(optimizer.getToken());

    Assertions.assertTrue(
        optimizingService().isDraining(optimizer.getToken()),
        "drain state must survive the failed release for a later retry");
    Assertions.assertEquals(
        1,
        optimizerManager().listOptimizers(group.getName()).size(),
        "the optimizer must stay registered while its pod may still exist");

    mockContainer.setReleaseAvailable(true);
    optimizingService().executeRemoval(optimizer.getToken());
    Assertions.assertFalse(optimizingService().isDraining(optimizer.getToken()));
    Assertions.assertTrue(optimizerManager().listOptimizers(group.getName()).isEmpty());
  }

  /**
   * DRA group whose rounds are driven manually with injected times. The huge real cadence keeps the
   * live keeper's own rounds from racing the injected ones on the per-group decision state
   * (idle-timeout 1200s respects the sustained &le; idle/2 validation).
   */
  private ResourceGroup buildSlowDraResourceGroup(String groupName, int minParallelism) {
    this.currentGroupName = groupName;
    Map<String, String> properties = Maps.newHashMap();
    properties.put(OptimizerProperties.DYNAMIC_ALLOCATION_ENABLED, "true");
    properties.put(
        OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM, String.valueOf(minParallelism));
    properties.put(OptimizerProperties.DYNAMIC_ALLOCATION_MAX_PARALLELISM, "8");
    properties.put(OptimizerProperties.DYNAMIC_ALLOCATION_EXECUTOR_PARALLELISM, "1");
    properties.put(OptimizerProperties.DYNAMIC_ALLOCATION_SUSTAINED_BACKLOG_TIMEOUT, "600s");
    properties.put(OptimizerProperties.DYNAMIC_ALLOCATION_EXECUTOR_IDLE_TIMEOUT, "1200s");
    properties.put("memory", "1024");
    return new ResourceGroup.Builder(groupName, MOCK_CONTAINER_NAME)
        .addProperties(properties)
        .build();
  }

  /** Register an optimizer the way a booted pod would: persisted resource row + self-register. */
  private OptimizerInstance registerOptimizer(String groupName, int threadCount) {
    org.apache.amoro.resource.Resource resource =
        new org.apache.amoro.resource.Resource.Builder(
                MOCK_CONTAINER_NAME, groupName, org.apache.amoro.resource.ResourceType.OPTIMIZER)
            .setThreadCount(threadCount)
            .build();
    optimizerManager().createResource(resource);
    org.apache.amoro.api.OptimizerRegisterInfo registerInfo =
        new org.apache.amoro.api.OptimizerRegisterInfo();
    Map<String, String> registerProperties = Maps.newHashMap();
    registerProperties.put(OptimizerProperties.OPTIMIZER_HEART_BEAT_INTERVAL, "100");
    registerInfo.setProperties(registerProperties);
    registerInfo.setThreadCount(threadCount);
    registerInfo.setMemoryMb(1024);
    registerInfo.setGroupName(groupName);
    registerInfo.setResourceId(resource.getResourceId());
    registerInfo.setStartTime(System.currentTimeMillis());
    String token = optimizingService().authenticate(registerInfo);
    return optimizerManager().listOptimizers(groupName).stream()
        .filter(optimizer -> token.equals(optimizer.getToken()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("registered optimizer not listed"));
  }

  /** An instance idle past executor-idle-timeout is drained and, being idle, removed in-round. */
  @Test
  public void testIdleOptimizerScaledDownViaInjectedRounds() {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    ResourceGroup group = buildSlowDraResourceGroup(TEST_GROUP_NAME + "-8", 0);
    optimizerManager().createResourceGroup(group);
    optimizingService().createResourceGroup(group);
    OptimizerInstance optimizer = registerOptimizer(group.getName(), 1);

    long t0 = System.currentTimeMillis();
    optimizingService().evaluateDynamicAllocation(group.getName(), t0); // seeds the observation
    Assertions.assertEquals(1, optimizerManager().listOptimizers(group.getName()).size());

    optimizingService().evaluateDynamicAllocation(group.getName(), t0 + 1_300_000L);
    Assertions.assertTrue(
        optimizerManager().listOptimizers(group.getName()).isEmpty(),
        "an idle instance past the timeout should be drained and removed in the same round");
    Assertions.assertTrue(
        mockContainer.getReleasedResources().stream()
            .anyMatch(r -> optimizer.getResourceId().equals(r.getResourceId())));
  }

  /** The min-parallelism floor keeps the last instance no matter how long it idles. */
  @Test
  public void testScaleDownRespectsFloor() {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    ResourceGroup group = buildSlowDraResourceGroup(TEST_GROUP_NAME + "-9", 1);
    optimizerManager().createResourceGroup(group);
    optimizingService().createResourceGroup(group);
    registerOptimizer(group.getName(), 1);

    long t0 = System.currentTimeMillis();
    optimizingService().evaluateDynamicAllocation(group.getName(), t0);
    optimizingService().evaluateDynamicAllocation(group.getName(), t0 + 1_300_000L);

    Assertions.assertEquals(
        1,
        optimizerManager().listOptimizers(group.getName()).size(),
        "the floor must keep the last instance");
  }

  /** Removals proceed one instance per cooldown period, never in batches. */
  @Test
  public void testScaleDownRemovesOneInstancePerCooldown() {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    ResourceGroup group = buildSlowDraResourceGroup(TEST_GROUP_NAME + "-10", 0);
    optimizerManager().createResourceGroup(group);
    optimizingService().createResourceGroup(group);
    registerOptimizer(group.getName(), 1);
    registerOptimizer(group.getName(), 1);

    long t0 = System.currentTimeMillis();
    optimizingService().evaluateDynamicAllocation(group.getName(), t0);
    long firstRemovalAt = t0 + 1_300_000L;
    optimizingService().evaluateDynamicAllocation(group.getName(), firstRemovalAt);
    Assertions.assertEquals(
        1,
        optimizerManager().listOptimizers(group.getName()).size(),
        "only one instance per round may be removed");

    // Inside the scale-down-cooldown window (default 1min): the second instance stays.
    optimizingService().evaluateDynamicAllocation(group.getName(), firstRemovalAt + 30_000L);
    Assertions.assertEquals(1, optimizerManager().listOptimizers(group.getName()).size());

    optimizingService().evaluateDynamicAllocation(group.getName(), firstRemovalAt + 70_000L);
    Assertions.assertTrue(
        optimizerManager().listOptimizers(group.getName()).isEmpty(),
        "the cooldown expiry should admit the next removal");
  }

  private Metric draMetric(MetricDefine define, String groupName) {
    return MetricManager.getInstance()
        .getGlobalRegistry()
        .getMetrics()
        .get(new MetricKey(define, ImmutableMap.of("group", groupName)));
  }

  /**
   * The keeper owns the DRA metric lifecycle: watching a group registers its gauges and counters,
   * and a disable — which hands the group back to the legacy floor keeper — removes them.
   */
  @Test
  public void testDraMetricsRegisteredOnWatchAndRemovedOnDisable() throws InterruptedException {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    ResourceGroup group = buildDraResourceGroup(TEST_GROUP_NAME + "-12", 0, 1);
    optimizerManager().createResourceGroup(group);
    optimizingService().createResourceGroup(group);

    Assertions.assertNotNull(
        draMetric(
            DynamicAllocationMetrics.OPTIMIZER_GROUP_PENDING_REMOVAL_OPTIMIZERS, group.getName()));
    Assertions.assertNotNull(
        draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_EFFECTIVE_THREADS, group.getName()));
    Assertions.assertNotNull(
        draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_BACKLOG_DURATION_MS, group.getName()));
    Assertions.assertNotNull(
        draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_UP_TOTAL, group.getName()));
    Assertions.assertNotNull(
        draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_DOWN_TOTAL, group.getName()));

    Map<String, String> legacyProps = Maps.newHashMap();
    legacyProps.put("memory", "1024");
    ResourceGroup disabled =
        new ResourceGroup.Builder(group.getName(), MOCK_CONTAINER_NAME)
            .addProperties(legacyProps)
            .build();
    optimizerManager().updateResourceGroup(disabled);
    optimizingService().updateResourceGroup(disabled);
    Thread.sleep(500);

    Assertions.assertNull(
        draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_EFFECTIVE_THREADS, group.getName()),
        "a disabled group's DRA metrics must go with its watch");
    Assertions.assertNull(
        draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_UP_TOTAL, group.getName()));
  }

  /**
   * A disable unwatches on the config-entry path itself, not on the keeper's next round: the
   * round-driven unwatch runs on the leader only, so without this a follower would keep exporting
   * the group's DRA metrics until failover.
   */
  @Test
  public void testDisableUnwatchesOnUpdatePathWithoutKeeperRound() throws InterruptedException {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    ResourceGroup group = buildSlowDraResourceGroup(TEST_GROUP_NAME + "-15", 0);
    optimizerManager().createResourceGroup(group);
    optimizingService().createResourceGroup(group);
    Assertions.assertNotNull(
        draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_UP_TOTAL, group.getName()));
    // Let the delay-0 first round pass; the next one is minutes away (600s cadence), so any
    // removal observed below must come from the update path, not from a keeper round.
    Thread.sleep(200);

    Map<String, String> legacyProps = Maps.newHashMap();
    legacyProps.put("memory", "1024");
    ResourceGroup disabled =
        new ResourceGroup.Builder(group.getName(), MOCK_CONTAINER_NAME)
            .addProperties(legacyProps)
            .build();
    optimizerManager().updateResourceGroup(disabled);
    optimizingService().updateResourceGroup(disabled);

    Assertions.assertNull(
        draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_UP_TOTAL, group.getName()),
        "the update path must unwatch a disabled group on every node");
  }

  /** A scale-out round counts as one scale-up action, and the gauges read the keeper's state. */
  @Test
  public void testScaleUpRoundIncrementsCounterAndGaugesReadState() throws InterruptedException {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    ResourceGroup group = buildDraResourceGroup(TEST_GROUP_NAME + "-13", 2, 1);
    optimizerManager().createResourceGroup(group);
    optimizingService().createResourceGroup(group);
    Thread.sleep(500);

    Counter scaleUp =
        (Counter)
            draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_UP_TOTAL, group.getName());
    Assertions.assertEquals(
        1, scaleUp.getCount(), "one floor-deficit round is exactly one scale-up action");
    @SuppressWarnings("unchecked")
    org.apache.amoro.metrics.Gauge<Integer> effectiveThreads =
        (org.apache.amoro.metrics.Gauge<Integer>)
            draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_EFFECTIVE_THREADS, group.getName());
    Assertions.assertEquals(
        2,
        effectiveThreads.getValue().intValue(),
        "registered floor capacity should be visible as effective threads");
  }

  /** The pending-removal gauge tracks instances through drain start, retry, and removal. */
  @Test
  public void testPendingRemovalGaugeCountsDrainingInstances() {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    ResourceGroup group = buildSlowDraResourceGroup(TEST_GROUP_NAME + "-16", 0);
    optimizerManager().createResourceGroup(group);
    optimizingService().createResourceGroup(group);
    OptimizerInstance optimizer = registerOptimizer(group.getName(), 1);
    @SuppressWarnings("unchecked")
    org.apache.amoro.metrics.Gauge<Integer> pendingRemoval =
        (org.apache.amoro.metrics.Gauge<Integer>)
            draMetric(
                DynamicAllocationMetrics.OPTIMIZER_GROUP_PENDING_REMOVAL_OPTIMIZERS,
                group.getName());
    Assertions.assertEquals(0, pendingRemoval.getValue().intValue());

    // A failing release keeps the instance draining, exactly the stuck state the gauge is for.
    mockContainer.setReleaseAvailable(false);
    optimizingService().beginGracefulDrain(optimizer.getToken(), Long.MAX_VALUE);
    optimizingService().executeRemoval(optimizer.getToken());
    Assertions.assertEquals(1, pendingRemoval.getValue().intValue());

    mockContainer.setReleaseAvailable(true);
    optimizingService().executeRemoval(optimizer.getToken());
    Assertions.assertEquals(0, pendingRemoval.getValue().intValue());
  }

  /** Starting a drain counts as one scale-down action. */
  @Test
  public void testScaleDownIncrementsCounter() {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    ResourceGroup group = buildSlowDraResourceGroup(TEST_GROUP_NAME + "-14", 0);
    optimizerManager().createResourceGroup(group);
    optimizingService().createResourceGroup(group);
    registerOptimizer(group.getName(), 1);

    long t0 = System.currentTimeMillis();
    optimizingService().evaluateDynamicAllocation(group.getName(), t0);
    Counter scaleDown =
        (Counter)
            draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_DOWN_TOTAL, group.getName());
    Assertions.assertEquals(0, scaleDown.getCount());

    optimizingService().evaluateDynamicAllocation(group.getName(), t0 + 1_300_000L);
    Assertions.assertEquals(
        1, scaleDown.getCount(), "the drain start is the scale-down action, counted once");
  }

  /**
   * A watch whose metric registration fails must leave no residue: the group stays unwatched, so
   * the next config pass can watch it again instead of finding a watched-but-dead entry that
   * swallows every retry until a restart.
   */
  @Test
  public void testWatchFailureLeavesGroupRewatchable() {
    ResourceGroup group = buildSlowDraResourceGroup(TEST_GROUP_NAME + "-17", 0);
    optimizerManager().createResourceGroup(group);
    // Occupy one of the group's DRA keys so the watch's registration fails midway.
    MetricKey conflict =
        MetricManager.getInstance()
            .getGlobalRegistry()
            .register(
                DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_UP_TOTAL,
                ImmutableMap.of("group", group.getName()),
                new Counter());
    Assertions.assertThrows(
        RuntimeException.class, () -> optimizingService().createResourceGroup(group));
    Assertions.assertNull(
        draMetric(
            DynamicAllocationMetrics.OPTIMIZER_GROUP_PENDING_REMOVAL_OPTIMIZERS, group.getName()),
        "a failed watch must roll back the metrics it managed to register");
    MetricManager.getInstance().getGlobalRegistry().unregister(conflict);

    optimizingService().updateResourceGroup(group);
    Assertions.assertNotNull(
        draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_UP_TOTAL, group.getName()),
        "the group must be rewatchable after a failed watch");
    Assertions.assertNotNull(
        draMetric(
            DynamicAllocationMetrics.OPTIMIZER_GROUP_PENDING_REMOVAL_OPTIMIZERS, group.getName()));
  }

  /**
   * The global metric registry outlives the service on a leader hand-off: a disposed service must
   * unregister its DRA metrics, or the next leader's watch of the same group throws on the leftover
   * keys and the group goes watched-but-dead until a JVM restart.
   */
  @Test
  public void testDisposeUnregistersDraMetrics() {
    ResourceGroup group = buildSlowDraResourceGroup(TEST_GROUP_NAME + "-18", 0);
    optimizerManager().createResourceGroup(group);
    DefaultOptimizingService formerLeader =
        new DefaultOptimizingService(
            new Configurations(), catalogManager(), optimizerManager(), tableService(), null, null);
    formerLeader.createResourceGroup(group);
    Assertions.assertNotNull(
        draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_UP_TOTAL, group.getName()));

    formerLeader.dispose();
    Assertions.assertNull(
        draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_UP_TOTAL, group.getName()),
        "a disposed service must not leave DRA metrics behind for the next leader");

    // The next leader takes over the group without colliding with leftover keys.
    optimizingService().createResourceGroup(group);
    Assertions.assertNotNull(
        draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_UP_TOTAL, group.getName()));
  }

  /**
   * A watch arriving after dispose — an in-flight config-sync run or the keeper round's re-check
   * racing a leader hand-off — must not register metrics from the dead service: the keys would
   * outlive it in the global registry and fail the next leader's watch. Only never-watched groups
   * are exposed (a watched group's entry survives in watchedGroups and swallows the call), which is
   * exactly the racing paths' state: a new or re-enabled group, or one the round just unwatched.
   */
  @Test
  public void testWatchAfterDisposeDoesNotRegisterMetrics() {
    ResourceGroup group = buildSlowDraResourceGroup(TEST_GROUP_NAME + "-19", 0);
    DefaultOptimizingService formerLeader =
        new DefaultOptimizingService(
            new Configurations(), catalogManager(), optimizerManager(), tableService(), null, null);
    formerLeader.dispose();

    formerLeader.updateResourceGroup(group);
    Assertions.assertNull(
        draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_UP_TOTAL, group.getName()),
        "a watch arriving after dispose must not register metrics from a dead service");
  }

  /**
   * watch() is documented as idempotent: an update of an already-watched group with a still-enabled
   * config — routine property tuning — must re-enter watch() as a no-op instead of colliding with
   * the group's live metric keys.
   */
  @Test
  public void testEnabledUpdateReentersWatchIdempotently() {
    ResourceGroup group = buildSlowDraResourceGroup(TEST_GROUP_NAME + "-20", 0);
    optimizerManager().createResourceGroup(group);
    optimizingService().createResourceGroup(group);
    Metric before =
        draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_UP_TOTAL, group.getName());
    Assertions.assertNotNull(before);

    Map<String, String> properties = Maps.newHashMap(group.getProperties());
    properties.put(OptimizerProperties.DYNAMIC_ALLOCATION_MAX_PARALLELISM, "6");
    ResourceGroup updated =
        new ResourceGroup.Builder(group.getName(), MOCK_CONTAINER_NAME)
            .addProperties(properties)
            .build();
    optimizerManager().updateResourceGroup(updated);
    optimizingService().updateResourceGroup(updated);

    Assertions.assertSame(
        before,
        draMetric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_UP_TOTAL, group.getName()),
        "an enabled-to-enabled update must keep the group's live metrics, not re-register them");
  }

  /**
   * Disabling dynamic allocation mid-drain re-admits the draining pod to task assignment: once the
   * legacy floor keeper resumes duty for the group, a leftover drain block would starve the pod
   * forever.
   */
  @Test
  public void testDisablingDraCancelsLingeringDrain() throws InterruptedException {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    ResourceGroup group = buildDraResourceGroup(TEST_GROUP_NAME + "-11", 0, 1);
    optimizerManager().createResourceGroup(group);
    optimizingService().createResourceGroup(group);
    OptimizerInstance optimizer = registerOptimizer(group.getName(), 1);

    // A drain that cannot complete (release keeps failing) lingers across keeper rounds.
    mockContainer.setReleaseAvailable(false);
    optimizingService().beginGracefulDrain(optimizer.getToken(), Long.MAX_VALUE);
    Thread.sleep(200);
    Assertions.assertTrue(optimizingService().isDraining(optimizer.getToken()));

    Map<String, String> legacyProps = Maps.newHashMap();
    legacyProps.put("memory", "1024");
    ResourceGroup disabled =
        new ResourceGroup.Builder(group.getName(), MOCK_CONTAINER_NAME)
            .addProperties(legacyProps)
            .build();
    optimizerManager().updateResourceGroup(disabled);
    optimizingService().updateResourceGroup(disabled);
    Thread.sleep(500);

    Assertions.assertFalse(
        optimizingService().isDraining(optimizer.getToken()),
        "unwatching a disabled group must lift its drain blocks");
    Assertions.assertEquals(
        1,
        optimizerManager().listOptimizers(group.getName()).size(),
        "the pod survives and resumes normal duty");
  }
}
