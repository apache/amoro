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

import org.apache.amoro.client.OptimizingClientPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages multiple AMS nodes in master-slave mode for DB-backed HA. Discovers the node list by
 * calling {@code getOptimizingNodeUrls()} via the Thrift API on any reachable AMS node, then caches
 * and periodically refreshes the list. This is the DB-mode counterpart of {@link AmsNodeManager}
 * (which reads node addresses directly from ZooKeeper).
 */
public class ThriftAmsNodeManager implements Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(ThriftAmsNodeManager.class);
  private static final long REFRESH_INTERVAL_MS = TimeUnit.SECONDS.toMillis(30);

  private final String initialAmsUrl;
  private final AtomicReference<List<String>> amsUrls =
      new AtomicReference<>(Collections.emptyList());
  private volatile long lastRefreshTime = 0;
  private transient Object refreshLock;

  public ThriftAmsNodeManager(String initialAmsUrl) {
    this.initialAmsUrl = initialAmsUrl;
    this.refreshLock = new Object();
    refreshNodes();
  }

  /** Get all available AMS URLs. Falls back to the initial URL when the list is empty. */
  public List<String> getAllAmsUrls() {
    refreshNodesIfNeeded();
    List<String> current = amsUrls.get();
    if (current.isEmpty()) {
      return Collections.singletonList(initialAmsUrl);
    }
    return new ArrayList<>(current);
  }

  /** Manually refresh the node list from the AMS Thrift API. */
  public void refreshNodes() {
    synchronized (getLock()) {
      refreshNodesInternal();
      lastRefreshTime = System.currentTimeMillis();
    }
  }

  private void refreshNodesIfNeeded() {
    long now = System.currentTimeMillis();
    if (now - lastRefreshTime > REFRESH_INTERVAL_MS) {
      synchronized (getLock()) {
        if (now - lastRefreshTime > REFRESH_INTERVAL_MS) {
          refreshNodesInternal();
          lastRefreshTime = now;
        }
      }
    }
  }

  private void refreshNodesInternal() {
    try {
      List<String> nodeUrls =
          OptimizingClientPools.getClient(initialAmsUrl).getOptimizingNodeUrls();
      if (nodeUrls != null && !nodeUrls.isEmpty()) {
        amsUrls.set(new ArrayList<>(nodeUrls));
        LOG.info(
            "Refreshed AMS nodes via Thrift ({}), found {} nodes: {}",
            initialAmsUrl,
            nodeUrls.size(),
            nodeUrls);
      } else {
        LOG.warn("No AMS nodes returned by getOptimizingNodeUrls() from {}", initialAmsUrl);
        amsUrls.set(Collections.emptyList());
      }
    } catch (Exception e) {
      LOG.warn("Failed to refresh AMS nodes from {} via Thrift", initialAmsUrl, e);
    }
  }

  private Object getLock() {
    if (refreshLock == null) {
      refreshLock = new Object();
    }
    return refreshLock;
  }
}
