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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import org.apache.amoro.server.dashboard.controller.ProcessController;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.dashboard.response.PageResult;
import org.apache.amoro.server.table.TableManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class TestProcessController {

  private TableManager tableManager;
  private ProcessController controller;
  private Context ctx;
  private Validator<Integer> pageValidator;
  private Validator<Integer> pageSizeValidator;

  @BeforeEach
  void setUp() {
    tableManager = mock(TableManager.class);
    controller = new ProcessController(tableManager);
    ctx = mock(Context.class);
    pageValidator = mock(Validator.class);
    pageSizeValidator = mock(Validator.class);
  }

  @Test
  void getTableProcessesReturnsEmptyPageForUnmanagedTable() {
    when(ctx.pathParam("catalog")).thenReturn("test_catalog");
    when(ctx.pathParam("db")).thenReturn("db");
    when(ctx.pathParam("table")).thenReturn("user");
    when(ctx.queryParam("type")).thenReturn(null);
    when(ctx.queryParam("status")).thenReturn(null);
    when(ctx.queryParamAsClass("page", Integer.class)).thenReturn(pageValidator);
    when(ctx.queryParamAsClass("pageSize", Integer.class)).thenReturn(pageSizeValidator);
    when(pageValidator.getOrDefault(1)).thenReturn(1);
    when(pageSizeValidator.getOrDefault(20)).thenReturn(20);
    when(ctx.json(any())).thenReturn(ctx);

    controller.getTableProcesses(ctx);

    ArgumentCaptor<OkResponse<PageResult<?>>> captor = ArgumentCaptor.forClass(OkResponse.class);
    verify(ctx).json(captor.capture());

    PageResult<?> result = captor.getValue().getResult();
    assertEquals(0, result.getTotal());
    assertEquals(0, result.getList().size());
  }
}
