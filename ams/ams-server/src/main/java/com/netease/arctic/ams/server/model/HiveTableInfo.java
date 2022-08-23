package com.netease.arctic.ams.server.model;

import com.netease.arctic.table.TableIdentifier;

import java.util.List;
import java.util.Map;

public class HiveTableInfo {
  private TableIdentifier tableIdentifier;
  private TableMeta.TableType tableType;
  private List<AMSColumnInfo> schema;
  private List<AMSColumnInfo> partitionColumnList;
  private Map<String, String> properties;
  private int createTime;

  public HiveTableInfo(TableIdentifier tableIdentifier, TableMeta.TableType tableType, List<AMSColumnInfo> schema,
                       List<AMSColumnInfo> partitionColumnList, Map<String, String> properties, int createTime) {
    this.tableIdentifier = tableIdentifier;
    this.tableType = tableType;
    this.schema = schema;
    this.partitionColumnList = partitionColumnList;
    this.properties = properties;
    this.createTime = createTime;
  }

  public HiveTableInfo() {
  }

  public TableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  public void setTableIdentifier(TableIdentifier tableIdentifier) {
    this.tableIdentifier = tableIdentifier;
  }

  public TableMeta.TableType getTableType() {
    return tableType;
  }

  public void setTableType(TableMeta.TableType tableType) {
    this.tableType = tableType;
  }

  public List<AMSColumnInfo> getSchema() {
    return schema;
  }

  public void setSchema(List<AMSColumnInfo> schema) {
    this.schema = schema;
  }

  public List<AMSColumnInfo> getPartitionColumnList() {
    return partitionColumnList;
  }

  public void setPartitionColumnList(List<AMSColumnInfo> partitionColumnList) {
    this.partitionColumnList = partitionColumnList;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public int getCreateTime() {
    return createTime;
  }

  public void setCreateTime(int createTime) {
    this.createTime = createTime;
  }
}
