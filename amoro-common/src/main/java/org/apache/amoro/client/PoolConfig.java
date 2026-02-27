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

import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.thrift.org.apache.commons.lang3.tuple.Pair;
import org.apache.amoro.shade.thrift.org.apache.thrift.TServiceClient;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URLDecoder;
import java.time.Duration;
import java.util.List;

public class PoolConfig<T extends TServiceClient> extends GenericObjectPoolConfig<ThriftClient<T>> {

  private static final int MIN_IDLE_DEFAULT = 0;
  private static final int MAX_IDLE_DEFAULT = 5;
  private static final int MAX_WAIT_MS_DEFAULT = 5000;

  private int connectTimeout = 0;
  private int socketTimeout = 0;
  private int maxMessageSize = 100 * 1024 * 1024; // 100MB
  private boolean autoReconnect = true;
  private int maxReconnects = 5;

  private static final String URL_ENCODING = "UTF-8";
  private static final String URL_QUERY_DELIMITER = "&";
  private static final String URL_QUERY_PARAMETER_DELIMITER = "=";

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
    parseQuery(URI.create(url))
        .forEach(
            pair -> {
              try {
                BeanUtils.setProperty(poolConfig, pair.getKey(), pair.getValue());
              } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Parse url parameters failed", e);
              }
            });
    return poolConfig;
  }

  static List<Pair<String, String>> parseQuery(URI uri) {
    Preconditions.checkNotNull(uri, "URI can not be null");
    List<Pair<String, String>> queries = Lists.newArrayList();
    String query = uri.getRawQuery();
    if (query != null && !query.trim().isEmpty()) {
      String[] pairs = query.trim().split(URL_QUERY_DELIMITER);
      for (String pair : pairs) {
        String[] kv = pair.trim().split(URL_QUERY_PARAMETER_DELIMITER, 2);
        try {
          String key = URLDecoder.decode(kv[0], URL_ENCODING);
          String value = URLDecoder.decode(kv[1], URL_ENCODING);
          queries.add(Pair.of(key, value));
        } catch (UnsupportedEncodingException e) {
          throw new RuntimeException("Unsupported encoding for uri", e);
        }
      }
    }
    return queries;
  }
}
