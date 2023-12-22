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

package com.netease.arctic.ams.api.client;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.netease.arctic.ams.api.ArcticTableMetastore;
import com.netease.arctic.ams.api.Constants;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;

/** Client pool cache for different ams server, sharing in jvm. */
public class AmsClientPools {

  private static final int CLIENT_POOL_MIN_IDLE = 0;
  private static final int CLIENT_POOL_MAX_IDLE = 5;
  private static final int CLIENT_POOL_MAX_WAIT_MS = 5000;

  private static final LoadingCache<String, ThriftClientPool<ArcticTableMetastore.Client>>
      CLIENT_POOLS = Caffeine.newBuilder().build(AmsClientPools::buildClientPool);

  public static ThriftClientPool<ArcticTableMetastore.Client> getClientPool(String metastoreUrl) {
    return CLIENT_POOLS.get(metastoreUrl);
  }

  public static void cleanAll() {
    CLIENT_POOLS.cleanUp();
  }

  private static ThriftClientPool<ArcticTableMetastore.Client> buildClientPool(String url) {
    PoolConfig poolConfig = new PoolConfig();
    poolConfig.setFailover(true);
    poolConfig.setMinIdle(CLIENT_POOL_MIN_IDLE);
    poolConfig.setMaxIdle(CLIENT_POOL_MAX_IDLE);
    poolConfig.setMaxWaitMillis(CLIENT_POOL_MAX_WAIT_MS);
    return new ThriftClientPool<>(
        url,
        s -> {
          TProtocol protocol = new TBinaryProtocol(s);
          ArcticTableMetastore.Client tableMetastore =
              new ArcticTableMetastore.Client(
                  new TMultiplexedProtocol(protocol, Constants.THRIFT_TABLE_SERVICE_NAME));
          return tableMetastore;
        },
        c -> {
          try {
            ((ArcticTableMetastore.Client) c).ping();
          } catch (TException e) {
            return false;
          }
          return true;
        },
        poolConfig,
        Constants.THRIFT_TABLE_SERVICE_NAME);
  }
}
