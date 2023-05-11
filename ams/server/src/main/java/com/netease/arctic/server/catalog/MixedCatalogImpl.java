package com.netease.arctic.server.catalog;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.catalog.MixedTables;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableMetadata;
import com.netease.arctic.table.ArcticTable;

import java.util.List;
import java.util.stream.Collectors;

public class MixedCatalogImpl extends InternalCatalog {

  private final MixedTables tables;

  protected MixedCatalogImpl(CatalogMeta metadata) {
    super(metadata);
    this.tables = new MixedTables(metadata);
  }

  @Override
  public List<String> listDatabases() {
    return getAs(TableMetaMapper.class, mapper -> mapper.selectDatabases(getMetadata().getCatalogName()));
  }

  @Override
  public List<TableIdentifier> listTables(String database) {
    return getAs(
        TableMetaMapper.class,
        mapper -> mapper.selectTableIdentifiersByDb(getMetadata().getCatalogName(), database))
        .stream()
        .map(ServerTableIdentifier::getIdentifier)
        .collect(Collectors.toList());
  }

  @Override
  public List<TableIdentifier> listTables() {
    return getAs(
        TableMetaMapper.class,
        mapper -> mapper.selectTableIdentifiersByCatalog(getMetadata().getCatalogName()))
        .stream()
        .map(ServerTableIdentifier::getIdentifier)
        .collect(Collectors.toList());
  }

  @Override
  public ArcticTable loadTable(String database, String tableName) {
    TableMetadata tableMetadata = getAs(TableMetaMapper.class, mapper ->
        mapper.selectTableMetaByName(getMetadata().getCatalogName(), database, tableName));
    return tableMetadata == null ? null : tables.loadTableByMeta(tableMetadata.buildTableMeta());
  }

  @Override
  public boolean exist(String database) {
    return getAs(TableMetaMapper.class, mapper ->
        mapper.selectDatabase(getMetadata().getCatalogName(), database)) != null;
  }

  @Override
  public boolean exist(String database, String tableName) {
    ServerTableIdentifier tableIdentifier = getAs(TableMetaMapper.class, mapper ->
        mapper.selectTableIdentifier(getMetadata().getCatalogName(), database, tableName));
    return tableIdentifier != null && getAs(TableMetaMapper.class, mapper ->
        mapper.selectTableMetaById(tableIdentifier.getId())) != null;
  }

  @Override
  protected void createTableInternal(TableMetadata tableMetadata) {
    doAs(TableMetaMapper.class, mapper -> mapper.insertTableMeta(tableMetadata));
  }
}
