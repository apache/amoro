/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server;

/**
 * Util method to help get system directories.
 */
public class Environments {

  /**
   * Adapt with older version to set AMORO_HOME
   */
  @Deprecated
  public static final String LEGACY_ENV_AMORO_HOME = "ARCTIC_HOME";

  /**
   * Environment variable to set config dir.
   */
  public static final String AMORO_CONF_DIR = "AMORO_CONF_DIR";

  /**
   * Environment variable to set HOME dir
   */
  public static final String AMORO_HOME = "AMORO_HOME";

  /**
   * Environment variable to set PLUGIN dir
   */
  public static final String AMORO_PLUGIN_DIR = "AMORO_PLUGIN_DIR";

  /**
   * Default directory name of config dir.
   */
  public static final String DEFAULT_CONFIG_DIR_NAME = "conf";

  /**
   * Default directory name of plugin dir.
   */
  public static final String DEFAULT_PLUGIN_DIR_NAME = "plugins";

  private static final String PROPERTY_USER_DIR = "user.dir";

  /**
   * Get the home path which contains sub-directories of `/bin`, `/conf`, `/plugins` and so on.
   * @return Get the home path
   */
  public static String getHomePath() {
    String amoroHome = System.getenv(LEGACY_ENV_AMORO_HOME);
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
    return System.getProperty(PROPERTY_USER_DIR);
  }

  /**
   * Get the config path which is a directory contains `config.yaml`, `jvm.properties`, `log4j2.xml` and so on.
   * @return The config path
   */
  public static String getConfigPath() {
    String amoroConfDir = System.getenv(AMORO_CONF_DIR);
    if (amoroConfDir != null) {
      return amoroConfDir;
    }
    return getHomePath() + "/" + DEFAULT_CONFIG_DIR_NAME;
  }

  /**
   * Get the path of plugins which is a directory contains jars of plugins.
   * @return The plugin path.
   */
  public static String getPluginPath() {
    String pluginDir = System.getenv(AMORO_PLUGIN_DIR);
    if (pluginDir != null) {
      return pluginDir;
    }
    return getHomePath() + "/" + DEFAULT_PLUGIN_DIR_NAME;
  }
}
