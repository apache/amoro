package com.netease.arctic.server.catalog;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.CommonUnifiedCatalog;
import com.netease.arctic.UnifiedCatalog;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.utils.CatalogUtil;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class PaimonServerCatalog extends ExternalCatalog {

  private volatile UnifiedCatalog paimonCatalog;

  private volatile TableMetaStore tableMetaStore;

  protected PaimonServerCatalog(CatalogMeta metadata) {
    super(metadata);
    this.tableMetaStore = CatalogUtil.buildMetaStore(metadata);
    this.paimonCatalog = doAs(() -> new CommonUnifiedCatalog(
        null,
        metadata,
        metadata.catalogProperties
    ));
  }

  @Override
  public void updateMetadata(CatalogMeta metadata) {
    super.updateMetadata(metadata);
    this.tableMetaStore = CatalogUtil.buildMetaStore(metadata);
    this.paimonCatalog = doAs(() -> new CommonUnifiedCatalog(
        null,
        metadata,
        metadata.catalogProperties
    ));
  }

  @Override
  public boolean exist(String database) {
    return doAs(() -> paimonCatalog.exist(database));
  }

  @Override
  public boolean exist(String database, String tableName) {
    return doAs(() -> paimonCatalog.exist(database, tableName));
  }

  @Override
  public List<String> listDatabases() {
    return doAs(() -> paimonCatalog.listDatabases());
  }

  @Override
  public List<TableIdentifier> listTables() {
    return doAs(() -> paimonCatalog.listDatabases()
        .stream()
        .map(this::listTables)
        .flatMap(List::stream)
        .collect(Collectors.toList()));
  }

  @Override
  public List<TableIdentifier> listTables(String database) {
    return doAs(() -> paimonCatalog.listTableMetas(database)
        .stream()
        .map(t -> t.getIdentifier().buildTableIdentifier())
        .collect(Collectors.toList()));
  }

  @Override
  public AmoroTable<?> loadTable(String database, String tableName) {
    return doAs(() -> paimonCatalog.loadTable(database, tableName));
  }

  private <T> T doAs(Callable<T> callable) {
    return tableMetaStore.doAs(callable);
  }
}
