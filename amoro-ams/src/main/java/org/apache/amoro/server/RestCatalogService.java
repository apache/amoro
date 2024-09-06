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

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.head;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import io.javalin.plugin.json.JavalinJackson;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.events.IcebergReportEvent;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.server.catalog.InternalCatalog;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.exception.ObjectNotExistsException;
import org.apache.amoro.server.manager.EventsManager;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.server.table.internal.InternalTableCreator;
import org.apache.amoro.server.table.internal.InternalTableHandler;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.utils.CatalogUtil;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.exceptions.NoSuchNamespaceException;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.apache.iceberg.rest.RESTResponse;
import org.apache.iceberg.rest.RESTSerializers;
import org.apache.iceberg.rest.requests.CreateNamespaceRequest;
import org.apache.iceberg.rest.requests.CreateTableRequest;
import org.apache.iceberg.rest.requests.ReportMetricsRequest;
import org.apache.iceberg.rest.requests.ReportMetricsRequestParser;
import org.apache.iceberg.rest.requests.UpdateTableRequest;
import org.apache.iceberg.rest.responses.ConfigResponse;
import org.apache.iceberg.rest.responses.CreateNamespaceResponse;
import org.apache.iceberg.rest.responses.ErrorResponse;
import org.apache.iceberg.rest.responses.GetNamespaceResponse;
import org.apache.iceberg.rest.responses.ListNamespacesResponse;
import org.apache.iceberg.rest.responses.ListTablesResponse;
import org.apache.iceberg.rest.responses.LoadTableResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class provides an implementation of <a
 * href="https://github.com/apache/iceberg/blob/main/open-api/rest-catalog-open-api.yaml">iceberg
 * rest-catalog-open-api </a>
 */
public class RestCatalogService extends PersistentBase {

  private static final Logger LOG = LoggerFactory.getLogger(RestCatalogService.class);

  public static final String ICEBERG_REST_API_PREFIX = "/api/iceberg/rest";

  private static final String ICEBERG_CATALOG_PREFIX_KEY = "prefix";

  private static final Set<String> catalogPropertiesNotReturned = Collections.emptySet();

  private static final Set<String> catalogPropertiesOverwrite =
      Collections.unmodifiableSet(Sets.newHashSet(CatalogMetaProperties.KEY_WAREHOUSE));

  private final JavalinJackson jsonMapper;

  private final TableService tableService;

  public RestCatalogService(TableService tableService) {
    this.tableService = tableService;
    ObjectMapper objectMapper = jsonMapper();
    this.jsonMapper = new JavalinJackson(objectMapper);
  }

  public EndpointGroup endpoints() {
    return () -> {
      // for iceberg rest catalog api
      path(
          ICEBERG_REST_API_PREFIX,
          () -> {
            get("/v1/config", this::getCatalogConfig);
            get("/v1/catalogs/{catalog}/namespaces", this::listNamespaces);
            post("/v1/catalogs/{catalog}/namespaces", this::createNamespace);
            get("/v1/catalogs/{catalog}/namespaces/{namespace}", this::getNamespace);
            delete("/v1/catalogs/{catalog}/namespaces/{namespace}", this::dropNamespace);
            post("/v1/catalogs/{catalog}/namespaces/{namespace}", this::setNamespaceProperties);
            get(
                "/v1/catalogs/{catalog}/namespaces/{namespace}/tables",
                this::listTablesInNamespace);
            post("/v1/catalogs/{catalog}/namespaces/{namespace}/tables", this::createTable);
            get("/v1/catalogs/{catalog}/namespaces/{namespace}/tables/{table}", this::loadTable);
            post("/v1/catalogs/{catalog}/namespaces/{namespace}/tables/{table}", this::commitTable);
            delete(
                "/v1/catalogs/{catalog}/namespaces/{namespace}/tables/{table}", this::deleteTable);
            head("/v1/catalogs/{catalog}/namespaces/{namespace}/tables/{table}", this::tableExists);
            post("/v1/catalogs/{catalog}/tables/rename", this::renameTable);
            post(
                "/v1/catalogs/{catalog}/namespaces/{namespace}/tables/{table}/metrics",
                this::reportMetrics);
          });
    };
  }

  public boolean needHandleException(Context ctx) {
    return ctx.req.getRequestURI().startsWith(ICEBERG_REST_API_PREFIX);
  }

  public void handleException(Exception e, Context ctx) {
    IcebergRestErrorCode code = IcebergRestErrorCode.exceptionToCode(e);
    ErrorResponse response =
        ErrorResponse.builder()
            .responseCode(code.code)
            .withType(e.getClass().getSimpleName())
            .withMessage(e.getMessage())
            .build();
    ctx.res.setStatus(code.code);
    jsonResponse(ctx, response);
    if (code.code >= 500) {
      LOG.warn("InternalServer Error", e);
    } else {
      // those errors happened when the client-side passed arguments with problems.
      LOG.info(
          "Exception when handle request: {} {}, code: {} message: {}",
          ctx.req.getMethod(),
          ctx.req.getRequestURI().substring(ICEBERG_REST_API_PREFIX.length()),
          code.code,
          e.getMessage());
    }
  }

  /** GET PREFIX/v1/config?warehouse={warehouse} */
  public void getCatalogConfig(Context ctx) {
    String warehouse = ctx.req.getParameter("warehouse");
    Preconditions.checkNotNull(warehouse, "lack required params: warehouse");
    InternalCatalog catalog = getCatalog(warehouse);
    Map<String, String> properties = Maps.newHashMap();
    Map<String, String> overwrites = Maps.newHashMap();
    catalog
        .getMetadata()
        .getCatalogProperties()
        .forEach(
            (k, v) -> {
              if (!catalogPropertiesNotReturned.contains(k)) {
                if (catalogPropertiesOverwrite.contains(k)) {
                  overwrites.put(k, v);
                } else {
                  properties.put(k, v);
                }
              }
            });
    overwrites.put(ICEBERG_CATALOG_PREFIX_KEY, "catalogs/" + warehouse);
    ConfigResponse configResponse =
        ConfigResponse.builder().withDefaults(properties).withOverrides(overwrites).build();
    jsonResponse(ctx, configResponse);
  }

  /** GET PREFIX/{catalog}/v1/namespaces */
  public void listNamespaces(Context ctx) {
    handleCatalog(
        ctx,
        catalog -> {
          String ns = ctx.req.getParameter("parent");
          List<Namespace> nsLists = Lists.newArrayList();
          List<String> databases = catalog.listDatabases();
          if (ns == null) {
            nsLists = databases.stream().map(Namespace::of).collect(Collectors.toList());
          } else {
            checkUnsupported(
                !ns.contains("."), "multi-level namespace is not supported, parent: " + ns);
            checkDatabaseExist(catalog.databaseExists(ns), ns);
          }
          return ListNamespacesResponse.builder().addAll(nsLists).build();
        });
  }

  /** POST PREFIX/{catalog}/v1/namespaces */
  public void createNamespace(Context ctx) {
    handleCatalog(
        ctx,
        catalog -> {
          CreateNamespaceRequest request = bodyAsClass(ctx, CreateNamespaceRequest.class);
          Namespace ns = request.namespace();
          checkUnsupported(ns.length() == 1, "multi-level namespace is not supported now");
          String database = ns.level(0);
          checkAlreadyExists(!catalog.databaseExists(database), "Database", database);
          catalog.createDatabase(database);
          return CreateNamespaceResponse.builder().withNamespace(Namespace.of(database)).build();
        });
  }

  /** GET PREFIX/v1/catalogs/{catalog}/namespaces/{namespaces} */
  public void getNamespace(Context ctx) {
    handleNamespace(
        ctx,
        (catalog, database) ->
            GetNamespaceResponse.builder().withNamespace(Namespace.of(database)).build());
  }

  /** DELETE PREFIX/v1/catalogs/{catalog}/namespaces/{namespace} */
  public void dropNamespace(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String ns = ctx.pathParam("namespace");
    Preconditions.checkNotNull(ns, "namespace is null");
    checkUnsupported(!ns.contains("."), "multi-level namespace is not supported");
    InternalCatalog internalCatalog = getCatalog(catalog);
    internalCatalog.dropDatabase(ns);
  }

  /** POST PREFIX/v1/catalogs/{catalog}/namespaces/{namespace}/properties */
  public void setNamespaceProperties(Context ctx) {
    throw new UnsupportedOperationException("namespace properties is not supported");
  }

  /** GET PREFIX/v1/catalogs/{catalog}/namespaces/{namespace}/tables */
  public void listTablesInNamespace(Context ctx) {
    handleNamespace(
        ctx,
        (catalog, database) -> {
          List<TableIdentifier> tableIdentifiers =
              catalog.listTables(database).stream()
                  .map(i -> TableIdentifier.of(database, i.getIdentifier().getTableName()))
                  .collect(Collectors.toList());

          return ListTablesResponse.builder().addAll(tableIdentifiers).build();
        });
  }

  /** POST PREFIX/v1/catalogs/{catalog}/namespaces/{namespace}/tables */
  public void createTable(Context ctx) {
    handleNamespace(
        ctx,
        (catalog, database) -> {
          CreateTableRequest request = bodyAsClass(ctx, CreateTableRequest.class);
          String tableName = request.name();
          TableFormat format =
              TablePropertyUtil.isBaseStore(request.properties(), TableFormat.MIXED_ICEBERG)
                  ? TableFormat.MIXED_ICEBERG
                  : TableFormat.ICEBERG;

          try (InternalTableCreator creator =
              catalog.newTableCreator(database, tableName, format, request)) {
            try {
              org.apache.amoro.server.table.TableMetadata metadata = creator.create();
              tableService.createTable(catalog.name(), metadata);
            } catch (RuntimeException e) {
              creator.rollback();
              throw e;
            }
          }

          try (InternalTableHandler<TableOperations> handler =
              catalog.newTableHandler(database, tableName)) {
            TableMetadata current = handler.newTableOperator().current();
            return LoadTableResponse.builder().withTableMetadata(current).build();
          }
        });
  }

  /** GET PREFIX/v1/catalogs/{catalog}/namespaces/{namespace}/tables/{table} */
  public void loadTable(Context ctx) {
    handleTable(
        ctx,
        handler -> {
          TableOperations ops = handler.newTableOperator();
          TableMetadata tableMetadata = ops.current();
          if (tableMetadata == null) {
            throw new NoSuchTableException("failed to load table from metadata file.");
          }
          return LoadTableResponse.builder().withTableMetadata(tableMetadata).build();
        });
  }

  /** POST PREFIX/v1/catalogs/{catalog}/namespaces/{namespace}/tables/{table} */
  public void commitTable(Context ctx) {
    handleTable(
        ctx,
        handler -> {
          UpdateTableRequest request = bodyAsClass(ctx, UpdateTableRequest.class);
          TableOperations ops = handler.newTableOperator();
          TableMetadata base = ops.current();
          if (base == null) {
            throw new CommitFailedException("table metadata lost.");
          }

          TableMetadata.Builder builder = TableMetadata.buildFrom(base);
          request.requirements().forEach(r -> r.validate(base));
          request.updates().forEach(u -> u.applyTo(builder));
          TableMetadata newMetadata = builder.build();

          ops.commit(base, newMetadata);
          TableMetadata current = ops.current();
          return LoadTableResponse.builder().withTableMetadata(current).build();
        });
  }

  /** DELETE PREFIX/v1/catalogs/{catalog}/namespaces/{namespace}/tables/{table} */
  public void deleteTable(Context ctx) {
    handleTable(
        ctx,
        handler -> {
          boolean purge =
              Boolean.parseBoolean(
                  Optional.ofNullable(ctx.req.getParameter("purgeRequested")).orElse("false"));
          org.apache.amoro.server.table.TableMetadata tableMetadata = handler.tableMetadata();
          tableService.dropTableMetadata(tableMetadata.getTableIdentifier().getIdentifier(), purge);
          handler.dropTable(purge);
          return null;
        });
  }

  /** HEAD PREFIX/v1/catalogs/{catalog}/namespaces/{namespace}/tables/{table} */
  public void tableExists(Context ctx) {
    handleTable(ctx, handler -> null);
  }

  /** POST PREFIX/v1/catalogs/{catalog}/tables/rename */
  public void renameTable(Context ctx) {
    throw new UnsupportedOperationException("rename is not supported now.");
  }

  /** POST PREFIX/v1/catalogs/{catalog}/namespaces/{namespace}/tables/{table}/metrics */
  public void reportMetrics(Context ctx) {
    handleTable(
        ctx,
        handler -> {
          String bodyJson = ctx.body();
          ReportMetricsRequest metricsRequest = ReportMetricsRequestParser.fromJson(bodyJson);
          ServerTableIdentifier identifier = handler.tableMetadata().getTableIdentifier();
          IcebergReportEvent event =
              new IcebergReportEvent(
                  identifier.getCatalog(),
                  identifier.getDatabase(),
                  identifier.getTableName(),
                  false,
                  metricsRequest.report());
          EventsManager.getInstance().emit(event);
          return null;
        });
  }

  private <T> T bodyAsClass(Context ctx, Class<T> clz) {
    return jsonMapper.fromJsonString(ctx.body(), clz);
  }

  private void jsonResponse(Context ctx, RESTResponse rsp) {
    ctx.contentType(ContentType.APPLICATION_JSON).result(jsonMapper.toJsonString(rsp));
  }

  private void handleCatalog(
      Context ctx, Function<InternalCatalog, ? extends RESTResponse> handler) {
    String catalog = ctx.pathParam("catalog");
    Preconditions.checkNotNull(catalog, "lack require path params: catalog");
    InternalCatalog internalCatalog = getCatalog(catalog);

    RESTResponse r = handler.apply(internalCatalog);
    if (r != null) {
      jsonResponse(ctx, r);
    } else {
      ctx.status(HttpCode.NO_CONTENT);
    }
  }

  private void handleNamespace(
      Context ctx, BiFunction<InternalCatalog, String, ? extends RESTResponse> handler) {
    handleCatalog(
        ctx,
        catalog -> {
          String ns = ctx.pathParam("namespace");
          Preconditions.checkNotNull(ns, "namespace is null");
          checkUnsupported(!ns.contains("."), "multi-level namespace is not supported");
          checkDatabaseExist(catalog.databaseExists(ns), ns);
          return handler.apply(catalog, ns);
        });
  }

  private void handleTable(
      Context ctx,
      Function<InternalTableHandler<TableOperations>, ? extends RESTResponse> tableHandler) {
    handleNamespace(
        ctx,
        (catalog, database) -> {
          String tableName = ctx.pathParam("table");
          Preconditions.checkNotNull(tableName, "table name is null");
          try (InternalTableHandler<TableOperations> internalTableHandler =
              catalog.newTableHandler(database, tableName)) {
            return tableHandler.apply(internalTableHandler);
          }
        });
  }

  private InternalCatalog getCatalog(String catalog) {
    Preconditions.checkNotNull(catalog, "lack required path variables: catalog");
    ServerCatalog internalCatalog = tableService.getServerCatalog(catalog);
    Preconditions.checkArgument(
        internalCatalog instanceof InternalCatalog, "The catalog is not an iceberg rest catalog");
    Set<TableFormat> tableFormats = CatalogUtil.tableFormats(internalCatalog.getMetadata());
    Preconditions.checkArgument(
        tableFormats.contains(TableFormat.ICEBERG)
            || tableFormats.contains(TableFormat.MIXED_ICEBERG),
        "The catalog is not an iceberg rest catalog");
    return (InternalCatalog) internalCatalog;
  }

  private static void checkUnsupported(boolean condition, String message) {
    if (!condition) {
      throw new UnsupportedOperationException(message);
    }
  }

  private static void checkDatabaseExist(boolean checkCondition, String database) {
    if (!checkCondition) {
      throw new NoSuchNamespaceException("Database: " + database + " doesn't exists");
    }
  }

  private static void checkAlreadyExists(
      boolean checkCondition, String resourceType, String object) {
    if (!checkCondition) {
      throw new AlreadyExistsException(resourceType + ": " + object + " already exists.");
    }
  }

  private ObjectMapper jsonMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.KebabCaseStrategy());
    RESTSerializers.registerAll(mapper);
    return mapper;
  }

  enum IcebergRestErrorCode {
    BadRequest(400),
    NotAuthorized(401),
    Forbidden(403),
    UnsupportedOperation(406),
    AuthenticationTimeout(419),
    NotFound(404),
    Conflict(409),
    InternalServerError(500),
    ServiceUnavailable(503);
    public final int code;

    IcebergRestErrorCode(int code) {
      this.code = code;
    }

    public static IcebergRestErrorCode exceptionToCode(Exception e) {
      if (e instanceof UnsupportedOperationException) {
        return UnsupportedOperation;
      } else if (e instanceof ObjectNotExistsException) {
        return NotFound;
      } else if (e instanceof CommitFailedException) {
        return Conflict;
      } else if (e instanceof NoSuchTableException) {
        return NotFound;
      } else if (e instanceof NoSuchNamespaceException) {
        return NotFound;
      } else if (e instanceof AlreadyExistsException) {
        return Conflict;
      }
      return InternalServerError;
    }
  }
}
