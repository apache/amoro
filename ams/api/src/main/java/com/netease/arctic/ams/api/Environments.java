package com.netease.arctic.ams.api;

public class Environments {

  public static final String SYSTEM_ARCTIC_HOME = "ARCTIC_HOME";
  public static final String AMORO_CONF_DIR = "AMORO_CONF_DIR";
  public static final String AMORO_HOME = "AMORO_HOME";

  public static String getHomePath() {
    String arcticHome = System.getenv(SYSTEM_ARCTIC_HOME);
    if (arcticHome != null) {
      return arcticHome;
    }
    arcticHome = System.getenv(AMORO_HOME);
    if (arcticHome != null) {
      return arcticHome;
    }
    return System.getProperty(AMORO_HOME);
  }

  public static String getConfigPath() {
    String amoroConfDir = System.getenv(AMORO_CONF_DIR);
    if (amoroConfDir != null) {
      return amoroConfDir;
    }
    return getHomePath() + "/conf";
  }
}
