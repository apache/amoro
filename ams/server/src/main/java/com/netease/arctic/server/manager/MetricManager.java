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

package com.netease.arctic.server.manager;

import com.netease.arctic.ams.api.metrics.MetricRegisterListener;
import com.netease.arctic.ams.api.metrics.MetricReporter;
import com.netease.arctic.server.metrics.MetricRegistry;
import org.apache.iceberg.relocated.com.google.common.annotations.VisibleForTesting;

import java.util.List;

/**
 * Metric plugins manager and registry
 */
public class MetricManager extends BasePluginManager<MetricReporter> {

  public static final String PLUGIN_CONFIG_KEY = "metric-reporters";
  private static volatile MetricManager INSTANCE;

  /**
   * @return Get the singleton object.
   */
  public static MetricManager getInstance() {
    if (INSTANCE == null) {
      synchronized (MetricManager.class) {
        if (INSTANCE == null) {
          throw new IllegalStateException("MetricManager is not initialized");
        }
      }
    }
    return INSTANCE;
  }

  public static void initialize(List<PluginConfiguration> pluginConfigurations) {
    synchronized (MetricManager.class) {
      if (INSTANCE != null) {
        throw new IllegalStateException("MetricManger has been already initialized.");
      }
      INSTANCE = new MetricManager(pluginConfigurations);
      INSTANCE.initialize();
    }
  }

  @VisibleForTesting
  public static void dispose() {
    synchronized (MetricManager.class) {
      if (INSTANCE != null) {
        INSTANCE.close();
      }
      INSTANCE = null;
    }
  }

  protected MetricManager(List<PluginConfiguration> pluginConfigurations) {
    super(pluginConfigurations);
  }

  private final MetricRegistry globalRegistry = new MetricRegistry();

  public MetricRegistry getGlobalRegistry() {
    return this.globalRegistry;
  }

  @Override
  protected String pluginCategory() {
    return PLUGIN_CONFIG_KEY;
  }

  @Override
  public void initialize() {
    super.initialize();
    callPlugins(
        l -> {
          l.setGlobalMetricSet(globalRegistry);
          if (l instanceof MetricRegisterListener) {
            globalRegistry.addListener((MetricRegisterListener) l);
          }
        });
  }
}
