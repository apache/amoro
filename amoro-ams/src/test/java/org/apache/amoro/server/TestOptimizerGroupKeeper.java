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

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

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
    setupMockContainer();
  }

  @After
  public void clear() {
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
  private void setupMockContainer() throws Exception {
    MockOptimizerContainer mockContainer =
        new MockOptimizerContainer(resourceAvailable, scaleOutCallCount, optimizerRegistrar);

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
    // OPTIMIZER_GROUP_MIN_PARALLELISM_CHECK_INTERVAL is set to 10ms, call intervals are 10, 20, 30,
    // 40, 10, 200ms can cover abnormal scenarios
    Thread.sleep(200);

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

    Thread.sleep(200);
    Assertions.assertEquals(
        OPTIMIZER_GROUP_MAX_KEEPING_ATTEMPTS.defaultValue(),
        scaleOutCallCount.get(),
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

    Thread.sleep(200);

    ResourceGroup updatedGroup = optimizerManager().getResourceGroup(resourceGroup.getName());
    String minParallelismStr =
        updatedGroup.getProperties().get(OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM);
    Assertions.assertEquals(
        OPTIMIZER_GROUP_MAX_KEEPING_ATTEMPTS.defaultValue(),
        scaleOutCallCount.get(),
        resourceGroup.getName()
            + ":max scale-out attempts should be exhausted when no resources available");
    Assertions.assertEquals(
        "1",
        minParallelismStr,
        resourceGroup.getName()
            + ":min-parallelism should be reset to optimizer's current total cores (1) when no more resources available");
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

    public MockOptimizerContainer(
        AtomicBoolean resourceAvailable,
        AtomicInteger scaleOutCallCount,
        Function<OptimizerRegisterInfo, String> optimizerRegistrar) {
      this.resourceAvailable = resourceAvailable;
      this.scaleOutCallCount = scaleOutCallCount;
      this.optimizerRegistrar = optimizerRegistrar;
    }

    @Override
    public void init(String name, Map<String, String> containerProperties) {}

    @Override
    protected Map<String, String> doScaleOut(Resource resource) {
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
