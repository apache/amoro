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
import org.apache.amoro.properties.AmsHAProperties;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.CuratorFramework;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.amoro.shade.zookeeper3.org.apache.zookeeper.CreateMode;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Integration test for {@link ZkBucketAssignStore} against a real ZooKeeper server (embedded via
 * curator-test). Covers the scenario called out in review: node registration followed by bucket
 * assignment on a fresh cluster. Registration uses a dedicated ephemeral path; assignments are
 * persistent children under the bucket-assignments path. ZooKeeper rejects children of ephemeral
 * nodes with NoChildrenForEphemeralsException, so this test would fail if registration were
 * co-located with assignment storage.
 */
public class TestZkBucketAssignStoreWithRealZk {

  private TestingServer zkServer;
  private CuratorFramework zkClient;
  private ZkBucketAssignStore assignStore;
  private AmsServerInfo node1;

  @Before
  public void setUp() throws Exception {
    zkServer = new TestingServer();
    zkServer.start();
    zkClient =
        CuratorFrameworkFactory.newClient(
            zkServer.getConnectString(), new ExponentialBackoffRetry(1000, 3));
    zkClient.start();
    assignStore = new ZkBucketAssignStore(zkClient, "test-cluster");

    node1 = new AmsServerInfo();
    node1.setHost("127.0.0.1");
    node1.setThriftBindPort(1260);
    node1.setRestBindPort(1630);
  }

  @After
  public void tearDown() throws Exception {
    if (assignStore != null) {
      try {
        assignStore.removeAssignments(node1);
      } catch (Exception e) {
        // ignore
      }
    }
    if (zkClient != null) {
      zkClient.close();
    }
    if (zkServer != null) {
      zkServer.close();
    }
  }

  /** Fresh cluster: register first, then save assignments. Must not throw. */
  @Test
  public void testRegisterThenAssignOnFreshCluster() throws Exception {
    assignStore.registerNode(node1);

    List<String> bucketIds = Arrays.asList("1", "2", "3");
    assignStore.saveAssignments(node1, bucketIds);

    Assert.assertEquals(bucketIds, assignStore.getAssignments(node1));
    Assert.assertTrue(assignStore.getLastUpdateTime(node1) > 0);

    List<AmsServerInfo> aliveNodes = assignStore.getAliveNodes();
    Assert.assertEquals(1, aliveNodes.size());
    Assert.assertEquals(node1.getHost(), aliveNodes.get(0).getHost());
    Assert.assertEquals(node1.getThriftBindPort(), aliveNodes.get(0).getThriftBindPort());

    // Registration must be ephemeral in the nodes path, not under bucket-assignments
    String nodesPath = AmsHAProperties.getNodesPath("test-cluster");
    Assert.assertNotNull(
        "Registration node should exist under /nodes",
        zkClient.checkExists().forPath(nodesPath + "/127.0.0.1:1260"));
    Assert.assertNotEquals(
        "Registration node should be ephemeral",
        0,
        zkClient.checkExists().forPath(nodesPath + "/127.0.0.1:1260").getEphemeralOwner());
  }

  /** Registration and assignments live in separate ZK paths. */
  @Test
  public void testRegistrationAndAssignmentsUseSeparatePaths() throws Exception {
    assignStore.registerNode(node1);
    assignStore.saveAssignments(node1, Arrays.asList("1", "2"));

    String nodesPath = AmsHAProperties.getNodesPath("test-cluster");
    String assignmentsPath = AmsHAProperties.getBucketAssignmentsPath("test-cluster");

    // /nodes/{nodeKey} exists (ephemeral registration)
    Assert.assertNotNull(zkClient.checkExists().forPath(nodesPath + "/127.0.0.1:1260"));
    // /bucket-assignments/{nodeKey}/assignments exists (persistent assignment)
    Assert.assertNotNull(
        zkClient.checkExists().forPath(assignmentsPath + "/127.0.0.1:1260/assignments"));
    Assert.assertNotNull(
        zkClient.checkExists().forPath(assignmentsPath + "/127.0.0.1:1260/last-update-time"));
    // No ephemeral node under bucket-assignments/{nodeKey}: data lives in persistent children
    List<String> assignmentChildren =
        zkClient.getChildren().forPath(assignmentsPath + "/127.0.0.1:1260");
    Assert.assertTrue(
        "Assignment subtree should only contain persistent children",
        assignmentChildren.contains("assignments")
            && assignmentChildren.contains("last-update-time"));
  }

  /** removeNode clears both the registration and the assignments. */
  @Test
  public void testRemoveNodeClearsRegistrationAndAssignments() throws Exception {
    assignStore.registerNode(node1);
    assignStore.saveAssignments(node1, Arrays.asList("1", "2"));

    assignStore.removeNode(node1);

    Assert.assertEquals(0, assignStore.getAliveNodes().size());
    Assert.assertTrue(assignStore.getAssignments(node1).isEmpty());

    String nodesPath = AmsHAProperties.getNodesPath("test-cluster");
    String assignmentsPath = AmsHAProperties.getBucketAssignmentsPath("test-cluster");
    Assert.assertNull(zkClient.checkExists().forPath(nodesPath + "/127.0.0.1:1260"));
    Assert.assertNull(zkClient.checkExists().forPath(assignmentsPath + "/127.0.0.1:1260"));
  }

  /** Assignment path nodes are persistent, so they survive client restart. */
  @Test
  public void testAssignmentsArePersistent() throws Exception {
    assignStore.registerNode(node1);
    assignStore.saveAssignments(node1, Arrays.asList("1", "2", "3"));

    String assignmentsPath = AmsHAProperties.getBucketAssignmentsPath("test-cluster");
    long ephemeralOwner =
        zkClient
            .checkExists()
            .forPath(assignmentsPath + "/127.0.0.1:1260/assignments")
            .getEphemeralOwner();
    Assert.assertEquals("Assignment node must be persistent", 0, ephemeralOwner);
  }

  /**
   * Upgrade regression: a cluster upgraded from the previous layout has persistent per-node
   * directories under /bucket-assignments (no /nodes registration). Those persistent directories
   * must NOT be reported as alive members, otherwise a crashed node would still be considered alive
   * and its buckets would never be reassigned.
   */
  @Test
  public void testUpgradePersistentAssignmentDirsNotReportedAlive() throws Exception {
    // Simulate the pre-upgrade layout: persistent nodes under /bucket-assignments only,
    // with assignments written by the old code path (parent created implicitly persistent).
    String assignmentsPath = AmsHAProperties.getBucketAssignmentsPath("test-cluster");
    String nodeDir = assignmentsPath + "/127.0.0.1:1260";
    zkClient
        .create()
        .creatingParentsIfNeeded()
        .withMode(CreateMode.PERSISTENT)
        .forPath(nodeDir + "/assignments", "[\"1\",\"2\"]".getBytes(StandardCharsets.UTF_8));

    // The old persistent directory is not a live member
    Assert.assertEquals(
        "Persistent assignment dirs from old layout must not be reported alive",
        0,
        assignStore.getAliveNodes().size());

    // But its assignments are still readable (data survives the upgrade)
    Assert.assertEquals(Arrays.asList("1", "2"), assignStore.getAssignments(node1));

    // After the node re-registers on the new layout, it becomes alive
    assignStore.registerNode(node1);
    Assert.assertEquals(1, assignStore.getAliveNodes().size());
  }

  /**
   * Crash regression: when a node's ZK session dies (node crash), its ephemeral registration
   * disappears and it must be dropped from the alive list so its buckets can be reassigned, even
   * though its persistent assignment data still exists.
   */
  @Test
  public void testCrashedNodeDroppedFromAliveList() throws Exception {
    // Use a second client to register a node that will "crash" independently
    CuratorFramework crashingClient =
        CuratorFrameworkFactory.newClient(
            zkServer.getConnectString(), 3000, 3000, new ExponentialBackoffRetry(1000, 3));
    crashingClient.start();
    ZkBucketAssignStore crashingStore = new ZkBucketAssignStore(crashingClient, "test-cluster");

    AmsServerInfo node2 = new AmsServerInfo();
    node2.setHost("127.0.0.2");
    node2.setThriftBindPort(1261);
    node2.setRestBindPort(1631);

    // node1 (stable) and node2 (will crash) both register and hold assignments
    assignStore.registerNode(node1);
    crashingStore.registerNode(node2);
    assignStore.saveAssignments(node1, Arrays.asList("1", "2"));
    crashingStore.saveAssignments(node2, Arrays.asList("3", "4"));

    Assert.assertEquals(2, assignStore.getAliveNodes().size());

    // Simulate node2's crash: close its ZK client so the session ends and the ephemeral
    // registration is removed by ZooKeeper. Persistent assignment data is intentionally
    // left behind so the assign service can reclaim and redistribute its buckets.
    crashingClient.close();

    // Wait for the session to expire and the ephemeral node to disappear
    String node2Path = AmsHAProperties.getNodesPath("test-cluster") + "/127.0.0.2:1261";
    waitForNodeRemoval(node2Path, 15000);

    // node2 is gone from the alive list even though its persistent assignments remain
    List<AmsServerInfo> aliveNodes = assignStore.getAliveNodes();
    Assert.assertEquals("Crashed node must be dropped from alive list", 1, aliveNodes.size());
    Assert.assertEquals(node1.getThriftBindPort(), aliveNodes.get(0).getThriftBindPort());

    // Its assignment data is still there so the assign service can reclaim and redistribute it
    Assert.assertEquals(
        "Crashed node's assignments must remain readable for redistribution",
        Arrays.asList("3", "4"),
        assignStore.getAssignments(node2));
  }

  /**
   * Fast-restart regression: if a node restarts before its old ephemeral registration expires,
   * registerNode() takes the NodeExists path. The re-registered node must still be reported alive
   * afterwards, and after its (new) session dies it must disappear again.
   */
  @Test
  public void testReRegisterWhileOldEphemeralExists() throws Exception {
    assignStore.registerNode(node1);
    // Re-register while the first ephemeral is still owned by the same session
    assignStore.registerNode(node1);
    Assert.assertEquals(1, assignStore.getAliveNodes().size());

    // Simulate a real restart with a NEW session: drop the stale registration owned by the
    // old session, then register again from the new session.
    String node1Path = AmsHAProperties.getNodesPath("test-cluster") + "/127.0.0.1:1260";
    zkClient.delete().forPath(node1Path);
    CuratorFramework restartedClient =
        CuratorFrameworkFactory.newClient(
            zkServer.getConnectString(), 3000, 3000, new ExponentialBackoffRetry(1000, 3));
    restartedClient.start();
    ZkBucketAssignStore restartedStore = new ZkBucketAssignStore(restartedClient, "test-cluster");
    restartedStore.registerNode(node1);

    // Old client's session ends. The registration now belongs to the restarted session, so
    // the node must still be reported alive by any observer.
    CuratorFramework observerClient =
        CuratorFrameworkFactory.newClient(
            zkServer.getConnectString(), 3000, 3000, new ExponentialBackoffRetry(1000, 3));
    observerClient.start();
    ZkBucketAssignStore observerStore = new ZkBucketAssignStore(observerClient, "test-cluster");
    zkClient.close();
    Assert.assertEquals(
        "Node must remain alive after fast restart with new session",
        1,
        observerStore.getAliveNodes().size());

    restartedStore.removeAssignments(node1);
    restartedClient.close();
    observerClient.close();
    // Prevent tearDown from closing zkClient again
    zkClient = observerClient;
  }

  private void waitForNodeRemoval(String path, long timeoutMs) throws InterruptedException {
    long deadline = System.currentTimeMillis() + timeoutMs;
    while (System.currentTimeMillis() < deadline) {
      try {
        if (zkClient.checkExists().forPath(path) == null) {
          return;
        }
      } catch (Exception e) {
        // fall through and retry
      }
      Thread.sleep(200);
    }
  }
}
