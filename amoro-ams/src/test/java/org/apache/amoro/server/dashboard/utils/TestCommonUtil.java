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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.server.dashboard.model.KafkaClusterSimpleInfo;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.ServerSocket;

public class TestCommonUtil {

  @Test
  void testIpv4Address() throws Exception {
    try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
      assertTrue(CommonUtil.telnetOrPing("127.0.0.1:" + server.getLocalPort()));
      assertTrue(CommonUtil.telnetOrPing("127.0.0.1:" + server.getLocalPort() + "/service"));
    }
  }

  @Test
  void testIpv6Address() throws Exception {
    try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getByName("::1"))) {
      assertTrue(CommonUtil.telnetOrPing("[::1]:" + server.getLocalPort()));
    }
  }

  @Test
  void testIpv6AddressWithChroot() throws Exception {
    try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getByName("::1"))) {
      assertTrue(CommonUtil.telnetOrPing("[::1]:" + server.getLocalPort() + "/service"));
    }
  }

  @Test
  void testKafkaClusterWithIpv6Broker() throws Exception {
    try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getByName("::1"))) {
      KafkaClusterSimpleInfo cluster = new KafkaClusterSimpleInfo();
      cluster.setBrokerList("[::1]:" + server.getLocalPort());
      assertDoesNotThrow(cluster::validate);
    }
  }

  @Test
  void testAddressListContinuesAfterUnreachableBroker() throws Exception {
    try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getByName("::1"))) {
      try (ServerSocket closedServer = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
        int unavailablePort = closedServer.getLocalPort();
        closedServer.close();
        assertTrue(
            CommonUtil.telnetOrPing(
                "127.0.0.1:" + unavailablePort + ",[::1]:" + server.getLocalPort()));
      }
    }
  }

  @Test
  void testInvalidPort() {
    assertThrows(IllegalArgumentException.class, () -> CommonUtil.telnetOrPing("127.0.0.1:nope"));
    assertThrows(IllegalArgumentException.class, () -> CommonUtil.telnetOrPing("[::1]:nope"));
  }

  @Test
  void testEmptyPort() {
    for (String address : new String[] {"127.0.0.1:/service", "[::1]:/service", "[::1]:"}) {
      assertThrows(IllegalArgumentException.class, () -> CommonUtil.telnetOrPing(address));
      KafkaClusterSimpleInfo cluster = new KafkaClusterSimpleInfo();
      cluster.setBrokerList(address);
      assertThrows(IllegalArgumentException.class, cluster::validate);
    }
  }
}
