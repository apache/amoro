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

import org.apache.amoro.shade.thrift.org.apache.thrift.TServiceClient;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.http.client.utils.URLEncodedUtils;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.Duration;

public class PoolConfig<T extends TServiceClient> extends GenericObjectPoolConfig<ThriftClient<T>> {

  private static final int MIN_IDLE_DEFAULT = 0;
  private static final int MAX_IDLE_DEFAULT = 5;
  private static final int MAX_WAIT_MS_DEFAULT = 5000;

  private int connectTimeout = 0;
  private int socketTimeout = 0;
  private int maxMessageSize = 100 * 1024 * 1024; // 100MB
  private boolean autoReconnect = true;
  private int maxReconnects = 5;

  public PoolConfig() {
    setMinIdle(MIN_IDLE_DEFAULT);
    setMaxIdle(MAX_IDLE_DEFAULT);
    setMaxWait(Duration.ofMillis(MAX_WAIT_MS_DEFAULT));
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public void setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  public int getSocketTimeout() {
    return socketTimeout;
  }

  public void setSocketTimeout(int socketTimeout) {
    this.socketTimeout = socketTimeout;
  }

  public int getMaxMessageSize() {
    return maxMessageSize;
  }

  public void setMaxMessageSize(int maxMessageSize) {
    this.maxMessageSize = maxMessageSize;
  }

  public boolean isAutoReconnect() {
    return autoReconnect;
  }

  public void setAutoReconnect(boolean autoReconnect) {
    this.autoReconnect = autoReconnect;
  }

  public int getMaxReconnects() {
    return maxReconnects;
  }

  public void setMaxReconnects(int maxReconnects) {
    this.maxReconnects = maxReconnects;
  }

  public static PoolConfig<?> forUrl(String url) {
    PoolConfig<?> poolConfig = new PoolConfig<>();
    URLEncodedUtils.parse(URI.create(url), Charset.defaultCharset())
        .forEach(
            pair -> {
              try {
                BeanUtils.setProperty(poolConfig, pair.getName(), pair.getValue());
              } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Parse url parameters failed", e);
              }
            });
    return poolConfig;
  }
}
