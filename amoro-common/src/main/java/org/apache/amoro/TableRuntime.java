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
import org.apache.amoro.process.OptimizingState;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.TableProcessState;

import java.util.List;

/**
 * TableRuntime is the key interface for the AMS framework to interact with the table. Typically, it
 * is used to get the table's configuration, process states, and table identifier. The key usage is
 * {@link ProcessFactory} to create and recover Process.
 */
public interface TableRuntime {

  /**
   * Get the list of optimizing process states. Normally, the list contains one default optimizing
   * state at least. There could be more than one states if multiple optimizing processes are
   * running.
   *
   * @return the list of optimizing process states
   */
  List<OptimizingState> getOptimizingStates();

  /**
   * Get the list of arbitrary process states. One arbitrary state belongs to one arbitrary process
   * related to one {@link Action#ARBITRARY_ACTIONS}. There could be more than one arbitrary states
   * depending on scheduler implementation.
   *
   * @return the list of arbitrary process states
   */
  List<TableProcessState> getArbitraryStates();

  /**
   * Get the table identifier containing server side id and table format.
   *
   * @return the table identifier
   */
  ServerTableIdentifier getTableIdentifier();

  /**
   * Get the table configuration.
   *
   * @return the table configuration
   */
  TableConfiguration getTableConfiguration();
}
