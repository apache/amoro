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

import java.io.IOException;

/** Metric plugins manager and registry */
public class MetricManager extends AbstractPluginManager<MetricReporter> {

  public static final String PLUGIN_CATEGORY = "metric-reporters";
  private static volatile MetricManager INSTANCE;

  /** @return Get the singleton object. */
  public static MetricManager getInstance() {
    if (INSTANCE == null) {
      synchronized (MetricManager.class) {
        if (INSTANCE == null) {
          INSTANCE = new MetricManager();
          try {
            INSTANCE.initialize();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
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
    super(PLUGIN_CATEGORY);
  }

  private final MetricRegistry globalRegistry = new MetricRegistry();

  public MetricRegistry getGlobalRegistry() {
    return this.globalRegistry;
  }

  @Override
  public void initialize() throws IOException {
    super.initialize();
    forEach(
        l -> {
          l.setGlobalMetricSet(globalRegistry);
          if (l instanceof MetricRegisterListener) {
            globalRegistry.addListener((MetricRegisterListener) l);
          }
        });
  }
}
