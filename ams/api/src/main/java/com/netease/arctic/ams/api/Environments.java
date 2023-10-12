package com.netease.arctic.ams.api;

public class Environments {

  public static final String SYSTEM_ARCTIC_HOME = "ARCTIC_HOME";
  public static final String AMORO_CONF_DIR = "AMORO_CONF_DIR";
  public static final String AMORO_HOME = "AMORO_HOME";

  public static final String USER_DIR = "user.dir";

  public static String getHomePath() {
    String amoroHome = System.getenv(SYSTEM_ARCTIC_HOME);
    if (amoroHome != null) {
      return amoroHome;
    }
    amoroHome = System.getenv(AMORO_HOME);
    if (amoroHome != null) {
      return amoroHome;
    }
    amoroHome = System.getProperty(AMORO_HOME);
    if (amoroHome != null) {
      return amoroHome;
    }
    return System.getProperty(USER_DIR);
  }

  public static String getConfigPath() {
    String amoroConfDir = System.getenv(AMORO_CONF_DIR);
    if (amoroConfDir != null) {
      return amoroConfDir;
    }
    return getHomePath() + "/conf";
  }
}
