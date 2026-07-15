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

package org.apache.amoro.server;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.javalin.http.Context;
import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.dashboard.controller.OptimizerGroupController;
import org.apache.amoro.server.resource.OptimizerManager;
import org.apache.amoro.server.table.TableManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.ws.rs.BadRequestException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class TestOptimizerGroupController {

  private TableManager tableManager;
  private DefaultOptimizingService optimizingService;
  private OptimizerManager optimizerManager;
  private OptimizerGroupController controller;
  private Context ctx;

  @BeforeEach
  void setUp() {
    tableManager = mock(TableManager.class);
    optimizingService = mock(DefaultOptimizingService.class);
    optimizerManager = mock(OptimizerManager.class);
    controller = new OptimizerGroupController(tableManager, optimizerManager);
    ctx = mock(Context.class);
  }

  private static Stream<String> invalidGroupNames() {
    return Stream.of(
        "invalid group name!",
        "invalid@group!",
        "",
        null,
        "invalidGroupName\t",
        "invalidGroupName\n",
        "无效组名",
        "invalid.group.name",
        "invalidinvalidinvalidinvalidinvalidinvalidinvalidin");
  }

  @ParameterizedTest
  @MethodSource("invalidGroupNames")
  void createInvalidGroupName(String groupName) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("name", groupName);
    requestBody.put("container", "Container");
    requestBody.put("properties", new HashMap<String, String>());

    when(ctx.bodyAsClass(Map.class)).thenReturn(requestBody);
    assertThrows(BadRequestException.class, () -> controller.createResourceGroup(ctx));
  }

  private Map<String, Object> groupRequest(String name, Map<String, String> properties) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("name", name);
    requestBody.put("container", "flink");
    requestBody.put("properties", properties);
    return requestBody;
  }

  @Test
  void createWithInvalidDynamicAllocationIsRejected() {
    Map<String, String> properties = new HashMap<>();
    // enabled without the required max-parallelism
    properties.put(OptimizerProperties.DYNAMIC_ALLOCATION_ENABLED, "true");

    when(ctx.bodyAsClass(Map.class)).thenReturn(groupRequest("group1", properties));
    assertThrows(BadRequestException.class, () -> controller.createResourceGroup(ctx));
  }

  @Test
  void updateWithInvalidDynamicAllocationIsRejected() {
    Map<String, String> properties = new HashMap<>();
    properties.put(OptimizerProperties.DYNAMIC_ALLOCATION_ENABLED, "true");
    properties.put(OptimizerProperties.DYNAMIC_ALLOCATION_MAX_PARALLELISM, "2048");

    when(ctx.bodyAsClass(Map.class)).thenReturn(groupRequest("group1", properties));
    assertThrows(BadRequestException.class, () -> controller.updateResourceGroup(ctx));
  }

  @Test
  void createWithValidDynamicAllocationSucceeds() {
    Map<String, String> properties = new HashMap<>();
    properties.put(OptimizerProperties.DYNAMIC_ALLOCATION_ENABLED, "true");
    properties.put(OptimizerProperties.DYNAMIC_ALLOCATION_MAX_PARALLELISM, "16");

    when(ctx.bodyAsClass(Map.class)).thenReturn(groupRequest("group1", properties));
    controller.createResourceGroup(ctx);
    verify(optimizerManager).createResourceGroup(any(ResourceGroup.class));
  }
}
