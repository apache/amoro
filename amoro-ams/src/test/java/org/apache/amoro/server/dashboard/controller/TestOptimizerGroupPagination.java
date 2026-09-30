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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.javalin.core.validation.ValidationException;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import org.apache.amoro.server.dashboard.model.OptimizerInstanceInfo;
import org.apache.amoro.server.dashboard.model.TableOptimizingInfo;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.dashboard.response.PageResult;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.resource.OptimizerManager;
import org.apache.amoro.server.table.TableManager;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestOptimizerGroupPagination {

  private TableManager tableManager;
  private OptimizerManager optimizerManager;
  private OptimizerGroupController controller;
  private Context ctx;
  private final Map<String, String> queryValues = new HashMap<>();

  @BeforeEach
  void setUp() {
    tableManager = mock(TableManager.class);
    optimizerManager = mock(OptimizerManager.class);
    controller = new OptimizerGroupController(tableManager, optimizerManager);
    ctx = mock(Context.class);
    queryValues.clear();
    when(ctx.pathParam("optimizerGroup")).thenReturn("all");
    when(ctx.queryParams("actions[]")).thenReturn(Collections.emptyList());
    when(ctx.queryParamAsClass(anyString(), eq(Integer.class)))
        .thenAnswer(
            invocation ->
                Validator.create(
                    Integer.class,
                    queryValues.get(invocation.getArgument(0, String.class)),
                    invocation.getArgument(0, String.class)));
  }

  private void usePagination(String page, String pageSize) {
    queryValues.put("page", page);
    queryValues.put("pageSize", pageSize);
  }

  private void stubEmptyTableQuery() {
    when(tableManager.queryTableOptimizingInfo(any(), any(), any(), any(), anyInt(), anyInt()))
        .thenReturn(Pair.of(Collections.<TableOptimizingInfo>emptyList(), 0));
  }

  @ParameterizedTest
  @CsvSource({"0,20", "-1,20", "1,0", "1,-1", "2147483647,20"})
  void getOptimizersRejectsInvalidPagination(String page, String pageSize) {
    usePagination(page, pageSize);

    assertThrows(ValidationException.class, () -> controller.getOptimizers(ctx));
    verifyNoInteractions(optimizerManager, tableManager);
  }

  @ParameterizedTest
  @CsvSource({"0,20", "-1,20", "1,0", "1,-1", "2147483647,20"})
  void getOptimizerTablesRejectsInvalidPagination(String page, String pageSize) {
    usePagination(page, pageSize);

    assertThrows(ValidationException.class, () -> controller.getOptimizerTables(ctx));
    verifyNoInteractions(optimizerManager, tableManager);
  }

  @Test
  void getOptimizerTablesUsesDefaultPaginationAndFilters() {
    stubEmptyTableQuery();

    controller.getOptimizerTables(ctx);

    verify(tableManager)
        .queryTableOptimizingInfo(isNull(), isNull(), isNull(), isNull(), eq(20), eq(0));
    PageResult<?> pageResult = responsePageResult();
    assertEquals(0, pageResult.getTotal());
    assertTrue(pageResult.getList().isEmpty());
  }

  @Test
  void getOptimizerTablesPassesPageAndSizeAsLimitAndOffset() {
    stubEmptyTableQuery();
    usePagination("2", "2");

    controller.getOptimizerTables(ctx);

    verify(tableManager)
        .queryTableOptimizingInfo(isNull(), isNull(), isNull(), isNull(), eq(2), eq(2));
  }

  @ParameterizedTest
  @ValueSource(strings = {"all", "group1"})
  void getOptimizersSortsAndPaginates(String optimizerGroup) {
    when(ctx.pathParam("optimizerGroup")).thenReturn(optimizerGroup);
    List<OptimizerInstance> unsorted =
        Arrays.asList(optimizer("three", 3L), optimizer("one", 1L), optimizer("two", 2L));
    if ("all".equals(optimizerGroup)) {
      when(optimizerManager.listOptimizers()).thenReturn(unsorted);
    } else {
      when(optimizerManager.listOptimizers(optimizerGroup)).thenReturn(unsorted);
    }
    usePagination("2", "1");

    controller.getOptimizers(ctx);

    if ("all".equals(optimizerGroup)) {
      verify(optimizerManager).listOptimizers();
    } else {
      verify(optimizerManager).listOptimizers(optimizerGroup);
    }
    PageResult<?> pageResult = responsePageResult();
    assertEquals(3, pageResult.getTotal());
    assertEquals(1, pageResult.getList().size());
    assertEquals("two", optimizerToken(pageResult.getList().get(0)));
  }

  @Test
  void getOptimizersReturnsFirstPageByDefault() {
    List<OptimizerInstance> optimizers = Collections.singletonList(optimizer("one", 1L));
    when(optimizerManager.listOptimizers()).thenReturn(optimizers);

    controller.getOptimizers(ctx);

    PageResult<?> pageResult = responsePageResult();
    assertEquals(1, pageResult.getTotal());
    assertEquals(1, pageResult.getList().size());
    assertEquals("one", optimizerToken(pageResult.getList().get(0)));
  }

  private static OptimizerInstance optimizer(String token, long startTime) {
    OptimizerInstance instance = mock(OptimizerInstance.class);
    when(instance.getToken()).thenReturn(token);
    when(instance.getStartTime()).thenReturn(startTime);
    return instance;
  }

  private static String optimizerToken(Object info) {
    return ((OptimizerInstanceInfo) info).getToken();
  }

  private PageResult<?> responsePageResult() {
    ArgumentCaptor<Object> jsonCaptor = ArgumentCaptor.forClass(Object.class);
    verify(ctx).json(jsonCaptor.capture());
    Object payload = jsonCaptor.getValue();
    assertNotNull(payload);
    return (PageResult<?>) ((OkResponse<?>) payload).getResult();
  }
}
