package com.netease.arctic.server.catalog;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.CommonUnifiedCatalog;
import com.netease.arctic.UnifiedCatalog;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableIdentifier;
import java.util.List;
import java.util.stream.Collectors;

public class PaimonServerCatalog extends ExternalCatalog {

  private volatile UnifiedCatalog paimonCatalog;

  protected PaimonServerCatalog(CatalogMeta metadata) {
    super(metadata);
    this.paimonCatalog = new CommonUnifiedCatalog(
        null,
        metadata,
        metadata.catalogProperties
    );
  }

  @Override
  public void updateMetadata(CatalogMeta metadata) {
    super.updateMetadata(metadata);
    this.paimonCatalog = new CommonUnifiedCatalog(
        null,
        metadata,
        metadata.catalogProperties
    );
  }

  @Override
  public boolean exist(String database) {
    return paimonCatalog.exist(database);
  }

  @Override
  public boolean exist(String database, String tableName) {
    return paimonCatalog.exist(database, tableName);
  }

  @Override
  public List<String> listDatabases() {
    return paimonCatalog.listDatabases();
  }

  @Override
  public List<TableIdentifier> listTables() {
    return paimonCatalog.listDatabases()
        .stream()
        .map(this::listTables)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  @Override
  public List<TableIdentifier> listTables(String database) {
    return paimonCatalog.listTableMetas(database)
        .stream()
        .map(t -> t.getIdentifier().buildTableIdentifier())
        .collect(Collectors.toList());
  }

  @Override
  public AmoroTable<?> loadTable(String database, String tableName) {
    return paimonCatalog.loadTable(database, tableName);
  }
}
