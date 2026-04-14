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

import static org.apache.amoro.server.AmoroManagementConf.OPTIMIZER_GROUP_MAX_KEEPING_ATTEMPTS;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.api.OptimizerRegisterInfo;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.resource.Resource;
import org.apache.amoro.resource.ResourceContainer;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.resource.ResourceType;
import org.apache.amoro.server.manager.AbstractOptimizerContainer;
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

@RunWith(Parameterized.class)
public class TestOptimizerGroupKeeper extends AMSTableTestBase {

  private static final String TEST_GROUP_NAME = "test-keeper-group";
  private static final String MOCK_CONTAINER_NAME = "mock-container";

  // Control flags for mock container behavior
  private final AtomicBoolean resourceAvailable = new AtomicBoolean(true);
  private final AtomicInteger scaleOutCallCount = new AtomicInteger(0);
  // Function to register optimizer (will call authenticate)
  private Function<OptimizerRegisterInfo, String> optimizerRegistrar;
  private static boolean originIsInitialized = false;
  // Track the current test's group name for cleanup
  private String currentGroupName;

  public TestOptimizerGroupKeeper(
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
  public void clear() throws InterruptedException {
    if (currentGroupName == null) {
      return;
    }
    try {
      // Clean up optimizers
      optimizerManager()
          .listOptimizers(currentGroupName)
          .forEach(
              optimizer ->
                  optimizingService()
                      .deleteOptimizer(optimizer.getGroupName(), optimizer.getResourceId()));
      // Clean up remaining resources
      try {
        optimizerManager()
            .listResourcesByGroup(currentGroupName)
            .forEach(resource -> optimizerManager().deleteResource(resource.getResourceId()));
      } catch (Exception ignored) {
      }
      // Delete resource group from optimizing service first (this will dispose and unregister
      // metrics)
      try {
        optimizingService().deleteResourceGroup(currentGroupName);
      } catch (Exception ignored) {
      }
      // Then delete from optimizer manager
      try {
        optimizerManager().deleteResourceGroup(currentGroupName);
      } catch (Exception ignored) {
      }
      // Wait for keeper thread to finish processing any in-flight tasks for this group
      Thread.sleep(50);
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

  /** Setup mock container and inject it into Containers using reflection. */
  private void setupMockContainer(Supplier<String> targetGroupNameSupplier) throws Exception {
    MockOptimizerContainer mockContainer =
        new MockOptimizerContainer(
            resourceAvailable, scaleOutCallCount, optimizerRegistrar, targetGroupNameSupplier);

    // Use reflection to set isInitialized to true
    DynFields.UnboundField<Boolean> initializedField =
        DynFields.builder().hiddenImpl(Containers.class, "isInitialized").build();
    if (!initializedField.asStatic().get()) {
      originIsInitialized = false;
      initializedField.asStatic().set(true);
    }

    // Use reflection to inject mock container into Containers
    DynFields.UnboundField<Map<String, Object>> containersField =
        DynFields.builder().hiddenImpl(Containers.class, "globalContainers").build();
    Map<String, Object> globalContainers = containersField.asStatic().get();

    // Create ContainerWrapper using reflection
    ContainerMetadata metadata =
        new ContainerMetadata(MOCK_CONTAINER_NAME, MockOptimizerContainer.class.getName());
    Map<String, String> properties = Maps.newHashMap();
    properties.put(OptimizerProperties.AMS_HOME, "/tmp");
    properties.put(OptimizerProperties.AMS_OPTIMIZER_URI, "thrift://localhost:1261");
    properties.put("memory", "1024");
    metadata.setProperties(properties);

    // Create ContainerWrapper with pre-initialized container
    Class<?> wrapperClass =
        Class.forName("org.apache.amoro.server.resource.Containers$ContainerWrapper");
    // Get the two-parameter constructor: ContainerWrapper(ContainerMetadata, ResourceContainer)
    java.lang.reflect.Constructor<?> constructor =
        wrapperClass.getDeclaredConstructor(ContainerMetadata.class, ResourceContainer.class);
    constructor.setAccessible(true);
    Object wrapper = constructor.newInstance(metadata, mockContainer);
    globalContainers.put(MOCK_CONTAINER_NAME, wrapper);
  }

  private ResourceGroup buildTestResourceGroup(String groupName, int minParallelism) {
    // Track the group name for cleanup
    this.currentGroupName = groupName;
    Map<String, String> properties = Maps.newHashMap();
    properties.put(
        OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM, String.valueOf(minParallelism));
    properties.put("memory", "1024");
    return new ResourceGroup.Builder(groupName, MOCK_CONTAINER_NAME)
        .addProperties(properties)
        .build();
  }

  /**
   * Test scenario 1: When resources are available, optimizer will be auto-allocated.
   *
   * <p>When min-parallelism > current optimizer cores and resources are available,
   * OptimizerGroupKeeper should automatically create new optimizer instances.
   */
  @Test
  public void testOptimizerAutoAllocatedWhenResourceAvailable() throws InterruptedException {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    ResourceGroup resourceGroup = buildTestResourceGroup(TEST_GROUP_NAME + "-1", 2);

    optimizerManager().createResourceGroup(resourceGroup);
    optimizingService().createResourceGroup(resourceGroup);

    // Wait for OptimizerGroupKeeper to detect and create optimizer
    waitUntil(() -> !optimizerManager().listOptimizers(resourceGroup.getName()).isEmpty(), 2000);

    int totalCores =
        optimizerManager().listOptimizers(resourceGroup.getName()).stream()
            .mapToInt(OptimizerInstance::getThreadCount)
            .sum();

    Assertions.assertEquals(
        1,
        scaleOutCallCount.get(),
        resourceGroup.getName()
            + ":One scale-out should be triggered when min-parallelism is not satisfied");
    Assertions.assertEquals(
        2,
        totalCores,
        resourceGroup.getName()
            + ":OptimizerGroupKeeper should attempt to create optimizer when resources are needed");
  }

  /**
   * Test scenario 2: When min-parallelism is already satisfied, optimizer will not be allocated.
   *
   * <p>When current optimizer cores >= min-parallelism, OptimizerGroupKeeper should not trigger any
   * scale-out operation.
   */
  @Test
  public void testNoAllocationWhenMinParallelismSatisfied() throws InterruptedException {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    ResourceGroup resourceGroup = buildTestResourceGroup(TEST_GROUP_NAME + "-2", 2);

    optimizerManager().createResourceGroup(resourceGroup);
    optimizingService().createResourceGroup(resourceGroup);

    // Register an optimizer with 3 threads (exceeds min-parallelism of 2)
    OptimizerRegisterInfo registerInfo = buildRegisterInfo(resourceGroup.getName(), 3);
    String testToken = optimizingService().authenticate(registerInfo);
    Assertions.assertNotNull(testToken, "Optimizer should be registered successfully");

    Thread.sleep(200);

    // Verify no scale-out was triggered since min-parallelism is satisfied
    Assertions.assertEquals(
        0,
        scaleOutCallCount.get(),
        resourceGroup.getName()
            + ":No scale-out should be triggered when min-parallelism is already satisfied");
  }

  /**
   * Test scenario 3: When no resources available, min-parallelism will be reset to 0.
   *
   * <p>When OptimizerGroupKeeper fails to create optimizer multiple times (exceeds max attempts),
   * and there are no existing optimizers, it will reset min-parallelism to 0.
   */
  @Test
  public void testMinParallelismResetToZeroWhenNoResource() throws InterruptedException {
    // Set resource not available - container will throw exception on scaleOut
    resourceAvailable.set(false);
    scaleOutCallCount.set(0);
    ResourceGroup resourceGroup = buildTestResourceGroup(TEST_GROUP_NAME + "-3", 2);

    optimizerManager().createResourceGroup(resourceGroup);
    optimizingService().createResourceGroup(resourceGroup);

    // Wait for max-keeping-attempts to be exhausted and min-parallelism to be reset
    waitUntil(
        () -> {
          ResourceGroup rg = optimizerManager().getResourceGroup(resourceGroup.getName());
          String mp = rg.getProperties().get(OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM);
          return "0".equals(mp);
        },
        2000);
    Assertions.assertTrue(
        scaleOutCallCount.get() >= OPTIMIZER_GROUP_MAX_KEEPING_ATTEMPTS.defaultValue(),
        resourceGroup.getName()
            + ":max scale-out attempts should be exhausted when no resources available");
    ResourceGroup updatedGroup = optimizerManager().getResourceGroup(resourceGroup.getName());
    String minParallelismStr =
        updatedGroup.getProperties().get(OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM);
    Assertions.assertEquals(
        "0",
        minParallelismStr,
        resourceGroup.getName()
            + ":min-parallelism should be reset to 0 when no resources available and no optimizer exists");
  }

  /**
   * Test scenario 4: When no resources but has optimizer, min-parallelism will be reset to
   * optimizer's executionParallel.
   *
   * <p>When OptimizerGroupKeeper fails to create optimizer multiple times and there are existing
   * optimizers but not enough to meet min-parallelism, it will reset min-parallelism to the current
   * total cores.
   */
  @Test
  public void testMinParallelismResetToOptimizerParallelWhenNoMoreResource()
      throws InterruptedException {
    resourceAvailable.set(false);
    scaleOutCallCount.set(0);
    ResourceGroup resourceGroup = buildTestResourceGroup(TEST_GROUP_NAME + "-4", 2);

    optimizerManager().createResourceGroup(resourceGroup);
    optimizingService().createResourceGroup(resourceGroup);

    OptimizerRegisterInfo registerInfo = buildRegisterInfo(resourceGroup.getName(), 1);
    String testToken = optimizingService().authenticate(registerInfo);
    Assertions.assertNotNull(testToken, "Optimizer should be registered successfully");

    // Wait for max-keeping-attempts to be exhausted and min-parallelism to be reset
    waitUntil(
        () -> {
          ResourceGroup rg = optimizerManager().getResourceGroup(resourceGroup.getName());
          String mp = rg.getProperties().get(OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM);
          return "1".equals(mp);
        },
        2000);

    ResourceGroup updatedGroup = optimizerManager().getResourceGroup(resourceGroup.getName());
    String minParallelismStr =
        updatedGroup.getProperties().get(OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM);
    Assertions.assertTrue(
        scaleOutCallCount.get() >= OPTIMIZER_GROUP_MAX_KEEPING_ATTEMPTS.defaultValue(),
        resourceGroup.getName()
            + ":max scale-out attempts should be exhausted when no resources available");
    Assertions.assertEquals(
        "1",
        minParallelismStr,
        resourceGroup.getName()
            + ":min-parallelism should be reset to optimizer's current total cores (1) when no more resources available");
  }

  /**
   * Test scenario 5: When auto-restart is enabled and an optimizer goes down unexpectedly, the
   * orphaned resource should be detected and the optimizer restarted automatically.
   *
   * <p>Steps:
   *
   * <ol>
   *   <li>Create a resource group and manually insert a resource (simulating a previous optimizer
   *       start)
   *   <li>Do NOT register any optimizer (simulating optimizer crash before/after registration)
   *   <li>Wait for OptimizerGroupKeeper to detect the orphaned resource and restart the optimizer
   *   <li>Verify that the container's requestResource was called to restart
   * </ol>
   */
  @Test
  public void testOrphanedResourceAutoRestart() throws InterruptedException {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    ResourceGroup resourceGroup = buildTestResourceGroup(TEST_GROUP_NAME + "-5", 0);

    optimizerManager().createResourceGroup(resourceGroup);
    optimizingService().createResourceGroup(resourceGroup);

    // Manually create a resource in DB (simulating a previously started optimizer)
    Resource orphanedResource =
        new Resource.Builder(MOCK_CONTAINER_NAME, resourceGroup.getName(), ResourceType.OPTIMIZER)
            .setThreadCount(2)
            .setProperties(resourceGroup.getProperties())
            .build();
    optimizerManager().createResource(orphanedResource);

    // No optimizer registered for this resource — it's orphaned
    // Wait for OptimizerGroupKeeper to detect and restart
    waitUntil(() -> scaleOutCallCount.get() >= 1, 2000);

    // Verify that requestResource was called exactly once for the orphaned resource
    Assertions.assertEquals(
        1,
        scaleOutCallCount.get(),
        resourceGroup.getName()
            + ":Exactly one restart should be triggered for the orphaned resource");
  }

  /**
   * Test scenario 6: When auto-restart is enabled and the restart fails repeatedly, the orphaned
   * resource should be cleaned up after exceeding max retries.
   *
   * <p>Steps:
   *
   * <ol>
   *   <li>Create a resource group with an orphaned resource
   *   <li>Set resource unavailable (container throws exception on requestResource)
   *   <li>Wait for max retries to be exhausted
   *   <li>Verify the orphaned resource is deleted from DB
   * </ol>
   */
  @Test
  public void testOrphanedResourceCleanupAfterMaxRetries() throws InterruptedException {
    resourceAvailable.set(false);
    scaleOutCallCount.set(0);
    ResourceGroup resourceGroup = buildTestResourceGroup(TEST_GROUP_NAME + "-6", 0);

    optimizerManager().createResourceGroup(resourceGroup);
    optimizingService().createResourceGroup(resourceGroup);

    // Manually create a resource (simulating a previously started optimizer that crashed)
    Resource orphanedResource =
        new Resource.Builder(MOCK_CONTAINER_NAME, resourceGroup.getName(), ResourceType.OPTIMIZER)
            .setThreadCount(2)
            .setProperties(resourceGroup.getProperties())
            .build();
    optimizerManager().createResource(orphanedResource);

    // Wait for orphaned resource to be cleaned up from DB after max retries
    waitUntil(
        () -> optimizerManager().listResourcesByGroup(resourceGroup.getName()).isEmpty(), 5000);

    // Verify the orphaned resource has been cleaned up from DB
    List<Resource> remainingResources =
        optimizerManager().listResourcesByGroup(resourceGroup.getName());
    Assertions.assertTrue(
        remainingResources.isEmpty(),
        resourceGroup.getName()
            + ":Orphaned resource should be cleaned up after max retries are exhausted");
  }

  /**
   * Test scenario 7: When auto-restart is enabled but the resource already has an active optimizer,
   * no restart should be triggered.
   */
  @Test
  public void testNoRestartWhenOptimizerIsActive() throws InterruptedException {
    resourceAvailable.set(true);
    scaleOutCallCount.set(0);
    ResourceGroup resourceGroup = buildTestResourceGroup(TEST_GROUP_NAME + "-7", 0);

    optimizerManager().createResourceGroup(resourceGroup);
    optimizingService().createResourceGroup(resourceGroup);

    // Create a resource and register an optimizer for it (healthy state)
    Resource resource =
        new Resource.Builder(MOCK_CONTAINER_NAME, resourceGroup.getName(), ResourceType.OPTIMIZER)
            .setThreadCount(2)
            .setProperties(resourceGroup.getProperties())
            .build();
    optimizerManager().createResource(resource);

    // Register an optimizer for this resource
    OptimizerRegisterInfo registerInfo =
        buildRegisterInfo(resourceGroup.getName(), resource.getThreadCount());
    registerInfo.setResourceId(resource.getResourceId());
    optimizingService().authenticate(registerInfo);

    // Reset counter after authenticate
    scaleOutCallCount.set(0);

    // Wait a few check cycles to verify no restart is triggered
    Thread.sleep(200);

    // Verify no restart was triggered since the resource has an active optimizer
    Assertions.assertEquals(
        0,
        scaleOutCallCount.get(),
        resourceGroup.getName()
            + ":No restart should be triggered when resource has an active optimizer");
  }

  /** Wait for a condition to become true, polling every 10ms. */
  private static void waitUntil(Supplier<Boolean> condition, long timeoutMs)
      throws InterruptedException {
    long deadline = System.currentTimeMillis() + timeoutMs;
    while (!condition.get()) {
      if (System.currentTimeMillis() > deadline) {
        throw new AssertionError("Condition not met within " + timeoutMs + "ms");
      }
      Thread.sleep(10);
    }
  }

  private static OptimizerRegisterInfo buildRegisterInfo(String groupName, int threadCount) {
    OptimizerRegisterInfo registerInfo = new OptimizerRegisterInfo();
    Map<String, String> registerProperties = Maps.newHashMap();
    registerProperties.put(OptimizerProperties.OPTIMIZER_HEART_BEAT_INTERVAL, "100");
    registerInfo.setProperties(registerProperties);
    registerInfo.setThreadCount(threadCount);
    registerInfo.setMemoryMb(1024);
    registerInfo.setGroupName(groupName);
    registerInfo.setResourceId("test-resource-" + System.currentTimeMillis() + "-" + threadCount);
    registerInfo.setStartTime(System.currentTimeMillis());
    return registerInfo;
  }

  /**
   * Mock optimizer container for testing.
   *
   * <p>Simulates resource availability by controlling doScaleOut behavior:
   *
   * <ul>
   *   <li>When resourceAvailable=true: calls authenticate to register optimizer
   *   <li>When resourceAvailable=false: throw RuntimeException
   * </ul>
   */
  public static class MockOptimizerContainer extends AbstractOptimizerContainer {

    private final AtomicBoolean resourceAvailable;
    private final AtomicInteger scaleOutCallCount;
    private final Function<OptimizerRegisterInfo, String> optimizerRegistrar;
    private final Supplier<String> targetGroupNameSupplier;

    public MockOptimizerContainer(
        AtomicBoolean resourceAvailable,
        AtomicInteger scaleOutCallCount,
        Function<OptimizerRegisterInfo, String> optimizerRegistrar,
        Supplier<String> targetGroupNameSupplier) {
      this.resourceAvailable = resourceAvailable;
      this.scaleOutCallCount = scaleOutCallCount;
      this.optimizerRegistrar = optimizerRegistrar;
      this.targetGroupNameSupplier = targetGroupNameSupplier;
    }

    @Override
    public void init(String name, Map<String, String> containerProperties) {}

    @Override
    protected Map<String, String> doScaleOut(Resource resource) {
      String targetGroup = targetGroupNameSupplier != null ? targetGroupNameSupplier.get() : null;
      if (targetGroup != null && !targetGroup.equals(resource.getGroupName())) {
        // Stale task from a previously leaked group; silently ignore to prevent cross-test
        // contamination of scaleOutCallCount via the shared static Containers registry.
        return Maps.newHashMap();
      }
      scaleOutCallCount.incrementAndGet();
      if (!resourceAvailable.get()) {
        throw new RuntimeException("No resources available");
      }
      // When resources are available, register optimizer by calling authenticate
      // This simulates the real behavior where SparkOptimizerContainer starts SparkOptimizer,
      // which uses OptimizerToucher to call authenticate
      if (optimizerRegistrar != null) {
        OptimizerRegisterInfo registerInfo =
            buildRegisterInfo(resource.getGroupName(), resource.getThreadCount());
        registerInfo.setMemoryMb(resource.getMemoryMb());
        registerInfo.setResourceId(resource.getResourceId());
        optimizerRegistrar.apply(registerInfo);
      }
      return Maps.newHashMap();
    }

    @Override
    public void releaseResource(Resource resource) {}
  }
}
