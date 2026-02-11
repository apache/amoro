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

package org.apache.amoro.server.table.simple;

import org.apache.amoro.Action;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.ActionCoordinator;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.StateKey;
import org.apache.amoro.table.TableRuntimeFactory;
import org.apache.amoro.table.TableRuntimeStore;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SimpleTableRuntimeFactory implements TableRuntimeFactory {
  private static final String SUPPORT_FORMATS = "support-formats";
  private static final String SUPPORT_ACTIONS = "support-actions";
  private static final String SUPPORT_FORMAT_ACTIONS_PREFIX = "support-format-actions.";
  private static final String PROCESS_FACTORY_IMPL = "process-factory-impl";

  private final Set<TableFormat> supportFormats = Sets.newHashSet();
  private final Map<TableFormat, Set<Action>> supportedActions = Maps.newHashMap();
  private final Map<Action, Map<String, String>> actionConfigs = Maps.newHashMap();
  private ProcessFactory processFactory;
  private final List<ActionCoordinator> supportedCoordinators = Lists.newArrayList();

  @Override
  public void open(Map<String, String> properties) {
    Preconditions.checkArgument(
        properties.containsKey(SUPPORT_FORMATS), SUPPORT_FORMATS + " is required");
    String supportFormatsStr = properties.get(SUPPORT_FORMATS);
    for (String format : supportFormatsStr.split(",")) {
      supportFormats.add(TableFormat.register(format.trim().toUpperCase()));
    }
    initializeProcessFactory(properties);
    initializeActions(properties);
  }

  @Override
  public void close() {}

  @Override
  public List<ActionCoordinator> supportedCoordinators() {
    return supportedCoordinators;
  }

  @Override
  public Optional<TableRuntimeCreator> accept(
      ServerTableIdentifier tableIdentifier, Map<String, String> tableProperties) {
    if (!supportFormats.contains(tableIdentifier.getFormat())) {
      return Optional.empty();
    }
    return Optional.of(new TableRuntimeCreatorImpl());
  }

  private void initializeActions(Map<String, String> properties) {
    if (properties.containsKey(SUPPORT_ACTIONS)) {
      String supportActionsStr = properties.get(SUPPORT_ACTIONS);
      for (String action : supportActionsStr.split(",")) {
        for (TableFormat format : supportFormats) {
          supportedActions
              .computeIfAbsent(format, k -> Sets.newHashSet())
              .add(Action.register(action.trim().toUpperCase()));
        }
      }
    }
    properties.forEach(
        (key, value) -> {
          if (key.startsWith(SUPPORT_FORMAT_ACTIONS_PREFIX)) {
            TableFormat format =
                TableFormat.valueOf(
                    key.substring(SUPPORT_FORMAT_ACTIONS_PREFIX.length()).trim().toUpperCase());
            Preconditions.checkArgument(
                format == null || !supportFormats.contains(format),
                "Unsupported format: " + format);
            for (String action : value.split(",")) {
              supportedActions
                  .computeIfAbsent(format, k -> Sets.newHashSet())
                  .add(Action.register(action.trim().toUpperCase()));
            }
          }
        });
    Preconditions.checkArgument(
        !supportedActions.isEmpty(),
        SUPPORT_ACTIONS + " or " + SUPPORT_FORMAT_ACTIONS_PREFIX + " is required");
    supportedActions.forEach(
        (f, actions) ->
            actions.forEach(a -> actionConfigs.computeIfAbsent(a, k -> Maps.newHashMap())));

    properties.forEach(
        (key, value) -> {
          if (key.startsWith("action.")) {
            String actionConfigKey = key.substring("action.".length()).trim().toUpperCase();
            int pos = actionConfigKey.indexOf(".");
            String actionName = actionConfigKey.substring(0, pos);
            Action action = Action.valueOf(actionName);
            if (action != null && actionConfigs.containsKey(action)) {
              String configKey = actionConfigKey.substring(pos + 1).trim();
              actionConfigs.get(action).put(configKey, value);
            }
          }
        });

    for (Action action : actionConfigs.keySet()) {
      Set<TableFormat> formatSet = Sets.newHashSet();
      supportedActions.forEach(
          (f, actions) -> {
            if (actions.contains(action)) {
              formatSet.add(f);
            }
          });
      supportedCoordinators.add(
          new SimpleActionCoordinator(
              action, processFactory, formatSet, actionConfigs.get(action)));
    }
  }

  private void initializeProcessFactory(Map<String, String> properties) {
    Preconditions.checkArgument(
        properties.containsKey(PROCESS_FACTORY_IMPL), PROCESS_FACTORY_IMPL + " is required");
    try {
      processFactory =
          (ProcessFactory)
              Class.forName(properties.get(PROCESS_FACTORY_IMPL))
                  .getDeclaredConstructor()
                  .newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Failed to create process factory", e);
    }
  }

  @Override
  public String name() {
    return "simple";
  }

  private class TableRuntimeCreatorImpl implements TableRuntimeFactory.TableRuntimeCreator {
    @Override
    public List<StateKey<?>> requiredStateKeys() {
      return processFactory.requiredStates();
    }

    @Override
    public TableRuntime create(TableRuntimeStore store) {
      return new SimpleTableRuntime(store);
    }
  }
}
