package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.ams.server.repair.Context;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogManager;
import com.netease.arctic.table.TableIdentifier;

import java.util.stream.Collectors;

public class ShowCall implements CallCommand {

  private CatalogManager catalogManager;
  private Namespaces namespaces;

  public ShowCall(CatalogManager catalogManager, Namespaces namespaces) {
    this.catalogManager = catalogManager;
    this.namespaces = namespaces;
  }

  @Override
  public String call(Context context) throws FullTableNameException {
    switch (this.namespaces) {
      case CATALOGS:
        return catalogManager.catalogs().stream().collect(Collectors.joining("\\n"));
      case DATABASES:
      case TABLES:
        if (context.getCatalog() == null) {
          throw new FullTableNameException("Can not find catalog name, your can use 'USE ${catalog}' statement");
        }
        ArcticCatalog arcticCatalog = catalogManager.getArcticCatalog(context.getCatalog());
        if (this.namespaces == Namespaces.DATABASES) {
          return arcticCatalog.listDatabases().stream().collect(Collectors.joining("\\n"));
        } else {
          if (context.getDb() == null) {
            throw new FullTableNameException("Can not find database name, your can use 'USE ${database}' statement");
          }
          return arcticCatalog.listTables(context.getDb())
              .stream()
              .map(TableIdentifier::getTableName)
              .collect(Collectors.joining("\\n"));
        }
      default:
        throw new UnsupportedOperationException("not support show operation named:" + this.namespaces);
    }
  }

  public enum Namespaces {
    CATALOGS,
    DATABASES,
    TABLES
  }
}
