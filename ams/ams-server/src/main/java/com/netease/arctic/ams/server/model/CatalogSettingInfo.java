package com.netease.arctic.ams.server.model;

import java.util.Map;

/**
 * @Auth: hzwangtao6
 * @Time: 2022/10/31 19:24
 * @Description:
 */
public class CatalogSettingInfo {
  String name;
  String type;
  Map<String, Object> storageConfig;
  Map<String, Object> authConfig;
  Map<String, String> properties;

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

  public Map<String, Object> getStorageConfig() {
    return storageConfig;
  }

  public void setStorageConfig(Map<String, Object> storageConfig) {
    this.storageConfig = storageConfig;
  }

  public Map<String, Object> getAuthConfig() {
    return authConfig;
  }

  public void setAuthConfig(Map<String, Object> authConfig) {
    this.authConfig = authConfig;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public static class ConfigFileItem {
    String fileName;
    String fileUrl;

    public ConfigFileItem(String fileName, String fileUrl) {
      this.fileName = fileName;
      this.fileUrl = fileUrl;
    }

    public String getFileName() {
      return fileName;
    }

    public void setFileName(String fileName) {
      this.fileName = fileName;
    }

    public String getFileUrl() {
      return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
      this.fileUrl = fileUrl;
    }
  }
}


