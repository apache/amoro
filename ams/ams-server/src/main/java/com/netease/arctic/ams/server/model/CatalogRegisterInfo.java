package com.netease.arctic.ams.server.model;

import java.util.Map;

/**
 * @Auth: hzwangtao6
 * @Time: 2022/10/25 22:52
 * @Description:
 */
public class CatalogRegisterInfo {
  String catalogName;
  String type;
  Map<String, String> storageConfig;
  Map<String, String> authConfig;
  Map<String, String> properties;

  public CatalogRegisterInfo(){

  }

  public String getCatalogName() {
    return catalogName;
  }

  public void setCatalogName(String catalogName) {
    this.catalogName = catalogName;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Map<String, String> getStorageConfig() {
    return storageConfig;
  }

  public void setStorageConfig(Map<String, String> storageConfig) {
    this.storageConfig = storageConfig;
  }

  public Map<String, String> getAuthConfig() {
    return authConfig;
  }

  public void setAuthConfig(Map<String, String> authConfig) {
    this.authConfig = authConfig;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
}
