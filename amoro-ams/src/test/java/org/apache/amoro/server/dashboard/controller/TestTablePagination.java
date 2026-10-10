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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.javalin.Javalin;
import io.javalin.core.validation.ValidationException;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.dashboard.ServerTableDescriptor;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.dashboard.response.PageResult;
import org.apache.amoro.server.table.TableManager;
import org.apache.amoro.table.descriptor.OptimizingTaskInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class TestTablePagination {
  private CatalogManager catalogManager;
  private TableManager tableManager;
  private ServerTableDescriptor tableDescriptor;
  private TableController controller;
  private Context ctx;
  private final Map<String, String> queryValues = new HashMap<>();

  @BeforeEach
  void setUp() {
    catalogManager = mock(CatalogManager.class);
    tableManager = mock(TableManager.class);
    tableDescriptor = mock(ServerTableDescriptor.class);
    controller =
        new TableController(catalogManager, tableManager, tableDescriptor, new Configurations());
    ctx = mock(Context.class);
    queryValues.clear();
    when(ctx.pathParam(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    when(ctx.queryParamAsClass(anyString(), eq(Integer.class)))
        .thenAnswer(
            invocation ->
                Validator.create(
                    Integer.class,
                    queryValues.get(invocation.getArgument(0, String.class)),
                    invocation.getArgument(0, String.class)));
    when(ctx.queryParamAsClass(anyString(), eq(String.class)))
        .thenAnswer(
            invocation ->
                Validator.create(
                    String.class,
                    queryValues.get(invocation.getArgument(0, String.class)),
                    invocation.getArgument(0, String.class)));
  }

  private static Stream<String> endpoints() {
    return Stream.of(
        "getOptimizingProcesses",
        "getOptimizingProcessTasks",
        "getTableSnapshots",
        "getSnapshotDetail",
        "getTablePartitions",
        "getPartitionFileListInfo",
        "getTableOperations",
        "getTableTags",
        "getTableBranches",
        "getTableConsumerInfos");
  }

  private static Stream<Arguments> invalidPagination() {
    return endpoints()
        .flatMap(
            endpoint ->
                Stream.of(
                        new String[] {"0", "20"},
                        new String[] {"-1", "20"},
                        new String[] {"1", "0"},
                        new String[] {"1", "-1"},
                        new String[] {"2147483647", "20"},
                        new String[] {"1073741825", "4"},
                        new String[] {"2147483648", "20"},
                        new String[] {"abc", "20"},
                        new String[] {"1", "abc"})
                    .map(values -> Arguments.of(endpoint, values[0], values[1])));
  }

  @ParameterizedTest(name = "{0}: page={1}, pageSize={2}")
  @MethodSource("invalidPagination")
  void rejectsInvalidPaginationBeforeLoadingTable(String endpoint, String page, String pageSize) {
    queryValues.put("page", page);
    queryValues.put("pageSize", pageSize);

    assertThrows(ValidationException.class, () -> invokeEndpoint(endpoint));
    verifyNoInteractions(catalogManager, tableManager, tableDescriptor);
  }

  @ParameterizedTest
  @MethodSource("endpoints")
  void usesDefaultPagination(String endpoint) throws Exception {
    stubEmptyProcesses();

    invokeEndpoint(endpoint);

    PageResult<?> result = responsePageResult();
    assertEquals(0, result.getTotal());
    assertTrue(result.getList().isEmpty());
  }

  @ParameterizedTest
  @CsvSource({"2,2147483647,2147483647,2147483647", "2147483647,1,1,2147483646", "3,2,2,4"})
  void optimizingProcessesPassesValidatedLimitAndOffset(
      String page, String pageSize, int expectedLimit, int expectedOffset) {
    stubEmptyProcesses();
    queryValues.put("page", page);
    queryValues.put("pageSize", pageSize);

    controller.getOptimizingProcesses(ctx);

    verify(tableDescriptor)
        .getOptimizingProcessesInfo(
            any(), any(), any(), any(), eq(expectedLimit), eq(expectedOffset));
  }

  @Test
  void optimizingTasksReturnsRequestedPage() {
    OptimizingTaskInfo first = mock(OptimizingTaskInfo.class);
    OptimizingTaskInfo second = mock(OptimizingTaskInfo.class);
    OptimizingTaskInfo third = mock(OptimizingTaskInfo.class);
    when(tableDescriptor.getOptimizingProcessTaskInfos(any(), anyString()))
        .thenReturn(Arrays.asList(first, second, third));
    queryValues.put("page", "2");
    queryValues.put("pageSize", "1");

    controller.getOptimizingProcessTasks(ctx);

    PageResult<?> result = responsePageResult();
    assertEquals(3, result.getTotal());
    assertEquals(Collections.singletonList(second), result.getList());
  }

  @ParameterizedTest
  @CsvSource({"0,20", "1,0", "2147483647,20", "1073741825,4"})
  void httpRequestsRejectInvalidPagination(String page, String pageSize) throws Exception {
    Javalin app = Javalin.create(config -> config.showJavalinBanner = false);
    app.get("/tables/{catalog}/{db}/{table}/processes", controller::getOptimizingProcesses);
    // AMS also registers a general exception handler; validation must use Javalin's 400 handler.
    app.exception(Exception.class, (exception, context) -> context.status(500));
    app.start(0);
    try {
      HttpRequest request =
          HttpRequest.newBuilder(
                  URI.create(
                      "http://localhost:"
                          + app.port()
                          + "/tables/catalog/db/table/processes?page="
                          + page
                          + "&pageSize="
                          + pageSize))
              .GET()
              .build();
      HttpResponse<String> response =
          HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

      assertEquals(400, response.statusCode());
      verifyNoInteractions(catalogManager, tableManager, tableDescriptor);
    } finally {
      app.stop();
    }
  }

  private void stubEmptyProcesses() {
    when(tableDescriptor.getOptimizingProcessesInfo(any(), any(), any(), any(), anyInt(), anyInt()))
        .thenReturn(Pair.of(Collections.emptyList(), 0));
  }

  private void invokeEndpoint(String endpoint) throws Exception {
    switch (endpoint) {
      case "getOptimizingProcesses":
        controller.getOptimizingProcesses(ctx);
        break;
      case "getOptimizingProcessTasks":
        controller.getOptimizingProcessTasks(ctx);
        break;
      case "getTableSnapshots":
        controller.getTableSnapshots(ctx);
        break;
      case "getSnapshotDetail":
        controller.getSnapshotDetail(ctx);
        break;
      case "getTablePartitions":
        controller.getTablePartitions(ctx);
        break;
      case "getPartitionFileListInfo":
        controller.getPartitionFileListInfo(ctx);
        break;
      case "getTableOperations":
        controller.getTableOperations(ctx);
        break;
      case "getTableTags":
        controller.getTableTags(ctx);
        break;
      case "getTableBranches":
        controller.getTableBranches(ctx);
        break;
      case "getTableConsumerInfos":
        controller.getTableConsumerInfos(ctx);
        break;
      default:
        throw new IllegalArgumentException("Unknown endpoint: " + endpoint);
    }
  }

  private PageResult<?> responsePageResult() {
    ArgumentCaptor<Object> jsonCaptor = ArgumentCaptor.forClass(Object.class);
    verify(ctx).json(jsonCaptor.capture());
    return (PageResult<?>) ((OkResponse<?>) jsonCaptor.getValue()).getResult();
  }
}
