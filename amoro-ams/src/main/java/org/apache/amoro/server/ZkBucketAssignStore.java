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
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.CuratorFramework;
import org.apache.amoro.shade.zookeeper3.org.apache.zookeeper.CreateMode;
import org.apache.amoro.shade.zookeeper3.org.apache.zookeeper.KeeperException;
import org.apache.amoro.utils.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ZooKeeper-based implementation of BucketAssignStore. Stores bucket ID assignments in ZooKeeper
 * with the following structure: /{namespace}/amoro/ams/bucket-assignments/{nodeKey}/assignments -
 * bucket IDs /{namespace}/amoro/ams/bucket-assignments/{nodeKey}/last-update-time - timestamp
 */
public class ZkBucketAssignStore implements BucketAssignStore {

  private static final Logger LOG = LoggerFactory.getLogger(ZkBucketAssignStore.class);
  private static final String ASSIGNMENTS_SUFFIX = "/assignments";
  private static final String LAST_UPDATE_TIME_SUFFIX = "/last-update-time";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final TypeReference<List<String>> LIST_STRING_TYPE =
      new TypeReference<List<String>>() {};

  private final CuratorFramework zkClient;
  private final String assignmentsBasePath;

  public ZkBucketAssignStore(CuratorFramework zkClient, String clusterName) {
    this.zkClient = zkClient;
    this.assignmentsBasePath = AmsHAProperties.getBucketAssignmentsPath(clusterName);
    try {
      createPathIfNeeded(assignmentsBasePath);
    } catch (Exception e) {
      LOG.error("Failed to create bucket assignments path", e);
      throw new RuntimeException("Failed to initialize ZkBucketAssignStore", e);
    }
  }

  @Override
  public void saveAssignments(AmsServerInfo nodeInfo, List<String> bucketIds) throws Exception {
    String nodeKey = getNodeKey(nodeInfo);
    String assignmentsPath = assignmentsBasePath + "/" + nodeKey + ASSIGNMENTS_SUFFIX;
    String assignmentsJson = JacksonUtil.toJSONString(bucketIds);
    try {
      if (zkClient.checkExists().forPath(assignmentsPath) != null) {
        zkClient
            .setData()
            .forPath(assignmentsPath, assignmentsJson.getBytes(StandardCharsets.UTF_8));
      } else {
        zkClient
            .create()
            .creatingParentsIfNeeded()
            .withMode(CreateMode.PERSISTENT)
            .forPath(assignmentsPath, assignmentsJson.getBytes(StandardCharsets.UTF_8));
      }
      updateLastUpdateTime(nodeInfo);
      LOG.debug("Saved bucket assignments for node {}: {}", nodeKey, bucketIds);
    } catch (Exception e) {
      LOG.error("Failed to save bucket assignments for node {}", nodeKey, e);
      throw e;
    }
  }

  @Override
  public List<String> getAssignments(AmsServerInfo nodeInfo) throws Exception {
    String nodeKey = getNodeKey(nodeInfo);
    String assignmentsPath = assignmentsBasePath + "/" + nodeKey + ASSIGNMENTS_SUFFIX;
    try {
      if (zkClient.checkExists().forPath(assignmentsPath) == null) {
        return new ArrayList<>();
      }
      byte[] data = zkClient.getData().forPath(assignmentsPath);
      if (data == null || data.length == 0) {
        return new ArrayList<>();
      }
      String assignmentsJson = new String(data, StandardCharsets.UTF_8);
      return OBJECT_MAPPER.readValue(assignmentsJson, LIST_STRING_TYPE);
    } catch (KeeperException.NoNodeException e) {
      return new ArrayList<>();
    } catch (Exception e) {
      LOG.error("Failed to get bucket assignments for node {}", nodeKey, e);
      throw e;
    }
  }

  @Override
  public void removeAssignments(AmsServerInfo nodeInfo) throws Exception {
    String nodeKey = getNodeKey(nodeInfo);
    String nodePath = assignmentsBasePath + "/" + nodeKey;
    try {
      if (zkClient.checkExists().forPath(nodePath) != null) {
        zkClient.delete().deletingChildrenIfNeeded().forPath(nodePath);
        LOG.debug("Removed bucket assignments for node {}", nodeKey);
      }
    } catch (KeeperException.NoNodeException e) {
      // Already deleted, ignore
    } catch (Exception e) {
      LOG.error("Failed to remove bucket assignments for node {}", nodeKey, e);
      throw e;
    }
  }

  @Override
  public Map<AmsServerInfo, List<String>> getAllAssignments() throws Exception {
    Map<AmsServerInfo, List<String>> allAssignments = new HashMap<>();
    try {
      if (zkClient.checkExists().forPath(assignmentsBasePath) == null) {
        return allAssignments;
      }
      List<String> nodeKeys = zkClient.getChildren().forPath(assignmentsBasePath);
      for (String nodeKey : nodeKeys) {
        try {
          AmsServerInfo nodeInfo = parseNodeKey(nodeKey);
          List<String> bucketIds = getAssignments(nodeInfo);
          if (!bucketIds.isEmpty()) {
            allAssignments.put(nodeInfo, bucketIds);
          }
        } catch (Exception e) {
          LOG.warn("Failed to parse node key or get assignments: {}", nodeKey, e);
        }
      }
    } catch (KeeperException.NoNodeException e) {
      // Path doesn't exist, return empty map
    } catch (Exception e) {
      LOG.error("Failed to get all bucket assignments", e);
      throw e;
    }
    return allAssignments;
  }

  @Override
  public long getLastUpdateTime(AmsServerInfo nodeInfo) throws Exception {
    String nodeKey = getNodeKey(nodeInfo);
    String timePath = assignmentsBasePath + "/" + nodeKey + LAST_UPDATE_TIME_SUFFIX;
    try {
      if (zkClient.checkExists().forPath(timePath) == null) {
        return 0;
      }
      byte[] data = zkClient.getData().forPath(timePath);
      if (data == null || data.length == 0) {
        return 0;
      }
      return Long.parseLong(new String(data, StandardCharsets.UTF_8));
    } catch (KeeperException.NoNodeException e) {
      return 0;
    } catch (Exception e) {
      LOG.error("Failed to get last update time for node {}", nodeKey, e);
      throw e;
    }
  }

  @Override
  public void updateLastUpdateTime(AmsServerInfo nodeInfo) throws Exception {
    String nodeKey = getNodeKey(nodeInfo);
    String timePath = assignmentsBasePath + "/" + nodeKey + LAST_UPDATE_TIME_SUFFIX;
    long currentTime = System.currentTimeMillis();
    String timeStr = String.valueOf(currentTime);
    try {
      if (zkClient.checkExists().forPath(timePath) != null) {
        zkClient.setData().forPath(timePath, timeStr.getBytes(StandardCharsets.UTF_8));
      } else {
        zkClient
            .create()
            .creatingParentsIfNeeded()
            .withMode(CreateMode.PERSISTENT)
            .forPath(timePath, timeStr.getBytes(StandardCharsets.UTF_8));
      }
    } catch (Exception e) {
      LOG.error("Failed to update last update time for node {}", nodeKey, e);
      throw e;
    }
  }

  private String getNodeKey(AmsServerInfo nodeInfo) {
    return nodeInfo.getHost() + ":" + nodeInfo.getThriftBindPort();
  }

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

  private void createPathIfNeeded(String path) throws Exception {
    try {
      zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
    } catch (KeeperException.NodeExistsException e) {
      // ignore
    }
  }
}
