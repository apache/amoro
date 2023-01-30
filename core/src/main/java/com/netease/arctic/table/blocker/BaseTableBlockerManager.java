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

package com.netease.arctic.table.blocker;

import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.api.BlockableOperation;
import com.netease.arctic.ams.api.OperationConflictException;
import com.netease.arctic.table.TableIdentifier;
import org.apache.iceberg.relocated.com.google.common.annotations.VisibleForTesting;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Base {@link TableBlockerManager} implementation.
 */
public class BaseTableBlockerManager implements TableBlockerManager {
  private static final Logger LOG = LoggerFactory.getLogger(BaseTableBlockerManager.class);

  private final TableIdentifier tableIdentifier;
  private final AmsClient client;

  private final Map<String, Blocker> toRenewBlockers = Maps.newHashMap();
  private volatile boolean stop = true;
  private Thread renewWorker;

  public BaseTableBlockerManager(TableIdentifier tableIdentifier, AmsClient client) {
    this.tableIdentifier = tableIdentifier;
    this.client = client;
  }

  public static TableBlockerManager build(TableIdentifier tableIdentifier, AmsClient amsClient) {
    return new BaseTableBlockerManager(tableIdentifier, amsClient);
  }

  @Override
  public Blocker block(List<BlockableOperation> operations, Map<String, String> properties)
      throws OperationConflictException {
    try {
      BaseBlocker blocker =
          BaseBlocker.of(client.block(tableIdentifier.buildTableIdentifier(), operations, properties));
      toRenewBlockers.put(blocker.blockerId(), blocker);
      if (renewWorker == null) {
        long timeout = blocker.getExpirationTime() - blocker.getCreateTime();
        startRenewWorker(timeout / 5);
      }
      return blocker;
    } catch (OperationConflictException e) {
      throw e;
    } catch (TException e) {
      throw new IllegalStateException("failed to block table " + tableIdentifier + " with " + operations, e);
    }
  }

  @Override
  public void release(Blocker blocker) {
    try {
      client.releaseBlocker(tableIdentifier.buildTableIdentifier(), blocker.blockerId());
      toRenewBlockers.remove(blocker.blockerId());
      if (toRenewBlockers.isEmpty()) {
        stopRenewWorker();
      }
    } catch (TException e) {
      throw new IllegalStateException("failed to release " + tableIdentifier + "'s blocker " + blocker.blockerId(), e);
    }
  }

  @Override
  public List<Blocker> getBlockers() {
    try {
      return client.getBlockers(tableIdentifier.buildTableIdentifier())
          .stream().map(BaseBlocker::of).collect(Collectors.toList());
    } catch (TException e) {
      throw new IllegalStateException("failed to get blockers of " + tableIdentifier, e);
    }
  }

  public TableIdentifier tableIdentifier() {
    return tableIdentifier;
  }

  private void refreshToRenewBlockers() {
    List<String> blockerIds = getBlockers().stream().map(Blocker::blockerId).collect(Collectors.toList());
    toRenewBlockers.keySet().removeIf(blockerId -> !blockerIds.contains(blockerId));
  }

  private synchronized void startRenewWorker(long interval) {
    if (renewWorker == null) {
      this.renewWorker = new Thread(() -> {
        try {
          while (!stop) {
            Thread.sleep(interval);
            LOG.info("start renew blocker of {}", tableIdentifier);
            refreshToRenewBlockers();
            if (toRenewBlockers.isEmpty()) {
              LOG.info("no valid blockers, stop renew blockers of {}", tableIdentifier);
              break;
            }
            List<Blocker> blockers = new ArrayList<>(toRenewBlockers.values());
            for (Blocker blocker : blockers) {
              try {
                client.renewBlocker(tableIdentifier.buildTableIdentifier(), blocker.blockerId());
                LOG.info("renew blocker {} success of {}", blocker.blockerId(), tableIdentifier);
              } catch (Throwable t) {
                LOG.warn("failed to renew block {} of table {}, ignore and continue", blocker.blockerId(),
                    tableIdentifier, t);
              }
            }
          }
        } catch (InterruptedException e) {
          LOG.info(Thread.currentThread() + " interrupted");
        } finally {
          LOG.info(Thread.currentThread() + " exit");
        }
      }, "renew-blocker-thread");
      this.stop = false;
      this.renewWorker.start();
      LOG.info("start renew worker with interval {}ms", interval);
    }
  }

  private synchronized void stopRenewWorker() {
    this.stop = true;
    if (renewWorker != null) {
      renewWorker.interrupt();
      renewWorker = null;
    }
    LOG.info("stop renew worker");
  }

  @VisibleForTesting
  Map<String, Blocker> getToRenewBlockers() {
    return toRenewBlockers;
  }

  @VisibleForTesting
  boolean isStop() {
    return stop;
  }

  @VisibleForTesting
  Thread getRenewWorker() {
    return renewWorker;
  }
}
