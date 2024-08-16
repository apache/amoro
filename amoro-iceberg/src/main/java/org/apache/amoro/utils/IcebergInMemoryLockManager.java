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

package org.apache.amoro.utils;

import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.iceberg.util.LockManagers;
import org.apache.iceberg.util.Tasks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Copy from Iceberg {@link LockManagers.InMemoryLockManager}, fix the NullPointerException when
 * release lock. After this <a href="https://github.com/apache/iceberg/issues/4550">issue</a> is
 * fixed, this class can be removed.
 */
public class IcebergInMemoryLockManager extends LockManagers.BaseLockManager {

  private static final Logger LOG = LoggerFactory.getLogger(IcebergInMemoryLockManager.class);

  private static final Map<String, InMemoryLockContent> LOCKS = Maps.newConcurrentMap();
  private static final Map<String, ScheduledFuture<?>> HEARTBEATS = Maps.newHashMap();

  private static final IcebergInMemoryLockManager INSTANCE =
      new IcebergInMemoryLockManager(Maps.newHashMap());

  public static IcebergInMemoryLockManager instance() {
    return INSTANCE;
  }

  IcebergInMemoryLockManager(Map<String, String> properties) {
    initialize(properties);
  }

  @VisibleForTesting
  void acquireOnce(String entityId, String ownerId) {
    InMemoryLockContent content = LOCKS.get(entityId);
    if (content != null && content.expireMs() > System.currentTimeMillis()) {
      throw new IllegalStateException(
          String.format(
              "Lock for %s currently held by %s, expiration: %s",
              entityId, content.ownerId(), content.expireMs()));
    }

    long expiration = System.currentTimeMillis() + heartbeatTimeoutMs();
    boolean succeed;
    if (content == null) {
      InMemoryLockContent previous =
          LOCKS.putIfAbsent(entityId, new InMemoryLockContent(ownerId, expiration));
      succeed = previous == null;
    } else {
      succeed = LOCKS.replace(entityId, content, new InMemoryLockContent(ownerId, expiration));
    }

    if (succeed) {
      // cleanup old heartbeat
      if (HEARTBEATS.containsKey(entityId)) {
        HEARTBEATS.remove(entityId).cancel(false);
      }

      HEARTBEATS.put(
          entityId,
          scheduler()
              .scheduleAtFixedRate(
                  () -> {
                    InMemoryLockContent lastContent = LOCKS.get(entityId);
                    try {
                      long newExpiration = System.currentTimeMillis() + heartbeatTimeoutMs();
                      LOCKS.replace(
                          entityId, lastContent, new InMemoryLockContent(ownerId, newExpiration));
                    } catch (NullPointerException e) {
                      throw new RuntimeException(
                          "Cannot heartbeat to a deleted lock " + entityId, e);
                    }
                  },
                  0,
                  heartbeatIntervalMs(),
                  TimeUnit.MILLISECONDS));

    } else {
      throw new IllegalStateException("Unable to acquire lock " + entityId);
    }
  }

  @Override
  public boolean acquire(String entityId, String ownerId) {
    try {
      Tasks.foreach(entityId)
          .retry(Integer.MAX_VALUE - 1)
          .onlyRetryOn(IllegalStateException.class)
          .throwFailureWhenFinished()
          .exponentialBackoff(acquireIntervalMs(), acquireIntervalMs(), acquireTimeoutMs(), 1)
          .run(id -> acquireOnce(id, ownerId));
      return true;
    } catch (IllegalStateException e) {
      return false;
    }
  }

  @Override
  public boolean release(String entityId, String ownerId) {
    InMemoryLockContent currentContent = LOCKS.get(entityId);
    if (currentContent == null) {
      LOG.error("Cannot find lock for entity {}", entityId);
      return false;
    }

    if (!currentContent.ownerId().equals(ownerId)) {
      LOG.error(
          "Cannot unlock {} by {}, current owner: {}", entityId, ownerId, currentContent.ownerId());
      return false;
    }

    // where NPE happens
    Optional.ofNullable(HEARTBEATS.remove(entityId)).ifPresent(future -> future.cancel(false));
    LOCKS.remove(entityId);
    return true;
  }

  @Override
  public void close() {
    HEARTBEATS.values().forEach(future -> future.cancel(false));
    HEARTBEATS.clear();
    LOCKS.clear();
  }

  private static class InMemoryLockContent {
    private final String ownerId;
    private final long expireMs;

    InMemoryLockContent(String ownerId, long expireMs) {
      this.ownerId = ownerId;
      this.expireMs = expireMs;
    }

    public long expireMs() {
      return expireMs;
    }

    public String ownerId() {
      return ownerId;
    }
  }
}
