package com.netease.arctic.server.catalog;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.server.exception.IllegalMetadataException;
import com.netease.arctic.server.exception.ObjectNotExistsException;
import com.netease.arctic.server.persistence.mapper.CatalogMetaMapper;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableMetadata;

public abstract class InternalCatalog extends ServerCatalog {

  protected InternalCatalog(CatalogMeta metadata) {
    super(metadata);
  }

  public void createDatabase(String databaseName) {
    if (!exist(databaseName)) {
      doAsTransaction(
          () -> doAsExisted(
              CatalogMetaMapper.class,
              mapper -> mapper.incDatabaseCount(1, name()),
              () -> new ObjectNotExistsException(name())),
          () -> doAs(
              TableMetaMapper.class,
              mapper -> mapper.insertDatabase(getMetadata().getCatalogName(), databaseName)),
          () -> createDatabaseInternal(databaseName));
    }
  }

  public void dropDatabase(String databaseName) {
    if (exist(databaseName)) {
      doAsTransaction(
          () -> doAsExisted(
              TableMetaMapper.class,
              mapper -> mapper.dropDb(getMetadata().getCatalogName(), databaseName),
              () -> new IllegalMetadataException("Database " + databaseName + " has more than one table")),
          () -> dropDatabaseInternal(databaseName),
          () -> doAsExisted(
              CatalogMetaMapper.class,
              mapper -> mapper.decDatabaseCount(1, name()),
              () -> new ObjectNotExistsException(name())));
    }
  }

  public ServerTableIdentifier createTable(TableMeta tableMeta) {
    ServerTableIdentifier tableIdentifier = ServerTableIdentifier.of(tableMeta.getTableIdentifier());
    TableMetadata tableMetadata = new TableMetadata(tableMeta, getMetadata());
    doAsTransaction(
        () -> doAs(TableMetaMapper.class, mapper -> mapper.insertTable(tableIdentifier)),
        () -> createTableInternal(tableMetadata),
        () -> doAsExisted(CatalogMetaMapper.class, mapper ->
                mapper.incTableCount(1, name()),
            () -> new ObjectNotExistsException(name())),
        () -> doAsExisted(
            TableMetaMapper.class,
            mapper -> mapper.incTableCount(1, tableIdentifier.getDatabase()),
            () -> new ObjectNotExistsException(getDatabaseDesc(tableIdentifier.getDatabase()))));
    return getAs(
        TableMetaMapper.class,
        mapper -> mapper.selectTableIdentifier(tableMeta.getTableIdentifier().getCatalog(),
            tableMeta.getTableIdentifier().getDatabase(), tableMeta.getTableIdentifier().getTableName()));
  }

  public ServerTableIdentifier dropTable(String databaseName, String tableName) {
    ServerTableIdentifier tableIdentifier = getAs(TableMetaMapper.class, mapper -> mapper
        .selectTableIdentifier(getMetadata().getCatalogName(), databaseName, tableName));
    if (tableIdentifier.getId() == null) {
      throw new ObjectNotExistsException(getTableDesc(databaseName, tableName));
    }
    doAsTransaction(
        () -> doAsExisted(
            TableMetaMapper.class,
            mapper -> mapper.deleteTableIdById(tableIdentifier.getId()),
            () -> new ObjectNotExistsException(getTableDesc(databaseName, tableName))),
        () -> dropTableInternal(databaseName, tableName),
        () -> doAsExisted(
            CatalogMetaMapper.class,
            mapper -> mapper.decTableCount(1, tableIdentifier.getCatalog()),
            () -> new ObjectNotExistsException(name())),
        () -> doAsExisted(
            TableMetaMapper.class,
            mapper -> mapper.decTableCount(1, tableIdentifier.getDatabase()),
            () -> new ObjectNotExistsException(getDatabaseDesc(tableIdentifier.getDatabase()))));
    return tableIdentifier;
  }

  private String getDatabaseDesc(String database) {
    return new StringBuilder()
        .append(name())
        .append('.')
        .append(database)
        .toString();
  }

  private String getTableDesc(String database, String tableName) {
    return new StringBuilder()
        .append(name())
        .append('.')
        .append(database)
        .append('.')
        .append(tableName)
        .toString();
  }

  public Integer getTableCount() {
    return getAs(CatalogMetaMapper.class, mapper -> mapper.selectTableCount(name()));
  }

  public Integer getTableCount(String databaseName) {
    return getAs(TableMetaMapper.class, mapper -> mapper.selectTableCount(name()));
  }

  protected void createTableInternal(TableMetadata tableMetaData) {
    //do nothing, create internal table default done on client side
  }

  protected void createDatabaseInternal(String databaseName) {
    //do nothing, create internal table default done on client side
  }

  protected void dropTableInternal(String databaseName, String tableName) {
    //do nothing, create internal table default done on client side
  }

  protected void dropDatabaseInternal(String databaseName) {
    //do nothing, create internal table default done on client side
  }
}
