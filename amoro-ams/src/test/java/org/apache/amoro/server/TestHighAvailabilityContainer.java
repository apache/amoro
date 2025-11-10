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
        "Thrift port should match", Integer.valueOf(1260), nodeInfo.getThriftBindPort());
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
        "Thrift port should match", Integer.valueOf(1260), nodeInfo.getThriftBindPort());
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

    // Register second node manually in mock state
    String nodesPath = AmsHAProperties.getNodesPath("test-cluster");
    AmsServerInfo nodeInfo2 = new AmsServerInfo();
    nodeInfo2.setHost("127.0.0.2");
    nodeInfo2.setThriftBindPort(1262);
    nodeInfo2.setRestBindPort(1631);
    String nodeInfo2Json = JacksonUtil.toJSONString(nodeInfo2);
    mockZkState.createNode(
        nodesPath + "/node-0000000001", nodeInfo2Json.getBytes(StandardCharsets.UTF_8));

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
    haContainer = new HighAvailabilityContainer(serviceConfig);

    // Should not throw exception
    haContainer.registAndElect();
  }

  /** Create HighAvailabilityContainer with mocked ZK components using reflection. */
  private HighAvailabilityContainer createContainerWithMockZk() throws Exception {
    HighAvailabilityContainer container = new HighAvailabilityContainer(serviceConfig);

    // Use reflection to inject mock ZK client and leader latch
    java.lang.reflect.Field zkClientField =
        HighAvailabilityContainer.class.getDeclaredField("zkClient");
    zkClientField.setAccessible(true);
    zkClientField.set(container, mockZkClient);

    java.lang.reflect.Field leaderLatchField =
        HighAvailabilityContainer.class.getDeclaredField("leaderLatch");
    leaderLatchField.setAccessible(true);
    leaderLatchField.set(container, mockLeaderLatch);

    return container;
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

    // Mock create() - use Answer to handle the entire fluent API chain
    @SuppressWarnings({"unchecked", "rawtypes"})
    org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.CreateBuilder createBuilder =
        mock(
            org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.CreateBuilder.class);
    @SuppressWarnings({"unchecked", "rawtypes"})
    org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.CreateBuilderMain
        createBuilderMain =
            mock(
                org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.CreateBuilderMain
                    .class);
    @SuppressWarnings({"unchecked", "rawtypes"})
    org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.PathAndBytesable
        createPathAndBytesable =
            mock(
                org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.PathAndBytesable
                    .class);

    when(mockClient.create()).thenReturn(createBuilder);
    // Use Answer to handle type mismatch in fluent API
    doAnswer(invocation -> createBuilderMain).when(createBuilder).creatingParentsIfNeeded();
    doAnswer(invocation -> createPathAndBytesable)
        .when(createBuilderMain)
        .withMode(any(CreateMode.class));
    when(createPathAndBytesable.forPath(anyString(), any(byte[].class)))
        .thenAnswer(
            invocation -> {
              String path = invocation.getArgument(0);
              byte[] data = invocation.getArgument(1);
              return mockZkState.createNode(path, data);
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
        if (nodePath.startsWith(prefix) && !nodePath.equals(path)) {
          String relativePath = nodePath.substring(prefix.length());
          if (!relativePath.contains("/")) {
            children.add(relativePath);
          }
        }
      }
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
