package com.netease.arctic.server.catalog;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.exception.ObjectNotExistsException;
import com.netease.arctic.formats.iceberg.IcebergTable;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.server.ArcticManagementConf;
import com.netease.arctic.server.RestCatalogService;
import com.netease.arctic.server.table.TableMetadata;
import com.netease.arctic.server.table.internal.InternalIcebergCreator;
import com.netease.arctic.server.table.internal.InternalIcebergHandler;
import com.netease.arctic.server.table.internal.InternalTableCreator;
import com.netease.arctic.server.table.internal.InternalTableHandler;
import com.netease.arctic.server.utils.Configurations;
import com.netease.arctic.server.utils.InternalTableUtil;
import com.netease.arctic.utils.CatalogUtil;
import org.apache.iceberg.BaseTable;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.rest.RESTCatalog;
import org.apache.iceberg.rest.requests.CreateTableRequest;

public class InternalIcebergCatalogImpl extends InternalCatalog {

  private static final String URI = "uri";

  final int httpPort;
  final String exposedHost;

  protected InternalIcebergCatalogImpl(CatalogMeta metadata, Configurations serverConfiguration) {
    super(metadata);
    this.httpPort = serverConfiguration.getInteger(ArcticManagementConf.HTTP_SERVER_PORT);
    this.exposedHost = serverConfiguration.getString(ArcticManagementConf.SERVER_EXPOSE_HOST);
  }

  @Override
  public CatalogMeta getMetadata() {
    CatalogMeta meta = super.getMetadata();
    if (!meta.getCatalogProperties().containsKey(URI)) {
      meta.putToCatalogProperties(URI, defaultRestURI());
    }
    meta.putToCatalogProperties(CatalogProperties.CATALOG_IMPL, RESTCatalog.class.getName());
    return meta.deepCopy();
  }

  @Override
  public void updateMetadata(CatalogMeta metadata) {
    String defaultUrl = defaultRestURI();
    String uri = metadata.getCatalogProperties().getOrDefault(URI, defaultUrl);
    if (defaultUrl.equals(uri)) {
      metadata.getCatalogProperties().remove(URI);
    }
    super.updateMetadata(metadata);
  }

  @Override
  public AmoroTable<?> loadTable(String database, String tableName) {
    InternalTableHandler<TableOperations> handler;
    try {
      handler = newTableHandler(database, tableName);
    } catch (ObjectNotExistsException e) {
      return null;
    }
    TableMetadata tableMetadata = handler.tableMetadata();
    TableOperations ops = handler.newTableOperator();

    BaseTable table =
        new BaseTable(
            ops,
            TableIdentifier.of(
                    tableMetadata.getTableIdentifier().getDatabase(),
                    tableMetadata.getTableIdentifier().getTableName())
                .toString());
    com.netease.arctic.table.TableIdentifier tableIdentifier =
        com.netease.arctic.table.TableIdentifier.of(name(), database, tableName);

    return IcebergTable.newIcebergTable(
        tableIdentifier,
        table,
        CatalogUtil.buildMetaStore(getMetadata()),
        getMetadata().getCatalogProperties());
  }

  protected ArcticFileIO fileIO(CatalogMeta catalogMeta) {
    return InternalTableUtil.newIcebergFileIo(catalogMeta);
  }

  private String defaultRestURI() {
    return "http://" + exposedHost + ":" + httpPort + RestCatalogService.ICEBERG_REST_API_PREFIX;
  }

  @Override
  public InternalTableCreator newTableCreator(
      String database, String tableName, TableFormat format, CreateTableRequest creatorArguments) {

    Preconditions.checkArgument(
        format == format(), "the catalog only support to create %s table", format().name());
    if (exist(database, tableName)) {
      throw new AlreadyExistsException(
          "Table " + name() + "." + database + "." + tableName + " already " + "exists.");
    }
    return newTableCreator(database, tableName, creatorArguments);
  }

  protected TableFormat format() {
    return TableFormat.ICEBERG;
  }

  protected InternalTableCreator newTableCreator(
      String database, String tableName, CreateTableRequest request) {
    return new InternalIcebergCreator(getMetadata(), database, tableName, request);
  }

  @Override
  public <O> InternalTableHandler<O> newTableHandler(String database, String tableName) {
    TableMetadata metadata = loadTableMetadata(database, tableName);
    Preconditions.checkState(
        metadata.getFormat() == format(),
        "the catalog only support to handle %s table",
        format().name());
    //noinspection unchecked
    return (InternalTableHandler<O>) new InternalIcebergHandler(getMetadata(), metadata);
  }
}
