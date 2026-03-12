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
import org.apache.amoro.exception.BucketAssignStoreException;
import org.apache.amoro.server.ha.HighAvailabilityContainer;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for assigning bucket IDs to AMS nodes in master-slave mode. Periodically detects node
 * changes and redistributes bucket IDs evenly.
 */
public class AmsAssignService {

  private static final Logger LOG = LoggerFactory.getLogger(AmsAssignService.class);

  private final ScheduledExecutorService assignScheduler =
      Executors.newSingleThreadScheduledExecutor(
          new ThreadFactoryBuilder()
              .setNameFormat("ams-assign-scheduler-%d")
              .setDaemon(true)
              .build());

  private final HighAvailabilityContainer haContainer;
  private final BucketAssignStore assignStore;
  private final Configurations serviceConfig;
  private final int bucketIdTotalCount;
  private final long nodeOfflineTimeoutMs;
  private final long assignIntervalSeconds;
  private volatile boolean running = false;

  boolean isRunning() {
    return running;
  }

  void doAssignForTest() {
    doAssign();
  }

  public AmsAssignService(HighAvailabilityContainer haContainer, Configurations serviceConfig) {
    this.haContainer = haContainer;
    this.serviceConfig = serviceConfig;
    this.bucketIdTotalCount =
        serviceConfig.getInteger(AmoroManagementConf.HA_BUCKET_ID_TOTAL_COUNT);
    this.nodeOfflineTimeoutMs =
        serviceConfig.get(AmoroManagementConf.HA_NODE_OFFLINE_TIMEOUT).toMillis();
    this.assignIntervalSeconds =
        serviceConfig.get(AmoroManagementConf.HA_ASSIGN_INTERVAL).getSeconds();
    this.assignStore = BucketAssignStoreFactory.create(haContainer, serviceConfig);
  }

  /**
   * Start the assignment service. Only works in master-slave mode and when current node is leader.
   */
  public void start() {
    if (!serviceConfig.getBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE)) {
      LOG.info("Master-slave mode is not enabled, skip starting bucket assignment service");
      return;
    }
    if (running) {
      LOG.warn("Bucket assignment service is already running");
      return;
    }
    running = true;
    assignScheduler.scheduleWithFixedDelay(
        this::doAssign, 10, assignIntervalSeconds, TimeUnit.SECONDS);
    LOG.info("Bucket assignment service started with interval: {} seconds", assignIntervalSeconds);
  }

  /** Stop the assignment service. */
  public void stop() {
    if (!running) {
      return;
    }
    running = false;
    assignScheduler.shutdown();
    try {
      if (!assignScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
        assignScheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      assignScheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }
    LOG.info("Bucket assignment service stopped");
  }

  private void doAssign() {
    try {
      if (!haContainer.hasLeadership()) {
        LOG.debug("Current node is not leader, skip bucket assignment");
        return;
      }

      List<AmsServerInfo> aliveNodes = haContainer.getAliveNodes();
      if (aliveNodes.isEmpty()) {
        LOG.debug("No alive nodes found, skip bucket assignment");
        return;
      }

      Map<AmsServerInfo, List<String>> currentAssignments = assignStore.getAllAssignments();
      Map<String, AmsServerInfo> aliveNodeMap = buildAliveNodeMap(aliveNodes);
      NormalizedAssignments normalized =
          normalizeCurrentAssignments(currentAssignments, aliveNodeMap);
      NodeChangeResult change =
          detectNodeChanges(aliveNodes, currentAssignments, aliveNodeMap, normalized.assignedNodes);

      if (!change.needReassign()) {
        refreshLastUpdateTime(aliveNodes);
        return;
      }

      LOG.info(
          "Detected node changes - New nodes: {}, Offline nodes: {}, Performing incremental reassignment...",
          change.newNodes.size(),
          change.offlineNodes.size());

      List<String> bucketsToRedistribute =
          handleOfflineNodes(change.offlineNodes, currentAssignments);
      List<String> allBuckets = generateBucketIds();
      Map<AmsServerInfo, List<String>> newAssignments =
          buildNewAssignments(aliveNodes, change.offlineNodes, normalized.assignments);
      rebalance(aliveNodes, change.newNodes, bucketsToRedistribute, allBuckets, newAssignments);
      persistAssignments(newAssignments);
    } catch (Exception e) {
      LOG.error("Error during bucket assignment", e);
    }
  }

  /**
   * Redistribute buckets incrementally to alive nodes using round-robin. This minimizes bucket
   * migration by only redistributing buckets from offline nodes.
   *
   * @param aliveNodes List of alive nodes
   * @param bucketsToRedistribute Buckets to redistribute (from offline nodes)
   * @param currentAssignments Current assignments map (will be modified)
   */
  private void redistributeBucketsIncrementally(
      List<AmsServerInfo> aliveNodes,
      List<String> bucketsToRedistribute,
      Map<AmsServerInfo, List<String>> currentAssignments) {
    if (aliveNodes.isEmpty() || bucketsToRedistribute.isEmpty()) {
      return;
    }

    // Distribute buckets using round-robin to minimize migration
    int nodeIndex = 0;
    for (String bucketId : bucketsToRedistribute) {
      AmsServerInfo node = aliveNodes.get(nodeIndex % aliveNodes.size());
      currentAssignments.get(node).add(bucketId);
      nodeIndex++;
    }
  }

  /**
   * Balance buckets for new nodes by taking buckets from existing nodes. This minimizes migration
   * by only moving necessary buckets to new nodes.
   *
   * @param aliveNodes All alive nodes
   * @param newNodes Newly added nodes
   * @param currentAssignments Current assignments map (will be modified)
   * @param targetBucketsPerNode Target number of buckets per node
   * @param remainder Remainder when dividing total buckets by node count
   */
  private void balanceBucketsForNewNodes(
      List<AmsServerInfo> aliveNodes,
      Set<AmsServerInfo> newNodes,
      Map<AmsServerInfo, List<String>> currentAssignments,
      int targetBucketsPerNode,
      int remainder) {
    if (newNodes.isEmpty()) {
      return;
    }

    // Calculate how many buckets each new node should get
    int bucketsPerNewNode = targetBucketsPerNode;
    int newNodeIndex = 0;
    for (AmsServerInfo newNode : newNodes) {
      // First 'remainder' nodes get one extra bucket
      int targetForNewNode = bucketsPerNewNode + (newNodeIndex < remainder ? 1 : 0);
      int currentCount = currentAssignments.get(newNode).size();
      int needed = targetForNewNode - currentCount;

      if (needed > 0) {
        // Collect buckets from existing nodes (prefer nodes with more buckets)
        List<String> bucketsToMove =
            collectBucketsFromExistingNodes(aliveNodes, newNodes, currentAssignments, needed);
        currentAssignments.get(newNode).addAll(bucketsToMove);
        LOG.info(
            "Moved {} buckets to new node {} (target: {})",
            bucketsToMove.size(),
            newNode,
            targetForNewNode);
      }
      newNodeIndex++;
    }
  }

  /**
   * Collect buckets from existing nodes to balance for new nodes. Prefer taking from nodes that
   * have more buckets than target.
   *
   * @param aliveNodes All alive nodes
   * @param newNodes New nodes (excluded from source)
   * @param currentAssignments Current assignments
   * @param needed Number of buckets needed
   * @return List of bucket IDs to move
   */
  private List<String> collectBucketsFromExistingNodes(
      List<AmsServerInfo> aliveNodes,
      Set<AmsServerInfo> newNodes,
      Map<AmsServerInfo, List<String>> currentAssignments,
      int needed) {
    List<String> bucketsToMove = new ArrayList<>();
    List<AmsServerInfo> existingNodes = new ArrayList<>();
    for (AmsServerInfo node : aliveNodes) {
      if (!newNodes.contains(node)) {
        existingNodes.add(node);
      }
    }

    if (existingNodes.isEmpty()) {
      return bucketsToMove;
    }

    // Sort existing nodes by current bucket count (descending)
    // This ensures we take from nodes with more buckets first
    existingNodes.sort(
        (n1, n2) -> {
          int count1 = currentAssignments.get(n1).size();
          int count2 = currentAssignments.get(n2).size();
          return Integer.compare(count2, count1);
        });

    // Collect buckets from existing nodes using round-robin
    int nodeIndex = 0;
    int collected = 0;
    while (collected < needed && !existingNodes.isEmpty()) {
      AmsServerInfo sourceNode = existingNodes.get(nodeIndex % existingNodes.size());
      List<String> sourceBuckets = currentAssignments.get(sourceNode);
      if (!sourceBuckets.isEmpty()) {
        // Take one bucket from this node
        String bucketToMove = sourceBuckets.remove(0);
        bucketsToMove.add(bucketToMove);
        collected++;
        LOG.debug("Moving bucket {} from node {} to new node", bucketToMove, sourceNode);
      } else {
        // This node has no more buckets, remove it from consideration
        existingNodes.remove(sourceNode);
        if (existingNodes.isEmpty()) {
          break;
        }
        nodeIndex = nodeIndex % existingNodes.size();
        continue;
      }
      nodeIndex++;
    }

    return bucketsToMove;
  }

  private List<String> generateBucketIds() {
    List<String> bucketIds = new ArrayList<>();
    for (int i = 1; i <= bucketIdTotalCount; i++) {
      bucketIds.add(String.valueOf(i));
    }
    return bucketIds;
  }

  /**
   * Get node key for matching nodes. Uses host:thriftBindPort format, consistent with
   * ZkBucketAssignStore.getNodeKey().
   */
  private String getNodeKey(AmsServerInfo nodeInfo) {
    return nodeInfo.getHost() + ":" + nodeInfo.getThriftBindPort();
  }

  private Map<String, AmsServerInfo> buildAliveNodeMap(List<AmsServerInfo> aliveNodes) {
    Map<String, AmsServerInfo> map = new HashMap<>();
    for (AmsServerInfo node : aliveNodes) {
      map.put(getNodeKey(node), node);
    }
    return map;
  }

  private static class NormalizedAssignments {
    final Map<AmsServerInfo, List<String>> assignments;
    final Set<AmsServerInfo> assignedNodes;

    NormalizedAssignments(
        Map<AmsServerInfo, List<String>> assignments, Set<AmsServerInfo> assignedNodes) {
      this.assignments = assignments;
      this.assignedNodes = assignedNodes;
    }
  }

  private NormalizedAssignments normalizeCurrentAssignments(
      Map<AmsServerInfo, List<String>> currentAssignments,
      Map<String, AmsServerInfo> aliveNodeMap) {
    Map<AmsServerInfo, List<String>> normalized = new HashMap<>();
    Set<AmsServerInfo> assignedNodes = new HashSet<>();
    for (Map.Entry<AmsServerInfo, List<String>> entry : currentAssignments.entrySet()) {
      AmsServerInfo storedNode = entry.getKey();
      String nodeKey = getNodeKey(storedNode);
      AmsServerInfo aliveNode = aliveNodeMap.get(nodeKey);
      if (aliveNode != null) {
        normalized.put(aliveNode, entry.getValue());
        assignedNodes.add(aliveNode);
      } else {
        normalized.put(storedNode, entry.getValue());
        assignedNodes.add(storedNode);
      }
    }
    return new NormalizedAssignments(normalized, assignedNodes);
  }

  private static class NodeChangeResult {
    final Set<AmsServerInfo> newNodes;
    final Set<AmsServerInfo> offlineNodes;

    NodeChangeResult(Set<AmsServerInfo> newNodes, Set<AmsServerInfo> offlineNodes) {
      this.newNodes = newNodes;
      this.offlineNodes = offlineNodes;
    }

    boolean needReassign() {
      return !newNodes.isEmpty() || !offlineNodes.isEmpty();
    }
  }

  private NodeChangeResult detectNodeChanges(
      List<AmsServerInfo> aliveNodes,
      Map<AmsServerInfo, List<String>> currentAssignments,
      Map<String, AmsServerInfo> aliveNodeMap,
      Set<AmsServerInfo> currentAssignedNodes) {
    Set<AmsServerInfo> aliveNodeSet = new HashSet<>(aliveNodes);
    Set<AmsServerInfo> newNodes = new HashSet<>(aliveNodeSet);
    newNodes.removeAll(currentAssignedNodes);

    Set<AmsServerInfo> offlineNodes = new HashSet<>();
    for (AmsServerInfo storedNode : currentAssignments.keySet()) {
      if (!aliveNodeMap.containsKey(getNodeKey(storedNode))) {
        offlineNodes.add(storedNode);
      }
    }

    long currentTime = System.currentTimeMillis();
    Set<String> aliveNodeKeys = new HashSet<>();
    for (AmsServerInfo node : aliveNodes) {
      aliveNodeKeys.add(getNodeKey(node));
    }
    for (AmsServerInfo node : currentAssignedNodes) {
      String nodeKey = getNodeKey(node);
      if (aliveNodeKeys.contains(nodeKey)) {
        try {
          long lastUpdateTime = assignStore.getLastUpdateTime(node);
          if (lastUpdateTime > 0 && (currentTime - lastUpdateTime) > nodeOfflineTimeoutMs) {
            for (AmsServerInfo storedNode : currentAssignments.keySet()) {
              if (getNodeKey(storedNode).equals(nodeKey)) {
                offlineNodes.add(storedNode);
                break;
              }
            }
            LOG.warn(
                "Node {} is considered offline due to timeout. Last update: {}",
                node,
                lastUpdateTime);
          }
        } catch (BucketAssignStoreException e) {
          LOG.warn("Failed to get last update time for node {}, treating as offline", node, e);
          for (AmsServerInfo storedNode : currentAssignments.keySet()) {
            if (getNodeKey(storedNode).equals(nodeKey)) {
              offlineNodes.add(storedNode);
              break;
            }
          }
        }
      }
    }
    return new NodeChangeResult(newNodes, offlineNodes);
  }

  private List<String> handleOfflineNodes(
      Set<AmsServerInfo> offlineNodes, Map<AmsServerInfo, List<String>> currentAssignments) {
    List<String> bucketsToRedistribute = new ArrayList<>();
    for (AmsServerInfo offlineNode : offlineNodes) {
      try {
        List<String> offlineBuckets = currentAssignments.get(offlineNode);
        if (offlineBuckets != null && !offlineBuckets.isEmpty()) {
          bucketsToRedistribute.addAll(offlineBuckets);
          LOG.info(
              "Collected {} buckets from offline node {} for redistribution",
              offlineBuckets.size(),
              offlineNode);
        }
        assignStore.removeAssignments(offlineNode);
      } catch (BucketAssignStoreException e) {
        LOG.warn("Failed to remove assignments for offline node {}", offlineNode, e);
      }
    }
    return bucketsToRedistribute;
  }

  private Map<AmsServerInfo, List<String>> buildNewAssignments(
      List<AmsServerInfo> aliveNodes,
      Set<AmsServerInfo> offlineNodes,
      Map<AmsServerInfo, List<String>> normalizedAssignments) {
    Map<AmsServerInfo, List<String>> newAssignments = new HashMap<>();
    Set<String> offlineNodeKeys = new HashSet<>();
    for (AmsServerInfo offlineNode : offlineNodes) {
      offlineNodeKeys.add(getNodeKey(offlineNode));
    }
    for (AmsServerInfo node : aliveNodes) {
      String nodeKey = getNodeKey(node);
      if (!offlineNodeKeys.contains(nodeKey)) {
        List<String> existing = normalizedAssignments.get(node);
        if (existing != null && !existing.isEmpty()) {
          newAssignments.put(node, new ArrayList<>(existing));
        } else {
          newAssignments.put(node, new ArrayList<>());
        }
      } else {
        newAssignments.put(node, new ArrayList<>());
      }
    }
    return newAssignments;
  }

  private void rebalance(
      List<AmsServerInfo> aliveNodes,
      Set<AmsServerInfo> newNodes,
      List<String> bucketsToRedistribute,
      List<String> allBuckets,
      Map<AmsServerInfo, List<String>> newAssignments) {
    if (!bucketsToRedistribute.isEmpty()) {
      redistributeBucketsIncrementally(aliveNodes, bucketsToRedistribute, newAssignments);
    }

    int totalBuckets = allBuckets.size();
    int totalAliveNodes = aliveNodes.size();
    int targetBucketsPerNode = totalBuckets / totalAliveNodes;
    int remainder = totalBuckets % totalAliveNodes;
    if (!newNodes.isEmpty()) {
      balanceBucketsForNewNodes(
          aliveNodes, newNodes, newAssignments, targetBucketsPerNode, remainder);
    }

    Set<String> allAssignedBuckets = new HashSet<>();
    for (List<String> buckets : newAssignments.values()) {
      allAssignedBuckets.addAll(buckets);
    }
    List<String> unassignedBuckets = new ArrayList<>();
    for (String bucket : allBuckets) {
      if (!allAssignedBuckets.contains(bucket)) {
        unassignedBuckets.add(bucket);
      }
    }
    if (!unassignedBuckets.isEmpty()) {
      redistributeBucketsIncrementally(aliveNodes, unassignedBuckets, newAssignments);
    }
  }

  private void persistAssignments(Map<AmsServerInfo, List<String>> newAssignments) {
    for (Map.Entry<AmsServerInfo, List<String>> entry : newAssignments.entrySet()) {
      try {
        assignStore.saveAssignments(entry.getKey(), entry.getValue());
        LOG.info(
            "Assigned {} buckets to node {}: {}",
            entry.getValue().size(),
            entry.getKey(),
            entry.getValue());
      } catch (BucketAssignStoreException e) {
        LOG.error("Failed to save assignments for node {}", entry.getKey(), e);
      }
    }
  }

  private void refreshLastUpdateTime(List<AmsServerInfo> aliveNodes) {
    for (AmsServerInfo node : aliveNodes) {
      try {
        assignStore.updateLastUpdateTime(node);
      } catch (BucketAssignStoreException e) {
        LOG.warn("Failed to update last update time for node {}", node, e);
      }
    }
  }
}
