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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.javalin.http.Context;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.dashboard.ServerTableDescriptor;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.table.TableManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;

public class TestTableControllerNamespaces {

  @Test
  public void testCatalogEndpointWithoutNamespaceKeepsLegacyCall() {
    CatalogManager catalogManager = mock(CatalogManager.class);
    CatalogMeta catalog = new CatalogMeta();
    when(catalogManager.listCatalogMetas()).thenReturn(Collections.singletonList(catalog));
    Context context = context();
    when(context.queryParam("namespace")).thenReturn(null);

    controller(catalogManager).getCatalogs(context);

    verify(catalogManager).listCatalogMetas();
    verify(catalogManager, never()).listCatalogMetas(any());
    Assertions.assertEquals(
        Collections.singletonList(catalog), capturedResponse(context).getResult());
  }

  @Test
  public void testCatalogEndpointFiltersByNamespace() {
    CatalogManager catalogManager = mock(CatalogManager.class);
    CatalogMeta catalog = new CatalogMeta();
    when(catalogManager.listCatalogMetas("tenant-a"))
        .thenReturn(Collections.singletonList(catalog));
    Context context = context();
    when(context.queryParam("namespace")).thenReturn(" tenant-a ");

    controller(catalogManager).getCatalogs(context);

    verify(catalogManager).listCatalogMetas("tenant-a");
    Assertions.assertEquals(
        Collections.singletonList(catalog), capturedResponse(context).getResult());
  }

  @Test
  public void testNamespaceAllowlistEndpointsDelegateToCatalogManager() {
    CatalogManager catalogManager = mock(CatalogManager.class);
    when(catalogManager.listNamespaces()).thenReturn(Collections.singletonList("tenant-a"));
    Context listContext = context();
    controller(catalogManager).getNamespaces(listContext);
    Assertions.assertEquals(
        Collections.singletonList("tenant-a"), capturedResponse(listContext).getResult());

    Context putContext = context();
    when(putContext.pathParam("namespace")).thenReturn("tenant-a");
    controller(catalogManager).addNamespace(putContext);
    verify(catalogManager).addNamespace("tenant-a");

    Context deleteContext = context();
    when(deleteContext.pathParam("namespace")).thenReturn("tenant-a");
    controller(catalogManager).removeNamespace(deleteContext);
    verify(catalogManager).removeNamespace("tenant-a");
  }

  private static TableController controller(CatalogManager catalogManager) {
    return new TableController(
        catalogManager,
        mock(TableManager.class),
        mock(ServerTableDescriptor.class),
        new Configurations());
  }

  private static Context context() {
    Context context = mock(Context.class);
    when(context.json(any())).thenReturn(context);
    return context;
  }

  private static OkResponse<?> capturedResponse(Context context) {
    ArgumentCaptor<Object> response = ArgumentCaptor.forClass(Object.class);
    verify(context).json(response.capture());
    return (OkResponse<?>) response.getValue();
  }
}
