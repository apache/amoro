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

import static org.apache.amoro.server.AmoroManagementConf.HA_CLUSTER_NAME;
import static org.apache.amoro.server.AmoroManagementConf.HA_ENABLE;
import static org.apache.amoro.server.AmoroManagementConf.HA_ZOOKEEPER_ADDRESS;
import static org.apache.amoro.server.AmoroManagementConf.SERVER_EXPOSE_HOST;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.client.AmsServerInfo;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.properties.AmsHAProperties;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.CuratorFramework;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.amoro.shade.zookeeper3.org.apache.zookeeper.CreateMode;
import org.apache.amoro.utils.JacksonUtil;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TestHighAvailabilityContainer {

  private TestingServer zkTestServer;
  private CuratorFramework zkClient;
  private String clusterName;
  private String zkConnectionString;

  @BeforeEach
  public void setup() throws Exception {
    // Start embedded ZooKeeper test server
    int port = (int) (Math.random() * 4000) + 14000;
    zkTestServer = new TestingServer(port, true);
    zkTestServer.start();
    // Wait for server to start
    zkTestServer.restart();
    zkConnectionString = "127.0.0.1:" + port;
    clusterName = "test-cluster";

    // Create ZooKeeper client
    ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
    zkClient = CuratorFrameworkFactory.newClient(zkConnectionString, retryPolicy);
    zkClient.start();
    // Wait for connection
    if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
      throw new RuntimeException("Failed to connect to ZooKeeper server");
    }

    // Clean up existing nodes
    String basePath = "/" + clusterName + "/arctic/ams";
    try {
      if (zkClient.checkExists().forPath(basePath) != null) {
        zkClient.delete().deletingChildrenIfNeeded().forPath(basePath);
      }
    } catch (Exception e) {
      // Ignore deletion exceptions
    }
  }

  @AfterEach
  public void teardown() {
    if (zkClient != null) {
      try {
        zkClient.close();
      } catch (Exception e) {
        // Ignore close exceptions
      }
    }
    if (zkTestServer != null) {
      try {
        zkTestServer.stop();
      } catch (Exception e) {
        // Ignore close exceptions
      }
    }
  }

  private Configurations createHAConfig() {
    Configurations config = new Configurations();
    config.set(HA_ENABLE, true);
    config.set(HA_ZOOKEEPER_ADDRESS, zkConnectionString);
    config.set(HA_CLUSTER_NAME, clusterName);
    config.set(SERVER_EXPOSE_HOST, "localhost");
    // Use existing configuration items
    config.set(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT, 12345);
    config.set(AmoroManagementConf.HTTP_SERVER_PORT, 19090);
    config.set(AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT, 12346);
    return config;
  }

  private void cleanupPath(String path) {
    try {
      if (zkClient.checkExists().forPath(path) != null) {
        // If path exists, clear its data
        zkClient.setData().forPath(path, new byte[0]);
      }
    } catch (Exception e) {
      // Ignore deletion exceptions
    }
  }

  /**
   * create previous leader info in ZooKeeper
   *
   * @param host
   * @param thriftPort
   * @param restPort
   * @throws Exception
   */
  private void setupPreviousLeaderInfo(String host, int thriftPort, int restPort) throws Exception {
    // Set up "previous leader" information that differs from current node
    String tableServiceMasterPath = AmsHAProperties.getTableServiceMasterPath(clusterName);
    String optimizingServiceMasterPath =
        AmsHAProperties.getOptimizingServiceMasterPath(clusterName);

    AmsServerInfo serverInfo = new AmsServerInfo();
    serverInfo.setHost(host);
    serverInfo.setThriftBindPort(thriftPort);
    serverInfo.setRestBindPort(restPort);
    String serverInfoJson = JacksonUtil.toJSONString(serverInfo);

    try {
      zkClient
          .create()
          .creatingParentsIfNeeded()
          .withMode(CreateMode.PERSISTENT)
          .forPath(tableServiceMasterPath, serverInfoJson.getBytes(StandardCharsets.UTF_8));
      zkClient
          .create()
          .creatingParentsIfNeeded()
          .withMode(CreateMode.PERSISTENT)
          .forPath(optimizingServiceMasterPath, serverInfoJson.getBytes(StandardCharsets.UTF_8));
    } catch (
        org.apache.amoro.shade.zookeeper3.org.apache.zookeeper.KeeperException.NodeExistsException
            e) {
      zkClient
          .setData()
          .forPath(tableServiceMasterPath, serverInfoJson.getBytes(StandardCharsets.UTF_8));
      zkClient
          .setData()
          .forPath(optimizingServiceMasterPath, serverInfoJson.getBytes(StandardCharsets.UTF_8));
    }
  }

  private void executeAndWaitForLeadership(
      HighAvailabilityContainer haContainer,
      AtomicBoolean hasLeadership,
      AtomicReference<Exception> exceptionRef)
      throws InterruptedException {
    // Execute waitLeaderShip in another thread and wait for result
    CountDownLatch leadershipLatch = new CountDownLatch(1);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(
        () -> {
          try {
            haContainer.waitLeaderShip();
            hasLeadership.set(true);
            leadershipLatch.countDown();
          } catch (Exception e) {
            exceptionRef.set(e);
          }
        });

    boolean success = leadershipLatch.await(30, TimeUnit.SECONDS);
    // Check for exceptions
    Exception thrownException = exceptionRef.get();
    if (thrownException != null) {
      throw new AssertionError(
          "Exception occurred during waitLeaderShip: " + thrownException.getMessage(),
          thrownException);
    }

    assertTrue(success, "Should acquire leadership within timeout");
    assertTrue(hasLeadership.get(), "Should have acquired leadership");
    executor.shutdown();
  }

  @Test
  public void testWaitLeadershipWithNoPreviousLeader() throws Exception {
    Configurations config = createHAConfig();

    // Ensure nodes are cleaned before test starts to avoid using previous test data
    String tableServiceMasterPath = AmsHAProperties.getTableServiceMasterPath(clusterName);
    cleanupPath(tableServiceMasterPath);

    HighAvailabilityContainer haContainer = new HighAvailabilityContainer(config);

    // Simulate waiting for leadership in another thread
    AtomicBoolean hasLeadership = new AtomicBoolean(false);
    AtomicReference<Exception> exceptionRef = new AtomicReference<>();
    executeAndWaitForLeadership(haContainer, hasLeadership, exceptionRef);

    haContainer.close();
  }

  @Test
  public void testWaitLeadershipWithSamePreviousLeaderInfo() throws Exception {
    Configurations config = createHAConfig();
    HighAvailabilityContainer haContainer = new HighAvailabilityContainer(config);

    // Set up "previous leader" info that matches current node (simulate restart scenario)
    setupPreviousLeaderInfo("localhost", 12345, 19090);

    // Simulate waiting for leadership in another thread
    AtomicBoolean hasLeadership = new AtomicBoolean(false);
    AtomicReference<Exception> exceptionRef = new AtomicReference<>();
    executeAndWaitForLeadership(haContainer, hasLeadership, exceptionRef);

    haContainer.close();
  }

  @Test
  public void testWaitLeadershipTimeoutWaitingForPreviousLeader() throws Exception {
    Configurations config = createHAConfig();
    HighAvailabilityContainer haContainer = new HighAvailabilityContainer(config);

    // Set up different "previous leader" info without creating disposeCompletePath (simulate
    // unresponsive previous leader)
    setupPreviousLeaderInfo("other-host", 12345, 19090);

    // Simulate waiting for leadership in another thread
    CountDownLatch leadershipLatch = new CountDownLatch(1);
    AtomicBoolean hasLeadership = new AtomicBoolean(false);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(
        () -> {
          try {
            haContainer.waitLeaderShip();
            hasLeadership.set(true);
            leadershipLatch.countDown();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });

    // Wait for leadership (should take over after timeout)
    boolean success =
        leadershipLatch.await(40, TimeUnit.SECONDS); // Wait beyond default 30s timeout
    assertTrue(
        success,
        "Should acquire leadership after timeout even if previous leader does not respond");
    assertTrue(hasLeadership.get(), "Should have acquired leadership");

    haContainer.close();
    executor.shutdown();
  }

  @Test
  public void testSignalDisposeComplete() throws Exception {
    Configurations config = createHAConfig();
    HighAvailabilityContainer haContainer = new HighAvailabilityContainer(config);

    String disposeCompletePath = AmsHAProperties.getMasterReleaseConfirmPath(clusterName);

    // Create node
    try {
      zkClient
          .create()
          .creatingParentsIfNeeded()
          .withMode(CreateMode.PERSISTENT)
          .forPath(disposeCompletePath);
    } catch (
        org.apache.amoro.shade.zookeeper3.org.apache.zookeeper.KeeperException.NodeExistsException
            e) {
      // Ignore if node already exists
    }

    // Ensure node exists
    assertNotNull(zkClient.checkExists().forPath(disposeCompletePath));

    // Call signal complete method
    haContainer.signalDisposeComplete();

    // Verify node has been deleted
    assertNull(zkClient.checkExists().forPath(disposeCompletePath));

    haContainer.close();
  }

  @Test
  public void testSignalDisposeCompleteWithNoPath() throws Exception {
    Configurations config = createHAConfig();
    HighAvailabilityContainer haContainer = new HighAvailabilityContainer(config);

    String disposeCompletePath = AmsHAProperties.getMasterReleaseConfirmPath(clusterName);

    // Ensure node does not exist
    assertNull(zkClient.checkExists().forPath(disposeCompletePath));

    // Call signal complete method - should not throw exception
    assertDoesNotThrow(() -> haContainer.signalDisposeComplete());

    haContainer.close();
  }
}
