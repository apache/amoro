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

package org.apache.amoro.server.manager;

import org.apache.amoro.config.ConfigurationManager;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.metrics.MetricRegisterListener;
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.metrics.MetricReporter;

/** Metric plugins manager and registry */
public class MetricManager extends AbstractPluginManager<MetricReporter> {

  public static final String PLUGIN_CATEGORY = "metric-reporters";
  private static volatile MetricManager INSTANCE;

  /** @return Get the singleton object. */
  public static MetricManager getInstance() {
    return getInstance(null, null);
  }

  public static MetricManager getInstance(ConfigurationManager configurationManager) {
    return getInstance(null, configurationManager);
  }

  public static MetricManager getInstance(
      Configurations serviceConfig, ConfigurationManager configurationManager) {
    if (INSTANCE == null) {
      synchronized (MetricManager.class) {
        if (INSTANCE == null) {
          INSTANCE = new MetricManager(serviceConfig, configurationManager);
          INSTANCE.initialize();
        }
      }
    }
    return INSTANCE;
  }

  /** Close the manager */
  public static void dispose() {
    synchronized (MetricManager.class) {
      if (INSTANCE != null) {
        INSTANCE.close();
      }
      INSTANCE = null;
    }
  }

  protected MetricManager() {
    this(null, null);
  }

  protected MetricManager(ConfigurationManager configurationManager) {
    this(null, configurationManager);
  }

  protected MetricManager(Configurations serviceConfig, ConfigurationManager configurationManager) {
    super(PLUGIN_CATEGORY, serviceConfig, configurationManager);
  }

  private final MetricRegistry globalRegistry = new MetricRegistry();

  public MetricRegistry getGlobalRegistry() {
    return this.globalRegistry;
  }

  @Override
  public void initialize() {
    super.initialize();
    forEach(
        reporter -> {
          reporter.setGlobalMetricSet(globalRegistry);
          if (reporter instanceof MetricRegisterListener) {
            globalRegistry.addListener((MetricRegisterListener) reporter);
          }
        });
  }
}
