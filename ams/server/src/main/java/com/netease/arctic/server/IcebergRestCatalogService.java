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
import com.netease.arctic.server.dashboard.controller.IcebergRestCatalogController;
import com.netease.arctic.server.table.TableService;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.utils.CatalogUtil;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.plugin.json.JavalinJackson;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.rest.RESTResponse;
import org.apache.iceberg.rest.RESTSerializers;
import org.apache.iceberg.rest.requests.CreateNamespaceRequest;
import org.apache.iceberg.rest.requests.CreateTableRequest;
import org.apache.iceberg.rest.responses.ConfigResponse;
import org.apache.iceberg.rest.responses.CreateNamespaceResponse;
import org.apache.iceberg.rest.responses.ErrorResponse;
import org.apache.iceberg.rest.responses.ListNamespacesResponse;
import org.apache.iceberg.rest.responses.ListTablesResponse;
import org.apache.iceberg.rest.responses.LoadTableResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class IcebergRestCatalogService {
  public static final String REST_CATALOG_API_PREFIX = "/api/iceberg/rest/catalog";
  public static final String DEFAULT_FILE_IO_IMPL = "org.apache.iceberg.io.ResolvingFileIO";
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
      });
    };
  }


  public boolean needHandleException(Context ctx) {
    return ctx.req.getRequestURI().startsWith(IcebergRestCatalogController.REST_CATALOG_API_PREFIX);
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
      TableMetadata tableMetadata = TableMetadata.newTableMetadata(
          request.schema(), request.spec(), request.writeOrder(), request.location(), request.properties()
      );
      TableMetaStore tableMetaStore = CatalogUtil.buildMetaStore(catalog.getMetadata());
      FileIO io = newFileIo(catalog.getMetadata().getCatalogProperties(), tableMetaStore.getConfiguration());
      String metadataLocation = request.location() + "/metadata/v1.metadata.json" ;
      OutputFile outputFile = io.newOutputFile(metadataLocation);


      return LoadTableResponse.builder()
          .build();
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

  private FileIO newFileIo(Map<String, String> catalogProperties, Configuration conf) {
    String ioImpl = catalogProperties.getOrDefault(CatalogProperties.FILE_IO_IMPL, DEFAULT_FILE_IO_IMPL);
    return org.apache.iceberg.CatalogUtil.loadFileIO(ioImpl, catalogProperties, conf);
  }

  enum IcebergRestErrorCode {
    BadRequest(400),
    NotAuthorized(401),
    Forbidden(403),
    UnsupportedOperation(406),
    AuthenticationTimeout(419),
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
      }
      return InternalServerError;
    }
  }
}
