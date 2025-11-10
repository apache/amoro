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
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.CuratorFramework;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.amoro.shade.zookeeper3.org.apache.zookeeper.CreateMode;
import org.apache.amoro.shade.zookeeper3.org.apache.zookeeper.KeeperException;
import org.apache.amoro.shade.zookeeper3.org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** Test for ZkBucketAssignStore using mocked ZK to avoid connection issues. */
public class TestZkBucketAssignStore {

  private CuratorFramework mockZkClient;
  private LeaderLatch mockLeaderLatch;
  private ZkBucketAssignStore assignStore;
  private AmsServerInfo node1;
  private AmsServerInfo node2;
  private MockZkState mockZkState;

  @Before
  public void setUp() throws Exception {
    mockZkState = new MockZkState();
    mockZkClient = createMockZkClient();
    mockLeaderLatch = createMockLeaderLatch(true); // Is leader by default

    assignStore = new ZkBucketAssignStore(mockZkClient, "test-cluster", mockLeaderLatch);

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
        assignStore.removeAssignments(node1);
        assignStore.removeAssignments(node2);
      } catch (Exception e) {
        // ignore
      }
    }
    mockZkState.clear();
  }

  @Test
  public void testSaveAndGetAssignments() throws Exception {
    List<String> bucketIds = Arrays.asList("1", "2", "3", "4", "5");

    // Save assignments
    assignStore.saveAssignments(node1, bucketIds);

    // Get assignments
    List<String> retrieved = assignStore.getAssignments(node1);
    Assert.assertEquals("Bucket IDs should match", bucketIds, retrieved);
  }

  @Test
  public void testGetAssignmentsForNonExistentNode() throws Exception {
    List<String> retrieved = assignStore.getAssignments(node1);
    Assert.assertNotNull("Should return empty list", retrieved);
    Assert.assertTrue("Should return empty list", retrieved.isEmpty());
  }

  @Test
  public void testUpdateAssignments() throws Exception {
    List<String> initialBuckets = Arrays.asList("1", "2", "3");
    List<String> updatedBuckets = Arrays.asList("4", "5", "6", "7");

    // Save initial assignments
    assignStore.saveAssignments(node1, initialBuckets);
    Assert.assertEquals(initialBuckets, assignStore.getAssignments(node1));

    // Update assignments
    assignStore.saveAssignments(node1, updatedBuckets);
    Assert.assertEquals(updatedBuckets, assignStore.getAssignments(node1));
  }

  @Test
  public void testRemoveAssignments() throws Exception {
    List<String> bucketIds = Arrays.asList("1", "2", "3");

    // Save assignments
    assignStore.saveAssignments(node1, bucketIds);
    Assert.assertFalse("Should have assignments", assignStore.getAssignments(node1).isEmpty());

    // Remove assignments
    assignStore.removeAssignments(node1);
    Assert.assertTrue("Should be empty after removal", assignStore.getAssignments(node1).isEmpty());
  }

  @Test
  public void testGetAllAssignments() throws Exception {
    List<String> buckets1 = Arrays.asList("1", "2", "3");
    List<String> buckets2 = Arrays.asList("4", "5", "6");

    // Save assignments for multiple nodes
    assignStore.saveAssignments(node1, buckets1);
    assignStore.saveAssignments(node2, buckets2);

    // Get all assignments
    Map<AmsServerInfo, List<String>> allAssignments = assignStore.getAllAssignments();
    Assert.assertEquals("Should have 2 nodes", 2, allAssignments.size());

    // Find nodes by host and port since parseNodeKey doesn't set restBindPort
    List<String> foundBuckets1 = null;
    List<String> foundBuckets2 = null;
    for (Map.Entry<AmsServerInfo, List<String>> entry : allAssignments.entrySet()) {
      AmsServerInfo node = entry.getKey();
      if (node1.getHost().equals(node.getHost())
          && node1.getThriftBindPort().equals(node.getThriftBindPort())) {
        foundBuckets1 = entry.getValue();
      } else if (node2.getHost().equals(node.getHost())
          && node2.getThriftBindPort().equals(node.getThriftBindPort())) {
        foundBuckets2 = entry.getValue();
      }
    }
    Assert.assertEquals(buckets1, foundBuckets1);
    Assert.assertEquals(buckets2, foundBuckets2);
  }

  @Test
  public void testGetAllAssignmentsEmpty() throws Exception {
    Map<AmsServerInfo, List<String>> allAssignments = assignStore.getAllAssignments();
    Assert.assertNotNull("Should return empty map", allAssignments);
    Assert.assertTrue("Should be empty", allAssignments.isEmpty());
  }

  @Test
  public void testLastUpdateTime() throws Exception {
    List<String> bucketIds = Arrays.asList("1", "2", "3");

    // Initially no update time
    long initialTime = assignStore.getLastUpdateTime(node1);
    Assert.assertEquals("Should be 0 initially", 0, initialTime);

    // Save assignments (should update time)
    long beforeSave = System.currentTimeMillis();
    assignStore.saveAssignments(node1, bucketIds);
    long afterSave = System.currentTimeMillis();

    long updateTime = assignStore.getLastUpdateTime(node1);
    Assert.assertTrue(
        "Update time should be between before and after",
        updateTime >= beforeSave && updateTime <= afterSave);

    // Manually update time
    Thread.sleep(10);
    assignStore.updateLastUpdateTime(node1);
    long newUpdateTime = assignStore.getLastUpdateTime(node1);
    Assert.assertTrue("New update time should be later", newUpdateTime > updateTime);
  }

  @Test
  public void testEmptyBucketList() throws Exception {
    List<String> emptyList = new ArrayList<>();
    assignStore.saveAssignments(node1, emptyList);
    List<String> retrieved = assignStore.getAssignments(node1);
    Assert.assertNotNull("Should return empty list", retrieved);
    Assert.assertTrue("Should be empty", retrieved.isEmpty());
  }

  @Test
  public void testMultipleNodesWithSameHostDifferentPort() throws Exception {
    AmsServerInfo node3 = new AmsServerInfo();
    node3.setHost("127.0.0.1");
    node3.setThriftBindPort(1262);
    node3.setRestBindPort(1632);

    List<String> buckets1 = Arrays.asList("1", "2");
    List<String> buckets3 = Arrays.asList("3", "4");

    assignStore.saveAssignments(node1, buckets1);
    assignStore.saveAssignments(node3, buckets3);

    Assert.assertEquals(buckets1, assignStore.getAssignments(node1));
    Assert.assertEquals(buckets3, assignStore.getAssignments(node3));

    Map<AmsServerInfo, List<String>> allAssignments = assignStore.getAllAssignments();
    Assert.assertEquals("Should have 2 nodes", 2, allAssignments.size());
  }

  /** Create a mock CuratorFramework that uses MockZkState for storage. */
  @SuppressWarnings("unchecked")
  private CuratorFramework createMockZkClient() throws Exception {
    CuratorFramework mockClient = mock(CuratorFramework.class);

    // Mock getChildren()
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

    // Mock create() - manually create the entire fluent API chain
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
    when(createBuilder.creatingParentsIfNeeded()).thenReturn(pathAndBytesable);
    when(pathAndBytesable.withMode(any(CreateMode.class))).thenReturn(pathAndBytesable);

    // Mock forPath(path, data) - used by saveAssignments()
    when(pathAndBytesable.forPath(anyString(), any(byte[].class)))
        .thenAnswer(
            invocation -> {
              String path = invocation.getArgument(0);
              byte[] data = invocation.getArgument(1);
              return mockZkState.createNode(path, data);
            });

    // Mock forPath(path) - used by createPathIfNeeded()
    when(pathAndBytesable.forPath(anyString()))
        .thenAnswer(
            invocation -> {
              String path = invocation.getArgument(0);
              if (mockZkState.exists(path) == null) {
                mockZkState.createNode(path, new byte[0]);
              }
              return null;
            });

    // Mock setData()
    org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.SetDataBuilder
        setDataBuilder =
            mock(
                org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.SetDataBuilder
                    .class);
    when(mockClient.setData()).thenReturn(setDataBuilder);
    when(setDataBuilder.forPath(anyString(), any(byte[].class)))
        .thenAnswer(
            invocation -> {
              String path = invocation.getArgument(0);
              byte[] data = invocation.getArgument(1);
              mockZkState.setData(path, data);
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

    // Mock deletingChildrenIfNeeded()
    org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.ChildrenDeletable
        childrenDeletable =
            mock(
                org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.api.ChildrenDeletable
                    .class);
    when(deleteBuilder.deletingChildrenIfNeeded()).thenReturn(childrenDeletable);
    doAnswer(
            invocation -> {
              String path = invocation.getArgument(0);
              mockZkState.deleteNodeRecursive(path);
              return null;
            })
        .when(childrenDeletable)
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

  /** Create a mock LeaderLatch with specified leadership status. */
  private LeaderLatch createMockLeaderLatch(boolean hasLeadership) throws Exception {
    LeaderLatch mockLatch = mock(LeaderLatch.class);
    when(mockLatch.hasLeadership()).thenReturn(hasLeadership);
    doAnswer(invocation -> null).when(mockLatch).addListener(any());
    doAnswer(invocation -> null).when(mockLatch).start();
    doAnswer(invocation -> null).when(mockLatch).close();
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

    public void setData(String path, byte[] data) throws KeeperException {
      if (!nodes.containsKey(path)) {
        throw new KeeperException.NoNodeException(path);
      }
      nodes.put(path, data);
    }

    public String createNode(String path, byte[] data) {
      // Handle sequential nodes
      if (path.endsWith("-")) {
        int seq = sequenceCounter.incrementAndGet();
        path = path + String.format("%010d", seq);
      }
      // Create parent paths if they don't exist (simulating creatingParentsIfNeeded)
      createParentPaths(path);
      nodes.put(path, data);
      return path;
    }

    private void createParentPaths(String path) {
      // Create all parent paths as empty nodes
      // Handle absolute paths (starting with "/")
      boolean isAbsolute = path.startsWith("/");
      String[] parts = path.split("/");
      StringBuilder currentPath = new StringBuilder();
      if (isAbsolute) {
        currentPath.append("/");
      }
      for (int i = 0; i < parts.length - 1; i++) {
        if (parts[i].isEmpty()) {
          continue; // Skip empty parts from split
        }
        if (currentPath.length() > 0 && !currentPath.toString().endsWith("/")) {
          currentPath.append("/");
        }
        currentPath.append(parts[i]);
        String parentPath = currentPath.toString();
        // Only create if it doesn't exist
        if (!nodes.containsKey(parentPath)) {
          nodes.put(parentPath, new byte[0]);
        }
      }
    }

    public void deleteNode(String path) throws KeeperException {
      if (!nodes.containsKey(path)) {
        throw new KeeperException.NoNodeException(path);
      }
      nodes.remove(path);
    }

    public void deleteNodeRecursive(String path) throws KeeperException {
      // Delete the node and all its children
      List<String> toDelete = new ArrayList<>();
      String prefix = path.endsWith("/") ? path : path + "/";
      for (String nodePath : nodes.keySet()) {
        if (nodePath.equals(path) || nodePath.startsWith(prefix)) {
          toDelete.add(nodePath);
        }
      }
      for (String nodePath : toDelete) {
        nodes.remove(nodePath);
      }
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
