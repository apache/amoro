package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.ams.server.repair.Context;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.table.TableIdentifier;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.thrift.TException;

public class ShowCall implements CallCommand {

  /**
   * DATABASES, TABLES
   */
  public enum Namespaces {
    DATABASES,
    TABLES
  }

  private ArcticCatalog arcticCatalog;
  private Namespaces namespaces;

  public ShowCall(ArcticCatalog arcticCatalog, Namespaces namespaces) {
    this.arcticCatalog = arcticCatalog;
    this.namespaces = namespaces;
  }

  @Override
  public String call(Context context) throws TException {
    switch (this.namespaces) {
      case DATABASES:
        return arcticCatalog.listDatabases().stream().collect(Collectors.joining("\\n"));
      case TABLES:
        return arcticCatalog.listTables(context.getDb())
            .stream()
            .map(TableIdentifier::getTableName)
            .collect(Collectors.joining("\\n"));
      default:
        throw new UnsupportedOperationException("not support show operation named:" + this.namespaces);
    }
  }
}
