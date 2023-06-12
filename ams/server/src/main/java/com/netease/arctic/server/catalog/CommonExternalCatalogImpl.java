package com.netease.arctic.server.catalog;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.catalog.CatalogOperations;
import com.netease.arctic.catalog.ExternalCatalogOperations;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.utils.CatalogUtil;

import java.util.List;
import java.util.stream.Collectors;

public class CommonExternalCatalogImpl extends ExternalCatalog {

  private CatalogOperations operations;

  protected CommonExternalCatalogImpl(CatalogMeta metadata) {
    super(metadata);
    this.operations = new ExternalCatalogOperations(metadata);
  }

  @Override
  public void updateMetadata(CatalogMeta metadata) {
    super.updateMetadata(metadata);
    this.operations = new ExternalCatalogOperations(metadata);
  }

  @Override
  public boolean exist(String database) {
    return operations.exist(database);
  }

  @Override
  public boolean exist(String database, String tableName) {
    return operations.exist(database, tableName);
  }

  @Override
  public List<String> listDatabases() {
    return operations.listDatabases();
  }

  @Override
  public List<TableIdentifier> listTables() {
    throw new UnsupportedOperationException("");
  }

  @Override
  public List<TableIdentifier> listTables(String database) {
    throw new UnsupportedOperationException("");
  }

  public List<TableIdentifier> toAmsIdList(List<com.netease.arctic.table.TableIdentifier> identifierList) {
    return identifierList.stream().map(CatalogUtil::amsTaleId).collect(Collectors.toList());
  }

  @Override
  public ArcticTable loadTable(String database, String tableName) {
    return operations.loadTable(database, tableName);
  }
}
