package com.netease.arctic.ams.api.properties;

public class AmsHAProperties {
  private static final String ROOT_PATH = "/arctic/ams";
  private static final String LEADER_PATH = "/leader";
  private static final String MASTER_PATH = "/master";
  private static final String NAMESPACE_DEFAULT = "/master";

  public static String getBasePath(String namespace) {
    if (namespace.isEmpty()) {
      namespace = getNamespaceDefault();
    }
    return "/" + namespace + ROOT_PATH;
  }

  public static String getMasterPath(String namespace) {
    if (namespace.isEmpty()) {
      namespace = getNamespaceDefault();
    }
    return "/" + namespace + ROOT_PATH + MASTER_PATH;
  }

  public static String getLeaderPath(String namespace) {
    if (namespace.isEmpty()) {
      namespace = getNamespaceDefault();
    }
    return "/" + namespace + ROOT_PATH + LEADER_PATH;
  }

  public static String getNamespaceDefault() {
    return NAMESPACE_DEFAULT;
  }
}
