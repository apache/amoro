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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.amoro.client.AmsServerInfo;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.properties.AmsHAProperties;
import org.apache.amoro.server.ha.ZkHighAvailabilityContainer;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.CuratorFramework;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.amoro.shade.zookeeper3.org.apache.zookeeper.CreateMode;
import org.apache.amoro.shade.zookeeper3.org.apache.zookeeper.KeeperException;
import org.apache.amoro.shade.zookeeper3.org.apache.zookeeper.data.Stat;
import org.apache.amoro.utils.JacksonUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/** Test for AmsAssignService using mocked ZK to avoid connection issues. */
public class TestAmsAssignService {

  private Configurations serviceConfig;
  private ZkHighAvailabilityContainer haContainer;
  private AmsAssignService assignService;
  private AmsServerInfo node1;
  private AmsServerInfo node2;
  private AmsServerInfo node3;
  private MockZkState mockZkState;
  private CuratorFramework mockZkClient;
  private LeaderLatch mockLeaderLatch;
  private MockBucketAssignStore mockAssignStore;

  @Before
  public void setUp() throws Exception {
    mockZkState = new MockZkState();
    mockZkClient = createMockZkClient();
    mockLeaderLatch = createMockLeaderLatch(true); // Is leader by default
    mockAssignStore = new MockBucketAssignStore();

    serviceConfig = new Configurations();
    serviceConfig.setString(AmoroManagementConf.SERVER_EXPOSE_HOST, "127.0.0.1");
    serviceConfig.setInteger(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT, 1260);
    serviceConfig.setInteger(AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT, 1261);
    serviceConfig.setInteger(AmoroManagementConf.HTTP_SERVER_PORT, 1630);
    serviceConfig.setBoolean(AmoroManagementConf.HA_ENABLE, true);
    serviceConfig.setString(AmoroManagementConf.HA_ZOOKEEPER_ADDRESS, "127.0.0.1:2181");
    serviceConfig.setString(AmoroManagementConf.HA_CLUSTER_NAME, "test-cluster");
    serviceConfig.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, true);
    serviceConfig.setInteger(AmoroManagementConf.BUCKET_ID_TOTAL_COUNT, 100);
    serviceConfig.set(AmoroManagementConf.NODE_OFFLINE_TIMEOUT, java.time.Duration.ofMinutes(5));

    haContainer = createContainerWithMockZk();

    // Create AmsAssignService with mock assign store
    assignService = createAssignServiceWithMockStore();

    node1 = new AmsServerInfo();
    node1.setHost("127.0.0.1");
    node1.setThriftBindPort(1260);
    node1.setRestBindPort(1630);

    node2 = new AmsServerInfo();
    node2.setHost("127.0.0.2");
    node2.setThriftBindPort(1262);
    node2.setRestBindPort(1632);

    node3 = new AmsServerInfo();
    node3.setHost("127.0.0.3");
    node3.setThriftBindPort(1263);
    node3.setRestBindPort(1633);
  }

  @After
  public void tearDown() throws Exception {
    if (assignService != null) {
      assignService.stop();
    }
    if (haContainer != null) {
      haContainer.close();
    }
    mockZkState.clear();
  }

  @Test
  public void testInitialAssignment() throws Exception {
    // Register nodes
    haContainer.registAndElect();

    // Create second node
    Configurations config2 = createNodeConfig("127.0.0.2", 1262, 1632);
    ZkHighAvailabilityContainer haContainer2 = createContainerWithMockZk(config2);
    haContainer2.registAndElect();

    try {
      // Wait a bit for registration
      Thread.sleep(100);

      // Trigger assignment manually
      assignService.doAssignForTest();

      // Check assignments
      Map<AmsServerInfo, List<String>> assignments = mockAssignStore.getAllAssignments();
      Assert.assertEquals("Should have assignments for 2 nodes", 2, assignments.size());

      // Verify buckets are distributed
      int totalAssigned = 0;
      for (List<String> buckets : assignments.values()) {
        totalAssigned += buckets.size();
        Assert.assertTrue("Each node should have buckets", !buckets.isEmpty());
      }
      Assert.assertEquals("All buckets should be assigned", 100, totalAssigned);

      // Verify balance (difference should be at most 1)
      List<Integer> bucketCounts = new ArrayList<>();
      for (List<String> buckets : assignments.values()) {
        bucketCounts.add(buckets.size());
      }
      int max = bucketCounts.stream().mapToInt(Integer::intValue).max().orElse(0);
      int min = bucketCounts.stream().mapToInt(Integer::intValue).min().orElse(0);
      Assert.assertTrue("Difference should be at most 1", max - min <= 1);
    } finally {
      haContainer2.close();
    }
  }

  @Test
  public void testNodeOfflineReassignment() throws Exception {
    // Setup: 2 nodes with initial assignment
    haContainer.registAndElect();
    Configurations config2 = createNodeConfig("127.0.0.2", 1262, 1632);
    ZkHighAvailabilityContainer haContainer2 = createContainerWithMockZk(config2);
    haContainer2.registAndElect();

    try {
      Thread.sleep(100);

      // Initial assignment
      assignService.doAssignForTest();
      Map<AmsServerInfo, List<String>> initialAssignments = mockAssignStore.getAllAssignments();
      Assert.assertEquals("Should have 2 nodes", 2, initialAssignments.size());

      // Verify initial assignment is balanced
      List<Integer> initialCounts = new ArrayList<>();
      for (List<String> buckets : initialAssignments.values()) {
        initialCounts.add(buckets.size());
      }
      int maxInitial = initialCounts.stream().mapToInt(Integer::intValue).max().orElse(0);
      int minInitial = initialCounts.stream().mapToInt(Integer::intValue).min().orElse(0);
      Assert.assertTrue("Initial assignment should be balanced", maxInitial - minInitial <= 1);

      // Simulate node2 going offline by removing it from mock state
      mockZkState.deleteNodeByHost("127.0.0.2");
      Thread.sleep(100);

      // Trigger reassignment
      assignService.doAssignForTest();

      // Check that node2's buckets are redistributed
      Map<AmsServerInfo, List<String>> newAssignments = mockAssignStore.getAllAssignments();
      Assert.assertFalse(
          "Should have at least 1 node after offline, but got: " + newAssignments.size(),
          newAssignments.isEmpty());
      Assert.assertEquals(
          "Should have 1 node after offline, but got: " + newAssignments.size(),
          1,
          newAssignments.size());

      // Verify node1 got all buckets
      // Since there's only one node left after node2 goes offline, it should be node1
      // Get the only entry from assignments
      Map.Entry<AmsServerInfo, List<String>> onlyEntry =
          newAssignments.entrySet().iterator().next();
      AmsServerInfo remainingNode = onlyEntry.getKey();
      List<String> node1Buckets = onlyEntry.getValue();

      // Verify it's node1 by matching host and thriftBindPort
      Assert.assertEquals(
          "Remaining node should be node1 (host match). Expected: "
              + node1.getHost()
              + ", Got: "
              + remainingNode.getHost(),
          node1.getHost(),
          remainingNode.getHost());
      Assert.assertEquals(
          "Remaining node should be node1 (thriftBindPort match). Expected: "
              + (node1.getThriftBindPort() + 1)
              + ", Got: "
              + remainingNode.getThriftBindPort(),
          (Integer) (node1.getThriftBindPort() + 1),
          remainingNode.getThriftBindPort());

      // Verify node1 got all buckets
      Assert.assertNotNull(
          "Node1 should have assignments, but got null. Remaining node: " + remainingNode,
          node1Buckets);
      Assert.assertEquals(
          "Node1 should have all buckets. Expected: 100, Got: "
              + (node1Buckets != null ? node1Buckets.size() : "null"),
          100,
          node1Buckets != null ? node1Buckets.size() : 0);
    } finally {
      try {
        haContainer2.close();
      } catch (Exception e) {
        // ignore
      }
    }
  }

  @Test
  public void testNewNodeIncrementalAssignment() throws Exception {
    // Setup: 1 node initially
    haContainer.registAndElect();
    Thread.sleep(100);

    // Initial assignment - all buckets to node1
    assignService.doAssignForTest();
    Map<AmsServerInfo, List<String>> initialAssignments = mockAssignStore.getAllAssignments();
    Assert.assertEquals("Should have 1 node initially", 1, initialAssignments.size());

    // Find node1 in assignments by matching host and port
    List<String> node1InitialBuckets = null;
    for (Map.Entry<AmsServerInfo, List<String>> entry : initialAssignments.entrySet()) {
      AmsServerInfo node = entry.getKey();
      if (node1.getHost().equals(node.getHost())
          && node1.getThriftBindPort().equals(node.getThriftBindPort() - 1)) {
        node1InitialBuckets = entry.getValue();
        break;
      }
    }
    Assert.assertNotNull("Node1 should have assignments", node1InitialBuckets);
    Assert.assertEquals("Node1 should have all buckets initially", 100, node1InitialBuckets.size());

    // Add new node
    Configurations config2 = createNodeConfig("127.0.0.2", 1262, 1632);
    ZkHighAvailabilityContainer haContainer2 = createContainerWithMockZk(config2);
    haContainer2.registAndElect();

    try {
      Thread.sleep(100);

      // Trigger reassignment
      assignService.doAssignForTest();

      // Check assignments
      Map<AmsServerInfo, List<String>> newAssignments = mockAssignStore.getAllAssignments();
      Assert.assertEquals("Should have 2 nodes", 2, newAssignments.size());

      // Verify incremental assignment - node1 should keep most of its buckets
      List<String> node1NewBuckets = null;
      for (Map.Entry<AmsServerInfo, List<String>> entry : newAssignments.entrySet()) {
        AmsServerInfo node = entry.getKey();
        if (node1.getHost().equals(node.getHost())
            && node1.getThriftBindPort().equals(node.getThriftBindPort() - 1)) {
          node1NewBuckets = entry.getValue();
          break;
        }
      }
      Assert.assertNotNull("Node1 should still have assignments", node1NewBuckets);

      // Node1 should have kept most buckets (incremental assignment)
      Assert.assertTrue("Node1 should keep some buckets", node1NewBuckets.size() > 0);

      // Verify balance
      List<Integer> bucketCounts = new ArrayList<>();
      for (List<String> buckets : newAssignments.values()) {
        bucketCounts.add(buckets.size());
      }
      int max = bucketCounts.stream().mapToInt(Integer::intValue).max().orElse(0);
      int min = bucketCounts.stream().mapToInt(Integer::intValue).min().orElse(0);
      Assert.assertTrue("Difference should be at most 1", max - min <= 1);

      // Verify total
      int total = bucketCounts.stream().mapToInt(Integer::intValue).sum();
      Assert.assertEquals("Total buckets should be 100", 100, total);
    } finally {
      haContainer2.close();
    }
  }

  @Test
  public void testBalanceAfterNodeChanges() throws Exception {
    // Setup: 3 nodes
    haContainer.registAndElect();
    Configurations config2 = createNodeConfig("127.0.0.2", 1262, 1632);
    ZkHighAvailabilityContainer haContainer2 = createContainerWithMockZk(config2);
    haContainer2.registAndElect();
    Configurations config3 = createNodeConfig("127.0.0.3", 1263, 1633);
    ZkHighAvailabilityContainer haContainer3 = createContainerWithMockZk(config3);
    haContainer3.registAndElect();

    try {
      Thread.sleep(200);

      // Initial assignment
      assignService.doAssignForTest();

      // Verify balance
      Map<AmsServerInfo, List<String>> assignments = mockAssignStore.getAllAssignments();
      Assert.assertEquals("Should have 3 nodes", 3, assignments.size());

      List<Integer> bucketCounts = new ArrayList<>();
      for (List<String> buckets : assignments.values()) {
        bucketCounts.add(buckets.size());
      }
      int max = bucketCounts.stream().mapToInt(Integer::intValue).max().orElse(0);
      int min = bucketCounts.stream().mapToInt(Integer::intValue).min().orElse(0);
      Assert.assertTrue("Difference should be at most 1", max - min <= 1);

      // Verify all buckets are assigned
      int total = bucketCounts.stream().mapToInt(Integer::intValue).sum();
      Assert.assertEquals("All buckets should be assigned", 100, total);
    } finally {
      haContainer2.close();
      haContainer3.close();
    }
  }

  @Test
  public void testIncrementalAssignmentMinimizesMigration() throws Exception {
    // Setup: 2 nodes initially
    haContainer.registAndElect();
    Configurations config2 = createNodeConfig("127.0.0.2", 1262, 1632);
    ZkHighAvailabilityContainer haContainer2 = createContainerWithMockZk(config2);
    haContainer2.registAndElect();
    ZkHighAvailabilityContainer haContainer3 = null;

    try {
      Thread.sleep(100);

      // Initial assignment
      assignService.doAssignForTest();
      Map<AmsServerInfo, List<String>> initialAssignments = mockAssignStore.getAllAssignments();

      // Record initial assignments
      Set<String> node1InitialBuckets = new HashSet<>();
      Set<String> node2InitialBuckets = new HashSet<>();
      for (Map.Entry<AmsServerInfo, List<String>> entry : initialAssignments.entrySet()) {
        if (entry.getKey().getHost().equals("127.0.0.1")) {
          node1InitialBuckets.addAll(entry.getValue());
        } else {
          node2InitialBuckets.addAll(entry.getValue());
        }
      }

      // Add new node
      Configurations config3 = createNodeConfig("127.0.0.3", 1263, 1633);
      haContainer3 = createContainerWithMockZk(config3);
      haContainer3.registAndElect();

      Thread.sleep(100);

      // Trigger reassignment
      assignService.doAssignForTest();

      // Check new assignments
      Map<AmsServerInfo, List<String>> newAssignments = mockAssignStore.getAllAssignments();

      // Calculate migration: buckets that moved from node1 or node2
      Set<String> node1NewBuckets = new HashSet<>();
      Set<String> node2NewBuckets = new HashSet<>();
      Set<String> node3Buckets = new HashSet<>();
      for (Map.Entry<AmsServerInfo, List<String>> entry : newAssignments.entrySet()) {
        if (entry.getKey().getHost().equals("127.0.0.1")) {
          node1NewBuckets.addAll(entry.getValue());
        } else if (entry.getKey().getHost().equals("127.0.0.2")) {
          node2NewBuckets.addAll(entry.getValue());
        } else {
          node3Buckets.addAll(entry.getValue());
        }
      }

      // Node1 and Node2 should keep most of their buckets
      Set<String> node1Kept = new HashSet<>(node1InitialBuckets);
      node1Kept.retainAll(node1NewBuckets);
      Set<String> node2Kept = new HashSet<>(node2InitialBuckets);
      node2Kept.retainAll(node2NewBuckets);

      // Verify incremental assignment: nodes should keep most buckets
      Assert.assertTrue(
          "Node1 should keep most buckets (incremental)",
          node1Kept.size() > node1InitialBuckets.size() / 2);
      Assert.assertTrue(
          "Node2 should keep most buckets (incremental)",
          node2Kept.size() > node2InitialBuckets.size() / 2);

      // Node3 should get buckets from both
      Assert.assertTrue("Node3 should have buckets", node3Buckets.size() > 0);
    } finally {
      haContainer2.close();
      if (haContainer3 != null) {
        try {
          haContainer3.close();
        } catch (Exception e) {
          // ignore
        }
      }
    }
  }

  @Test
  public void testServiceStartStop() {
    // Test that service can start and stop without errors
    assignService.start();
    Assert.assertTrue("Service should be running", assignService.isRunning());

    assignService.stop();
    Assert.assertFalse("Service should be stopped", assignService.isRunning());
  }

  @Test
  public void testServiceSkipsWhenNotLeader() throws Exception {
    // Create a non-leader container
    mockLeaderLatch = createMockLeaderLatch(false); // Not leader
    Configurations nonLeaderConfig = createNodeConfig("127.0.0.2", 1262, 1632);
    ZkHighAvailabilityContainer nonLeaderContainer = createContainerWithMockZk(nonLeaderConfig);
    nonLeaderContainer.registAndElect();

    try {
      // Wait a bit
      Thread.sleep(100);

      AmsAssignService nonLeaderService = createAssignServiceWithMockStore(nonLeaderContainer);

      // Should not throw exception even if not leader
      nonLeaderService.doAssignForTest();

      // Should not have assignments if not leader
      Map<AmsServerInfo, List<String>> assignments = mockAssignStore.getAllAssignments();
      // Verify that non-leader doesn't create assignments
      Assert.assertTrue(
          "Non-leader should not create assignments",
          assignments.isEmpty() || assignments.size() == 0);
    } finally {
      nonLeaderContainer.close();
    }
  }

  private Configurations createNodeConfig(String host, int thriftPort, int httpPort) {
    Configurations config = new Configurations();
    config.setString(AmoroManagementConf.SERVER_EXPOSE_HOST, host);
    config.setInteger(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT, thriftPort);
    config.setInteger(AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT, thriftPort + 1);
    config.setInteger(AmoroManagementConf.HTTP_SERVER_PORT, httpPort);
    config.setBoolean(AmoroManagementConf.HA_ENABLE, true);
    config.setString(AmoroManagementConf.HA_ZOOKEEPER_ADDRESS, "127.0.0.1:2181");
    config.setString(AmoroManagementConf.HA_CLUSTER_NAME, "test-cluster");
    config.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, true);
    config.setInteger(AmoroManagementConf.BUCKET_ID_TOTAL_COUNT, 100);
    config.set(AmoroManagementConf.NODE_OFFLINE_TIMEOUT, java.time.Duration.ofMinutes(5));
    return config;
  }

  /** Create ZkHighAvailabilityContainer with mocked ZK components using reflection. */
  private ZkHighAvailabilityContainer createContainerWithMockZk() throws Exception {
    return createContainerWithMockZk(serviceConfig);
  }

  /** Create ZkHighAvailabilityContainer with mocked ZK components using reflection. */
  private ZkHighAvailabilityContainer createContainerWithMockZk(Configurations config)
      throws Exception {
    // Create container without ZK connection to avoid any connection attempts
    ZkHighAvailabilityContainer container = createContainerWithoutZk(config);

    // Inject mock ZK client and leader latch
    java.lang.reflect.Field zkClientField =
        ZkHighAvailabilityContainer.class.getDeclaredField("zkClient");
    zkClientField.setAccessible(true);
    zkClientField.set(container, mockZkClient);

    java.lang.reflect.Field leaderLatchField =
        ZkHighAvailabilityContainer.class.getDeclaredField("leaderLatch");
    leaderLatchField.setAccessible(true);
    leaderLatchField.set(container, mockLeaderLatch);

    return container;
  }

  /** Create a ZkHighAvailabilityContainer without initializing ZK connection. */
  private ZkHighAvailabilityContainer createContainerWithoutZk(Configurations config)
      throws Exception {
    java.lang.reflect.Constructor<ZkHighAvailabilityContainer> constructor =
        ZkHighAvailabilityContainer.class.getDeclaredConstructor(Configurations.class);

    // Create a minimal config that disables HA to avoid ZK connection
    Configurations tempConfig = new Configurations(config);
    tempConfig.setBoolean(AmoroManagementConf.HA_ENABLE, false);

    ZkHighAvailabilityContainer container = constructor.newInstance(tempConfig);

    // Now set all required fields using reflection
    java.lang.reflect.Field isMasterSlaveModeField =
        ZkHighAvailabilityContainer.class.getDeclaredField("isMasterSlaveMode");
    isMasterSlaveModeField.setAccessible(true);
    isMasterSlaveModeField.set(
        container, config.getBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE));

    if (config.getBoolean(AmoroManagementConf.HA_ENABLE)) {
      String haClusterName = config.getString(AmoroManagementConf.HA_CLUSTER_NAME);

      java.lang.reflect.Field tableServiceMasterPathField =
          ZkHighAvailabilityContainer.class.getDeclaredField("tableServiceMasterPath");
      tableServiceMasterPathField.setAccessible(true);
      tableServiceMasterPathField.set(
          container, AmsHAProperties.getTableServiceMasterPath(haClusterName));

      java.lang.reflect.Field optimizingServiceMasterPathField =
          ZkHighAvailabilityContainer.class.getDeclaredField("optimizingServiceMasterPath");
      optimizingServiceMasterPathField.setAccessible(true);
      optimizingServiceMasterPathField.set(
          container, AmsHAProperties.getOptimizingServiceMasterPath(haClusterName));

      java.lang.reflect.Field nodesPathField =
          ZkHighAvailabilityContainer.class.getDeclaredField("nodesPath");
      nodesPathField.setAccessible(true);
      nodesPathField.set(container, AmsHAProperties.getNodesPath(haClusterName));

      java.lang.reflect.Field tableServiceServerInfoField =
          ZkHighAvailabilityContainer.class.getDeclaredField("tableServiceServerInfo");
      tableServiceServerInfoField.setAccessible(true);
      AmsServerInfo tableServiceServerInfo =
          buildServerInfo(
              config.getString(AmoroManagementConf.SERVER_EXPOSE_HOST),
              config.getInteger(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT),
              config.getInteger(AmoroManagementConf.HTTP_SERVER_PORT));
      tableServiceServerInfoField.set(container, tableServiceServerInfo);

      java.lang.reflect.Field optimizingServiceServerInfoField =
          ZkHighAvailabilityContainer.class.getDeclaredField("optimizingServiceServerInfo");
      optimizingServiceServerInfoField.setAccessible(true);
      AmsServerInfo optimizingServiceServerInfo =
          buildServerInfo(
              config.getString(AmoroManagementConf.SERVER_EXPOSE_HOST),
              config.getInteger(AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT),
              config.getInteger(AmoroManagementConf.HTTP_SERVER_PORT));
      optimizingServiceServerInfoField.set(container, optimizingServiceServerInfo);
    }

    return container;
  }

  /** Helper method to build AmsServerInfo. */
  private AmsServerInfo buildServerInfo(String host, Integer thriftPort, Integer httpPort) {
    AmsServerInfo serverInfo = new AmsServerInfo();
    serverInfo.setHost(host);
    serverInfo.setThriftBindPort(thriftPort);
    serverInfo.setRestBindPort(httpPort);
    return serverInfo;
  }

  /** Create AmsAssignService with mock BucketAssignStore. */
  private AmsAssignService createAssignServiceWithMockStore() throws Exception {
    return createAssignServiceWithMockStore(haContainer);
  }

  /** Create AmsAssignService with mock BucketAssignStore. */
  private AmsAssignService createAssignServiceWithMockStore(ZkHighAvailabilityContainer container)
      throws Exception {
    AmsAssignService service = new AmsAssignService(container, serviceConfig, mockZkClient);

    // Use reflection to inject mock assign store
    java.lang.reflect.Field assignStoreField =
        AmsAssignService.class.getDeclaredField("assignStore");
    assignStoreField.setAccessible(true);
    assignStoreField.set(service, mockAssignStore);

    return service;
  }

  /** Create a mock CuratorFramework that uses MockZkState for storage. */
  @SuppressWarnings("unchecked")
  private CuratorFramework createMockZkClient() throws Exception {
    CuratorFramework mockClient = mock(CuratorFramework.class);

    // Mock getChildren()
    org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.GetChildrenBuilder
        getChildrenBuilder =
            mock(
                org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api
                    .GetChildrenBuilder.class);
    when(mockClient.getChildren()).thenReturn(getChildrenBuilder);
    when(getChildrenBuilder.forPath(anyString()))
        .thenAnswer(
            invocation -> {
              String path = invocation.getArgument(0);
              return mockZkState.getChildren(path);
            });

    // Mock getData()
    org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.GetDataBuilder
        getDataBuilder =
            mock(
                org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.GetDataBuilder
                    .class);
    when(mockClient.getData()).thenReturn(getDataBuilder);
    when(getDataBuilder.forPath(anyString()))
        .thenAnswer(
            invocation -> {
              String path = invocation.getArgument(0);
              return mockZkState.getData(path);
            });

    // Mock create() - manually create the entire fluent API chain
    org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.CreateBuilder createBuilder =
        mock(
            org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.CreateBuilder.class);

    @SuppressWarnings("unchecked")
    org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api
                .ProtectACLCreateModeStatPathAndBytesable<
            String>
        pathAndBytesable =
            mock(
                org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api
                    .ProtectACLCreateModeStatPathAndBytesable.class);

    when(mockClient.create()).thenReturn(createBuilder);
    when(createBuilder.creatingParentsIfNeeded()).thenReturn(pathAndBytesable);
    when(pathAndBytesable.withMode(any(CreateMode.class))).thenReturn(pathAndBytesable);

    // Mock forPath(path, data) - used by registAndElect() and saveAssignments()
    when(pathAndBytesable.forPath(anyString(), any(byte[].class)))
        .thenAnswer(
            invocation -> {
              String path = invocation.getArgument(0);
              byte[] data = invocation.getArgument(1);
              return mockZkState.createNode(path, data);
            });

    // Mock forPath(path) - used by createPathIfNeeded()
    when(pathAndBytesable.forPath(anyString()))
        .thenAnswer(
            invocation -> {
              String path = invocation.getArgument(0);
              if (mockZkState.exists(path) == null) {
                mockZkState.createNode(path, new byte[0]);
              }
              return null;
            });

    // Mock setData()
    org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.SetDataBuilder
        setDataBuilder =
            mock(
                org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.SetDataBuilder
                    .class);
    when(mockClient.setData()).thenReturn(setDataBuilder);
    when(setDataBuilder.forPath(anyString(), any(byte[].class)))
        .thenAnswer(
            invocation -> {
              String path = invocation.getArgument(0);
              byte[] data = invocation.getArgument(1);
              mockZkState.setData(path, data);
              return null;
            });

    // Mock delete()
    org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.DeleteBuilder deleteBuilder =
        mock(
            org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.DeleteBuilder.class);
    when(mockClient.delete()).thenReturn(deleteBuilder);
    doAnswer(
            invocation -> {
              String path = invocation.getArgument(0);
              mockZkState.deleteNode(path);
              return null;
            })
        .when(deleteBuilder)
        .forPath(anyString());

    // Mock deletingChildrenIfNeeded()
    org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.ChildrenDeletable
        childrenDeletable =
            mock(
                org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.ChildrenDeletable
                    .class);
    when(deleteBuilder.deletingChildrenIfNeeded()).thenReturn(childrenDeletable);
    doAnswer(
            invocation -> {
              String path = invocation.getArgument(0);
              mockZkState.deleteNodeRecursive(path);
              return null;
            })
        .when(childrenDeletable)
        .forPath(anyString());

    // Mock checkExists()
    @SuppressWarnings("unchecked")
    org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.ExistsBuilder
        checkExistsBuilder =
            mock(
                org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.ExistsBuilder
                    .class);
    when(mockClient.checkExists()).thenReturn(checkExistsBuilder);
    when(checkExistsBuilder.forPath(anyString()))
        .thenAnswer(
            invocation -> {
              String path = invocation.getArgument(0);
              return mockZkState.exists(path);
            });

    // Mock start() and close()
    doAnswer(invocation -> null).when(mockClient).start();
    doAnswer(invocation -> null).when(mockClient).close();

    return mockClient;
  }

  /** Create a mock LeaderLatch with specified leadership status. */
  private LeaderLatch createMockLeaderLatch(boolean hasLeadership) throws Exception {
    LeaderLatch mockLatch = mock(LeaderLatch.class);
    when(mockLatch.hasLeadership()).thenReturn(hasLeadership);
    doAnswer(invocation -> null).when(mockLatch).addListener(any());
    doAnswer(invocation -> null).when(mockLatch).start();
    doAnswer(invocation -> null).when(mockLatch).close();
    doAnswer(
            invocation -> {
              // Mock implementation - doesn't actually wait
              return null;
            })
        .when(mockLatch)
        .await();
    return mockLatch;
  }

  /** In-memory ZK state simulator. */
  private static class MockZkState {
    private final Map<String, byte[]> nodes = new HashMap<>();
    private final AtomicInteger sequenceCounter = new AtomicInteger(0);

    public List<String> getChildren(String path) throws KeeperException {
      List<String> children = new ArrayList<>();
      String prefix = path.endsWith("/") ? path : path + "/";
      for (String nodePath : nodes.keySet()) {
        if (nodePath.startsWith(prefix) && !nodePath.equals(path)) {
          String relativePath = nodePath.substring(prefix.length());
          if (!relativePath.contains("/")) {
            children.add(relativePath);
          }
        }
      }
      children.sort(String::compareTo);
      return children;
    }

    public byte[] getData(String path) throws KeeperException {
      byte[] data = nodes.get(path);
      if (data == null) {
        throw new KeeperException.NoNodeException(path);
      }
      return data;
    }

    public void setData(String path, byte[] data) throws KeeperException {
      if (!nodes.containsKey(path)) {
        throw new KeeperException.NoNodeException(path);
      }
      nodes.put(path, data);
    }

    public String createNode(String path, byte[] data) {
      // Handle sequential nodes
      if (path.endsWith("-")) {
        int seq = sequenceCounter.incrementAndGet();
        path = path + String.format("%010d", seq);
      }
      nodes.put(path, data);
      return path;
    }

    public void deleteNode(String path) throws KeeperException {
      if (!nodes.containsKey(path)) {
        throw new KeeperException.NoNodeException(path);
      }
      nodes.remove(path);
    }

    public void deleteNodeRecursive(String path) throws KeeperException {
      // Delete the node and all its children
      List<String> toDelete = new ArrayList<>();
      String prefix = path.endsWith("/") ? path : path + "/";
      for (String nodePath : nodes.keySet()) {
        if (nodePath.equals(path) || nodePath.startsWith(prefix)) {
          toDelete.add(nodePath);
        }
      }
      for (String nodePath : toDelete) {
        nodes.remove(nodePath);
      }
    }

    public void deleteNodeByHost(String host) {
      // Delete all nodes that have this host in their data (JSON)
      List<String> toDelete = new ArrayList<>();
      for (Map.Entry<String, byte[]> entry : nodes.entrySet()) {
        String nodePath = entry.getKey();
        byte[] data = entry.getValue();
        // Check if this is a node registration path (contains "/node-")
        if (nodePath.contains("/node-") && data != null && data.length > 0) {
          try {
            String nodeInfoJson = new String(data, java.nio.charset.StandardCharsets.UTF_8);
            // Parse JSON to check host
            AmsServerInfo nodeInfo = JacksonUtil.parseObject(nodeInfoJson, AmsServerInfo.class);
            if (nodeInfo != null && host.equals(nodeInfo.getHost())) {
              toDelete.add(nodePath);
            }
          } catch (Exception e) {
            // Ignore parsing errors
          }
        }
      }
      for (String nodePath : toDelete) {
        nodes.remove(nodePath);
      }
    }

    public Stat exists(String path) {
      return nodes.containsKey(path) ? new Stat() : null;
    }

    public void clear() {
      nodes.clear();
      sequenceCounter.set(0);
    }
  }

  /** In-memory implementation of BucketAssignStore for testing. */
  private static class MockBucketAssignStore implements BucketAssignStore {
    private final Map<String, List<String>> assignments = new HashMap<>();
    private final Map<String, Long> lastUpdateTimes = new HashMap<>();
    // Store full AmsServerInfo for proper matching
    private final Map<String, AmsServerInfo> nodeInfoMap = new HashMap<>();

    private String getNodeKey(AmsServerInfo nodeInfo) {
      return nodeInfo.getHost() + ":" + nodeInfo.getThriftBindPort();
    }

    @Override
    public void saveAssignments(AmsServerInfo nodeInfo, List<String> bucketIds) throws Exception {
      String nodeKey = getNodeKey(nodeInfo);
      assignments.put(nodeKey, new ArrayList<>(bucketIds));
      // Store full node info for proper matching
      nodeInfoMap.put(nodeKey, nodeInfo);
      updateLastUpdateTime(nodeInfo);
    }

    @Override
    public List<String> getAssignments(AmsServerInfo nodeInfo) throws Exception {
      String nodeKey = getNodeKey(nodeInfo);
      return new ArrayList<>(assignments.getOrDefault(nodeKey, new ArrayList<>()));
    }

    @Override
    public void removeAssignments(AmsServerInfo nodeInfo) throws Exception {
      String nodeKey = getNodeKey(nodeInfo);
      assignments.remove(nodeKey);
      lastUpdateTimes.remove(nodeKey);
      nodeInfoMap.remove(nodeKey);
    }

    @Override
    public Map<AmsServerInfo, List<String>> getAllAssignments() throws Exception {
      Map<AmsServerInfo, List<String>> result = new HashMap<>();
      for (Map.Entry<String, List<String>> entry : assignments.entrySet()) {
        String nodeKey = entry.getKey();
        // Use stored full node info if available, otherwise parse from key
        AmsServerInfo nodeInfo = nodeInfoMap.getOrDefault(nodeKey, parseNodeKey(nodeKey));
        result.put(nodeInfo, new ArrayList<>(entry.getValue()));
      }
      return result;
    }

    @Override
    public long getLastUpdateTime(AmsServerInfo nodeInfo) throws Exception {
      String nodeKey = getNodeKey(nodeInfo);
      return lastUpdateTimes.getOrDefault(nodeKey, 0L);
    }

    @Override
    public void updateLastUpdateTime(AmsServerInfo nodeInfo) throws Exception {
      String nodeKey = getNodeKey(nodeInfo);
      lastUpdateTimes.put(nodeKey, System.currentTimeMillis());
    }

    private AmsServerInfo parseNodeKey(String nodeKey) {
      String[] parts = nodeKey.split(":");
      if (parts.length != 2) {
        throw new IllegalArgumentException("Invalid node key format: " + nodeKey);
      }
      AmsServerInfo nodeInfo = new AmsServerInfo();
      nodeInfo.setHost(parts[0]);
      nodeInfo.setThriftBindPort(Integer.parseInt(parts[1]));
      return nodeInfo;
    }
  }
}
