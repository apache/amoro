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

package com.netease.arctic.server.dashboard.controller;


import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.server.catalog.InternalCatalog;
import com.netease.arctic.server.catalog.ServerCatalog;
import com.netease.arctic.server.dashboard.response.IcebergRestErrorCode;
import com.netease.arctic.server.table.TableService;
import com.netease.arctic.utils.CatalogUtil;
import io.javalin.http.Context;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.rest.RESTResponse;
import org.apache.iceberg.rest.requests.CreateNamespaceRequest;
import org.apache.iceberg.rest.responses.ConfigResponse;
import org.apache.iceberg.rest.responses.CreateNamespaceResponse;
import org.apache.iceberg.rest.responses.ErrorResponse;
import org.apache.iceberg.rest.responses.ListNamespacesResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The controller to provider iceberg rest-catalog apis.
 */
public class IcebergRestCatalogController {

  public static final String REST_CATALOG_API_PREFIX = "/api/iceberg/rest/catalog";


  private final TableService tableService;
  private final Set<String> catalogPropertiesNotReturned = Collections.unmodifiableSet(
      Sets.newHashSet(CatalogMetaProperties.TABLE_FORMATS)
  );

  private final Set<String> catalogPropertiesOverwrite = Collections.unmodifiableSet(
      Sets.newHashSet(CatalogMetaProperties.KEY_WAREHOUSE)
  );

  public IcebergRestCatalogController(TableService tableService) {
    this.tableService = tableService;
  }


  public void handleException(Exception e, Context ctx) {
    IcebergRestErrorCode code = IcebergRestErrorCode.exceptionToCode(e);
    ErrorResponse response = ErrorResponse.builder()
        .responseCode(code.code)
        .withMessage(e.getClass().getSimpleName())
        .withMessage(e.getMessage())
        .build();
    ctx.res.setStatus(code.code);
    ctx.json(response);
  }

  /**
   * GET PREFIX/{catalog}/v1/config?warehouse={warehouse}
   */
  public void getCatalogConfig(Context ctx) {
    handleCatalog(ctx, catalog -> {
      Map<String, String> properties = Maps.newHashMap();
      Map<String, String> overwrites = Maps.newHashMap();
      catalog.getMetadata().getCatalogProperties().forEach((k, v) -> {
        if (!catalogPropertiesNotReturned.contains(k)) {
          if (catalogPropertiesOverwrite.contains(k)) {
            overwrites.put(k, v);
          } else {
            properties.put(k, v);
          }
        }
      });
      return ConfigResponse.builder()
          .withDefaults(properties)
          .withOverrides(overwrites)
          .build();
    });
  }


  /**
   * GET PREFIX/{catalog}/v1/namespaces
   */
  public void listNamespaces(Context ctx) {
    handleCatalog(ctx, catalog -> {
      String ns = ctx.req.getParameter("parent");
      checkUnsupported(ns == null,
          "The catalog doesn't support multi-level namespaces");
      List<Namespace> nsLists = catalog.listDatabases()
          .stream().map(Namespace::of)
          .collect(Collectors.toList());
      return ListNamespacesResponse.builder()
          .addAll(nsLists)
          .build();
    });

  }

  /**
   * POST PREFIX/{catalog}/v1/namespaces
   */
  public void createNamespace(Context ctx) {
    handleCatalog(ctx, catalog -> {
      CreateNamespaceRequest request = ctx.bodyAsClass(CreateNamespaceRequest.class);
      Namespace ns = request.namespace();
      checkUnsupported(
          request.properties() == null || request.properties().isEmpty(),
          "create namespace with properties is not supported now."
      );
      checkUnsupported(ns.length() == 1,
          "multi-level namespace is not supported now");
      catalog.createDatabase(ns.levels()[0]);
      return CreateNamespaceResponse.builder().withNamespace(ns).build();
    });
  }

  /**
   * GET /{catalog}/v1/namespaces/{namespaces}
   */
  public void getNamespace(Context ctx) {
    throw new UnsupportedOperationException("namespace properties is not supported");
  }

  /**
   * DELETE PREFIX/{catalog}/v1/namespaces/{namespace}
   */
  public void dropNamespace(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String ns = ctx.pathParam("namespace");
    Preconditions.checkNotNull(ns, "namespace is null");
    checkUnsupported(!ns.contains("."), "multi-level namespace is not supported");
    InternalCatalog internalCatalog = getCatalog(catalog);
    internalCatalog.dropDatabase(ns);
  }


  /**
   * POST PREFIX/{catalog}/v1/namespaces/{namespace}/properties
   */
  public void setNamespaceProperties(Context ctx) {
    throw new UnsupportedOperationException("namespace properties is not supported");
  }

  /**
   * GET PREFIX/{catalog}/v1/namespaces/{namespace}/tables
   */
  public void listTablesInNamespace(Context ctx) {

  }


  private void handleCatalog(Context ctx, Function<InternalCatalog, ? extends RESTResponse> handler) {
    String catalog = ctx.pathParam("catalog");
    Preconditions.checkNotNull(catalog, "lack require path params: catalog");
    InternalCatalog internalCatalog = getCatalog(catalog);

    RESTResponse r = handler.apply(internalCatalog);
    if (r != null) {
      ctx.json(r);
    }
  }

  private InternalCatalog getCatalog(String catalog) {
    Preconditions.checkNotNull(catalog, "lack required path variables: catalog");
    ServerCatalog internalCatalog = tableService.getServerCatalog(catalog);
    Preconditions.checkArgument(
        internalCatalog instanceof InternalCatalog,
        "The catalog is not an iceberg rest catalog"
    );
    Set<TableFormat> tableFormats = CatalogUtil.tableFormats(internalCatalog.getMetadata());
    Preconditions.checkArgument(
        tableFormats.size() == 1 && tableFormats.contains(TableFormat.ICEBERG),
        "The catalog is not an iceberg rest catalog"
    );
    return (InternalCatalog) internalCatalog;
  }

  private static void checkUnsupported(boolean condition, String message) {
    if (!condition) {
      throw new UnsupportedOperationException(message);
    }
  }
}
