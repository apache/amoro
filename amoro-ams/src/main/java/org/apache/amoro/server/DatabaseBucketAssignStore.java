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
import org.apache.amoro.server.persistence.BucketAssignMeta;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.BucketAssignMapper;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.amoro.utils.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database-based implementation of BucketAssignStore. Stores bucket ID assignments in database
 * table bucket_assign.
 */
public class DatabaseBucketAssignStore extends PersistentBase implements BucketAssignStore {

  private static final Logger LOG = LoggerFactory.getLogger(DatabaseBucketAssignStore.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final TypeReference<List<String>> LIST_STRING_TYPE =
      new TypeReference<List<String>>() {};

  private final String clusterName;

  public DatabaseBucketAssignStore(String clusterName) {
    this.clusterName = clusterName;
  }

  @Override
  public void saveAssignments(AmsServerInfo nodeInfo, List<String> bucketIds) throws Exception {
    String nodeKey = getNodeKey(nodeInfo);
    String bucketIdsJson = JacksonUtil.toJSONString(bucketIds);
    long currentTime = System.currentTimeMillis();

    BucketAssignMeta meta = new BucketAssignMeta(clusterName, nodeKey, bucketIdsJson, currentTime);
    try {
      doAs(BucketAssignMapper.class, mapper -> mapper.upsertAssignments(meta));
      LOG.debug("Saved bucket assignments for node {}: {}", nodeKey, bucketIds);
    } catch (Exception e) {
      LOG.error("Failed to save bucket assignments for node {}", nodeKey, e);
      throw e;
    }
  }

  @Override
  public List<String> getAssignments(AmsServerInfo nodeInfo) throws Exception {
    String nodeKey = getNodeKey(nodeInfo);
    try {
      BucketAssignMeta meta =
          getAs(BucketAssignMapper.class, mapper -> mapper.selectAssignments(clusterName, nodeKey));
      if (meta == null || meta.getBucketIdsJson() == null || meta.getBucketIdsJson().isEmpty()) {
        return new ArrayList<>();
      }
      return OBJECT_MAPPER.readValue(meta.getBucketIdsJson(), LIST_STRING_TYPE);
    } catch (Exception e) {
      LOG.error("Failed to get bucket assignments for node {}", nodeKey, e);
      throw e;
    }
  }

  @Override
  public void removeAssignments(AmsServerInfo nodeInfo) throws Exception {
    String nodeKey = getNodeKey(nodeInfo);
    try {
      doAs(BucketAssignMapper.class, mapper -> mapper.deleteAssignments(clusterName, nodeKey));
      LOG.debug("Removed bucket assignments for node {}", nodeKey);
    } catch (Exception e) {
      LOG.error("Failed to remove bucket assignments for node {}", nodeKey, e);
      throw e;
    }
  }

  @Override
  public Map<AmsServerInfo, List<String>> getAllAssignments() throws Exception {
    Map<AmsServerInfo, List<String>> allAssignments = new HashMap<>();
    try {
      List<BucketAssignMeta> metas =
          getAs(BucketAssignMapper.class, mapper -> mapper.selectAllAssignments(clusterName));
      for (BucketAssignMeta meta : metas) {
        try {
          AmsServerInfo nodeInfo = parseNodeKey(meta.getNodeKey());
          if (meta.getBucketIdsJson() != null && !meta.getBucketIdsJson().isEmpty()) {
            List<String> bucketIds =
                OBJECT_MAPPER.readValue(meta.getBucketIdsJson(), LIST_STRING_TYPE);
            if (!bucketIds.isEmpty()) {
              allAssignments.put(nodeInfo, bucketIds);
            }
          }
        } catch (Exception e) {
          LOG.warn("Failed to parse node key or bucket IDs: {}", meta.getNodeKey(), e);
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to get all bucket assignments", e);
      throw e;
    }
    return allAssignments;
  }

  @Override
  public long getLastUpdateTime(AmsServerInfo nodeInfo) throws Exception {
    String nodeKey = getNodeKey(nodeInfo);
    try {
      BucketAssignMeta meta =
          getAs(BucketAssignMapper.class, mapper -> mapper.selectAssignments(clusterName, nodeKey));
      if (meta == null || meta.getLastUpdateTime() == null) {
        return 0;
      }
      return meta.getLastUpdateTime();
    } catch (Exception e) {
      LOG.error("Failed to get last update time for node {}", nodeKey, e);
      throw e;
    }
  }

  @Override
  public void updateLastUpdateTime(AmsServerInfo nodeInfo) throws Exception {
    String nodeKey = getNodeKey(nodeInfo);
    long currentTime = System.currentTimeMillis();
    try {
      doAs(
          BucketAssignMapper.class,
          mapper -> mapper.updateLastUpdateTime(clusterName, nodeKey, currentTime));
    } catch (Exception e) {
      LOG.error("Failed to update last update time for node {}", nodeKey, e);
      throw e;
    }
  }

  /**
   * Get node key for matching nodes. Uses host:thriftBindPort format, consistent with
   * ZkBucketAssignStore.getNodeKey().
   */
  private String getNodeKey(AmsServerInfo nodeInfo) {
    return nodeInfo.getHost() + ":" + nodeInfo.getThriftBindPort();
  }

  /**
   * Parse node key to AmsServerInfo.
   *
   * @param nodeKey node key in format "host:thriftBindPort"
   * @return AmsServerInfo
   */
  private AmsServerInfo parseNodeKey(String nodeKey) {
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
