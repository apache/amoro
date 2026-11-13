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
import org.apache.amoro.server.table.DerbyPersistence;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Test for {@link DBBucketAssignStore} using an embedded Derby database. */
public class TestDBBucketAssignStore {

  private static final String CLUSTER_NAME = "test-cluster";
  private static final long HEARTBEAT_TTL_MS = TimeUnit.SECONDS.toMillis(60);

  @ClassRule public static DerbyPersistence DERBY = new DerbyPersistence();

  private DBBucketAssignStore assignStore;
  private AmsServerInfo node1;
  private AmsServerInfo node2;

  @Before
  public void setUp() throws Exception {
    assignStore = new DBBucketAssignStore(CLUSTER_NAME, HEARTBEAT_TTL_MS);

    node1 = new AmsServerInfo();
    node1.setHost("127.0.0.1");
    node1.setThriftBindPort(1260);
    node1.setRestBindPort(1630);

    node2 = new AmsServerInfo();
    node2.setHost("127.0.0.2");
    node2.setThriftBindPort(1261);
    node2.setRestBindPort(1631);
  }

  @After
  public void tearDown() throws Exception {
    if (assignStore != null) {
      try {
        assignStore.removeNode(node1);
        assignStore.removeNode(node2);
      } catch (Exception e) {
        // ignore
      }
      assignStore.close();
    }
  }

  @Test
  public void testRegisterNode() throws Exception {
    assignStore.registerNode(node1);

    List<AmsServerInfo> aliveNodes = assignStore.getAliveNodes();
    Assert.assertEquals("Should have 1 alive node", 1, aliveNodes.size());
    Assert.assertEquals(node1.getHost(), aliveNodes.get(0).getHost());
    Assert.assertEquals(node1.getThriftBindPort(), aliveNodes.get(0).getThriftBindPort());
  }

  @Test
  public void testRegisterMultipleNodes() throws Exception {
    assignStore.registerNode(node1);
    assignStore.registerNode(node2);

    List<AmsServerInfo> aliveNodes = assignStore.getAliveNodes();
    Assert.assertEquals("Should have 2 alive nodes", 2, aliveNodes.size());
  }

  @Test
  public void testRegisterNodeIdempotent() throws Exception {
    assignStore.registerNode(node1);
    assignStore.registerNode(node1); // should update heartbeat, not duplicate

    List<AmsServerInfo> aliveNodes = assignStore.getAliveNodes();
    Assert.assertEquals("Should still have 1 alive node", 1, aliveNodes.size());
  }

  @Test
  public void testGetAliveNodesEmpty() throws Exception {
    List<AmsServerInfo> aliveNodes = assignStore.getAliveNodes();
    Assert.assertNotNull("Should return empty list", aliveNodes);
    Assert.assertTrue("Should be empty", aliveNodes.isEmpty());
  }

  @Test
  public void testRemoveNode() throws Exception {
    assignStore.registerNode(node1);
    Assert.assertEquals(1, assignStore.getAliveNodes().size());

    assignStore.removeNode(node1);
    Assert.assertEquals(0, assignStore.getAliveNodes().size());
  }

  @Test
  public void testRemoveNodeNotRegistered() throws Exception {
    // Should not throw
    assignStore.removeNode(node1);
    Assert.assertEquals(0, assignStore.getAliveNodes().size());
  }

  @Test
  public void testSaveAndGetAssignments() throws Exception {
    List<String> bucketIds = Arrays.asList("1", "2", "3");
    assignStore.saveAssignments(node1, bucketIds);

    List<String> retrieved = assignStore.getAssignments(node1);
    Assert.assertEquals(bucketIds, retrieved);
  }

  @Test
  public void testUpdateAssignments() throws Exception {
    List<String> initial = Arrays.asList("1", "2");
    List<String> updated = Arrays.asList("3", "4", "5");

    assignStore.saveAssignments(node1, initial);
    Assert.assertEquals(initial, assignStore.getAssignments(node1));

    assignStore.saveAssignments(node1, updated);
    Assert.assertEquals(updated, assignStore.getAssignments(node1));
  }

  @Test
  public void testRemoveAssignments() throws Exception {
    assignStore.saveAssignments(node1, Arrays.asList("1", "2"));
    Assert.assertFalse(assignStore.getAssignments(node1).isEmpty());

    assignStore.removeAssignments(node1);
    Assert.assertTrue(assignStore.getAssignments(node1).isEmpty());
  }

  @Test
  public void testGetAllAssignments() throws Exception {
    assignStore.saveAssignments(node1, Arrays.asList("1", "2"));
    assignStore.saveAssignments(node2, Arrays.asList("3", "4"));

    Map<AmsServerInfo, List<String>> all = assignStore.getAllAssignments();
    Assert.assertEquals(2, all.size());
  }

  @Test
  public void testGetAllAssignmentsEmpty() throws Exception {
    Map<AmsServerInfo, List<String>> all = assignStore.getAllAssignments();
    Assert.assertTrue(all.isEmpty());
  }

  @Test
  public void testLastUpdateTime() throws Exception {
    long initial = assignStore.getLastUpdateTime(node1);
    Assert.assertEquals(0, initial);

    assignStore.saveAssignments(node1, Arrays.asList("1", "2"));
    long afterSave = assignStore.getLastUpdateTime(node1);
    Assert.assertTrue(afterSave > 0);

    Thread.sleep(10);
    assignStore.updateLastUpdateTime(node1);
    long afterUpdate = assignStore.getLastUpdateTime(node1);
    Assert.assertTrue(afterUpdate > afterSave);
  }

  /**
   * Concurrent-heartbeat regression (review P2): the HA container's heartbeat uses the same
   * UPDATE-then-INSERT upsert pattern as registerNode() and starts before registration. If the
   * heartbeat inserts the node after registerNode()'s UPDATE returned zero, the naive INSERT fails
   * with a duplicate key and propagates out of startBaseServices(), killing AMS startup.
   * registerNode() must tolerate this interleaving.
   */
  @Test
  public void testRegisterNodeToleratesConcurrentHeartbeatInsert() throws Exception {
    // Pre-insert the row exactly the way the HA heartbeat's upsertNodeHeartbeat() would when
    // it wins the race: same (cluster_name, node_key), heartbeat + last_update set, no
    // assignments yet. This leaves registerNode() with a duplicate-key INSERT.
    // Insert via a second store instance simulating the heartbeat thread
    DBBucketAssignStore heartbeatStore = new DBBucketAssignStore(CLUSTER_NAME, HEARTBEAT_TTL_MS);
    heartbeatStore.registerNode(node1); // heartbeat thread wins the race and inserts first
    heartbeatStore.close();

    // The main registration runs afterwards and must not fail even though the row already
    // exists (heartbeat inserted it first).
    assignStore.registerNode(node1);
    Assert.assertEquals(1, assignStore.getAliveNodes().size());

    // Direct duplicate-key simulation: call registerNode twice concurrently; both may see
    // UPDATE == 0 and both attempt INSERT. At least one must succeed and registration must
    // complete without an exception propagating (previously: duplicate key -> AMS exit).
    assignStore.removeNode(node1);
    java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
    java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(2);
    final java.util.List<Throwable> failures =
        java.util.Collections.synchronizedList(new java.util.ArrayList<>());
    Runnable registerTask =
        () -> {
          try {
            start.await();
            assignStore.registerNode(node1);
          } catch (Throwable t) {
            failures.add(t);
          } finally {
            done.countDown();
          }
        };
    Thread t1 = new Thread(registerTask, "register-1");
    Thread t2 = new Thread(registerTask, "register-2");
    t1.start();
    t2.start();
    start.countDown();
    Assert.assertTrue("Concurrent registrations must complete", done.await(10, TimeUnit.SECONDS));
    t1.join(5000);
    t2.join(5000);

    Assert.assertTrue(
        "registerNode must tolerate concurrent duplicate insert: " + failures, failures.isEmpty());
    Assert.assertEquals(
        "Node must be registered exactly once after concurrent registration",
        1,
        assignStore.getAliveNodes().size());
  }
}
