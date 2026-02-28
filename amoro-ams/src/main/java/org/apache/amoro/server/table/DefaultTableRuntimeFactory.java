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

package org.apache.amoro.server.table;

import org.apache.amoro.Action;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.ActionCoordinator;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.StateKey;
import org.apache.amoro.table.TableRuntimeFactory;
import org.apache.amoro.table.TableRuntimeStore;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Default {@link TableRuntimeFactory} implementation used by AMS.
 *
 * <p>Besides creating {@link DefaultTableRuntime} instances for mixed/iceberg formats, this factory
 * also aggregates {@link ProcessFactory} declarations to expose {@link ActionCoordinator} plugins
 * for different {@link TableFormat}/{@link Action} combinations.
 */
public class DefaultTableRuntimeFactory implements TableRuntimeFactory {

  /** Mapping from table format to its supported actions and corresponding process factory. */
  private final Map<TableFormat, Map<Action, ProcessFactory>> factoriesByFormat = new HashMap<>();

  /** Coordinators derived from all installed process factories. */
  private final List<ActionCoordinator> supportedCoordinators = Lists.newArrayList();

  @Override
  public List<ActionCoordinator> supportedCoordinators() {
    return supportedCoordinators;
  }

  @Override
  public void initialize(List<ProcessFactory> factories) {
    factoriesByFormat.clear();
    supportedCoordinators.clear();

    for (ProcessFactory factory : factories) {
      Map<TableFormat, Set<Action>> supported = factory.supportedActions();
      if (supported == null || supported.isEmpty()) {
        continue;
      }

      for (Map.Entry<TableFormat, Set<Action>> entry : supported.entrySet()) {
        TableFormat format = entry.getKey();
        Map<Action, ProcessFactory> byAction =
            factoriesByFormat.computeIfAbsent(format, k -> new HashMap<>());

        for (Action action : entry.getValue()) {
          ProcessFactory existed = byAction.get(action);
          if (existed != null && existed != factory) {
            throw new IllegalArgumentException(
                String.format(
                    "ProcessFactory conflict for format %s and action %s, existing: %s, new: %s",
                    format, action, existed.name(), factory.name()));
          }
          byAction.put(action, factory);
          supportedCoordinators.add(new DefaultActionCoordinator(format, action, factory));
        }
      }
    }
  }

  @Override
  public Optional<TableRuntimeCreator> accept(
      ServerTableIdentifier tableIdentifier, Map<String, String> tableProperties) {
    TableFormat format = tableIdentifier.getFormat();
    boolean defaultSupported =
        format.in(TableFormat.MIXED_ICEBERG, TableFormat.MIXED_HIVE, TableFormat.ICEBERG);
    boolean hasProcessFactories = factoriesByFormat.containsKey(format);

    if (!defaultSupported && !hasProcessFactories) {
      return Optional.empty();
    }

    return Optional.of(new TableRuntimeCreatorImpl(format));
  }

  private class TableRuntimeCreatorImpl implements TableRuntimeFactory.TableRuntimeCreator {

    private final TableFormat format;

    private TableRuntimeCreatorImpl(TableFormat format) {
      this.format = format;
    }

    @Override
    public List<StateKey<?>> requiredStateKeys() {
      Map<String, StateKey<?>> merged = new LinkedHashMap<>();
      // 1) DefaultTableRuntime required states
      for (StateKey<?> stateKey : DefaultTableRuntime.REQUIRED_STATES) {
        merged.put(stateKey.getKey(), stateKey);
      }

      // 2) Extra states from all process factories for this format (if any)
      Map<Action, ProcessFactory> byAction = factoriesByFormat.get(format);
      if (byAction != null) {
        byAction
            .values()
            .forEach(
                factory ->
                    factory
                        .requiredStates()
                        .forEach(stateKey -> merged.put(stateKey.getKey(), stateKey)));
      }

      return Lists.newArrayList(merged.values());
    }

    @Override
    public TableRuntime create(TableRuntimeStore store) {
      return new DefaultTableRuntime(store);
    }
  }
}
