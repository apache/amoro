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

package org.apache.amoro.io.reader;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** A common implementation of {@link org.apache.iceberg.data.DeleteLoader}. */
public class DeleteCache {
  private static final Logger LOG = LoggerFactory.getLogger(DeleteCache.class);
  private static final Duration DEFAULT_TIME_OUT = Duration.ofMinutes(10);
  private static final long DEFAULT_MAX_SIZE = 64 * 1024 * 1024; // 128MB
  private static final long DEFAULT_TOTAL_SIZE = 128 * 1024 * 1024; // 64MB
  private static final int MAX_GROUPS = 5;
  private static volatile DeleteCache INSTANCE;

  private final Duration timeout;
  private final long maxEntrySize;
  private final long maxTotalSize;
  private final List<String> groups = new CopyOnWriteArrayList<>();
  private volatile Cache<String, CacheValue> state;

  private DeleteCache(Duration timeout, long maxEntrySize, long maxTotalSize) {
    this.timeout = timeout;
    this.maxEntrySize = maxEntrySize;
    this.maxTotalSize = maxTotalSize;
  }

  public static DeleteCache getInstance() {
    if (INSTANCE == null) {
      initialInstance(DEFAULT_TIME_OUT, DEFAULT_MAX_SIZE, DEFAULT_TOTAL_SIZE);
    }
    return INSTANCE;
  }

  public static synchronized void initialInstance(
      Duration timeout, long maxEntrySize, long maxTotalSize) {
    if (INSTANCE == null) {
      INSTANCE = new DeleteCache(timeout, maxEntrySize, maxTotalSize);
    } else {
      LOG.warn("Cache is already initialed.");
    }
  }

  public <V> V getOrLoad(String group, String key, Supplier<V> valueSupplier, long valueSize) {
    if (valueSize > maxEntrySize) {
      LOG.debug("{} exceeds max entry size: {} > {}", key, valueSize, maxEntrySize);
      return valueSupplier.get();
    }
    if (!groups.contains(group) && groups.size() > MAX_GROUPS) {
      String removed = groups.remove(MAX_GROUPS - 1);
      groups.add(group);
      if (removed != null) {
        invalidate(removed);
      }
    }
    String internalKey = group + "_" + key;
    CacheValue value = state().get(internalKey, loadFunc(valueSupplier, valueSize));
    Preconditions.checkNotNull(value, "Loaded value must not be null");
    return value.get();
  }

  public void invalidate(String group) {
    if (state != null && group != null) {
      List<String> internalKeys = findInternalKeys(group);
      LOG.info("Invalidating {} keys associated with {}", internalKeys.size(), group);
      internalKeys.forEach(internalKey -> state.invalidate(internalKey));
      LOG.info("Current cache stats {}", state.stats());
    }
  }

  private List<String> findInternalKeys(String group) {
    return state.asMap().keySet().stream()
        .filter(internalKey -> internalKey.startsWith(group))
        .collect(Collectors.toList());
  }

  private <V> Function<String, CacheValue> loadFunc(Supplier<V> valueSupplier, long valueSize) {
    return key -> {
      long start = System.currentTimeMillis();
      V value = valueSupplier.get();
      long end = System.currentTimeMillis();
      LOG.debug("Loaded {} with size {} in {} ms", key, valueSize, (end - start));
      return new CacheValue(value, valueSize);
    };
  }

  private Cache<String, CacheValue> state() {
    if (state == null) {
      synchronized (this) {
        if (state == null) {
          LOG.info("Initializing cache state");
          this.state = initState();
        }
      }
    }

    return state;
  }

  private Cache<String, CacheValue> initState() {
    return Caffeine.newBuilder()
        .expireAfterAccess(timeout)
        .maximumWeight(maxTotalSize)
        .weigher((key, value) -> ((CacheValue) value).weight())
        .recordStats()
        .removalListener((key, value, cause) -> LOG.debug("Evicted {} ({})", key, cause))
        .build();
  }

  public long maxEntrySize() {
    return maxEntrySize;
  }

  static class CacheValue {
    private final Object value;
    private final long size;

    CacheValue(Object value, long size) {
      this.value = value;
      this.size = size;
    }

    @SuppressWarnings("unchecked")
    public <V> V get() {
      return (V) value;
    }

    public int weight() {
      return (int) Math.min(size, Integer.MAX_VALUE);
    }
  }
}
