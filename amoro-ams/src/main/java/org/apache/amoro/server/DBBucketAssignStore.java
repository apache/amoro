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
import org.apache.amoro.exception.BucketAssignStoreException;
import org.apache.amoro.server.persistence.BucketAssignmentMeta;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.BucketAssignMapper;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.amoro.utils.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database-backed implementation of BucketAssignStore for HA_TYPE_DATABASE. Stores bucket
 * assignments in the {@code bucket_assignments} table, keyed by cluster name and node key
 * (host:thriftBindPort).
 */
public class DBBucketAssignStore extends PersistentBase implements BucketAssignStore {

  private static final Logger LOG = LoggerFactory.getLogger(DBBucketAssignStore.class);
  private static final TypeReference<List<String>> LIST_STRING_TYPE =
      new TypeReference<List<String>>() {};

  private final String clusterName;

  public DBBucketAssignStore(String clusterName) {
    this.clusterName = clusterName;
  }

  @Override
  public void saveAssignments(AmsServerInfo nodeInfo, List<String> bucketIds)
      throws BucketAssignStoreException {
    String nodeKey = getNodeKey(nodeInfo);
    String serverInfoJson = JacksonUtil.toJSONString(nodeInfo);
    String assignmentsJson =
        JacksonUtil.toJSONString(bucketIds != null ? bucketIds : new ArrayList<>());
    long now = System.currentTimeMillis();
    try {
      // Use atomic operation: try insert first, if failed then update
      try {
        doAs(
            BucketAssignMapper.class,
            mapper ->
                mapper.insert(
                    new BucketAssignmentMeta(
                        clusterName, nodeKey, serverInfoJson, assignmentsJson, now)));
      } catch (Exception insertException) {
        // If insert failed (record already exists), then perform update
        doAsExisted(
            BucketAssignMapper.class,
            mapper -> mapper.update(clusterName, nodeKey, serverInfoJson, assignmentsJson, now),
            () ->
                new BucketAssignStoreException(
                    "Failed to save bucket assignments for node " + nodeKey, insertException));
      }
      LOG.debug("Saved bucket assignments for node {}: {}", nodeKey, bucketIds);
    } catch (Exception e) {
      LOG.error("Failed to save bucket assignments for node {}", nodeKey, e);
      throw new BucketAssignStoreException(
          "Failed to save bucket assignments for node " + nodeKey, e);
    }
  }

  @Override
  public List<String> getAssignments(AmsServerInfo nodeInfo) throws BucketAssignStoreException {
    String nodeKey = getNodeKey(nodeInfo);
    try {
      BucketAssignmentMeta meta =
          getAs(BucketAssignMapper.class, mapper -> mapper.selectByNode(clusterName, nodeKey));
      if (meta == null
          || meta.getAssignmentsJson() == null
          || meta.getAssignmentsJson().isEmpty()) {
        return new ArrayList<>();
      }
      return JacksonUtil.parseObject(meta.getAssignmentsJson(), LIST_STRING_TYPE);
    } catch (Exception e) {
      LOG.error("Failed to get bucket assignments for node {}", nodeKey, e);
      throw new BucketAssignStoreException(
          "Failed to get bucket assignments for node " + nodeKey, e);
    }
  }

  @Override
  public void removeAssignments(AmsServerInfo nodeInfo) throws BucketAssignStoreException {
    String nodeKey = getNodeKey(nodeInfo);
    try {
      doAs(BucketAssignMapper.class, mapper -> mapper.deleteByNode(clusterName, nodeKey));
      LOG.debug("Removed bucket assignments for node {}", nodeKey);
    } catch (Exception e) {
      LOG.error("Failed to remove bucket assignments for node {}", nodeKey, e);
      throw new BucketAssignStoreException(
          "Failed to remove bucket assignments for node " + nodeKey, e);
    }
  }

  @Override
  public Map<AmsServerInfo, List<String>> getAllAssignments() throws BucketAssignStoreException {
    try {
      List<BucketAssignmentMeta> rows =
          getAs(BucketAssignMapper.class, mapper -> mapper.selectAllByCluster(clusterName));
      Map<AmsServerInfo, List<String>> result = new HashMap<>();
      for (BucketAssignmentMeta meta : rows) {
        if (meta.getAssignmentsJson() == null || meta.getAssignmentsJson().isEmpty()) {
          continue;
        }
        List<String> bucketIds =
            JacksonUtil.parseObject(meta.getAssignmentsJson(), LIST_STRING_TYPE);
        if (bucketIds == null || bucketIds.isEmpty()) {
          continue;
        }
        AmsServerInfo nodeInfo = parseNodeInfo(meta);
        result.put(nodeInfo, bucketIds);
      }
      return result;
    } catch (Exception e) {
      LOG.error("Failed to get all bucket assignments", e);
      throw new BucketAssignStoreException("Failed to get all bucket assignments", e);
    }
  }

  @Override
  public long getLastUpdateTime(AmsServerInfo nodeInfo) throws BucketAssignStoreException {
    String nodeKey = getNodeKey(nodeInfo);
    try {
      BucketAssignmentMeta meta =
          getAs(BucketAssignMapper.class, mapper -> mapper.selectByNode(clusterName, nodeKey));
      if (meta == null || meta.getLastUpdateTime() == null) {
        return 0;
      }
      return meta.getLastUpdateTime();
    } catch (Exception e) {
      LOG.error("Failed to get last update time for node {}", nodeKey, e);
      throw new BucketAssignStoreException("Failed to get last update time for node " + nodeKey, e);
    }
  }

  @Override
  public void updateLastUpdateTime(AmsServerInfo nodeInfo) throws BucketAssignStoreException {
    String nodeKey = getNodeKey(nodeInfo);
    long now = System.currentTimeMillis();
    try {
      Long updated =
          updateAs(
              BucketAssignMapper.class,
              mapper -> mapper.updateLastUpdateTime(clusterName, nodeKey, now));
      if (updated != null && updated == 0) {
        // Row may not exist; insert a minimal row so last_update_time is stored
        doAs(
            BucketAssignMapper.class,
            mapper ->
                mapper.insert(new BucketAssignmentMeta(clusterName, nodeKey, null, null, now)));
      }
    } catch (Exception e) {
      LOG.error("Failed to update last update time for node {}", nodeKey, e);
      throw new BucketAssignStoreException(
          "Failed to update last update time for node " + nodeKey, e);
    }
  }

  @Override
  public List<AmsServerInfo> getAliveNodes() throws BucketAssignStoreException {
    try {
      List<BucketAssignmentMeta> rows =
          getAs(BucketAssignMapper.class, mapper -> mapper.selectAllByCluster(clusterName));
      List<AmsServerInfo> nodes = new ArrayList<>();
      for (BucketAssignmentMeta meta : rows) {
        AmsServerInfo nodeInfo = parseNodeInfo(meta);
        if (nodeInfo.getThriftBindPort() != null && nodeInfo.getThriftBindPort() > 0) {
          nodes.add(nodeInfo);
        }
      }
      return nodes;
    } catch (Exception e) {
      LOG.error("Failed to get alive nodes", e);
      throw new BucketAssignStoreException("Failed to get alive nodes", e);
    }
  }

  private static String getNodeKey(AmsServerInfo nodeInfo) {
    return nodeInfo.getHost() + ":" + nodeInfo.getThriftBindPort();
  }

  private static AmsServerInfo parseNodeInfo(BucketAssignmentMeta meta) {
    if (meta.getServerInfoJson() != null && !meta.getServerInfoJson().isEmpty()) {
      try {
        return JacksonUtil.parseObject(meta.getServerInfoJson(), AmsServerInfo.class);
      } catch (Exception e) {
        LOG.warn(
            "Failed to parse server_info_json for node {}, fallback to node_key",
            meta.getNodeKey(),
            e);
      }
    }
    return parseNodeKey(meta.getNodeKey());
  }

  private static AmsServerInfo parseNodeKey(String nodeKey) {
    String[] parts = nodeKey.split(":");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid node key format: " + nodeKey);
    }
    AmsServerInfo nodeInfo = new AmsServerInfo();
    nodeInfo.setHost(parts[0]);
    nodeInfo.setThriftBindPort(Integer.parseInt(parts[1]));
    return nodeInfo;
  }
}
