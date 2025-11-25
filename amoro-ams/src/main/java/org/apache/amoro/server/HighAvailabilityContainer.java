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

import org.apache.amoro.client.AmsServerInfo;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.properties.AmsHAProperties;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.CuratorFramework;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.amoro.shade.zookeeper3.org.apache.zookeeper.CreateMode;
import org.apache.amoro.shade.zookeeper3.org.apache.zookeeper.KeeperException;
import org.apache.amoro.utils.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class HighAvailabilityContainer implements LeaderLatchListener {

  public static final Logger LOG = LoggerFactory.getLogger(HighAvailabilityContainer.class);

  private final LeaderLatch leaderLatch;
  private final CuratorFramework zkClient;
  private final String tableServiceMasterPath;
  private final String optimizingServiceMasterPath;
  private final String nodesPath;
  private final AmsServerInfo tableServiceServerInfo;
  private final AmsServerInfo optimizingServiceServerInfo;
  private final boolean isMasterSlaveMode;
  private volatile CountDownLatch followerLatch;
  private String registeredNodePath;

  public HighAvailabilityContainer(Configurations serviceConfig) throws Exception {
    this.isMasterSlaveMode = serviceConfig.getBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE);
    if (serviceConfig.getBoolean(AmoroManagementConf.HA_ENABLE)) {
      String zkServerAddress = serviceConfig.getString(AmoroManagementConf.HA_ZOOKEEPER_ADDRESS);
      int zkSessionTimeout =
          (int) serviceConfig.get(AmoroManagementConf.HA_ZOOKEEPER_SESSION_TIMEOUT).toMillis();
      int zkConnectionTimeout =
          (int) serviceConfig.get(AmoroManagementConf.HA_ZOOKEEPER_CONNECTION_TIMEOUT).toMillis();
      String haClusterName = serviceConfig.getString(AmoroManagementConf.HA_CLUSTER_NAME);
      tableServiceMasterPath = AmsHAProperties.getTableServiceMasterPath(haClusterName);
      optimizingServiceMasterPath = AmsHAProperties.getOptimizingServiceMasterPath(haClusterName);
      nodesPath = AmsHAProperties.getNodesPath(haClusterName);
      ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3, 5000);
      this.zkClient =
          CuratorFrameworkFactory.builder()
              .connectString(zkServerAddress)
              .sessionTimeoutMs(zkSessionTimeout)
              .connectionTimeoutMs(zkConnectionTimeout)
              .retryPolicy(retryPolicy)
              .build();
      zkClient.start();
      createPathIfNeeded(tableServiceMasterPath);
      createPathIfNeeded(optimizingServiceMasterPath);
      createPathIfNeeded(nodesPath);
      String leaderPath = AmsHAProperties.getLeaderPath(haClusterName);
      createPathIfNeeded(leaderPath);
      leaderLatch = new LeaderLatch(zkClient, leaderPath);
      leaderLatch.addListener(this);
      leaderLatch.start();
      this.tableServiceServerInfo =
          buildServerInfo(
              serviceConfig.getString(AmoroManagementConf.SERVER_EXPOSE_HOST),
              serviceConfig.getInteger(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT),
              serviceConfig.getInteger(AmoroManagementConf.HTTP_SERVER_PORT));
      this.optimizingServiceServerInfo =
          buildServerInfo(
              serviceConfig.getString(AmoroManagementConf.SERVER_EXPOSE_HOST),
              serviceConfig.getInteger(AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT),
              serviceConfig.getInteger(AmoroManagementConf.HTTP_SERVER_PORT));
    } else {
      leaderLatch = null;
      zkClient = null;
      tableServiceMasterPath = null;
      optimizingServiceMasterPath = null;
      nodesPath = null;
      tableServiceServerInfo = null;
      optimizingServiceServerInfo = null;
      registeredNodePath = null;
      // block follower latch forever when ha is disabled
      followerLatch = new CountDownLatch(1);
    }
  }

  public void waitLeaderShip() throws Exception {
    LOG.info("Waiting to become the leader of AMS");
    if (leaderLatch != null) {
      leaderLatch.await();
      if (leaderLatch.hasLeadership()) {
        CuratorOp tableServiceMasterPathOp =
            zkClient
                .transactionOp()
                .setData()
                .forPath(
                    tableServiceMasterPath,
                    JacksonUtil.toJSONString(tableServiceServerInfo)
                        .getBytes(StandardCharsets.UTF_8));
        CuratorOp optimizingServiceMasterPathOp =
            zkClient
                .transactionOp()
                .setData()
                .forPath(
                    optimizingServiceMasterPath,
                    JacksonUtil.toJSONString(optimizingServiceServerInfo)
                        .getBytes(StandardCharsets.UTF_8));
        zkClient
            .transaction()
            .forOperations(tableServiceMasterPathOp, optimizingServiceMasterPathOp);
      }
    }
    LOG.info("Became the leader of AMS");
  }

  public void registAndElect() throws Exception {
    if (!isMasterSlaveMode) {
      LOG.debug("Master-slave mode is not enabled, skip node registration");
      return;
    }
    if (zkClient == null || nodesPath == null) {
      LOG.warn("HA is not enabled, skip node registration");
      return;
    }
    // Register node to ZK using ephemeral node
    // The node will be automatically deleted when the session expires
    String nodeInfo = JacksonUtil.toJSONString(optimizingServiceServerInfo);
    registeredNodePath =
        zkClient
            .create()
            .creatingParentsIfNeeded()
            .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
            .forPath(nodesPath + "/node-", nodeInfo.getBytes(StandardCharsets.UTF_8));
    LOG.info("Registered AMS node to ZK: {}", registeredNodePath);
  }

  public void waitFollowerShip() throws Exception {
    LOG.info("Waiting to become the follower of AMS");
    if (followerLatch != null) {
      followerLatch.await();
    }
    LOG.info("Became the follower of AMS");
  }

  public void close() {
    if (leaderLatch != null) {
      try {
        // Unregister node from ZK
        if (registeredNodePath != null) {
          try {
            zkClient.delete().forPath(registeredNodePath);
            LOG.info("Unregistered AMS node from ZK: {}", registeredNodePath);
          } catch (KeeperException.NoNodeException e) {
            // Node already deleted, ignore
            LOG.debug("Node {} already deleted", registeredNodePath);
          } catch (Exception e) {
            LOG.warn("Failed to unregister node from ZK: {}", registeredNodePath, e);
          }
        }
        this.leaderLatch.close();
        this.zkClient.close();
      } catch (IOException e) {
        LOG.error("Close high availability services failed", e);
      }
    }
  }

  @Override
  public void isLeader() {
    LOG.info(
        "Table service server {} and optimizing service server {} got leadership",
        tableServiceServerInfo.toString(),
        optimizingServiceServerInfo.toString());
    followerLatch = new CountDownLatch(1);
  }

  @Override
  public void notLeader() {
    LOG.info(
        "Table service server {} and optimizing service server {} lost leadership",
        tableServiceServerInfo.toString(),
        optimizingServiceServerInfo.toString());
    followerLatch.countDown();
  }

  private AmsServerInfo buildServerInfo(String host, int thriftBindPort, int restBindPort) {
    AmsServerInfo amsServerInfo = new AmsServerInfo();
    amsServerInfo.setHost(host);
    amsServerInfo.setRestBindPort(restBindPort);
    amsServerInfo.setThriftBindPort(thriftBindPort);
    return amsServerInfo;
  }

  /**
   * Get list of alive nodes. Only the leader node can call this method.
   *
   * @return List of alive node information
   */
  public List<AmsServerInfo> getAliveNodes() throws Exception {
    List<AmsServerInfo> aliveNodes = new ArrayList<>();
    if (!isMasterSlaveMode) {
      LOG.debug("Master-slave mode is not enabled, return empty node list");
      return aliveNodes;
    }
    if (zkClient == null || nodesPath == null) {
      LOG.warn("HA is not enabled, return empty node list");
      return aliveNodes;
    }
    if (!leaderLatch.hasLeadership()) {
      LOG.warn("Only leader node can get alive nodes list");
      return aliveNodes;
    }
    try {
      List<String> nodePaths = zkClient.getChildren().forPath(nodesPath);
      for (String nodePath : nodePaths) {
        try {
          String fullPath = nodesPath + "/" + nodePath;
          byte[] data = zkClient.getData().forPath(fullPath);
          if (data != null && data.length > 0) {
            String nodeInfoJson = new String(data, StandardCharsets.UTF_8);
            AmsServerInfo nodeInfo = JacksonUtil.parseObject(nodeInfoJson, AmsServerInfo.class);
            aliveNodes.add(nodeInfo);
          }
        } catch (Exception e) {
          LOG.warn("Failed to get node info for path: {}", nodePath, e);
        }
      }
    } catch (KeeperException.NoNodeException e) {
      LOG.debug("Nodes path {} does not exist", nodesPath);
    }
    return aliveNodes;
  }

  /**
   * Check if current node is the leader.
   *
   * @return true if current node is the leader, false otherwise
   */
  public boolean hasLeadership() {
    if (leaderLatch == null) {
      return false;
    }
    return leaderLatch.hasLeadership();
  }

  /**
   * Get the current node's table service server info.
   *
   * @return The current node's server info, null if HA is not enabled
   */
  public AmsServerInfo getOptimizingServiceServerInfo() {
    return optimizingServiceServerInfo;
  }

  /**
   * Get the ZooKeeper client. This is used for creating BucketAssignStore.
   *
   * @return The ZooKeeper client, null if HA is not enabled
   */
  public CuratorFramework getZkClient() {
    return zkClient;
  }

  /**
   * Get the leader node's table service server info from ZooKeeper.
   *
   * @return The leader node's server info, null if HA is not enabled or leader info is not
   *     available
   */
  public AmsServerInfo getLeaderNodeInfo() {
    if (zkClient == null || tableServiceMasterPath == null) {
      return null;
    }
    try {
      byte[] data = zkClient.getData().forPath(tableServiceMasterPath);
      if (data != null && data.length > 0) {
        String nodeInfoJson = new String(data, StandardCharsets.UTF_8);
        return JacksonUtil.parseObject(nodeInfoJson, AmsServerInfo.class);
      }
    } catch (KeeperException.NoNodeException e) {
      LOG.debug("Leader node info not found in ZooKeeper path: {}", tableServiceMasterPath);
    } catch (Exception e) {
      LOG.warn("Failed to get leader node info from ZooKeeper", e);
    }
    return null;
  }

  /**
   * Check if master-slave mode is enabled.
   *
   * @return true if master-slave mode is enabled, false otherwise
   */
  public boolean isMasterSlaveMode() {
    return isMasterSlaveMode;
  }

  private void createPathIfNeeded(String path) throws Exception {
    try {
      zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
    } catch (KeeperException.NodeExistsException e) {
      // ignore
    }
  }
}
