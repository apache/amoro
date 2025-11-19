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
import org.apache.amoro.client.AmsThriftUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
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
public class AmsNodeManager implements Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(AmsNodeManager.class);
  private static final Pattern ZOOKEEPER_PATTERN = Pattern.compile("zookeeper://(\\S+)/([\\w-]+)");
  private static final long REFRESH_INTERVAL_MS =
      TimeUnit.SECONDS.toMillis(30); // Refresh every 30 seconds

  private final String zkServerAddress;
  private final AtomicReference<List<String>> amsUrls =
      new AtomicReference<>(Collections.emptyList());
  private volatile long lastRefreshTime = 0;
  private transient Object refreshLock;

  public AmsNodeManager(String amsUrl) {
    Matcher m = ZOOKEEPER_PATTERN.matcher(amsUrl);
    if (!m.matches()) {
      throw new IllegalArgumentException(
          "AmsNodeManager only supports ZooKeeper URL format: zookeeper://host:port/cluster");
    }
    zkServerAddress = amsUrl;
    refreshLock = new Object();
    refreshNodes();
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
      LOG.info("Refreshing nodes from {}", zkServerAddress);
      List<String> nodeUrls = new ArrayList<>();
      List<AmsServerInfo> amsServerInfos = AmsThriftUrl.parseMasterSlaveAmsNodes(zkServerAddress);
      LOG.info("Refreshing nodes from {}", amsServerInfos);
      for (AmsServerInfo amsServerInfo : amsServerInfos) {
        nodeUrls.add(buildAmsUrl(amsServerInfo));
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

  private String buildAmsUrl(AmsServerInfo serverInfo) {
    return String.format("thrift://%s:%d", serverInfo.getHost(), serverInfo.getThriftBindPort());
  }
}
