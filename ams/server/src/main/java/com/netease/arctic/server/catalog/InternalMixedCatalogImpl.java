package com.netease.arctic.server.catalog;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.MixedTables;
import com.netease.arctic.formats.mixed.MixedTable;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.table.TableMetadata;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.utils.CatalogUtil;

import java.util.Map;

public class InternalMixedCatalogImpl extends InternalCatalog {

  protected MixedTables tables;

  protected InternalMixedCatalogImpl(CatalogMeta metadata) {
    super(metadata);
    this.tables = newTables(metadata.getCatalogProperties(), CatalogUtil.buildMetaStore(metadata));
  }

  protected MixedTables newTables(Map<String, String> catalogProperties, TableMetaStore metaStore) {
    return new MixedTables(catalogProperties, metaStore);
  }

  @Override
  public void updateMetadata(CatalogMeta metadata) {
    super.updateMetadata(metadata);
    this.tables = newTables(metadata.getCatalogProperties(), CatalogUtil.buildMetaStore(metadata));
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
    return new MixedTable(
        tables.loadTableByMeta(tableMetadata.buildTableMeta()), TableFormat.MIXED_ICEBERG);
  }

  protected MixedTables tables() {
    return tables;
  }
}
