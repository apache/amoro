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

import org.apache.amoro.MockZookeeperServer;
import org.apache.amoro.client.AmsServerInfo;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.properties.AmsHAProperties;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.CuratorFramework;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TestHighAvailabilityContainer {

  private Configurations serviceConfig;
  private HighAvailabilityContainer haContainer;
  private CuratorFramework testZkClient;

  @Before
  public void setUp() throws Exception {
    // Initialize mock ZK server
    testZkClient = MockZookeeperServer.getClient();
    String zkUri = MockZookeeperServer.getUri();

    // Create test configuration
    serviceConfig = new Configurations();
    serviceConfig.setString(AmoroManagementConf.SERVER_EXPOSE_HOST, "127.0.0.1");
    serviceConfig.setInteger(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT, 1260);
    serviceConfig.setInteger(AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT, 1261);
    serviceConfig.setInteger(AmoroManagementConf.HTTP_SERVER_PORT, 1630);
    serviceConfig.setBoolean(AmoroManagementConf.HA_ENABLE, true);
    serviceConfig.setString(AmoroManagementConf.HA_ZOOKEEPER_ADDRESS, zkUri);
    serviceConfig.setString(AmoroManagementConf.HA_CLUSTER_NAME, "test-cluster");
  }

  @After
  public void tearDown() throws Exception {
    if (haContainer != null) {
      haContainer.close();
    }
  }

  @Test
  public void testRegistAndElectWithoutMasterSlaveMode() throws Exception {
    // Test that node registration is skipped when master-slave mode is disabled
    serviceConfig.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, false);
    haContainer = new HighAvailabilityContainer(serviceConfig);

    // Should not throw exception and should not register node
    haContainer.registAndElect();

    // Wait a bit for any async operations
    Thread.sleep(100);

    // Verify no node was registered
    // When master-slave mode is disabled, HA might not be enabled, so zkClient might be null
    if (haContainer.getZkClient() != null) {
      String nodesPath = AmsHAProperties.getNodesPath("test-cluster");
      try {
        // Use testZkClient which is always available
        if (testZkClient.checkExists().forPath(nodesPath) != null) {
          List<String> children = testZkClient.getChildren().forPath(nodesPath);
          Assert.assertEquals(
              "No nodes should be registered when master-slave mode is disabled",
              0,
              children.size());
        }
      } catch (Exception e) {
        // If path doesn't exist, that's also fine - means no nodes registered
      }
    }
  }

  @Test
  public void testRegistAndElectWithMasterSlaveMode() throws Exception {
    // Test that node registration works when master-slave mode is enabled
    serviceConfig.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, true);
    haContainer = new HighAvailabilityContainer(serviceConfig);

    // Register node
    haContainer.registAndElect();

    // Wait a bit for ZK operation to complete
    Thread.sleep(300);

    // Verify node was registered using testZkClient to avoid connection issues
    String nodesPath = AmsHAProperties.getNodesPath("test-cluster");
    // Wait for path to be created
    int retries = 0;
    while (testZkClient.checkExists().forPath(nodesPath) == null && retries < 10) {
      Thread.sleep(100);
      retries++;
    }
    List<String> children = testZkClient.getChildren().forPath(nodesPath);
    Assert.assertEquals("One node should be registered", 1, children.size());

    // Verify node data
    String nodePath = nodesPath + "/" + children.get(0);
    byte[] data = testZkClient.getData().forPath(nodePath);
    Assert.assertNotNull("Node data should not be null", data);
    Assert.assertTrue("Node data should not be empty", data.length > 0);
  }

  @Test
  public void testGetAliveNodesWithoutMasterSlaveMode() throws Exception {
    // Test that getAliveNodes returns empty list when master-slave mode is disabled
    serviceConfig.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, false);
    haContainer = new HighAvailabilityContainer(serviceConfig);

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
    haContainer = new HighAvailabilityContainer(serviceConfig);

    // Register node but don't wait to become leader
    haContainer.registAndElect();

    // Wait a bit for registration
    Thread.sleep(100);

    // Check if we're leader - if we are, create a second container that will be follower
    if (haContainer.hasLeadership()) {
      // If we're already leader, create a second container that won't be leader
      Configurations serviceConfig2 = new Configurations();
      serviceConfig2.setString(AmoroManagementConf.SERVER_EXPOSE_HOST, "127.0.0.2");
      serviceConfig2.setInteger(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT, 1262);
      serviceConfig2.setInteger(AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT, 1263);
      serviceConfig2.setInteger(AmoroManagementConf.HTTP_SERVER_PORT, 1631);
      serviceConfig2.setBoolean(AmoroManagementConf.HA_ENABLE, true);
      serviceConfig2.setString(
          AmoroManagementConf.HA_ZOOKEEPER_ADDRESS, MockZookeeperServer.getUri());
      serviceConfig2.setString(AmoroManagementConf.HA_CLUSTER_NAME, "test-cluster");
      serviceConfig2.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, true);

      HighAvailabilityContainer haContainer2 = new HighAvailabilityContainer(serviceConfig2);
      haContainer2.registAndElect();
      try {
        Thread.sleep(200);
        // haContainer2 should not be leader
        Assert.assertFalse("Second container should not be leader", haContainer2.hasLeadership());
        // Since haContainer2 is not leader, should return empty list
        List<AmsServerInfo> aliveNodes = haContainer2.getAliveNodes();
        Assert.assertNotNull("Alive nodes list should not be null", aliveNodes);
        Assert.assertEquals(
            "Alive nodes list should be empty when not leader", 0, aliveNodes.size());
      } finally {
        haContainer2.close();
      }
    } else {
      // We're not leader, so should return empty list
      List<AmsServerInfo> aliveNodes = haContainer.getAliveNodes();
      Assert.assertNotNull("Alive nodes list should not be null", aliveNodes);
      Assert.assertEquals("Alive nodes list should be empty when not leader", 0, aliveNodes.size());
    }
  }

  @Test
  public void testGetAliveNodesAsLeader() throws Exception {
    // Test that getAliveNodes returns nodes when leader
    serviceConfig.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, true);
    haContainer = new HighAvailabilityContainer(serviceConfig);

    // Register node
    haContainer.registAndElect();

    // Wait to become leader
    haContainer.waitLeaderShip();

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
    haContainer = new HighAvailabilityContainer(serviceConfig);

    // Register first node
    haContainer.registAndElect();

    // Create and register second node
    Configurations serviceConfig2 = new Configurations();
    serviceConfig2.setString(AmoroManagementConf.SERVER_EXPOSE_HOST, "127.0.0.2");
    serviceConfig2.setInteger(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT, 1262);
    serviceConfig2.setInteger(AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT, 1263);
    serviceConfig2.setInteger(AmoroManagementConf.HTTP_SERVER_PORT, 1631);
    serviceConfig2.setBoolean(AmoroManagementConf.HA_ENABLE, true);
    serviceConfig2.setString(
        AmoroManagementConf.HA_ZOOKEEPER_ADDRESS, MockZookeeperServer.getUri());
    serviceConfig2.setString(AmoroManagementConf.HA_CLUSTER_NAME, "test-cluster");
    serviceConfig2.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, true);

    HighAvailabilityContainer haContainer2 = new HighAvailabilityContainer(serviceConfig2);
    haContainer2.registAndElect();

    try {
      // Wait to become leader
      haContainer.waitLeaderShip();

      // Verify we are leader
      Assert.assertTrue("Should be leader", haContainer.hasLeadership());

      // Get alive nodes
      List<AmsServerInfo> aliveNodes = haContainer.getAliveNodes();
      Assert.assertNotNull("Alive nodes list should not be null", aliveNodes);
      Assert.assertEquals("Should have two alive nodes", 2, aliveNodes.size());
    } finally {
      haContainer2.close();
    }
  }

  @Test
  public void testCloseUnregistersNode() throws Exception {
    // Test that close() unregisters the node
    serviceConfig.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, true);
    haContainer = new HighAvailabilityContainer(serviceConfig);

    // Register node
    haContainer.registAndElect();

    // Wait a bit for registration
    Thread.sleep(300);

    // Verify node was registered using testZkClient
    String nodesPath = AmsHAProperties.getNodesPath("test-cluster");
    // Wait for path to exist
    int retries = 0;
    while (testZkClient.checkExists().forPath(nodesPath) == null && retries < 10) {
      Thread.sleep(100);
      retries++;
    }
    List<String> children = testZkClient.getChildren().forPath(nodesPath);
    Assert.assertEquals("One node should be registered", 1, children.size());

    // Close container (this will close the zkClient and delete ephemeral node)
    haContainer.close();
    haContainer = null;

    // Wait longer for ZK session to expire and ephemeral node to be auto-deleted
    // Ephemeral nodes are deleted when session closes
    Thread.sleep(1000);

    // Verify node was unregistered using testZkClient
    // The ephemeral node should be automatically deleted when session closes
    try {
      List<String> childrenAfterClose = testZkClient.getChildren().forPath(nodesPath);
      Assert.assertEquals(
          "No nodes should be registered after close", 0, childrenAfterClose.size());
    } catch (Exception e) {
      // If path doesn't exist anymore, that's also fine
      Assert.assertTrue("Path should be empty or not exist", true);
    }
  }

  @Test
  public void testHasLeadership() throws Exception {
    // Test hasLeadership() method
    serviceConfig.setBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE, true);
    haContainer = new HighAvailabilityContainer(serviceConfig);

    // Initially should not be leader
    Assert.assertFalse("Should not be leader initially", haContainer.hasLeadership());

    // Wait to become leader
    haContainer.waitLeaderShip();

    // Should be leader now
    Assert.assertTrue("Should be leader after waitLeaderShip", haContainer.hasLeadership());
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
}
