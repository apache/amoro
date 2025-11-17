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

package org.apache.amoro.optimizer.common;

import org.apache.amoro.client.AmsServerInfo;
import org.apache.amoro.properties.AmsHAProperties;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.CuratorFramework;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.amoro.shade.zookeeper3.org.apache.zookeeper.client.ZKClientConfig;
import org.apache.amoro.utils.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages multiple AMS nodes in master-slave mode. Fetches node list from ZooKeeper and provides
 * failover support.
 */
public class AmsNodeManager {
  private static final Logger LOG = LoggerFactory.getLogger(AmsNodeManager.class);
  private static final Pattern ZOOKEEPER_PATTERN = Pattern.compile("zookeeper://(\\S+)/([\\w-]+)");
  private static final long REFRESH_INTERVAL_MS =
      TimeUnit.SECONDS.toMillis(30); // Refresh every 30 seconds

  private final String zkServerAddress;
  private final String cluster;
  private final AtomicReference<List<String>> amsUrls =
      new AtomicReference<>(Collections.emptyList());
  private volatile long lastRefreshTime = 0;
  private final Object refreshLock = new Object();
  private volatile CuratorFramework zkClient;

  public AmsNodeManager(String amsUrl) {
    Matcher m = ZOOKEEPER_PATTERN.matcher(amsUrl);
    if (!m.matches()) {
      throw new IllegalArgumentException(
          "AmsNodeManager only supports ZooKeeper URL format: zookeeper://host:port/cluster");
    }
    if (m.group(1).contains("/")) {
      this.zkServerAddress = m.group(1).substring(0, m.group(1).indexOf("/"));
      this.cluster = m.group(1).substring(m.group(1).indexOf("/") + 1);
    } else {
      this.zkServerAddress = m.group(1);
      this.cluster = m.group(2);
    }
    initZkClient();
    refreshNodes();
  }

  private void initZkClient() {
    if (zkClient == null) {
      synchronized (this) {
        if (zkClient == null) {
          ZKClientConfig zkClientConfig = new ZKClientConfig();
          zkClientConfig.setProperty(ZKClientConfig.ENABLE_CLIENT_SASL_KEY, "false");
          ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3, 5000);
          zkClient =
              CuratorFrameworkFactory.builder()
                  .connectString(zkServerAddress)
                  .sessionTimeoutMs(5000)
                  .connectionTimeoutMs(5000)
                  .retryPolicy(retryPolicy)
                  .build();
          zkClient.start();
        }
      }
    }
  }

  /** Get all available AMS URLs. */
  public List<String> getAllAmsUrls() {
    refreshNodesIfNeeded();
    return new ArrayList<>(amsUrls.get());
  }

  /** Manually refresh node list from ZooKeeper. */
  public void refreshNodes() {
    synchronized (refreshLock) {
      refreshNodesInternal();
      lastRefreshTime = System.currentTimeMillis();
    }
  }

  /** Refresh node list from ZooKeeper if needed. */
  private void refreshNodesIfNeeded() {
    long now = System.currentTimeMillis();
    if (now - lastRefreshTime > REFRESH_INTERVAL_MS) {
      synchronized (refreshLock) {
        if (now - lastRefreshTime > REFRESH_INTERVAL_MS) {
          refreshNodesInternal();
          lastRefreshTime = now;
        }
      }
    }
  }

  /** Refresh node list from ZooKeeper (internal implementation). */
  private void refreshNodesInternal() {
    try {
      initZkClient();
      List<String> nodeUrls = new ArrayList<>();
      String nodesPath = AmsHAProperties.getNodesPath(cluster);

      // Get all nodes from ZooKeeper
      try {
        if (zkClient.checkExists().forPath(nodesPath) != null) {
          List<String> nodeKeys = zkClient.getChildren().forPath(nodesPath);
          for (String nodeKey : nodeKeys) {
            try {
              String nodeDataPath = nodesPath + "/" + nodeKey;
              byte[] data = zkClient.getData().forPath(nodeDataPath);
              if (data != null && data.length > 0) {
                String nodeInfoJson = new String(data, java.nio.charset.StandardCharsets.UTF_8);
                AmsServerInfo nodeInfo = JacksonUtil.parseObject(nodeInfoJson, AmsServerInfo.class);
                if (nodeInfo != null && nodeInfo.getThriftBindPort() != null) {
                  String nodeUrl = buildAmsUrl(nodeInfo);
                  nodeUrls.add(nodeUrl);
                }
              }
            } catch (Exception e) {
              LOG.debug("Failed to parse node info for key: {}", nodeKey, e);
            }
          }
        }
      } catch (Exception e) {
        LOG.debug("Failed to get nodes from ZooKeeper path: {}", nodesPath, e);
      }

      // Fallback: try to get master node if no nodes found
      if (nodeUrls.isEmpty()) {
        AmsServerInfo masterNode = getMasterNode();
        if (masterNode != null) {
          String masterUrl = buildAmsUrl(masterNode);
          nodeUrls.add(masterUrl);
          LOG.debug("Using master node as fallback: {}", masterUrl);
        }
      }

      if (!nodeUrls.isEmpty()) {
        amsUrls.set(nodeUrls);
        LOG.info("Refreshed AMS nodes, found {} nodes: {}", nodeUrls.size(), nodeUrls);
      } else {
        LOG.warn("No AMS nodes found in ZooKeeper");
        amsUrls.set(Collections.emptyList());
      }
    } catch (Exception e) {
      LOG.error("Failed to refresh AMS nodes from ZooKeeper", e);
      // Keep existing nodes on error
    }
  }

  private AmsServerInfo getMasterNode() {
    try {
      initZkClient();
      String masterPath = AmsHAProperties.getOptimizingServiceMasterPath(cluster);
      if (zkClient.checkExists().forPath(masterPath) != null) {
        byte[] data = zkClient.getData().forPath(masterPath);
        if (data != null && data.length > 0) {
          String nodeInfoJson = new String(data, java.nio.charset.StandardCharsets.UTF_8);
          return JacksonUtil.parseObject(nodeInfoJson, AmsServerInfo.class);
        }
      }
    } catch (Exception e) {
      LOG.debug("Failed to get master node from ZooKeeper", e);
    }
    return null;
  }

  private String buildAmsUrl(AmsServerInfo serverInfo) {
    return String.format("thrift://%s:%d", serverInfo.getHost(), serverInfo.getThriftBindPort());
  }
}
