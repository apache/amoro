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

public class TestAmsThriftUrl {

  @Test
  public void testParseThriftUrlPreservesCatalogCase() {
    AmsThriftUrl thriftUrl =
        AmsThriftUrl.parse("ThRiFt://LOCALHOST:1260/MyCatalog?socketTimeout=6000", null);

    Assertions.assertEquals("thrift", thriftUrl.schema());
    Assertions.assertEquals("LOCALHOST", thriftUrl.host());
    Assertions.assertEquals(1260, thriftUrl.port());
    Assertions.assertEquals("MyCatalog", thriftUrl.catalogName());
    Assertions.assertEquals(6000, thriftUrl.socketTimeout());
    Assertions.assertEquals(
        "ThRiFt://LOCALHOST:1260/MyCatalog?socketTimeout=6000", thriftUrl.url());
  }

  @Test
  public void testParseThriftUrlSocketTimeoutParameterIsCaseInsensitive() {
    AmsThriftUrl thriftUrl =
        AmsThriftUrl.parse("thrift://127.0.0.1:1260/catalog?SocketTimeout=7000", null);

    Assertions.assertEquals(7000, thriftUrl.socketTimeout());
  }

  @Test
  public void testParseThriftUrlRejectsUnsupportedScheme() {
    IllegalArgumentException exception =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> AmsThriftUrl.parse("http://127.0.0.1:1260/catalog", null));

    Assertions.assertTrue(exception.getMessage().contains("Unsupported AMS URL scheme"));
  }

  @Test
  public void testParseThriftUrlRejectsMissingHost() {
    IllegalArgumentException exception =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> AmsThriftUrl.parse("thrift:///catalog", null));

    Assertions.assertTrue(exception.getMessage().contains("host is required"));
  }

  @Test
  public void testParseThriftUrlRejectsMissingPort() {
    IllegalArgumentException exception =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> AmsThriftUrl.parse("thrift://127.0.0.1/catalog", null));

    Assertions.assertTrue(exception.getMessage().contains("port is required"));
  }
}
