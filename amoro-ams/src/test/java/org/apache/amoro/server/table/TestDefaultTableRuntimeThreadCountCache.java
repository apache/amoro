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

package org.apache.amoro.server.table;

import org.apache.amoro.api.OptimizerRegisterInfo;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.server.AMSManagerTestBase;
import org.apache.amoro.server.persistence.SqlSessionFactoryProvider;
import org.apache.amoro.server.persistence.mapper.OptimizerMapper;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TestDefaultTableRuntimeThreadCountCache extends AMSManagerTestBase {

  private static final String GROUP = "thread-count-cache-test-group";

  @BeforeEach
  public void resetCache() throws Exception {
    cacheMap().clear();
    cacheTime().set(0);
  }

  @Test
  public void threadCountIsCachedPerGroupUntilTtlExpiry() throws Exception {
    insertOptimizer(GROUP, 7);
    DefaultTableRuntime runtime = runtimeOf(GROUP);

    // targetQuota = 1.0, quotaTime = LOOK_BACK_TIME => weight = 1 / threadCount
    Assertions.assertEquals(1 / 7.0, runtime.calculateQuotaOccupy(), 1e-9);

    // A new optimizer joins the group; within the TTL the cached count is still used.
    insertOptimizer(GROUP, 3);
    Assertions.assertEquals(1 / 7.0, runtime.calculateQuotaOccupy(), 1e-9);

    // Simulate TTL expiry: the next read refreshes from the database (7 + 3 = 10).
    cacheTime().set(System.currentTimeMillis() - 60_000);
    Assertions.assertEquals(1 / 10.0, runtime.calculateQuotaOccupy(), 1e-9);
    Assertions.assertEquals(1, cacheMap().size());
  }

  private DefaultTableRuntime runtimeOf(String group) {
    DefaultTableRuntime runtime =
        Mockito.mock(
            DefaultTableRuntime.class,
            Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
    Mockito.doReturn(group).when(runtime).getGroupName();
    OptimizingConfig config = new OptimizingConfig();
    config.setEnabled(true);
    config.setTargetQuota(1.0);
    Mockito.doReturn(config).when(runtime).getOptimizingConfig();
    Mockito.doReturn((long) org.apache.amoro.server.AmoroServiceConstants.QUOTA_LOOK_BACK_TIME)
        .when(runtime)
        .getQuotaTime();
    return runtime;
  }

  private void insertOptimizer(String group, int threadCount) {
    OptimizerRegisterInfo info = new OptimizerRegisterInfo();
    Map<String, String> properties = Maps.newHashMap();
    properties.put("heartbeat", "100");
    info.setProperties(properties);
    info.setThreadCount(threadCount);
    info.setMemoryMb(1024);
    info.setGroupName(group);
    info.setResourceId("cache-test-" + System.nanoTime());
    info.setStartTime(System.currentTimeMillis());
    OptimizerInstance instance = new OptimizerInstance(info, "local");
    try (SqlSession session = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
      session.getMapper(OptimizerMapper.class).insertOptimizer(instance);
    }
  }

  @SuppressWarnings("unchecked")
  private static ConcurrentHashMap<String, Integer> cacheMap() throws Exception {
    Field field = DefaultTableRuntime.class.getDeclaredField("groupThreadCountCache");
    field.setAccessible(true);
    return (ConcurrentHashMap<String, Integer>) field.get(null);
  }

  private static AtomicLong cacheTime() throws Exception {
    Field field = DefaultTableRuntime.class.getDeclaredField("threadCountCacheTime");
    field.setAccessible(true);
    return (AtomicLong) field.get(null);
  }
}
