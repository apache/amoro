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
import org.apache.amoro.shade.thrift.org.apache.commons.lang3.StringUtils;
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
import java.util.concurrent.CountDownLatch;

public class HighAvailabilityContainer implements LeaderLatchListener {

  public static final Logger LOG = LoggerFactory.getLogger(HighAvailabilityContainer.class);

  private final LeaderLatch leaderLatch;
  private final CuratorFramework zkClient;
  private final String tableServiceMasterPath;
  private final String optimizingServiceMasterPath;
  // path to signal that this node has completed ams dispose
  private final String disposeCompletePath;
  private final AmsServerInfo tableServiceServerInfo;
  private final AmsServerInfo optimizingServiceServerInfo;
  private volatile CountDownLatch followerLatch;

  public HighAvailabilityContainer(Configurations serviceConfig) throws Exception {
    if (serviceConfig.getBoolean(AmoroManagementConf.HA_ENABLE)) {
      String zkServerAddress = serviceConfig.getString(AmoroManagementConf.HA_ZOOKEEPER_ADDRESS);
      int zkSessionTimeout =
          (int) serviceConfig.get(AmoroManagementConf.HA_ZOOKEEPER_SESSION_TIMEOUT).toMillis();
      int zkConnectionTimeout =
          (int) serviceConfig.get(AmoroManagementConf.HA_ZOOKEEPER_CONNECTION_TIMEOUT).toMillis();
      String haClusterName = serviceConfig.getString(AmoroManagementConf.HA_CLUSTER_NAME);
      tableServiceMasterPath = AmsHAProperties.getTableServiceMasterPath(haClusterName);
      optimizingServiceMasterPath = AmsHAProperties.getOptimizingServiceMasterPath(haClusterName);
      disposeCompletePath = AmsHAProperties.getMasterReleaseConfirmPath(haClusterName);
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
      disposeCompletePath = null;
      tableServiceServerInfo = null;
      optimizingServiceServerInfo = null;
      // block follower latch forever when ha is disabled
      followerLatch = new CountDownLatch(1);
    }
  }

  public void waitLeaderShip() throws Exception {
    LOG.info("Waiting to become the leader of AMS");
    if (leaderLatch != null) {
      leaderLatch.await();
      if (leaderLatch.hasLeadership()) {
        waitPreviousLeaderDisposeComplete();

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

  public void waitFollowerShip() throws Exception {
    LOG.info("Waiting to become the follower of AMS");
    if (followerLatch != null) {
      followerLatch.await();
    }
    LOG.info("Became the follower of AMS");
  }

  public void waitPreviousLeaderDisposeComplete() throws Exception {
    // 1、Create the path if it does not exist, to ensure it exists for future primary and standby
    // node switchover.
    if (zkClient.checkExists().forPath(disposeCompletePath) == null) {
      createPathIfNeeded(disposeCompletePath);
    }

    // 2、Determine if there is a previous leader, or if it is different from current node.
    boolean hasPreviousOtherLeader = false;
    try {
      byte[] masterData = zkClient.getData().forPath(tableServiceMasterPath);
      if (masterData != null && masterData.length > 0) {
        String masterInfoInZkNode = new String(masterData, StandardCharsets.UTF_8);
        if (!StringUtils.isEmpty(masterInfoInZkNode)) {
          try {
            // If data cannot be parsed correctly, it indicates that the AMS service is starting for
            // the first time.
            AmsServerInfo previousLeaderInfo =
                JacksonUtil.parseObject(masterInfoInZkNode, AmsServerInfo.class);
            if (previousLeaderInfo != null) {
              // If parsing succeeds, check if it's different from current node
              String currentInfoStr = JacksonUtil.toJSONString(tableServiceServerInfo);
              LOG.debug(
                  "Current node info JSON: {}, ZK node info JSON: {}",
                  currentInfoStr,
                  masterInfoInZkNode);
              if (!masterInfoInZkNode.equals(currentInfoStr)) {
                hasPreviousOtherLeader = true;
              } else {
                LOG.debug(
                    "Previous leader is the same as current node (self-restart)."
                        + " No need to wait for dispose signal.");
              }
            }
          } catch (Exception e) {
            LOG.warn(
                "Failed to parse master info from ZooKeeper: {}, treating as no previous leader",
                masterInfoInZkNode,
                e);
            // If parsing fails, treat as no previous leader
          }
        }
      }
    } catch (KeeperException.NoNodeException e) {
      // No previous leader node found, indicating that this is the first startup of ams.
      LOG.debug("No previous leader node found, indicating that this is the first startup of ams.");
    }

    if (!hasPreviousOtherLeader) {
      LOG.debug("No previous other master detected, start service immediately.");
      return;
    }

    // If disposeCompletePath exists, the following scenarios may occur:
    // 1) A primary-standby node switchover occurs, and the former primary
    //      node has not completed the AMS dispose operation.
    // 2) The previous primary node is unreachable due to network issues.
    // 3) No primary-standby node switchover occurred, but ZK retains
    // information about the previous primary node.
    long startTime = System.currentTimeMillis();
    int maxWaitTime = 30000; // 30s
    while (System.currentTimeMillis() - startTime <= maxWaitTime) {
      // At this point, the disposeCompletePath does not exist,
      // indicating that the previous master node has completed
      // the AMS service shutdown operation and deleted the path.
      if (zkClient.checkExists().forPath(disposeCompletePath) == null) {
        LOG.info("Previous leader has completed dispose. Proceeding.");
        return;
      }
    }

    LOG.debug(
        "Timeout ({}ms) waiting for previous other leader to signal dispose complete. Proceeding anyway. "
            + "This might indicate the previous leader is unresponsive.",
        maxWaitTime);
  }

  public void close() {
    if (leaderLatch != null) {
      try {
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

  /**
   * In HA mode, when the AMS service is stopped, delete the existing `disposeCompletePath` file
   * from ZK to indicate that the AMS service has been terminated.
   */
  public void signalDisposeComplete() {
    // when HA is disabled, do nothing
    if (zkClient == null) {
      return;
    }

    try {
      if (zkClient.checkExists().forPath(disposeCompletePath) != null) {
        zkClient.delete().forPath(disposeCompletePath);
        return;
      }
      LOG.debug("ams dispose complete signal written.");
    } catch (Exception e) {
      LOG.warn("Failed to write dispose complete signal", e);
    }
  }

  private AmsServerInfo buildServerInfo(String host, int thriftBindPort, int restBindPort) {
    AmsServerInfo amsServerInfo = new AmsServerInfo();
    amsServerInfo.setHost(host);
    amsServerInfo.setRestBindPort(restBindPort);
    amsServerInfo.setThriftBindPort(thriftBindPort);
    return amsServerInfo;
  }

  private void createPathIfNeeded(String path) throws Exception {
    try {
      zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
    } catch (KeeperException.NodeExistsException e) {
      // ignore
    }
  }
}
