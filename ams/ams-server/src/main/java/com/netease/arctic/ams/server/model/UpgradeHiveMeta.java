package com.netease.arctic.ams.server.model;

import java.util.List;
import java.util.Map;

public class UpgradeHiveMeta {
  private Map<String, String> properties;
  private List<PrimaryKeyField> pkList;

  public UpgradeHiveMeta(Map<String, String> properties, List<PrimaryKeyField> pkList) {
    this.properties = properties;
    this.pkList = pkList;
  }

  public UpgradeHiveMeta() {
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public List<PrimaryKeyField> getPkList() {
    return pkList;
  }

  public void setPkList(List<PrimaryKeyField> pkList) {
    this.pkList = pkList;
  }

  public class PrimaryKeyField {
    private String fieldName;

    public PrimaryKeyField(String fieldName) {
      this.fieldName = fieldName;
    }

    public PrimaryKeyField() {
    }

    public String getFieldName() {
      return fieldName;
    }

    public void setFieldName(String fieldName) {
      this.fieldName = fieldName;
    }
  }
}
