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

import org.apache.amoro.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestAmsThriftUrl {
  @Test
  public void testParseMixedCaseThriftUrl() {
    String url = "ThRiFt://LOCALHOST:1260/MyCatalog?socketTimeout=6000";

    AmsThriftUrl thriftUrl = AmsThriftUrl.parse(url, null);

    Assertions.assertEquals("thrift", thriftUrl.schema());
    Assertions.assertEquals("LOCALHOST", thriftUrl.host());
    Assertions.assertEquals(1260, thriftUrl.port());
    Assertions.assertEquals("MyCatalog", thriftUrl.catalogName());
    Assertions.assertEquals(6000, thriftUrl.socketTimeout());
    Assertions.assertEquals(url, thriftUrl.url());
  }

  @Test
  public void testParseThriftUrl() {
    AmsThriftUrl thriftUrl =
        AmsThriftUrl.parse(
            "thrift://127.0.0.1:1260/catalog?socketTimeout=7000",
            Constants.THRIFT_TABLE_SERVICE_NAME);

    Assertions.assertEquals("127.0.0.1", thriftUrl.host());
    Assertions.assertEquals(1260, thriftUrl.port());
    Assertions.assertEquals("catalog", thriftUrl.catalogName());
    Assertions.assertEquals(7000, thriftUrl.socketTimeout());
  }

  @Test
  public void testRejectUnsupportedScheme() {
    assertInvalidUrl("http://127.0.0.1:1260/catalog", "scheme");
    assertInvalidUrl("zookeeperx://127.0.0.1:2181/cluster", "scheme");
  }

  @Test
  public void testRejectMissingHost() {
    assertInvalidUrl("thrift:///catalog", "host");
  }

  @Test
  public void testRejectMissingPort() {
    assertInvalidUrl("thrift://127.0.0.1/catalog", "port");
  }

  @Test
  public void testRejectInvalidPort() {
    assertInvalidUrl("thrift://127.0.0.1:65536/catalog", "port");
  }

  @Test
  public void testRejectInvalidZookeeperUrl() {
    assertInvalidUrl("zookeeper:/127.0.0.1:2181/cluster", "ZooKeeper URL");
  }

  @Test
  public void testParseZookeeperUrlBeforeServiceResolution() {
    String url = "ZoOkEePeR://127.0.0.1:2181/test-cluster/MyCatalog?socketTimeout=8000";

    RuntimeException exception =
        Assertions.assertThrows(
            RuntimeException.class, () -> AmsThriftUrl.parse(url, "unsupported-service"));

    Assertions.assertTrue(
        exception.getMessage().contains("Failed to resolve AMS URL from ZooKeeper URL"),
        exception.getMessage());
    Assertions.assertNotNull(exception.getCause());
    Assertions.assertTrue(
        exception.getCause().getMessage().contains("invalid service name unsupported-service"),
        exception.getCause().getMessage());
  }

  @Test
  public void testRejectInvalidZookeeperSocketTimeout() {
    String url = "ZoOkEePeR://127.0.0.1:2181/test-cluster/MyCatalog?socketTimeout=invalid";

    IllegalArgumentException exception =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> AmsThriftUrl.parse(url, Constants.THRIFT_TABLE_SERVICE_NAME));

    Assertions.assertTrue(
        exception.getMessage().contains("Invalid socketTimeout value"), exception.getMessage());
  }

  private void assertInvalidUrl(String url, String messagePart) {
    IllegalArgumentException exception =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> AmsThriftUrl.parse(url, null));
    Assertions.assertTrue(exception.getMessage().contains(messagePart), exception.getMessage());
  }
}
