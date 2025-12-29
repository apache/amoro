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
import org.apache.amoro.server.ha.HighAvailabilityContainer;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** Test for HighAvailabilityContainer using mocked ZK to avoid connection issues. */
public class TestHighAvailabilityContainer {

  private Configurations serviceConfig;
  private HighAvailabilityContainer haContainer;
  private MockZkState mockZkState;
  private CuratorFramework mockZkClient;
  private LeaderLatch mockLeaderLatch;

  @Before
  public void setUp() throws Exception {
    mockZkState = new MockZkState();
    mockZkClient = createMockZkClient();
    mockLeaderLatch = createMockLeaderLatch();

    // Create test configuration
    serviceConfig = new Configurations();
    serviceConfig.setString(AmoroManagementConf.SERVER_EXPOSE_HOST, "127.0.0.1");
    serviceConfig.setInteger(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT, 1260);
    serviceConfig.setInteger(AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT, 1261);
    serviceConfig.setInteger(AmoroManagementConf.HTTP_SERVER_PORT, 1630);
    serviceConfig.setBoolean(AmoroManagementConf.HA_ENABLE, true);
    serviceConfig.setString(AmoroManagementConf.HA_ZOOKEEPER_ADDRESS, "127.0.0.1:2181");
    serviceConfig.setString(AmoroManagementConf.HA_CLUSTER_NAME, "test-cluster");
  }

  @After
  public void tearDown() throws Exception {
    if (haContainer != null) {
      haContainer.close();
    }
    mockZkState.clear();
  }

  @Test
  public void testRegistAndElectWithoutMasterSlaveMode() throws Exception {
    // Test that node registration is skipped when master-slave mode is disabled
    serviceConfig.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, false);
    haContainer = createContainerWithMockZk();

    // Should not throw exception and should not register node
    haContainer.registAndElect();

    // Verify no node was registered
    String nodesPath = AmsHAProperties.getNodesPath("test-cluster");
    List<String> children = mockZkState.getChildren(nodesPath);
    Assert.assertEquals(
        "No nodes should be registered when master-slave mode is disabled", 0, children.size());
  }

  @Test
  public void testRegistAndElectWithMasterSlaveMode() throws Exception {
    // Test that node registration works when master-slave mode is enabled
    serviceConfig.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, true);
    haContainer = createContainerWithMockZk();

    // Register node
    haContainer.registAndElect();

    // Verify node was registered
    String nodesPath = AmsHAProperties.getNodesPath("test-cluster");
    List<String> children = mockZkState.getChildren(nodesPath);
    Assert.assertEquals("One node should be registered", 1, children.size());

    // Verify node data
    String nodePath = nodesPath + "/" + children.get(0);
    byte[] data = mockZkState.getData(nodePath);
    Assert.assertNotNull("Node data should not be null", data);
    Assert.assertTrue("Node data should not be empty", data.length > 0);

    // Verify node info
    String nodeInfoJson = new String(data, StandardCharsets.UTF_8);
    AmsServerInfo nodeInfo = JacksonUtil.parseObject(nodeInfoJson, AmsServerInfo.class);
    Assert.assertEquals("Host should match", "127.0.0.1", nodeInfo.getHost());
    Assert.assertEquals(
        "Thrift port should match", Integer.valueOf(1261), nodeInfo.getThriftBindPort());
  }

  @Test
  public void testGetAliveNodesWithoutMasterSlaveMode() throws Exception {
    // Test that getAliveNodes returns empty list when master-slave mode is disabled
    serviceConfig.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, false);
    haContainer = createContainerWithMockZk();

    List<AmsServerInfo> aliveNodes = haContainer.getAliveNodes();
    Assert.assertNotNull("Alive nodes list should not be null", aliveNodes);
    Assert.assertEquals(
        "Alive nodes list should be empty when master-slave mode is disabled",
        0,
        aliveNodes.size());
  }

  @Test
  public void testGetAliveNodesWhenNotLeader() throws Exception {
    // Test that getAliveNodes returns empty list when not leader
    serviceConfig.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, true);
    mockLeaderLatch = createMockLeaderLatch(false); // Not leader
    haContainer = createContainerWithMockZk();

    // Register node
    haContainer.registAndElect();

    // Since we're not the leader, should return empty list
    List<AmsServerInfo> aliveNodes = haContainer.getAliveNodes();
    Assert.assertNotNull("Alive nodes list should not be null", aliveNodes);
    Assert.assertEquals("Alive nodes list should be empty when not leader", 0, aliveNodes.size());
  }

  @Test
  public void testGetAliveNodesAsLeader() throws Exception {
    // Test that getAliveNodes returns nodes when leader
    serviceConfig.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, true);
    mockLeaderLatch = createMockLeaderLatch(true); // Is leader
    haContainer = createContainerWithMockZk();

    // Register node
    haContainer.registAndElect();

    // Verify we are leader
    Assert.assertTrue("Should be leader", haContainer.hasLeadership());

    // Get alive nodes
    List<AmsServerInfo> aliveNodes = haContainer.getAliveNodes();
    Assert.assertNotNull("Alive nodes list should not be null", aliveNodes);
    Assert.assertEquals("Should have one alive node", 1, aliveNodes.size());

    // Verify node info
    AmsServerInfo nodeInfo = aliveNodes.get(0);
    Assert.assertEquals("Host should match", "127.0.0.1", nodeInfo.getHost());
    Assert.assertEquals(
        "Thrift port should match", Integer.valueOf(1261), nodeInfo.getThriftBindPort());
    Assert.assertEquals(
        "HTTP port should match", Integer.valueOf(1630), nodeInfo.getRestBindPort());
  }

  @Test
  public void testGetAliveNodesWithMultipleNodes() throws Exception {
    // Test that getAliveNodes returns all registered nodes
    serviceConfig.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, true);
    mockLeaderLatch = createMockLeaderLatch(true); // Is leader
    haContainer = createContainerWithMockZk();

    // Register first node
    haContainer.registAndElect();

    // Verify first node was registered
    String nodesPath = AmsHAProperties.getNodesPath("test-cluster");
    List<String> childrenAfterFirst = mockZkState.getChildren(nodesPath);
    Assert.assertEquals("First node should be registered", 1, childrenAfterFirst.size());

    // Register second node manually in mock state
    // Use createNode with sequential path to get the correct sequence number
    AmsServerInfo nodeInfo2 = new AmsServerInfo();
    nodeInfo2.setHost("127.0.0.2");
    nodeInfo2.setThriftBindPort(1262);
    nodeInfo2.setRestBindPort(1631);
    String nodeInfo2Json = JacksonUtil.toJSONString(nodeInfo2);
    // Use sequential path ending with "-" to let createNode generate the sequence number
    // This ensures the second node gets the correct sequence number (0000000001)
    mockZkState.createNode(nodesPath + "/node-", nodeInfo2Json.getBytes(StandardCharsets.UTF_8));

    // Get alive nodes
    List<AmsServerInfo> aliveNodes = haContainer.getAliveNodes();
    Assert.assertNotNull("Alive nodes list should not be null", aliveNodes);
    Assert.assertEquals("Should have two alive nodes", 2, aliveNodes.size());
  }

  @Test
  public void testCloseUnregistersNode() throws Exception {
    // Test that close() unregisters the node
    serviceConfig.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, true);
    haContainer = createContainerWithMockZk();

    // Register node
    haContainer.registAndElect();

    // Verify node was registered
    String nodesPath = AmsHAProperties.getNodesPath("test-cluster");
    List<String> children = mockZkState.getChildren(nodesPath);
    Assert.assertEquals("One node should be registered", 1, children.size());

    // Close container
    haContainer.close();
    haContainer = null;

    // Verify node was unregistered
    List<String> childrenAfterClose = mockZkState.getChildren(nodesPath);
    Assert.assertEquals("No nodes should be registered after close", 0, childrenAfterClose.size());
  }

  @Test
  public void testHasLeadership() throws Exception {
    // Test hasLeadership() method
    serviceConfig.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, true);
    mockLeaderLatch = createMockLeaderLatch(false); // Not leader initially
    haContainer = createContainerWithMockZk();

    // Initially should not be leader
    Assert.assertFalse("Should not be leader initially", haContainer.hasLeadership());

    // Change to leader
    mockLeaderLatch = createMockLeaderLatch(true);
    haContainer = createContainerWithMockZk();

    // Should be leader now
    Assert.assertTrue("Should be leader", haContainer.hasLeadership());
  }

  @Test
  public void testRegistAndElectWithoutHAEnabled() throws Exception {
    // Test that registAndElect skips when HA is not enabled
    serviceConfig.setBoolean(AmoroManagementConf.HA_ENABLE, false);
    serviceConfig.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, true);
    haContainer = new ZkHighAvailabilityContainer(serviceConfig);

    // Should not throw exception
    haContainer.registAndElect();
  }

  /** Create HighAvailabilityContainer with mocked ZK components using reflection. */
  private HighAvailabilityContainer createContainerWithMockZk() throws Exception {
    // Create container without ZK connection to avoid any connection attempts
    HighAvailabilityContainer container = createContainerWithoutZk();

    // Inject mock ZK client and leader latch
    java.lang.reflect.Field zkClientField =
        HighAvailabilityContainer.class.getDeclaredField("zkClient");
    zkClientField.setAccessible(true);
    zkClientField.set(container, mockZkClient);

    java.lang.reflect.Field leaderLatchField =
        HighAvailabilityContainer.class.getDeclaredField("leaderLatch");
    leaderLatchField.setAccessible(true);
    leaderLatchField.set(container, mockLeaderLatch);

    // Note: We don't need to create the paths themselves as nodes in ZK
    // ZK paths are logical containers, not actual nodes
    // The createPathIfNeeded() calls will be handled by the mock when needed

    return container;
  }

  /**
   * Create a HighAvailabilityContainer without initializing ZK connection. This is used when we
   * want to completely avoid ZK connection attempts.
   */
  private HighAvailabilityContainer createContainerWithoutZk() throws Exception {
    // Use reflection to create container without calling constructor
    java.lang.reflect.Constructor<HighAvailabilityContainer> constructor =
        HighAvailabilityContainer.class.getDeclaredConstructor(Configurations.class);

    // Create a minimal config that disables HA to avoid ZK connection
    Configurations tempConfig = new Configurations(serviceConfig);
    tempConfig.setBoolean(AmoroManagementConf.HA_ENABLE, false);

    HighAvailabilityContainer container = constructor.newInstance(tempConfig);

    // Now set all required fields using reflection
    java.lang.reflect.Field isMasterSlaveModeField =
        HighAvailabilityContainer.class.getDeclaredField("isMasterSlaveMode");
    isMasterSlaveModeField.setAccessible(true);
    isMasterSlaveModeField.set(
        container, serviceConfig.getBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE));

    if (serviceConfig.getBoolean(AmoroManagementConf.HA_ENABLE)) {
      String haClusterName = serviceConfig.getString(AmoroManagementConf.HA_CLUSTER_NAME);

      java.lang.reflect.Field tableServiceMasterPathField =
          HighAvailabilityContainer.class.getDeclaredField("tableServiceMasterPath");
      tableServiceMasterPathField.setAccessible(true);
      tableServiceMasterPathField.set(
          container, AmsHAProperties.getTableServiceMasterPath(haClusterName));

      java.lang.reflect.Field optimizingServiceMasterPathField =
          HighAvailabilityContainer.class.getDeclaredField("optimizingServiceMasterPath");
      optimizingServiceMasterPathField.setAccessible(true);
      optimizingServiceMasterPathField.set(
          container, AmsHAProperties.getOptimizingServiceMasterPath(haClusterName));

      java.lang.reflect.Field nodesPathField =
          HighAvailabilityContainer.class.getDeclaredField("nodesPath");
      nodesPathField.setAccessible(true);
      nodesPathField.set(container, AmsHAProperties.getNodesPath(haClusterName));

      java.lang.reflect.Field tableServiceServerInfoField =
          HighAvailabilityContainer.class.getDeclaredField("tableServiceServerInfo");
      tableServiceServerInfoField.setAccessible(true);
      AmsServerInfo tableServiceServerInfo =
          buildServerInfo(
              serviceConfig.getString(AmoroManagementConf.SERVER_EXPOSE_HOST),
              serviceConfig.getInteger(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT),
              serviceConfig.getInteger(AmoroManagementConf.HTTP_SERVER_PORT));
      tableServiceServerInfoField.set(container, tableServiceServerInfo);

      java.lang.reflect.Field optimizingServiceServerInfoField =
          HighAvailabilityContainer.class.getDeclaredField("optimizingServiceServerInfo");
      optimizingServiceServerInfoField.setAccessible(true);
      AmsServerInfo optimizingServiceServerInfo =
          buildServerInfo(
              serviceConfig.getString(AmoroManagementConf.SERVER_EXPOSE_HOST),
              serviceConfig.getInteger(AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT),
              serviceConfig.getInteger(AmoroManagementConf.HTTP_SERVER_PORT));
      optimizingServiceServerInfoField.set(container, optimizingServiceServerInfo);
    }

    return container;
  }

  /** Helper method to build AmsServerInfo (copied from HighAvailabilityContainer). */
  private AmsServerInfo buildServerInfo(String host, Integer thriftPort, Integer httpPort) {
    AmsServerInfo serverInfo = new AmsServerInfo();
    serverInfo.setHost(host);
    serverInfo.setThriftBindPort(thriftPort);
    serverInfo.setRestBindPort(httpPort);
    return serverInfo;
  }

  /** Create a mock CuratorFramework that uses MockZkState for storage. */
  @SuppressWarnings("unchecked")
  private CuratorFramework createMockZkClient() throws Exception {
    CuratorFramework mockClient = mock(CuratorFramework.class);

    // Mock getChildren() - create a chain of mocks
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

    // Mock create() - manually create the entire fluent API chain to ensure consistency
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

    // Mock the chain: creatingParentsIfNeeded() -> withMode() -> forPath()
    // Use the same mock object for the entire chain
    when(createBuilder.creatingParentsIfNeeded()).thenReturn(pathAndBytesable);
    when(pathAndBytesable.withMode(any(CreateMode.class))).thenReturn(pathAndBytesable);

    // Mock forPath(path, data) - used by registAndElect()
    when(pathAndBytesable.forPath(anyString(), any(byte[].class)))
        .thenAnswer(
            invocation -> {
              String path = invocation.getArgument(0);
              byte[] data = invocation.getArgument(1);
              return mockZkState.createNode(path, data);
            });

    // Mock forPath(path) - used by createPathIfNeeded()
    // Note: createPathIfNeeded() creates paths without data, but we still need to store them
    // so that getChildren() can work correctly
    when(pathAndBytesable.forPath(anyString()))
        .thenAnswer(
            invocation -> {
              String path = invocation.getArgument(0);
              // Create the path as an empty node (this simulates ZK path creation)
              // In real ZK, paths are logical containers, but we need to store them
              // to make getChildren() work correctly
              if (mockZkState.exists(path) == null) {
                mockZkState.createNode(path, new byte[0]);
              }
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

  /** Create a mock LeaderLatch. */
  private LeaderLatch createMockLeaderLatch() throws Exception {
    return createMockLeaderLatch(true);
  }

  /** Create a mock LeaderLatch with specified leadership status. */
  private LeaderLatch createMockLeaderLatch(boolean hasLeadership) throws Exception {
    LeaderLatch mockLatch = mock(LeaderLatch.class);
    when(mockLatch.hasLeadership()).thenReturn(hasLeadership);
    doAnswer(invocation -> null).when(mockLatch).addListener(any());
    doAnswer(invocation -> null).when(mockLatch).start();
    doAnswer(invocation -> null).when(mockLatch).close();
    // Mock await() - it throws IOException and InterruptedException
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
        // Only include direct children (not the path itself, and not nested paths)
        if (nodePath.startsWith(prefix) && !nodePath.equals(path)) {
          String relativePath = nodePath.substring(prefix.length());
          // Only add direct children (no additional slashes)
          // This means the path should be exactly: prefix + relativePath
          if (!relativePath.contains("/")) {
            children.add(relativePath);
          }
        }
      }
      // Sort to ensure consistent ordering
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

    public Stat exists(String path) {
      return nodes.containsKey(path) ? new Stat() : null;
    }

    public void clear() {
      nodes.clear();
      sequenceCounter.set(0);
    }
  }
}
