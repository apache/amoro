package com.netease.arctic.server.catalog;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.formats.iceberg.IcebergTable;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.server.ArcticManagementConf;
import com.netease.arctic.server.IcebergRestCatalogService;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.table.TableMetadata;
import com.netease.arctic.server.utils.Configurations;
import com.netease.arctic.server.utils.InternalTableUtil;
import com.netease.arctic.utils.CatalogUtil;
import org.apache.iceberg.BaseTable;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.rest.RESTCatalog;

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
    TableMetadata tableMetadata =
        getAs(
            TableMetaMapper.class,
            mapper ->
                mapper.selectTableMetaByName(getMetadata().getCatalogName(), database, tableName));
    if (tableMetadata == null) {
      return null;
    }
    ArcticFileIO fileIO = fileIO(getMetadata());
    BaseTable table = loadIcebergTable(fileIO, tableMetadata);
    com.netease.arctic.table.TableIdentifier tableIdentifier =
        com.netease.arctic.table.TableIdentifier.of(name(), database, tableName);

    return IcebergTable.newIcebergTable(
        tableIdentifier,
        table,
        CatalogUtil.buildMetaStore(getMetadata()),
        getMetadata().getCatalogProperties());
  }

  protected BaseTable loadIcebergTable(ArcticFileIO fileIO, TableMetadata tableMetadata) {
    TableOperations ops =
        InternalTableUtil.newTableOperations(getMetadata(), tableMetadata, fileIO, false);
    return new BaseTable(
        ops,
        TableIdentifier.of(
                tableMetadata.getTableIdentifier().getDatabase(),
                tableMetadata.getTableIdentifier().getTableName())
            .toString());
  }

  protected ArcticFileIO fileIO(CatalogMeta catalogMeta) {
    return InternalTableUtil.newIcebergFileIo(catalogMeta);
  }

  private String defaultRestURI() {
    return "http://"
        + exposedHost
        + ":"
        + httpPort
        + IcebergRestCatalogService.ICEBERG_REST_API_PREFIX;
  }
}
