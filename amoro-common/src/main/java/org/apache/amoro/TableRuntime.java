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

package org.apache.amoro;

import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.TableProcessStore;

import java.util.List;
import java.util.Map;

/**
 * TableRuntime is the key interface for the AMS framework to interact with the table. Typically, it
 * is used to get the table's configuration, process states, and table identifier. The key usage is
 * {@link ProcessFactory} to create and recover Process.
 */
public interface TableRuntime {

  /**
   * Get the list of process states. which belong to all running table processes. There could be
   * more than one external process states depending on scheduler implementation.
   *
   * @return the list of arbitrary process states
   */
  List<? extends TableProcessStore> getProcessStates();

  /**
   * Get the list of process states. which belong to all running table processes. There could be
   * more than one external process states depending on scheduler implementation.
   *
   * @return the list of arbitrary process states
   */
  List<? extends TableProcessStore> getProcessStates(Action action);

  /** Get the group name of the table runtime. */
  String getGroupName();

  /**
   * Get the table identifier containing server side id and table format.
   *
   * @return the table identifier
   */
  ServerTableIdentifier getTableIdentifier();

  /**
   * Load the current table instance for this runtime.
   *
   * <p>This method is mainly intended for in-AMS processes.
   */
  AmoroTable<?> loadTable();

  /**
   * Get the table configuration. @Deprecated use {@link #getTableConfig()} instead.
   *
   * @return the table configuration
   */
  @Deprecated
  TableConfiguration getTableConfiguration();

  /**
   * Get the table configuration.
   *
   * @return the table configuration
   */
  Map<String, String> getTableConfig();

  /**
   * Register the metric of the table runtime.
   *
   * @param metricRegistry the metric registry
   */
  void registerMetric(MetricRegistry metricRegistry);

  void unregisterMetric();

  /** Get table format */
  default TableFormat getFormat() {
    return getTableIdentifier().getFormat();
  }

  /** Dispose the table runtime. */
  default void dispose() {}
}
