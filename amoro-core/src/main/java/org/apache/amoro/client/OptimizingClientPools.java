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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.amoro.Constants;
import org.apache.amoro.api.OptimizingService;
import org.apache.amoro.shade.thrift.org.apache.thrift.TException;
import org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TMultiplexedProtocol;

import java.util.Objects;

/** Client pool cache for different ams optimize server, sharing in jvm. */
public class OptimizingClientPools {

  private static final LoadingCache<String, ThriftClientPool<OptimizingService.Client>>
      CLIENT_POOLS = Caffeine.newBuilder().build(OptimizingClientPools::buildClient);

  public static OptimizingService.Iface getClient(String metastoreUrl) {
    return Objects.requireNonNull(CLIENT_POOLS.get(metastoreUrl)).iface();
  }

  @SuppressWarnings("unchecked")
  private static ThriftClientPool<OptimizingService.Client> buildClient(String url) {
    PoolConfig<OptimizingService.Client> poolConfig =
        (PoolConfig<OptimizingService.Client>) PoolConfig.forUrl(url);
    poolConfig.setMaxTotal(-1);
    return new ThriftClientPool<>(
        url,
        s ->
            new OptimizingService.Client(
                new TMultiplexedProtocol(
                    new TBinaryProtocol(s), Constants.THRIFT_OPTIMIZING_SERVICE_NAME)),
        c -> {
          try {
            ((OptimizingService.Client) c).ping();
          } catch (TException e) {
            return false;
          }
          return true;
        },
        poolConfig,
        Constants.THRIFT_OPTIMIZING_SERVICE_NAME);
  }
}
