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

package com.netease.arctic.server.metrics;

import com.netease.arctic.ams.api.Environments;
import com.netease.arctic.server.manager.MetricsManager;
import java.io.File;
import java.util.Arrays;

/**
 * Load configuration files to install MetricsEmitter from the specified system directory, and support removing it
 * after installing the plugin through command line or front-end interface
 */
public class MetricsEmitterLoader {

  private static final String PLUGIN_CONFIG_DIRECTORY = "plugins";

  private final MetricsManager metricsManager;

  public MetricsEmitterLoader() {
    this.metricsManager = new MetricsManager(Environments.getConfigPath() + "/" + PLUGIN_CONFIG_DIRECTORY);
    installPlugins();
  }

  public MetricsManager metricsManager() {
    return metricsManager;
  }

  private void installPlugins() {
    String configDir = Environments.getConfigPath() + "/" + PLUGIN_CONFIG_DIRECTORY;
    File dir = new File(configDir);
    File[] yamlFiles = dir.listFiles((dir1, name) -> name.endsWith(".yaml"));
    if (yamlFiles != null) {
      Arrays.stream(yamlFiles).forEach(file -> {
        metricsManager.install(file.getName().replace(".yaml", ""));
      });
    }
  }
}
