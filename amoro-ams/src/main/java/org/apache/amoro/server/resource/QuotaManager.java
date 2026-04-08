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

package org.apache.amoro.server.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** Manages optimizer quotas per resource group. Thread-safe via internal lock. */
public class QuotaManager implements QuotaProvider {

  private static final Logger LOG = LoggerFactory.getLogger(QuotaManager.class);

  private final Lock quotaLock = new ReentrantLock();
  private final Map<String, Set<OptimizerInstance>> optimizersByGroup = new HashMap<>();

  @Override
  public int getTotalQuota(String resourceGroup) {
    quotaLock.lock();
    try {
      Set<OptimizerInstance> optimizers = optimizersByGroup.get(resourceGroup);
      if (optimizers == null || optimizers.isEmpty()) {
        return 0;
      }
      return optimizers.stream().mapToInt(OptimizerInstance::getThreadCount).sum();
    } finally {
      quotaLock.unlock();
    }
  }

  public double getAvailableCore(String resourceGroup) {
    return getTotalQuota(resourceGroup);
  }

  public void addOptimizer(OptimizerInstance instance) {
    quotaLock.lock();
    try {
      optimizersByGroup
          .computeIfAbsent(instance.getGroupName(), k -> new HashSet<>())
          .add(instance);
      LOG.info(
          "Added optimizer {} to group {}, total quota: {}",
          instance.getResourceId(),
          instance.getGroupName(),
          getTotalQuota(instance.getGroupName()));
    } finally {
      quotaLock.unlock();
    }
  }

  public void removeOptimizer(OptimizerInstance instance) {
    quotaLock.lock();
    try {
      Set<OptimizerInstance> optimizers = optimizersByGroup.get(instance.getGroupName());
      if (optimizers != null) {
        optimizers.remove(instance);
        if (optimizers.isEmpty()) {
          optimizersByGroup.remove(instance.getGroupName());
        }
      }
      LOG.info(
          "Removed optimizer {} from group {}", instance.getResourceId(), instance.getGroupName());
    } finally {
      quotaLock.unlock();
    }
  }

  public Set<OptimizerInstance> getOptimizers(String groupName) {
    quotaLock.lock();
    try {
      return Collections.unmodifiableSet(
          optimizersByGroup.getOrDefault(groupName, Collections.emptySet()));
    } finally {
      quotaLock.unlock();
    }
  }

  public Set<String> getGroupNames() {
    quotaLock.lock();
    try {
      return Collections.unmodifiableSet(optimizersByGroup.keySet());
    } finally {
      quotaLock.unlock();
    }
  }
}
