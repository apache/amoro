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
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.shade.thrift.org.apache.commons.lang3.tuple.Pair;
import org.apache.amoro.table.StateKey;
import org.apache.amoro.table.TableRuntimeFactory;
import org.apache.amoro.table.TableRuntimeStore;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DefaultTableRuntimeFactory implements TableRuntimeFactory {
  private final List<ActionCoordinator> supportedCoordinators = Lists.newArrayList();

  private final Map<Pair<TableFormat, Action>, ProcessFactory> actions = Maps.newHashMap();
  private final Set<TableFormat> supportFormats = Sets.newHashSet();
  private final Set<TableFormat> compatibleFormats =
      Sets.newHashSet(TableFormat.MIXED_ICEBERG, TableFormat.MIXED_HIVE, TableFormat.ICEBERG);

  @Override
  public void open(Map<String, String> properties) {}

  @Override
  public void close() {}

  public void initialize(List<ProcessFactory> processFactories) {
    processFactories.forEach(
        f -> {
          Map<TableFormat, Set<Action>> supportedActions = f.supportedActions();
          for (TableFormat format : supportedActions.keySet()) {
            Set<Action> supportedActionsForFormat = supportedActions.get(format);
            for (Action action : supportedActionsForFormat) {
              ProcessFactory exists = actions.get(Pair.of(format, action));
              if (exists != null) {
                throw new IllegalArgumentException(
                    String.format(
                        "Plugin conflict, ProcessFactory: %s is supported for format: %s, action: %s, conflict with %s",
                        exists.name(), format, action, f.name()));
              }
              actions.put(Pair.of(format, action), f);
            }
          }
        });
    actions.keySet().forEach(e -> supportFormats.add(e.getLeft()));
    for (Pair<TableFormat, Action> key : actions.keySet()) {
      supportedCoordinators.add(
          new DefaultActionCoordinator(key.getLeft(), key.getRight(), actions.get(key)));
    }
  }

  @Override
  public List<ActionCoordinator> supportedCoordinators() {
    return supportedCoordinators;
  }

  @Override
  public Optional<TableRuntimeCreator> accept(
      ServerTableIdentifier tableIdentifier, Map<String, String> tableProperties) {
    if (!supportFormats.contains(tableIdentifier.getFormat())) {
      if (!compatibleFormats.contains(tableIdentifier.getFormat())) {
        return Optional.empty();
      } else {
        return Optional.of(new CompatibleTableRuntimeCreatorImpl());
      }
    }
    return Optional.of(new TableRuntimeCreatorImpl(tableIdentifier.getFormat()));
  }

  @Override
  public String name() {
    return "default";
  }

  private class TableRuntimeCreatorImpl implements TableRuntimeFactory.TableRuntimeCreator {

    private final TableFormat format;

    public TableRuntimeCreatorImpl(TableFormat format) {
      this.format = format;
    }

    @Override
    public List<StateKey<?>> requiredStateKeys() {
      Map<String, StateKey<?>> requiredStates = Maps.newHashMap();
      actions.keySet().stream()
          .filter(e -> e.getLeft().equals(format))
          .forEach(
              e -> {
                ProcessFactory factory = actions.get(e);
                factory
                    .requiredStates()
                    .forEach(stateKey -> requiredStates.put(stateKey.getKey(), stateKey));
              });
      return Lists.newArrayList(requiredStates.values());
    }

    @Override
    public TableRuntime create(TableRuntimeStore store) {
      return new DefaultTableRuntime(store);
    }
  }

  private static class CompatibleTableRuntimeCreatorImpl
      implements TableRuntimeFactory.TableRuntimeCreator {

    @Override
    public List<StateKey<?>> requiredStateKeys() {
      return CompatibleTableRuntime.REQUIRED_STATES;
    }

    @Override
    public TableRuntime create(TableRuntimeStore store) {
      return new CompatibleTableRuntime(store);
    }
  }
}
