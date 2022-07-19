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

package com.netease.arctic.optimizer.util;


import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.netease.arctic.ams.api.ArcticTableMetastore;
import com.netease.arctic.ams.api.OptimizeManager;
import com.netease.arctic.ams.api.client.ArcticThriftUrl;
import com.netease.arctic.ams.api.client.PoolConfig;
import com.netease.arctic.ams.api.client.ServiceInfo;
import com.netease.arctic.ams.api.client.ThriftClientPool;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;

import java.util.Collections;

public class OptimizeManagerClients {
  private static final int CLIENT_POOL_MIN = 1;
  private static final int CLIENT_POOL_MAX = 5;

  private static final LoadingCache<String, ThriftClientPool<OptimizeManager.Client>> CLIENT_POOLS
      = Caffeine.newBuilder()
      .build(OptimizeManagerClients::buildClient);

  public static OptimizeManager.Iface getClient(String metastoreUrl) {
    Preconditions.checkNotNull(metastoreUrl, "metastoreUrl is null");
    return CLIENT_POOLS.get(metastoreUrl).iface();
  }

  private static ThriftClientPool<OptimizeManager.Client> buildClient(String url) {
    ArcticThriftUrl arcticThriftUrl = ArcticThriftUrl.parse(url);
    PoolConfig poolConfig = new PoolConfig();
    poolConfig.setTimeout(arcticThriftUrl.socketTimeout());
    poolConfig.setFailover(true);
    poolConfig.setMinIdle(CLIENT_POOL_MIN);
    poolConfig.setMaxIdle(CLIENT_POOL_MAX);
    return new ThriftClientPool<>(Collections.singletonList(
        new ServiceInfo(arcticThriftUrl.host(), arcticThriftUrl.port())),
        s -> new OptimizeManager.Client(
            new TMultiplexedProtocol(new TBinaryProtocol(s), "OptimizeManager")), c -> true, new PoolConfig());
  }

}
