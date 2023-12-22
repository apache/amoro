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
