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

package org.apache.amoro.server.ha;

import org.apache.amoro.client.AmsServerInfo;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.properties.AmsHAProperties;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.CuratorFramework;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.amoro.shade.zookeeper3.org.apache.zookeeper.CreateMode;
import org.apache.amoro.shade.zookeeper3.org.apache.zookeeper.KeeperException;
import org.apache.amoro.utils.DynConstructors;
import org.apache.amoro.utils.JacksonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class ZkHighAvailabilityContainer implements HighAvailabilityContainer, LeaderLatchListener {

  public static final Logger LOG = LoggerFactory.getLogger(ZkHighAvailabilityContainer.class);

  private final LeaderLatch leaderLatch;
  private final CuratorFramework zkClient;
  private final String tableServiceMasterPath;
  private final String optimizingServiceMasterPath;
  private final AmsServerInfo tableServiceServerInfo;
  private final AmsServerInfo optimizingServiceServerInfo;
  private volatile CountDownLatch followerLatch;

  public ZkHighAvailabilityContainer(Configurations serviceConfig) throws Exception {
    if (serviceConfig.getBoolean(AmoroManagementConf.HA_ENABLE)) {
      String zkServerAddress = serviceConfig.getString(AmoroManagementConf.HA_ZOOKEEPER_ADDRESS);
      int zkSessionTimeout =
          (int) serviceConfig.get(AmoroManagementConf.HA_ZOOKEEPER_SESSION_TIMEOUT).toMillis();
      int zkConnectionTimeout =
          (int) serviceConfig.get(AmoroManagementConf.HA_ZOOKEEPER_CONNECTION_TIMEOUT).toMillis();
      String haClusterName = serviceConfig.getString(AmoroManagementConf.HA_CLUSTER_NAME);
      tableServiceMasterPath = AmsHAProperties.getTableServiceMasterPath(haClusterName);
      optimizingServiceMasterPath = AmsHAProperties.getOptimizingServiceMasterPath(haClusterName);
      ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3, 5000);
      setupZookeeperAuth(serviceConfig);
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
      tableServiceServerInfo = null;
      optimizingServiceServerInfo = null;
      // block follower latch forever when ha is disabled
      followerLatch = new CountDownLatch(1);
    }
  }

  @Override
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
    // TODO Here you can register for AMS and participate in the election.
  }

  @Override
  public void registAndElect() throws Exception {
    // TODO Here you can register for AMS and participate in the election.
  }

  public void waitFollowerShip() throws Exception {
    LOG.info("Waiting to become the follower of AMS");
    if (followerLatch != null) {
      followerLatch.await();
    }
    LOG.info("Became the follower of AMS");
  }

  @Override
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

  private static final Map<Pair<String, String>, Configuration> JAAS_CONFIGURATION_CACHE =
      Maps.newConcurrentMap();

  /** For a kerberized cluster, we dynamically set up the client's JAAS conf. */
  public static void setupZookeeperAuth(Configurations configurations) throws IOException {
    String zkAuthType = configurations.get(AmoroManagementConf.HA_ZOOKEEPER_AUTH_TYPE);
    if ("KERBEROS".equalsIgnoreCase(zkAuthType) && UserGroupInformation.isSecurityEnabled()) {
      String principal = configurations.get(AmoroManagementConf.HA_ZOOKEEPER_AUTH_PRINCIPAL);
      String keytab = configurations.get(AmoroManagementConf.HA_ZOOKEEPER_AUTH_KEYTAB);
      Preconditions.checkArgument(
          StringUtils.isNoneBlank(principal, keytab),
          "%s and %s must be provided for KERBEROS authentication",
          AmoroManagementConf.HA_ZOOKEEPER_AUTH_PRINCIPAL.key(),
          AmoroManagementConf.HA_ZOOKEEPER_AUTH_KEYTAB.key());
      if (!new File(keytab).exists()) {
        throw new IOException(
            String.format(
                "%s: %s does not exist",
                AmoroManagementConf.HA_ZOOKEEPER_AUTH_KEYTAB.key(), keytab));
      }
      System.setProperty("zookeeper.sasl.clientconfig", "AmoroZooKeeperClient");
      String zkClientPrincipal = SecurityUtil.getServerPrincipal(principal, "0.0.0.0");
      Configuration jaasConf =
          JAAS_CONFIGURATION_CACHE.computeIfAbsent(
              Pair.of(principal, keytab),
              pair -> {
                // HDFS-16591 makes breaking change on JaasConfiguration
                return DynConstructors.builder()
                    .impl( // Hadoop 3.3.5 and above
                        "org.apache.hadoop.security.authentication.util.JaasConfiguration",
                        String.class,
                        String.class,
                        String.class)
                    .impl( // Hadoop 3.3.4 and previous
                        // scalastyle:off
                        "org.apache.hadoop.security.token.delegation.ZKDelegationTokenSecretManager$JaasConfiguration",
                        // scalastyle:on
                        String.class,
                        String.class,
                        String.class)
                    .<Configuration>build()
                    .newInstance("AmoroZooKeeperClient", zkClientPrincipal, keytab);
              });
      Configuration.setConfiguration(jaasConf);
    }
  }
}
