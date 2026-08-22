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

package org.apache.amoro.server.persistence;

import org.apache.amoro.api.OptimizerRegisterInfo;
import org.apache.amoro.server.AMSManagerTestBase;
import org.apache.amoro.server.persistence.mapper.OptimizerMapper;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class TestOptimizerMapperTouchTime extends AMSManagerTestBase {

  @Test
  public void updateTouchTimePersistsJvmEpochMillis() {
    // The keeper's expiry math and the leader-failover baseline compare touch_time against
    // System.currentTimeMillis(); the value written must come from the AMS JVM, not the DB
    // clock (CURRENT_TIMESTAMP would skew expiry by the DB-AMS clock offset).
    OptimizerRegisterInfo info = new OptimizerRegisterInfo();
    Map<String, String> properties = Maps.newHashMap();
    properties.put("heartbeat", "100");
    info.setProperties(properties);
    info.setThreadCount(1);
    info.setMemoryMb(1024);
    info.setGroupName("touch-time-test-group");
    info.setResourceId("touch-time-test-" + System.nanoTime());
    info.setStartTime(System.currentTimeMillis());
    OptimizerInstance optimizer = new OptimizerInstance(info, "local");
    try (SqlSession session = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
      session.getMapper(OptimizerMapper.class).insertOptimizer(optimizer);
    }

    long jvmTouchTime = System.currentTimeMillis() - 123_456L;
    try (SqlSession session = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
      session.getMapper(OptimizerMapper.class).updateTouchTime(optimizer.getToken(), jvmTouchTime);
    }

    List<OptimizerInstance> all;
    try (SqlSession session = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
      all = session.getMapper(OptimizerMapper.class).selectAll();
    }
    long stored =
        all.stream()
            .filter(o -> o.getToken().equals(optimizer.getToken()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("inserted optimizer not found"))
            .getTouchTime();
    Assertions.assertEquals(jvmTouchTime, stored, "touch_time must store the JVM epoch millis");
  }
}
