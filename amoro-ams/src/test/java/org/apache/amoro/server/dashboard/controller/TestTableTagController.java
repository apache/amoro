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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.javalin.http.Context;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.dashboard.ServerTableDescriptor;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.table.TableManager;
import org.apache.amoro.table.TableIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;

/** Tests for the create-tag / delete-tag dashboard endpoints. */
public class TestTableTagController {

  private static final String CATALOG = "catalog";
  private static final String DB = "db";
  private static final String TABLE = "table";

  private ServerTableDescriptor tableDescriptor;
  private TableController controller;
  private Context ctx;
  private final Map<String, String> pathParams = new HashMap<>();

  @BeforeEach
  void setUp() {
    CatalogManager catalogManager = mock(CatalogManager.class);
    TableManager tableManager = mock(TableManager.class);
    tableDescriptor = mock(ServerTableDescriptor.class);
    controller =
        new TableController(catalogManager, tableManager, tableDescriptor, new Configurations());
    ctx = mock(Context.class);
    pathParams.clear();
    pathParams.put("catalog", CATALOG);
    pathParams.put("db", DB);
    pathParams.put("table", TABLE);
    pathParams.put("tagName", "tag1");
    when(ctx.pathParam(any(String.class)))
        .thenAnswer(invocation -> pathParams.get(invocation.getArgument(0, String.class)));
  }

  @Test
  void createTagSuccess() {
    stubBody("tag1", "100", 1000L);

    controller.createTag(ctx);

    verify(tableDescriptor).createTag(eq(expectedIdentifier()), eq("tag1"), eq(100L), eq(1000L));
    assertOkResponse();
  }

  @Test
  void createTagWithoutMaxRefAge() {
    stubBody("tag1", "100", null);

    controller.createTag(ctx);

    verify(tableDescriptor).createTag(eq(expectedIdentifier()), eq("tag1"), eq(100L), eq(null));
    assertOkResponse();
  }

  @Test
  void createTagTrimsSnapshotIdWhitespace() {
    stubBody("tag1", "  100  ", null);

    controller.createTag(ctx);

    verify(tableDescriptor).createTag(eq(expectedIdentifier()), eq("tag1"), eq(100L), eq(null));
    assertOkResponse();
  }

  @Test
  void createTagRejectsBlankTagName() {
    stubBody("  ", "100", null);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> controller.createTag(ctx));
    assertTrue(exception.getMessage().contains("tagName"));
    verifyNoInteractions(tableDescriptor);
  }

  @Test
  void createTagRejectsNullTagName() {
    stubBody(null, "100", null);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> controller.createTag(ctx));
    assertTrue(exception.getMessage().contains("tagName"));
    verifyNoInteractions(tableDescriptor);
  }

  @Test
  void createTagRejectsBlankSnapshotId() {
    stubBody("tag1", "  ", null);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> controller.createTag(ctx));
    assertTrue(exception.getMessage().contains("snapshotId"));
    verifyNoInteractions(tableDescriptor);
  }

  @Test
  void createTagRejectsNullSnapshotId() {
    stubBody("tag1", null, null);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> controller.createTag(ctx));
    assertTrue(exception.getMessage().contains("snapshotId"));
    verifyNoInteractions(tableDescriptor);
  }

  @Test
  void createTagRejectsNonNumericSnapshotId() {
    stubBody("tag1", "not-a-number", null);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> controller.createTag(ctx));
    assertTrue(exception.getMessage().contains("snapshotId"));
    verifyNoInteractions(tableDescriptor);
  }

  @Test
  void createTagRejectsNegativeOrZeroMaxRefAge() {
    stubBody("tag1", "100", 0L);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> controller.createTag(ctx));
    assertTrue(exception.getMessage().contains("maxRefAgeMs"));
    verifyNoInteractions(tableDescriptor);
  }

  @Test
  void createTagRejectsEmptyPathParams() {
    pathParams.put("table", " ");
    stubBody("tag1", "100", null);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> controller.createTag(ctx));
    assertTrue(exception.getMessage().contains("can not be empty"));
    verifyNoInteractions(tableDescriptor);
  }

  @Test
  void createTagPropagatesDescriptorFailure() {
    stubBody("tag1", "100", null);
    doThrow(new IllegalArgumentException("Ref tag1 already exists in table"))
        .when(tableDescriptor)
        .createTag(any(), any(), anyLong(), any());

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> controller.createTag(ctx));
    assertTrue(exception.getMessage().contains("already exists"));
    verify(ctx, never()).json(any());
  }

  @Test
  void deleteTagSuccess() {
    controller.deleteTag(ctx);

    verify(tableDescriptor).deleteTag(eq(expectedIdentifier()), eq("tag1"));
    assertOkResponse();
  }

  @Test
  void deleteTagWithEncodedTagName() {
    pathParams.put("tagName", "tag-with.dots");

    controller.deleteTag(ctx);

    verify(tableDescriptor).deleteTag(eq(expectedIdentifier()), eq("tag-with.dots"));
    assertOkResponse();
  }

  @Test
  void deleteTagRejectsBlankTagName() {
    pathParams.put("tagName", "  ");

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> controller.deleteTag(ctx));
    assertTrue(exception.getMessage().contains("can not be empty"));
    verifyNoInteractions(tableDescriptor);
  }

  @Test
  void deleteTagRejectsMissingTagName() {
    pathParams.remove("tagName");

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> controller.deleteTag(ctx));
    assertTrue(exception.getMessage().contains("can not be empty"));
    verifyNoInteractions(tableDescriptor);
  }

  @Test
  void deleteMissingTagPropagatesDescriptorFailure() {
    doThrow(new IllegalArgumentException("Tag tag1 not found in table"))
        .when(tableDescriptor)
        .deleteTag(any(), any());

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> controller.deleteTag(ctx));
    assertTrue(exception.getMessage().contains("not found"));
    verify(ctx, never()).json(any());
  }

  private void stubBody(String tagName, String snapshotId, Long maxRefAgeMs) {
    TableController.CreateTagRequest request = new TableController.CreateTagRequest();
    request.setTagName(tagName);
    request.setSnapshotId(snapshotId);
    request.setMaxRefAgeMs(maxRefAgeMs);
    when(ctx.bodyAsClass(TableController.CreateTagRequest.class)).thenReturn(request);
  }

  private org.apache.amoro.api.TableIdentifier expectedIdentifier() {
    return TableIdentifier.of(CATALOG, DB, TABLE).buildTableIdentifier();
  }

  private void assertOkResponse() {
    ArgumentCaptor<Object> jsonCaptor = ArgumentCaptor.forClass(Object.class);
    verify(ctx).json(jsonCaptor.capture());
    OkResponse<?> response = (OkResponse<?>) jsonCaptor.getValue();
    assertEquals(200, response.getCode());
    assertNull(response.getResult());
  }
}
