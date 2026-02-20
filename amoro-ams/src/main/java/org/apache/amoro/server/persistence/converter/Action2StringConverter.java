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

package org.apache.amoro.server.persistence.converter;

import org.apache.amoro.Action;
import org.apache.amoro.IcebergActions;
import org.apache.amoro.PaimonActions;
import org.apache.amoro.TableFormat;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MyBatis TypeHandler for converting Action to/from String in database. This converter maintains a
 * registry of known actions and can dynamically create temporary actions for unknown names to
 * support backward compatibility.
 */
@MappedTypes(Action.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class Action2StringConverter implements TypeHandler<Action> {

  private static final Logger LOG = LoggerFactory.getLogger(Action2StringConverter.class);

  /** Registry of all registered actions, keyed by action name. */
  private static final Map<String, Action> ACTION_REGISTRY = new ConcurrentHashMap<>();

  /** Default formats for dynamically created actions. */
  private static final TableFormat[] DEFAULT_FORMATS =
      new TableFormat[] {
        TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG, TableFormat.MIXED_HIVE, TableFormat.PAIMON
      };

  /** Static initialization block to register all built-in Iceberg and Paimon actions. */
  static {
    // Register Iceberg actions
    registerAction(IcebergActions.SYSTEM);
    registerAction(IcebergActions.REWRITE);
    registerAction(IcebergActions.DELETE_ORPHANS);
    registerAction(IcebergActions.SYNC_HIVE);
    registerAction(IcebergActions.EXPIRE_DATA);
    registerAction(IcebergActions.OPTIMIZING_MINOR);
    registerAction(IcebergActions.OPTIMIZING_MAJOR);
    registerAction(IcebergActions.OPTIMIZING_FULL);

    // Register Paimon actions
    registerAction(PaimonActions.COMPACT);
    registerAction(PaimonActions.FULL_COMPACT);
    registerAction(PaimonActions.CLEAN_METADATA);
    registerAction(PaimonActions.DELETE_SNAPSHOTS);
  }

  /**
   * Register an action in the registry.
   *
   * @param action the action to register
   */
  public static void registerAction(Action action) {
    if (action != null && action.getName() != null) {
      ACTION_REGISTRY.put(action.getName(), action);
    }
  }

  /**
   * Register a custom action. This is a convenience method that delegates to {@link
   * #registerAction(Action)}.
   *
   * @param action the custom action to register
   */
  public static void registerCustomAction(Action action) {
    registerAction(action);
  }

  /**
   * Get an action by its name from the registry.
   *
   * @param name the action name to look up
   * @return the registered action, or null if not found and name is null/empty
   */
  public static Action getActionByName(String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }
    return ACTION_REGISTRY.get(name);
  }

  /**
   * Get all registered actions.
   *
   * @return array of all registered actions
   */
  public static Action[] getRegisteredActions() {
    return ACTION_REGISTRY.values().toArray(new Action[0]);
  }

  @Override
  public void setParameter(PreparedStatement ps, int i, Action parameter, JdbcType jdbcType)
      throws SQLException {
    if (parameter == null) {
      ps.setString(i, "");
    } else {
      ps.setString(i, parameter.getName());
    }
  }

  @Override
  public Action getResult(ResultSet rs, String columnName) throws SQLException {
    String actionName = rs.getString(columnName);
    return convertToAction(actionName);
  }

  @Override
  public Action getResult(ResultSet rs, int columnIndex) throws SQLException {
    String actionName = rs.getString(columnIndex);
    return convertToAction(actionName);
  }

  @Override
  public Action getResult(CallableStatement cs, int columnIndex) throws SQLException {
    String actionName = cs.getString(columnIndex);
    return convertToAction(actionName);
  }

  /**
   * Convert a string action name to an Action object. First attempts to find the action in the
   * registry. If not found, creates a temporary action with the given name for backward
   * compatibility.
   *
   * @param actionName the action name to convert
   * @return the corresponding Action object, or null if actionName is null/empty
   */
  private Action convertToAction(String actionName) {
    if (actionName == null || actionName.isEmpty()) {
      return null;
    }

    Action action = ACTION_REGISTRY.get(actionName);
    if (action != null) {
      return action;
    }

    LOG.warn(
        "Unknown action name '{}', creating temporary action for backward compatibility",
        actionName);
    Action tempAction = new Action(DEFAULT_FORMATS, 0, actionName);
    registerAction(tempAction);
    return tempAction;
  }
}
