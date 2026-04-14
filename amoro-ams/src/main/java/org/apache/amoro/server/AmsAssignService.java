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
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
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
 * changes and redistributes bucket IDs evenly. Works with any {@link BucketAssignStore}
 * implementation (e.g. ZK or database); the store is chosen by {@link BucketAssignStoreFactory}
 * based on {@code ha.type}.
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

  public AmsAssignService(HighAvailabilityContainer haContainer, Configurations serviceConfig) {
    this(haContainer, serviceConfig, null);
  }

  /**
   * @param assignStore if non-null, used as the bucket assignment store; otherwise one is created
   *     via {@link BucketAssignStoreFactory} (same instance can be shared with {@code
   *     DefaultTableService}).
   */
  public AmsAssignService(
      HighAvailabilityContainer haContainer,
      Configurations serviceConfig,
      BucketAssignStore assignStore) {
    this.haContainer = haContainer;
    this.serviceConfig = serviceConfig;
    this.bucketIdTotalCount =
        serviceConfig.getInteger(AmoroManagementConf.HA_BUCKET_ID_TOTAL_COUNT);
    this.nodeOfflineTimeoutMs =
        serviceConfig.get(AmoroManagementConf.HA_NODE_OFFLINE_TIMEOUT).toMillis();
    this.assignIntervalSeconds =
        serviceConfig.get(AmoroManagementConf.HA_ASSIGN_INTERVAL).getSeconds();
    this.assignStore =
        assignStore != null
            ? assignStore
            : BucketAssignStoreFactory.create(haContainer, serviceConfig);
  }

  /**
   * Start the assignment service. Only works in master-slave mode and when current node is leader.
   */
  public void start() {
    if (!serviceConfig.getBoolean(AmoroManagementConf.HA_USE_MASTER_SLAVE_MODE)) {
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

  @VisibleForTesting
  public void doAssign() {
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
        // Even if no node changes, check if current assignments are balanced.
        // This handles cases where previous rebalancing was incomplete or
        // assignments became uneven due to historical reasons.
        if (isUnbalanced(aliveNodes, normalized.assignments)) {
          LOG.info(
              "No node changes detected, but current assignments are unbalanced. Performing rebalance...");
          List<String> allBuckets = generateBucketIds();
          Map<AmsServerInfo, List<String>> newAssignments =
              buildNewAssignments(aliveNodes, new HashSet<>(), normalized.assignments);
          rebalanceExistingAssignments(aliveNodes, allBuckets, newAssignments);
          persistAssignments(newAssignments);
        } else {
          refreshLastUpdateTime(aliveNodes);
        }
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
      // Catch broadly to keep the scheduler running; store (ZK/DB) errors are logged and retried
      // next interval
      LOG.error("Error during bucket assignment", e);
    }
  }

  /**
   * Check if the current assignments are unbalanced. Assignments are considered unbalanced if the
   * difference between the max and min bucket count across nodes exceeds 1.
   *
   * @param aliveNodes List of alive nodes
   * @param normalizedAssignments Current normalized assignments
   * @return true if assignments are unbalanced
   */
  private boolean isUnbalanced(
      List<AmsServerInfo> aliveNodes, Map<AmsServerInfo, List<String>> normalizedAssignments) {
    if (aliveNodes.size() <= 1) {
      return false;
    }
    int maxCount = Integer.MIN_VALUE;
    int minCount = Integer.MAX_VALUE;
    for (AmsServerInfo node : aliveNodes) {
      int count = normalizedAssignments.getOrDefault(node, new ArrayList<>()).size();
      maxCount = Math.max(maxCount, count);
      minCount = Math.min(minCount, count);
    }
    // Balanced means the difference between max and min should be at most 1
    return (maxCount - minCount) > 1;
  }

  /**
   * Rebalance existing assignments to ensure even distribution across all alive nodes. Moves
   * buckets from nodes with too many to nodes with too few, minimizing total migration.
   *
   * @param aliveNodes All alive nodes
   * @param allBuckets All bucket IDs
   * @param currentAssignments Current assignments map (will be modified)
   */
  private void rebalanceExistingAssignments(
      List<AmsServerInfo> aliveNodes,
      List<String> allBuckets,
      Map<AmsServerInfo, List<String>> currentAssignments) {
    int totalBuckets = allBuckets.size();
    int totalNodes = aliveNodes.size();
    int targetPerNode = totalBuckets / totalNodes;
    int remainder = totalBuckets % totalNodes;

    // Collect excess buckets from nodes that have too many
    List<String> excessBuckets = new ArrayList<>();

    // Sort nodes by current bucket count descending, so we take from the most loaded first
    List<AmsServerInfo> sortedNodes = new ArrayList<>(aliveNodes);
    sortedNodes.sort(
        (n1, n2) -> {
          int c1 = currentAssignments.getOrDefault(n1, new ArrayList<>()).size();
          int c2 = currentAssignments.getOrDefault(n2, new ArrayList<>()).size();
          return Integer.compare(c2, c1);
        });

    // First pass: determine each node's target and collect excess buckets
    // The first 'remainder' nodes (sorted by count desc) get targetPerNode + 1,
    // the rest get targetPerNode
    int remainderSlots = remainder;
    for (AmsServerInfo node : sortedNodes) {
      int nodeTarget = targetPerNode + (remainderSlots > 0 ? 1 : 0);
      List<String> buckets = currentAssignments.getOrDefault(node, new ArrayList<>());
      if (buckets.size() > nodeTarget) {
        // Remove excess buckets from the end of the list
        int excess = buckets.size() - nodeTarget;
        for (int i = 0; i < excess; i++) {
          excessBuckets.add(buckets.remove(buckets.size() - 1));
        }
        LOG.info(
            "Collected {} excess buckets from node {} (had: {}, target: {})",
            excess,
            node,
            buckets.size() + excess,
            nodeTarget);
      }
      if (remainderSlots > 0) {
        remainderSlots--;
      }
    }

    // Second pass: distribute excess buckets to nodes that have fewer than target
    if (!excessBuckets.isEmpty()) {
      redistributeBucketsIncrementally(aliveNodes, excessBuckets, currentAssignments);
    }
  }

  /**
   * Redistribute buckets incrementally to alive nodes, always assigning each bucket to the node
   * with the fewest buckets. This ensures even distribution regardless of the current state.
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

    // Assign each bucket to the node with the fewest buckets to ensure even distribution
    for (String bucketId : bucketsToRedistribute) {
      AmsServerInfo minNode = null;
      int minCount = Integer.MAX_VALUE;
      for (AmsServerInfo node : aliveNodes) {
        int count = currentAssignments.get(node).size();
        if (count < minCount) {
          minCount = count;
          minNode = node;
        }
      }
      currentAssignments.get(minNode).add(bucketId);
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

    // Determine which alive nodes already have extra buckets (i.e. targetBucketsPerNode + 1)
    // so that remainder slots are distributed fairly across all nodes, not just new ones.
    int remainderSlotsTaken = 0;
    for (AmsServerInfo node : aliveNodes) {
      if (!newNodes.contains(node)) {
        int count = currentAssignments.getOrDefault(node, new ArrayList<>()).size();
        if (count > targetBucketsPerNode) {
          remainderSlotsTaken++;
        }
      }
    }

    int newNodeIndex = 0;
    for (AmsServerInfo newNode : newNodes) {
      // Assign extra bucket only if there are remaining remainder slots
      int targetForNewNode =
          targetBucketsPerNode + ((remainderSlotsTaken + newNodeIndex) < remainder ? 1 : 0);
      int currentCount = currentAssignments.getOrDefault(newNode, new ArrayList<>()).size();
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
   * BucketAssignStore implementations (ZkBucketAssignStore and DBBucketAssignStore).
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
    // Build nodeKey sets for comparison instead of relying on AmsServerInfo.equals(),
    // because ZkBucketAssignStore.parseNodeKey() only sets host and thriftBindPort (restBindPort
    // is null), which would cause equals() mismatch with the full AmsServerInfo from
    // getAliveNodes().
    Set<String> assignedNodeKeys = new HashSet<>();
    for (AmsServerInfo node : currentAssignedNodes) {
      assignedNodeKeys.add(getNodeKey(node));
    }
    Set<AmsServerInfo> newNodes = new HashSet<>();
    for (AmsServerInfo aliveNode : aliveNodes) {
      if (!assignedNodeKeys.contains(getNodeKey(aliveNode))) {
        newNodes.add(aliveNode);
      }
    }

    Set<AmsServerInfo> offlineNodes = new HashSet<>();
    long currentTime = System.currentTimeMillis();
    Set<String> aliveNodeKeys = new HashSet<>();
    for (AmsServerInfo node : aliveNodes) {
      aliveNodeKeys.add(getNodeKey(node));
    }

    for (AmsServerInfo node : currentAssignedNodes) {
      String nodeKey = getNodeKey(node);
      // If the node is currently alive, it should never be considered offline,
      // even if its lastUpdateTime is stale (e.g. node restarted within nodeOfflineTimeoutMs
      // but lastUpdateTime was not refreshed while it was down).
      if (aliveNodeKeys.contains(nodeKey)) {
        continue;
      }
      try {
        long lastUpdateTime = assignStore.getLastUpdateTime(node);
        boolean shouldMarkOffline;
        if (lastUpdateTime <= 0) {
          // Missing timestamp means the node's saveAssignments never completed its
          // updateLastUpdateTime call (e.g. leader crashed between the two ZK writes)
          // or the store data was corrupted.  Since the node is already absent from
          // the alive list, treat it as offline immediately to avoid stranded buckets.
          shouldMarkOffline = true;
          LOG.warn(
              "Node {} is considered offline (missing last update time, not in alive list)", node);
        } else if ((currentTime - lastUpdateTime) > nodeOfflineTimeoutMs) {
          shouldMarkOffline = true;
          LOG.warn(
              "Node {} is considered offline due to timeout. Last update: {}",
              node,
              lastUpdateTime);
        } else {
          shouldMarkOffline = false;
          LOG.debug(
              "Node {} is not in alive list but heartbeat not timeout (last update: {}), waiting for timeout",
              node,
              lastUpdateTime);
        }
        if (shouldMarkOffline) {
          for (AmsServerInfo storedNode : currentAssignments.keySet()) {
            if (getNodeKey(storedNode).equals(nodeKey)) {
              offlineNodes.add(storedNode);
              break;
            }
          }
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
    return new NodeChangeResult(newNodes, offlineNodes);
  }

  /**
   * Removes offline nodes from the store and collects their buckets for redistribution. Only adds
   * buckets to the result after a successful remove, so that store failures do not lead to the same
   * bucket being assigned to both the offline node and an alive node.
   */
  private List<String> handleOfflineNodes(
      Set<AmsServerInfo> offlineNodes, Map<AmsServerInfo, List<String>> currentAssignments) {
    List<String> bucketsToRedistribute = new ArrayList<>();
    for (AmsServerInfo offlineNode : offlineNodes) {
      List<String> offlineBuckets = currentAssignments.get(offlineNode);
      try {
        assignStore.removeAssignments(offlineNode);
        if (offlineBuckets != null && !offlineBuckets.isEmpty()) {
          bucketsToRedistribute.addAll(offlineBuckets);
          LOG.info(
              "Collected {} buckets from offline node {} for redistribution",
              offlineBuckets.size(),
              offlineNode);
        }
      } catch (BucketAssignStoreException e) {
        LOG.warn(
            "Failed to remove assignments for offline node {}, skip redistributing its buckets",
            offlineNode,
            e);
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

  /**
   * Persists the new assignment map to the store. On per-node failure we log and continue so that
   * other nodes are still updated; the next run will retry.
   */
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

  /**
   * Refreshes last update time for all alive nodes when no reassignment is needed. Per-node
   * failures are logged and skipped; the next run will retry.
   */
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
