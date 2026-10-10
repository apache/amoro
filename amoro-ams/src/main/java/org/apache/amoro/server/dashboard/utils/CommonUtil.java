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

package org.apache.amoro.server.dashboard.utils;

import org.apache.amoro.shade.guava32.com.google.common.net.HostAndPort;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public class CommonUtil {
  private static final Logger LOG = LoggerFactory.getLogger(CommonUtil.class);

  /**
   * @param addresses support type 127.0.0.1:2181/ddd,host2:2181,host3:2181/service or
   *     music-hbase64.jd.163.org,music-hbase65.jd.163.org,
   *     music-hbase66.jd.163.org/hbase-music-feature-jd
   * @return true if success
   */
  public static boolean telnetOrPing(String addresses) {
    String[] split = addresses.split(",");
    for (String address : split) {
      String hostAndPort = address.split("/", 2)[0];
      HostAndPort endpoint = HostAndPort.fromString(hostAndPort);
      if (!endpoint.hasPort()) {
        if (hostAndPort.endsWith(":")
            && !hostAndPort.equals(endpoint.getHost())
            && (address.contains("/") || hostAndPort.startsWith("["))) {
          throw new IllegalArgumentException("port is empty");
        }
        if (ping(endpoint.getHost())) {
          return true;
        } else {
          continue;
        }
      }
      if (telnet(endpoint.getHost(), endpoint.getPort())) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param host host
   * @param port port
   * @return true if success
   */
  public static boolean telnet(String host, int port) {
    try {
      TelnetClient telnetClient = new TelnetClient("vt200");
      telnetClient.setConnectTimeout(500);
      telnetClient.connect(host, port);
      telnetClient.disconnect();
      return true;
    } catch (Exception e) {
      LOG.warn("telnet {} {} timeout! ", host, port);
      return false;
    }
  }

  public static boolean ping(String ip) {
    try {
      return InetAddress.getByName(ip).isReachable(500);
    } catch (Exception e) {
      LOG.warn("ping {} timeout! ", ip);
      return false;
    }
  }
}
