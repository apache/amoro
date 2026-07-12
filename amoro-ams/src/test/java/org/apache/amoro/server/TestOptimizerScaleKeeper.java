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
import org.apache.amoro.resource.ResourceContainer;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.resource.ContainerMetadata;
import org.apache.amoro.server.resource.Containers;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.table.AMSTableTestBase;
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
    TestOptimizerGroupKeeper.MockOptimizerContainer mockContainer =
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
}
