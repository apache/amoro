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
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.persistence.HaLeaseMeta;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.HaLeaseMapper;
import org.apache.amoro.utils.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * High availability (HA) container backed by a Database lease (RDBMS lock).
 *
 * <p>Core mechanics: - fixed-rate heartbeat to acquire/renew the lease - optimistic row version
 * plus expiration timestamp - leader election: first successful acquire becomes the leader; losing
 * the lease demotes the node - persistence via PersistentBase and HaLeaseMetaMapper - concurrency
 * guard using AtomicBoolean flags; once the lease is revoked, leadership is not re-gained
 *
 * <p>Reference to Hudi AnalogousZkClient (for alignment only, no System.exit): - await/signal:
 * waitLeaderShip() condition wait and signal on leadership gain - heartbeat: scheduled
 * acquire/renew - losing leadership: demote and countDown follower latch, no System.exit
 */
public class DataBaseHighAvailabilityContainer extends PersistentBase
    implements HighAvailabilityContainer {
  private static final Logger LOG =
      LoggerFactory.getLogger(DataBaseHighAvailabilityContainer.class);
  private static final String AMS_SERVICE = "AMS";
  private static final String TABLE_SERVICE = "TABLE_SERVICE";
  private static final String OPTIMIZING_SERVICE = "OPTIMIZING_SERVICE";

  private final Configurations serviceConfig;
  private final ScheduledExecutorService executor;
  private final AtomicBoolean isLeader = new AtomicBoolean(false);
  /** Prevent re-gaining leadership once the lease is lost. */
  private final AtomicBoolean leadershipRevoked = new AtomicBoolean(false);

  private volatile CountDownLatch followerLatch;

  private final Lock leaderLock = new ReentrantLock();
  private final Condition leaderCondition = leaderLock.newCondition();

  private final String clusterName;
  private final long heartbeatIntervalSeconds;
  private final long ttlSeconds;

  private final String nodeId;
  private final String nodeIp;
  private final AmsServerInfo tableServiceServerInfo;
  private final AmsServerInfo optimizingServiceServerInfo;

  /** Local lease version (optimistic lock). Null means not acquired yet. */
  private volatile Integer leaseVersion = null;

  public DataBaseHighAvailabilityContainer(Configurations serviceConfig) {
    this.serviceConfig = serviceConfig;
    this.clusterName = serviceConfig.getString(AmoroManagementConf.HA_CLUSTER_NAME);

    // Read heartbeat interval and lease TTL from common settings (Duration), expressed in seconds
    this.heartbeatIntervalSeconds =
        serviceConfig.get(AmoroManagementConf.HA_HEARTBEAT_INTERVAL).getSeconds();
    this.ttlSeconds = serviceConfig.get(AmoroManagementConf.HA_LEASE_TTL).getSeconds();

    this.nodeIp = serviceConfig.getString(AmoroManagementConf.SERVER_EXPOSE_HOST);
    int tableThriftPort =
        serviceConfig.getInteger(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT);
    int optimizingThriftPort =
        serviceConfig.getInteger(AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT);
    int restPort = serviceConfig.getInteger(AmoroManagementConf.HTTP_SERVER_PORT);

    // Build a unique node ID (IP + optimizing port + random suffix) to distinguish multi-instance
    // on the same host
    this.nodeId = nodeIp + ":" + optimizingThriftPort + ":" + UUID.randomUUID();

    this.tableServiceServerInfo = buildServerInfo(nodeIp, tableThriftPort, restPort);
    this.optimizingServiceServerInfo = buildServerInfo(nodeIp, optimizingThriftPort, restPort);

    this.executor = Executors.newSingleThreadScheduledExecutor();
    // Heartbeat similar to AnalogousZkClient: fixed-rate renewal with initial acquire attempt
    this.executor.scheduleAtFixedRate(
        new HeartbeatRunnable(), 5, Math.max(1, heartbeatIntervalSeconds), TimeUnit.SECONDS);
  }

  /** Blocks until leadership is acquired and server info is written for both services. */
  @Override
  public void waitLeaderShip() throws InterruptedException {
    LOG.info("Waiting to become the leader of AMS (Database lease)");
    leaderLock.lock();
    try {
      while (!isLeader.get()) {
        leaderCondition.await();
      }
    } finally {
      leaderLock.unlock();
    }
    LOG.info("Became the leader of AMS (Database lease)");
  }

  /** Blocks until the follower latch is counted down (leadership lost or demoted). */
  @Override
  public void waitFollowerShip() throws InterruptedException {
    LOG.info("Waiting to become the follower of AMS (Database lease)");
    if (followerLatch != null) {
      followerLatch.await();
    }
    LOG.info("Became the follower of AMS (Database lease)");
  }

  @Override
  public void registAndElect() throws Exception {
    boolean isMasterSlaveMode = serviceConfig.getBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE);
    if (!isMasterSlaveMode) {
      LOG.debug("Master-slave mode is not enabled, skip node registration");
      return;
    }
    // In master-slave mode, register node to database by writing OPTIMIZING_SERVICE info
    // This is similar to ZK mode registering ephemeral nodes
    long now = System.currentTimeMillis();
    String optimizingInfoJson = JacksonUtil.toJSONString(optimizingServiceServerInfo);
    try {
      doAsIgnoreError(
          HaLeaseMapper.class,
          mapper -> {
            int updated =
                mapper.updateServerInfo(
                    clusterName, OPTIMIZING_SERVICE, nodeId, nodeIp, optimizingInfoJson, now);
            if (updated == 0) {
              mapper.insertServerInfoIfAbsent(
                  clusterName, OPTIMIZING_SERVICE, nodeId, nodeIp, optimizingInfoJson, now);
            }
          });
      LOG.info(
          "Registered AMS node to database: nodeId={}, optimizingService={}",
          nodeId,
          optimizingServiceServerInfo);
    } catch (Exception e) {
      LOG.error("Failed to register node to database", e);
      throw e;
    }
  }

  @Override
  public boolean hasLeadership() {
    return isLeader.get();
  }

  @Override
  public AmsServerInfo getOptimizingServiceServerInfo() {
    return optimizingServiceServerInfo;
  }

  @Override
  public AmsServerInfo getLeaderNodeInfo() {
    return null;
  }

  /** Closes the heartbeat executor safely. */
  @Override
  public void close() {
    try {
      if (executor != null && !executor.isShutdown()) {
        executor.shutdown();
      }
    } catch (Exception e) {
      LOG.error("Close Database HighAvailabilityContainer failed", e);
    }
  }

  private class HeartbeatRunnable implements Runnable {
    @Override
    public void run() {
      try {
        long now = System.currentTimeMillis();
        long newExpireTs = now + TimeUnit.SECONDS.toMillis(ttlSeconds);

        if (leadershipRevoked.get()) {
          // Lease already revoked; skip any subsequent leadership path
          return;
        }

        if (!isLeader.get()) {
          // First attempt to acquire the lease (similar to candidate/await)
          boolean success = tryAcquireLease(newExpireTs, now);
          LOG.info(
              "Try to acquire AMS lease: success={}, cluster={}, nodeId={}, nodeIp={}",
              success,
              clusterName,
              nodeId,
              nodeIp);
          if (success) {
            // On leadership gained: signal waiters and write server info for both services
            onLeaderGained(now);
          }
        } else {
          // Leader renew (similar to renew lease)
          boolean renewed = tryRenewLease(newExpireTs, now);
          LOG.info(
              "Try to renew AMS lease: success={}, cluster={}, nodeId={}",
              renewed,
              clusterName,
              nodeId);
          if (!renewed && isLeader.get()) {
            // Leadership lost: demote; no System.exit
            onLeaderLost();
          }
        }
      } catch (Throwable t) {
        LOG.error("Database HA heartbeat failed", t);
      }
    }
  }

  private boolean tryAcquireLease(long newExpireTs, long now) {
    // Insert lease row if absent (idempotent) to ensure existence
    doAsIgnoreError(
        HaLeaseMapper.class,
        mapper ->
            mapper.insertIfAbsent(
                new HaLeaseMeta(
                    clusterName, AMS_SERVICE, nodeId, nodeIp, "", newExpireTs, 0, now)));

    // Acquire when the lease is expired or free
    int affected =
        updateAs(
                HaLeaseMapper.class,
                mapper ->
                    mapper.acquireLease(
                        clusterName,
                        AMS_SERVICE,
                        nodeId,
                        nodeIp,
                        JacksonUtil.toJSONString(tableServiceServerInfo),
                        newExpireTs,
                        now))
            .intValue();
    if (affected == 1) {
      // Read current version
      HaLeaseMeta lease =
          getAs(HaLeaseMapper.class, mapper -> mapper.selectLease(clusterName, AMS_SERVICE));
      leaseVersion = lease != null ? lease.getVersion() : 0;
      return true;
    }
    return false;
  }

  private boolean tryRenewLease(long newExpireTs, long now) {
    if (leaseVersion == null) {
      HaLeaseMeta lease =
          getAs(HaLeaseMapper.class, mapper -> mapper.selectLease(clusterName, AMS_SERVICE));
      leaseVersion = lease != null ? lease.getVersion() : 0;
    }
    int affected =
        updateAs(
                HaLeaseMapper.class,
                mapper ->
                    mapper.renewLease(
                        clusterName, AMS_SERVICE, nodeId, leaseVersion, newExpireTs, now))
            .intValue();
    if (affected == 1) {
      leaseVersion = leaseVersion + 1;
      return true;
    }
    return false;
  }

  private void onLeaderGained(long now) {
    isLeader.set(true);
    followerLatch = new CountDownLatch(1);

    // Equivalent to AnalogousZkClient.signalAll()
    leaderLock.lock();
    try {
      leaderCondition.signalAll();
    } finally {
      leaderLock.unlock();
    }

    // Persist server info for TABLE_SERVICE and OPTIMIZING_SERVICE on leadership
    String tableInfoJson = JacksonUtil.toJSONString(tableServiceServerInfo);
    String optimizingInfoJson = JacksonUtil.toJSONString(optimizingServiceServerInfo);

    doAsIgnoreError(
        HaLeaseMapper.class,
        mapper -> {
          int u1 =
              mapper.updateServerInfo(
                  clusterName, TABLE_SERVICE, nodeId, nodeIp, tableInfoJson, now);
          if (u1 == 0) {
            mapper.insertServerInfoIfAbsent(
                clusterName, TABLE_SERVICE, nodeId, nodeIp, tableInfoJson, now);
          }
        });
    doAsIgnoreError(
        HaLeaseMapper.class,
        mapper -> {
          int u2 =
              mapper.updateServerInfo(
                  clusterName, OPTIMIZING_SERVICE, nodeId, nodeIp, optimizingInfoJson, now);
          if (u2 == 0) {
            mapper.insertServerInfoIfAbsent(
                clusterName, OPTIMIZING_SERVICE, nodeId, nodeIp, optimizingInfoJson, now);
          }
        });

    LOG.info(
        "Current node become leader, tableService={}, optimizingService={}",
        tableServiceServerInfo,
        optimizingServiceServerInfo);
  }

  private void onLeaderLost() {
    LOG.info(
        "Current node as leader is disconnected or lost the lease, cluster={}, nodeId={}",
        clusterName,
        nodeId);
    leadershipRevoked.set(true);
    isLeader.set(false);
    if (followerLatch != null) {
      followerLatch.countDown();
    }
  }

  @Override
  public List<AmsServerInfo> getAliveNodes() throws Exception {
    List<AmsServerInfo> aliveNodes = new ArrayList<>();
    if (!isLeader.get()) {
      LOG.warn("Only leader node can get alive nodes list");
      return aliveNodes;
    }
    try {
      long currentTime = System.currentTimeMillis();
      List<HaLeaseMeta> leases =
          getAs(
              HaLeaseMapper.class,
              mapper -> mapper.selectLeasesByService(clusterName, OPTIMIZING_SERVICE));
      for (HaLeaseMeta lease : leases) {
        // Only include nodes with valid (non-expired) leases
        if (lease.getLeaseExpireTs() != null && lease.getLeaseExpireTs() > currentTime) {
          if (lease.getServerInfoJson() != null && !lease.getServerInfoJson().isEmpty()) {
            try {
              AmsServerInfo nodeInfo =
                  JacksonUtil.parseObject(lease.getServerInfoJson(), AmsServerInfo.class);
              aliveNodes.add(nodeInfo);
            } catch (Exception e) {
              LOG.warn("Failed to parse server info for node {}", lease.getNodeId(), e);
            }
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to get alive nodes from database", e);
      throw e;
    }
    return aliveNodes;
  }

  private AmsServerInfo buildServerInfo(String host, int thriftBindPort, int restBindPort) {
    AmsServerInfo amsServerInfo = new AmsServerInfo();
    amsServerInfo.setHost(host);
    amsServerInfo.setRestBindPort(restBindPort);
    amsServerInfo.setThriftBindPort(thriftBindPort);
    return amsServerInfo;
  }
}
