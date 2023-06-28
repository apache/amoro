package com.netease.arctic.server;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.server.catalog.InternalCatalog;
import com.netease.arctic.server.catalog.ServerCatalog;
import com.netease.arctic.server.exception.ObjectNotExistsException;
import com.netease.arctic.server.iceberg.InternalTableOperations;
import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableService;
import com.netease.arctic.server.utils.IcebergTableUtils;
import com.netease.arctic.utils.CatalogUtil;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import io.javalin.plugin.json.JavalinJackson;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.rest.RESTResponse;
import org.apache.iceberg.rest.RESTSerializers;
import org.apache.iceberg.rest.requests.CreateNamespaceRequest;
import org.apache.iceberg.rest.requests.CreateTableRequest;
import org.apache.iceberg.rest.requests.ReportMetricsRequest;
import org.apache.iceberg.rest.requests.UpdateTableRequest;
import org.apache.iceberg.rest.responses.ConfigResponse;
import org.apache.iceberg.rest.responses.CreateNamespaceResponse;
import org.apache.iceberg.rest.responses.ErrorResponse;
import org.apache.iceberg.rest.responses.ListNamespacesResponse;
import org.apache.iceberg.rest.responses.ListTablesResponse;
import org.apache.iceberg.rest.responses.LoadTableResponse;
import org.apache.iceberg.util.LocationUtil;
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

import static com.netease.arctic.server.utils.IcebergTableUtils.loadIcebergTableMetadata;
import static com.netease.arctic.server.utils.IcebergTableUtils.newIcebergFileIo;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.head;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class IcebergRestCatalogService extends PersistentBase {

  private static final Logger LOG = LoggerFactory.getLogger(IcebergRestCatalogService.class);

  public static final String REST_CATALOG_API_PREFIX = "/api/iceberg/rest/catalog";

  private final TableService tableService;

  private final Set<String> catalogPropertiesNotReturned = Collections.unmodifiableSet(
      Sets.newHashSet(CatalogMetaProperties.TABLE_FORMATS)
  );

  private final Set<String> catalogPropertiesOverwrite = Collections.unmodifiableSet(
      Sets.newHashSet(CatalogMetaProperties.KEY_WAREHOUSE)
  );


  public IcebergRestCatalogService(TableService tableService) {
    this.tableService = tableService;
  }


  public JavalinJackson jsonMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.KebabCaseStrategy());
    RESTSerializers.registerAll(mapper);
    return new JavalinJackson(mapper);
  }


  public EndpointGroup endpoints() {
    return () -> {
      // for iceberg rest catalog api
      path(REST_CATALOG_API_PREFIX, () -> {
        get("/{catalog}/v1/config", this::getCatalogConfig);
        get("/{catalog}/v1/namespaces", this::listNamespaces);
        post("/{catalog}/v1/namespaces", this::createNamespace);
        get("/{catalog}/v1/namespaces/{namespace}", this::getNamespace);
        delete("/{catalog}/v1/namespaces/{namespace}", this::dropNamespace);
        post("/{catalog}/v1/namespaces/{namespace}", this::setNamespaceProperties);
        get("/{catalog}/v1/namespaces/{namespace}/tables", this::listTablesInNamespace);
        post("/{catalog}/v1/namespaces/{namespace}/tables", this::createTable);
        get("/{catalog}/v1/namespaces/{namespace}/tables/{table}", this::loadTable);
        post("/{catalog}/v1/namespaces/{namespace}/tables/{table}", this::commitTable);
        delete("/{catalog}/v1/namespaces/{namespace}/tables/{table}", this::deleteTable);
        head("/{catalog}/v1/namespaces/{namespace}/tables/{table}", this::tableExists);
        post("/{catalog}/v1/tables/rename", this::renameTable);
        post("/{catalog}/v1/namespaces/{namespace}/tables/{table}/metrics", this::metricReport);
      });
    };
  }


  public boolean needHandleException(Context ctx) {
    return ctx.req.getRequestURI().startsWith(REST_CATALOG_API_PREFIX);
  }

  public void handleException(Exception e, Context ctx) {
    IcebergRestErrorCode code = IcebergRestErrorCode.exceptionToCode(e);
    ErrorResponse response = ErrorResponse.builder()
        .responseCode(code.code)
        .withType(e.getClass().getSimpleName())
        .withMessage(e.getMessage())
        .build();
    ctx.res.setStatus(code.code);
    ctx.json(response);
    if (code.code >= 500) {
      LOG.warn("InternalServer Error", e);
    }
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
    handleNamespace(ctx, (catalog, database) -> {
      List<TableIdentifier> tableIdentifiers = catalog.listTables(database).stream()
          .map(i -> TableIdentifier.of(database, i.getTableName()))
          .collect(Collectors.toList());

      return ListTablesResponse.builder()
          .addAll(tableIdentifiers)
          .build();
    });
  }

  /**
   * POST PREFIX/{catalog}/v1/namespaces/{namespace}/tables
   */
  public void createTable(Context ctx) {
    handleNamespace(ctx, (catalog, database) -> {
      Preconditions.checkArgument(catalog.exist(database));
      CreateTableRequest request = ctx.bodyAsClass(CreateTableRequest.class);
      String tableName = request.name();
      Preconditions.checkArgument(!catalog.exist(database, tableName));
      String location = request.location();
      if (StringUtils.isBlank(location)) {
        String warehouse = catalog.getMetadata().getCatalogProperties().get(CatalogMetaProperties.KEY_WAREHOUSE);
        Preconditions.checkState(StringUtils.isNotBlank(warehouse),
            "catalog warehouse is not configured");
        warehouse = LocationUtil.stripTrailingSlash(warehouse);
        location = warehouse + "/" + database + ".db/" + tableName;
      } else {
        location = LocationUtil.stripTrailingSlash(location);
      }
      PartitionSpec spec = request.spec();
      SortOrder sortOrder = request.writeOrder();
      TableMetadata tableMetadata = TableMetadata.newTableMetadata(
          request.schema(),
          spec != null ? spec : PartitionSpec.unpartitioned(),
          sortOrder != null ? sortOrder : SortOrder.unsorted(),
          location, request.properties()
      );
      ServerTableIdentifier identifier = ServerTableIdentifier.of(catalog.name(), database, tableName);

      String newMetadataFileLocation = IcebergTableUtils.genNewMetadataFileLocation(null, tableMetadata);
      FileIO io = newIcebergFileIo(catalog.getMetadata());
      try {
        com.netease.arctic.server.table.TableMetadata amsTableMeta = IcebergTableUtils.createTableInternal(
            identifier, catalog.getMetadata(), tableMetadata, newMetadataFileLocation, io
        );
        tableService.createTable(catalog.name(), amsTableMeta);
        TableMetadata current = loadIcebergTableMetadata(io, amsTableMeta);
        return LoadTableResponse.builder()
            .withTableMetadata(current)
            .build();
      } catch (RuntimeException e) {
        io.deleteFile(newMetadataFileLocation);
        throw e;
      } finally {
        io.close();
      }
    });
  }

  /**
   * GET PREFIX/{catalog}/v1/namespaces/{namespace}/tables/{table}
   */
  public void loadTable(Context ctx) {
    handleTable(ctx, (catalog, tableMeta) -> {
      TableMetadata tableMetadata = null;
      try (FileIO io = newIcebergFileIo(catalog.getMetadata())) {
        tableMetadata = IcebergTableUtils.loadIcebergTableMetadata(io, tableMeta);
      }
      if (tableMetadata == null) {
        throw new NoSuchTableException("failed to load table from metadata file.");
      }
      return LoadTableResponse.builder()
          .withTableMetadata(tableMetadata)
          .build();

    });
  }

  /**
   * POST PREFIX/{catalog}/v1/namespaces/{namespace}/tables/{table}
   */
  public void commitTable(Context ctx) {
    handleTable(ctx, (catalog, tableMeta) -> {
      UpdateTableRequest request = ctx.bodyAsClass(UpdateTableRequest.class);
      try (FileIO io = newIcebergFileIo(catalog.getMetadata())) {
        TableOperations ops = InternalTableOperations.buildForLoad(tableMeta, io);
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
        return LoadTableResponse.builder()
            .withTableMetadata(current)
            .build();
      }
    });
  }


  /**
   * DELETE PREFIX/{catalog}/v1/namespaces/{namespace}/tables/{table}
   */
  public void deleteTable(Context ctx) {
    handleTable(ctx, (catalog, tableMetadata) -> {
      boolean purge = Boolean.parseBoolean(
          Optional.ofNullable(ctx.req.getParameter("purgeRequested")).orElse("false"));
      TableMetadata current = null;
      try (FileIO io = newIcebergFileIo(catalog.getMetadata())) {
        try {
          current = IcebergTableUtils.loadIcebergTableMetadata(io, tableMetadata);
        } catch (Exception e) {
          LOG.warn("failed to load iceberg table metadata, metadata file maybe lost: " + e.getMessage());
        }

        tableService.dropTableMetadata(
            tableMetadata.getTableIdentifier().getIdentifier(), true);
        if (purge && current != null) {
          org.apache.iceberg.CatalogUtil.dropTableData(io, current);
        }
      }


      ctx.status(HttpCode.NO_CONTENT);
      return null;
    });
  }


  /**
   * HEAD PREFIX/{catalog}/v1/namespaces/{namespace}/tables/{table}
   */
  public void tableExists(Context ctx) {
    handleTable(ctx, ((catalog, tableMeta) -> null));
  }


  /**
   * POST PREFIX/{catalog}/v1/tables/rename
   */
  public void renameTable(Context ctx) {
    throw new UnsupportedOperationException("rename is not supported now.");
  }


  /**
   * POST PREFIX/{catalog}/v1/namespaces/{namespace}/tables/{table}/metrics
   */
  public void metricReport(Context ctx) {
    handleTable(ctx, (catalog, tableMeta) -> {
      ReportMetricsRequest request = ctx.bodyAsClass(ReportMetricsRequest.class);
      LOG.debug(request.toString());
      return null;
    });
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


  private void handleNamespace(Context ctx, BiFunction<InternalCatalog, String, ? extends RESTResponse> handler) {
    handleCatalog(ctx, catalog -> {
      String ns = ctx.pathParam("namespace");
      Preconditions.checkNotNull(ns, "namespace is null");
      checkUnsupported(!ns.contains("."), "multi-level namespace is not supported");
      return handler.apply(catalog, ns);
    });
  }


  private void handleTable(
      Context ctx,
      BiFunction<InternalCatalog, com.netease.arctic.server.table.TableMetadata, ? extends RESTResponse> handler) {
    handleNamespace(ctx, (catalog, database) -> {
      Preconditions.checkArgument(catalog.exist(database));
      String tableName = ctx.pathParam("table");
      Preconditions.checkNotNull(tableName, "table name is null");
      com.netease.arctic.server.table.TableMetadata metadata = tableService.loadTableMetadata(
          com.netease.arctic.table.TableIdentifier.of(catalog.name(), database, tableName).buildTableIdentifier()
      );
      Preconditions.checkArgument(metadata.getFormat() == TableFormat.ICEBERG,
          "it's not an iceberg table");
      return handler.apply(catalog, metadata);
    });
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

  enum IcebergRestErrorCode {
    BadRequest(400),
    NotAuthorized(401),
    Forbidden(403),
    UnsupportedOperation(406),
    AuthenticationTimeout(419),
    NotFound(404),
    Conflict(409),
    InternalServerError(500),
    ServiceUnavailable(503),

    ;
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
      }
      return InternalServerError;
    }
  }
}
