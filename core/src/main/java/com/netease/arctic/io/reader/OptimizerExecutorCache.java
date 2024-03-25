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

package com.netease.arctic.io.reader;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Copy from {@link org.apache.iceberg.spark.SparkExecutorCache}. */
public class OptimizerExecutorCache {

  private static final Logger LOG = LoggerFactory.getLogger(OptimizerExecutorCache.class);

  private static volatile OptimizerExecutorCache instance = null;

  private final Duration timeout;
  private final long maxEntrySize;
  private final long maxTotalSize;
  private volatile Cache<String, CacheValue> state;

  private OptimizerExecutorCache(Duration timeout, long maxEntrySize, long maxTotalSize) {
    this.timeout = timeout;
    this.maxEntrySize = maxEntrySize;
    this.maxTotalSize = maxTotalSize;
  }

  public static OptimizerExecutorCache create(
      Duration timeout, long maxEntrySize, long maxTotalSize) {
    if (instance == null) {
      synchronized (OptimizerExecutorCache.class) {
        if (instance == null) {
          OptimizerExecutorCache.instance =
              new OptimizerExecutorCache(timeout, maxEntrySize, maxTotalSize);
        }
      }
    }

    return instance;
  }

  /**
   * Returns the cache if created.
   *
   * <p>Note this method returns null if caching is disabled.
   */
  public static OptimizerExecutorCache getInstance() {
    return instance;
  }

  /** Returns the cache if already created or null otherwise. */
  public static OptimizerExecutorCache get() {
    return instance;
  }

  /** Returns the max entry size in bytes that will be considered for caching. */
  public long maxEntrySize() {
    return maxEntrySize;
  }

  /**
   * Gets the cached value for the key or populates the cache with a new mapping.
   *
   * @param group a group ID
   * @param key a cache key
   * @param valueSupplier a supplier to compute the value
   * @param valueSize an estimated memory size of the value in bytes
   * @return the cached or computed value
   */
  public <V> V getOrLoad(String group, String key, Supplier<V> valueSupplier, long valueSize) {
    if (valueSize > maxEntrySize) {
      LOG.debug("{} exceeds max entry size: {} > {}", key, valueSize, maxEntrySize);
      return valueSupplier.get();
    }

    String internalKey = group + "_" + key;
    CacheValue value = state().get(internalKey, loadFunc(valueSupplier, valueSize));
    Preconditions.checkNotNull(value, "Loaded value must not be null");
    return value.get();
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

  /**
   * Invalidates all keys associated with the given group ID.
   *
   * @param group a group ID
   */
  public void invalidate(String group) {
    if (state != null) {
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

  @org.apache.iceberg.relocated.com.google.common.annotations.VisibleForTesting
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
