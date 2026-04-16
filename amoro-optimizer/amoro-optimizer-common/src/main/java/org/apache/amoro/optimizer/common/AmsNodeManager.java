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
  private transient volatile Object refreshLock;

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

  private Object getRefreshLock() {
    if (refreshLock == null) {
      synchronized (this) {
        if (refreshLock == null) {
          refreshLock = new Object();
        }
      }
    }
    return refreshLock;
  }

  /** Get all available AMS URLs. */
  public List<String> getAllAmsUrls() {
    refreshNodesIfNeeded();
    return new ArrayList<>(amsUrls.get());
  }

  /** Manually refresh node list from ZooKeeper. */
  public void refreshNodes() {
    synchronized (getRefreshLock()) {
      refreshNodesInternal();
      lastRefreshTime = System.currentTimeMillis();
    }
  }

  /** Refresh node list from ZooKeeper if needed. */
  private void refreshNodesIfNeeded() {
    if (System.currentTimeMillis() - lastRefreshTime > REFRESH_INTERVAL_MS) {
      synchronized (getRefreshLock()) {
        if (System.currentTimeMillis() - lastRefreshTime > REFRESH_INTERVAL_MS) {
          refreshNodesInternal();
          lastRefreshTime = System.currentTimeMillis();
        }
      }
    }
  }

  /** Refresh node list from ZooKeeper (internal implementation). */
  private void refreshNodesInternal() {
    try {
      LOG.debug("Refreshing AMS nodes from ZooKeeper: {}", zkServerAddress);
      List<String> nodeUrls = new ArrayList<>();
      List<AmsServerInfo> amsServerInfos = AmsThriftUrl.parseMasterSlaveAmsNodes(zkServerAddress);
      for (AmsServerInfo amsServerInfo : amsServerInfos) {
        nodeUrls.add(buildAmsUrl(amsServerInfo));
      }

      List<String> previousUrls = amsUrls.get();
      if (!nodeUrls.isEmpty()) {
        amsUrls.set(nodeUrls);
        if (!nodeUrls.equals(previousUrls)) {
          LOG.info(
              "AMS node list updated: {} -> {} ({} nodes)",
              previousUrls.isEmpty() ? "(empty)" : previousUrls,
              nodeUrls,
              nodeUrls.size());
        } else {
          LOG.debug("AMS node list unchanged: {} ({} nodes)", nodeUrls, nodeUrls.size());
        }
      } else {
        amsUrls.set(Collections.emptyList());
        if (!previousUrls.isEmpty()) {
          LOG.warn(
              "All AMS nodes disappeared from ZooKeeper (was: {}), "
                  + "will fall back to configured URL until nodes re-register",
              previousUrls);
        } else {
          LOG.warn("No AMS nodes found in ZooKeeper path for {}", zkServerAddress);
        }
      }
    } catch (Exception e) {
      LOG.error(
          "Failed to refresh AMS nodes from ZooKeeper, keeping previous list: {}",
          amsUrls.get(),
          e);
    }
  }

  private String buildAmsUrl(AmsServerInfo serverInfo) {
    return String.format("thrift://%s:%d", serverInfo.getHost(), serverInfo.getThriftBindPort());
  }
}
