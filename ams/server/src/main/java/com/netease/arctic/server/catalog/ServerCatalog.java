package com.netease.arctic.server.catalog;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.TableIDWithFormat;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.server.exception.IllegalMetadataException;
import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.mapper.CatalogMetaMapper;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;

import java.util.List;

public abstract class ServerCatalog extends PersistentBase {

  private volatile CatalogMeta metadata;

  protected ServerCatalog(CatalogMeta metadata) {
    this.metadata = metadata;
  }

  public String name() {
    return metadata.getCatalogName();
  }

  public CatalogMeta getMetadata() {
    return metadata;
  }

  public void updateMetadata(CatalogMeta metadata) {
    doAs(CatalogMetaMapper.class, mapper -> mapper.updateCatalog(metadata));
    this.metadata = metadata;
  }

  public abstract boolean exist(String database);

  public abstract boolean exist(String database, String tableName);

  public abstract List<String> listDatabases();

  public abstract List<TableIDWithFormat> listTables();

  public abstract List<TableIDWithFormat> listTables(String database);

  public abstract AmoroTable<?> loadTable(String database, String tableName);

  public void dispose() {
    doAsTransaction(
        () ->
            doAsExisted(
                CatalogMetaMapper.class,
                mapper -> mapper.deleteCatalog(name()),
                () ->
                    new IllegalMetadataException(
                        "Catalog " + name() + " has more than one database or table")));
  }
}
