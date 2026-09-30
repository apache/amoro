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

package org.apache.amoro.server.dashboard.controller;

import io.javalin.http.Context;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.table.TableProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Map;

public class TestTableControllerUpgradeProperties {

  @SuppressWarnings("unchecked")
  private static Map<String, String> upgradeProperties() throws IllegalAccessException {
    TableController controller = new TableController(null, null, null, null);
    Context ctx = Mockito.mock(Context.class);

    controller.getUpgradeHiveTableProperties(ctx);

    ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
    Mockito.verify(ctx).json(payloadCaptor.capture());
    OkResponse<Map<String, String>> response =
        (OkResponse<Map<String, String>>) payloadCaptor.getValue();
    Assertions.assertNotNull(response.getResult());
    return response.getResult();
  }

  @Test
  public void testWriteProtectedPropertiesAreNotUpgradable() throws IllegalAccessException {
    Map<String, String> properties = upgradeProperties();

    for (String hidden :
        Arrays.asList(
            "watermark.table",
            "watermark.base",
            "location",
            "base.table.max-transaction-id",
            "max-txId",
            "table.partition-properties",
            "schema.name-mapping.default",
            "format-version",
            "flink.max-continuous-empty-commits",
            "table.create-timestamp",
            "table.event-time-field")) {
      Assertions.assertFalse(
          properties.containsKey(hidden), "hidden property leaked to the API: " + hidden);
    }

    // Reflection also exposes the set itself as a property option.
    String reflectedSetKey = TableProperties.WRITE_PROTECTED_PROPERTIES.toString();
    Assertions.assertFalse(
        properties.containsKey(reflectedSetKey),
        "synthetic write-protected set option leaked to the API: " + reflectedSetKey);
  }

  @Test
  public void testUserEditablePropertiesAreStillUpgradable() throws IllegalAccessException {
    Map<String, String> properties = upgradeProperties();

    Assertions.assertEquals("true", properties.get("self-optimizing.enabled"));
    Assertions.assertEquals("false", properties.get("base.hive.auto-sync-data-write"));
    Assertions.assertEquals("true", properties.get("base.hive.auto-sync-schema-change"));
  }
}
