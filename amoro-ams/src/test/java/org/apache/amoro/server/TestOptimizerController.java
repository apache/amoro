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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import org.apache.amoro.server.dashboard.controller.OptimizerController;
import org.apache.amoro.server.resource.OptimizerManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

public class TestOptimizerController {

  private OptimizerManager optimizerManager;
  private OptimizerController controller;
  private Context ctx;

  @BeforeEach
  void setUp() {
    optimizerManager = mock(OptimizerManager.class);
    controller = new OptimizerController(optimizerManager);
    ctx = mock(Context.class);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1})
  void createOptimizerWithInvalidParallelism(int parallelism) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("parallelism", parallelism);
    requestBody.put("optimizerGroup", "group1");

    when(ctx.bodyAsClass(Map.class)).thenReturn(requestBody);

    BadRequestResponse exception =
        assertThrows(BadRequestResponse.class, () -> controller.createOptimizer(ctx));
    assertEquals(400, exception.getStatus());
    verifyNoInteractions(optimizerManager);
  }
}
