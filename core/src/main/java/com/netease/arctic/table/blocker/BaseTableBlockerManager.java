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
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
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
  private final long blockerTimeout;
  
  private final Map<String, Blocker> validBlockers = Maps.newHashMap();
  private volatile boolean stop;
  private Thread renewWorker;

  public BaseTableBlockerManager(TableIdentifier tableIdentifier, AmsClient client, long blockerTimeout) {
    this.tableIdentifier = tableIdentifier;
    this.client = client;
    this.blockerTimeout = blockerTimeout;
  }

  @Override
  public Blocker block(List<BlockableOperation> operations) throws OperationConflictException {
    try {
      BaseBlocker blocker = BaseBlocker.of(client.block(tableIdentifier.buildTableIdentifier(), operations));
      validBlockers.put(blocker.blockerId(), blocker);
      if (renewWorker == null) {
        startRenewWorker();
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
      validBlockers.remove(blocker.blockerId());
      if (validBlockers.isEmpty()) {
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
  
  private void checkValidBlockers() {
    List<String> blockerIds = getBlockers().stream().map(Blocker::blockerId).collect(Collectors.toList());
    validBlockers.keySet().removeIf(blockerId -> !blockerIds.contains(blockerId));
  }

  private synchronized void startRenewWorker() {
    if (renewWorker == null) {
      this.renewWorker = new Thread(() -> {
        try {
          while (!stop) {
            Thread.sleep(blockerTimeout / 5);
            LOG.info("start renew blocker of {}", tableIdentifier);
            checkValidBlockers();
            if (validBlockers.isEmpty()) {
              LOG.info("no valid blockers, stop renew blockers of {}", tableIdentifier);
              break;
            }
            List<Blocker> blockers = new ArrayList<>(validBlockers.values());
            for (Blocker blocker : blockers) {
              try {
                client.renewBlocker(tableIdentifier.buildTableIdentifier(), blocker.blockerId());
                LOG.info("renew blocker {} success of {}", blocker.blockerId(), tableIdentifier);
              } catch (Throwable t) {
                LOG.error("failed to renew block {} of table {}, ignore and continue", blocker.blockerId(),
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
      this.renewWorker.start();
      LOG.info("start renew worker");
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


  public static TableBlockerManager build(TableIdentifier tableIdentifier, AmsClient amsClient, long blockerTimeout) {
    // TODO 
    // return new BaseTableBlockerManager(tableIdentifier, amsClient, blockerTimeout);
    return new DummyTableBlockerManager(tableIdentifier);
  }

  // TODO remove
  public static class DummyTableBlockerManager implements TableBlockerManager {
    private static final Blocker TEMP_BLOCKER = new BaseBlocker(
        "temp",
        Collections.emptyList(),
        System.currentTimeMillis(),
        System.currentTimeMillis() +
            TableProperties.TABLE_BLOCKER_TIMEOUT_DEFAULT,
        Maps.newHashMap());

    private final TableIdentifier tableIdentifier;

    public DummyTableBlockerManager(TableIdentifier tableIdentifier) {
      this.tableIdentifier = tableIdentifier;
    }

    @Override
    public TableIdentifier tableIdentifier() {
      return tableIdentifier;
    }

    @Override
    public Blocker block(List<BlockableOperation> operations) throws OperationConflictException {
      return TEMP_BLOCKER;
    }

    @Override
    public void release(Blocker blocker) {

    }

    @Override
    public List<Blocker> getBlockers() {
      return Collections.singletonList(TEMP_BLOCKER);
    }
  }
}
