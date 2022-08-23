package com.netease.arctic.ams.server.model;

import java.util.Objects;

public class TableMeta {
  public String name;
  public String type;

  public TableMeta(String name, String type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TableMeta tableMeta = (TableMeta) o;
    return Objects.equals(name, tableMeta.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  public enum TableType {
    ARCTIC("arctic"),
    HIVE("hive");

    private String name;

    TableType(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }
}
