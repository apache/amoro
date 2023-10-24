package com.netease.arctic.server.table;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.TableIdentifier;

import java.util.Objects;

public class ServerTableIdentifier {

  private Long id;
  private String catalog;
  private String database;
  private String tableName;
  private TableFormat format;

  // used by the MyBatis framework.
  private ServerTableIdentifier() {}

  private ServerTableIdentifier(TableIdentifier tableIdentifier, TableFormat format) {
    this.catalog = tableIdentifier.getCatalog();
    this.database = tableIdentifier.getDatabase();
    this.tableName = tableIdentifier.getTableName();
    this.format = format;
  }

  private ServerTableIdentifier(
      String catalog, String database, String tableName, TableFormat format) {
    this.catalog = catalog;
    this.database = database;
    this.tableName = tableName;
    this.format = format;
  }

  private ServerTableIdentifier(
      Long id, String catalog, String database, String tableName, TableFormat format) {
    this.id = id;
    this.catalog = catalog;
    this.database = database;
    this.tableName = tableName;
    this.format = format;
  }

  public Long getId() {
    return id;
  }

  public String getCatalog() {
    return catalog;
  }

  public String getDatabase() {
    return database;
  }

  public String getTableName() {
    return tableName;
  }

  public TableFormat getFormat() {
    return this.format;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setCatalog(String catalog) {
    this.catalog = catalog;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public void setFormat(TableFormat format) {
    this.format = format;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServerTableIdentifier that = (ServerTableIdentifier) o;
    return Objects.equals(id, that.id)
        && Objects.equals(catalog, that.catalog)
        && Objects.equals(database, that.database)
        && Objects.equals(tableName, that.tableName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, catalog, database, tableName);
  }

  @Override
  public String toString() {
    return String.format("%s.%s.%s(tableId=%d)", catalog, database, tableName, id);
  }

  public static ServerTableIdentifier of(TableIdentifier tableIdentifier, TableFormat format) {
    return new ServerTableIdentifier(tableIdentifier, format);
  }

  public static ServerTableIdentifier of(
      String catalog, String database, String tableName, TableFormat format) {
    return new ServerTableIdentifier(catalog, database, tableName, format);
  }

  public static ServerTableIdentifier of(
      Long id, String catalog, String database, String tableName, TableFormat format) {
    return new ServerTableIdentifier(id, catalog, database, tableName, format);
  }

  public TableIdentifier getIdentifier() {
    return new TableIdentifier(catalog, database, tableName);
  }
}
