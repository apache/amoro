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

package org.apache.amoro.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPoolConfig {

  @Test
  void testConstructFromUrl() {
    String url =
        "thrift://127.0.0.1:1261?connectTimeout=5000&socketTimeout=6000&maxMessageSize=100&autoReconnect=false"
            + "&maxReconnects=3&minIdle=10&maxIdle=10&maxWaitMillis=500";
    PoolConfig<?> poolConfig = PoolConfig.forUrl(url);

    Assertions.assertEquals(5000, poolConfig.getConnectTimeout());
    Assertions.assertEquals(6000, poolConfig.getSocketTimeout());
    Assertions.assertEquals(100, poolConfig.getMaxMessageSize());
    Assertions.assertFalse(poolConfig.isAutoReconnect());
    Assertions.assertEquals(3, poolConfig.getMaxReconnects());
    Assertions.assertEquals(10, poolConfig.getMinIdle());
    Assertions.assertEquals(10, poolConfig.getMaxIdle());
    Assertions.assertEquals(500, poolConfig.getMaxWaitMillis());
  }

  @Test
  void tetUrlParameterNameError() {
    // We will ignore parameters with unknown name
    String url = "thrift://127.0.0.1:1261?connectTimeouts=300";
    PoolConfig<?> poolConfig = PoolConfig.forUrl(url);
    Assertions.assertEquals(0, poolConfig.getConnectTimeout());
  }

  @Test
  void testUrlFormatError() {
    String url = "thrift://127.0.0.1:1261?connectTimeout=5000& ";
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          PoolConfig.forUrl(url);
        });
  }
}
