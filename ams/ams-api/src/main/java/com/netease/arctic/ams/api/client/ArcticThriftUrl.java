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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class ArcticThriftUrl {
  public static final String PARAM_SOCKET_TIMEOUT = "socketTimeout";
  public static final int DEFAULT_SOCKET_TIMEOUT = 5000;
  private final String schema;
  private final String host;
  private final int port;
  private final String catalogName;
  private final int socketTimeout;

  // origin url before parse
  private final String url;

  /**
   * parse thrift url.
   * @param url - thrift url
   * @return -
   */
  public static ArcticThriftUrl parse(String url) {
    if (url == null) {
      throw new IllegalArgumentException("thrift url is null");
    }
    String schema;
    String host;
    int port;
    int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
    String catalogName;
    try {
      URI uri = new URI(url.toLowerCase(Locale.ROOT));
      schema = uri.getScheme();
      host = uri.getHost();
      port = uri.getPort();
      String path = uri.getPath();
      if (path != null && path.startsWith("/")) {
        path = path.substring(1);
      }
      if (uri.getQuery() != null) {
        for (String paramExpression : uri.getQuery().split("&")) {
          String[] paramSplit = paramExpression.split("=");
          if (paramSplit.length == 2) {
            if (paramSplit[0].equalsIgnoreCase(PARAM_SOCKET_TIMEOUT)) {
              socketTimeout = Integer.parseInt(paramSplit[1]);
            }
          }
        }
      }
      catalogName = path;
      return new ArcticThriftUrl(schema, host, port, catalogName, socketTimeout, url);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("parse metastore url failed", e);
    }
  }

  private ArcticThriftUrl(String schema, String host, int port, String catalogName, int socketTimeout,
                         String url) {
    this.schema = schema;
    this.host = host;
    this.port = port;
    this.catalogName = catalogName;
    this.socketTimeout = socketTimeout;
    this.url = url;
  }

  public String schema() {
    return schema;
  }

  public String host() {
    return host;
  }

  public int port() {
    return port;
  }

  public String catalogName() {
    return catalogName;
  }

  public int socketTimeout() {
    return socketTimeout;
  }

  public String url() {
    return url;
  }

  @Override
  public String toString() {
    return "ArcticThriftUrl{" +
        "schema='" + schema + '\'' +
        ", host='" + host + '\'' +
        ", port=" + port +
        ", catalogName='" + catalogName + '\'' +
        ", socketTimeout=" + socketTimeout +
        ", url='" + url + '\'' +
        '}';
  }
}
