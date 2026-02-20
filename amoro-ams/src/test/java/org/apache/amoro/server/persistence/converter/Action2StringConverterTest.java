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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.Action;
import org.apache.amoro.IcebergActions;
import org.apache.amoro.PaimonActions;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Action2StringConverter}. */
public class Action2StringConverterTest {

  @Test
  public void testGetRegisteredIcebergAction() {
    Action action = Action2StringConverter.getActionByName("optimizing-minor");
    assertNotNull(action, "Should find registered optimizing-minor action");
    assertEquals("optimizing-minor", action.getName());
    assertSame(IcebergActions.OPTIMIZING_MINOR, action, "Should return the same instance");
  }

  @Test
  public void testGetRegisteredIcebergActions() {
    assertEquals(IcebergActions.SYSTEM, Action2StringConverter.getActionByName("system"));
    assertEquals(IcebergActions.REWRITE, Action2StringConverter.getActionByName("rewrite"));
    assertEquals(
        IcebergActions.DELETE_ORPHANS, Action2StringConverter.getActionByName("delete-orphans"));
    assertEquals(IcebergActions.SYNC_HIVE, Action2StringConverter.getActionByName("sync-hive"));
    assertEquals(IcebergActions.EXPIRE_DATA, Action2StringConverter.getActionByName("expire-data"));
    assertEquals(
        IcebergActions.OPTIMIZING_MINOR,
        Action2StringConverter.getActionByName("optimizing-minor"));
    assertEquals(
        IcebergActions.OPTIMIZING_MAJOR,
        Action2StringConverter.getActionByName("optimizing-major"));
    assertEquals(
        IcebergActions.OPTIMIZING_FULL, Action2StringConverter.getActionByName("optimizing-full"));
  }

  @Test
  public void testGetRegisteredPaimonActions() {
    assertEquals(PaimonActions.COMPACT, Action2StringConverter.getActionByName("compact"));
    assertEquals(
        PaimonActions.FULL_COMPACT, Action2StringConverter.getActionByName("full-compact"));
    assertEquals(
        PaimonActions.CLEAN_METADATA, Action2StringConverter.getActionByName("clean-meta"));
    assertEquals(
        PaimonActions.DELETE_SNAPSHOTS, Action2StringConverter.getActionByName("del-snapshots"));
  }

  @Test
  public void testGetUnknownActionCreatesTemporary() {
    String unknownActionName = "custom-optimizing-action";
    Action action = Action2StringConverter.getActionByName(unknownActionName);

    assertNotNull(action, "Should create action for unknown name");
    assertEquals(unknownActionName, action.getName());
  }

  @Test
  public void testGetUnknownActionReturnsSameInstance() {
    String unknownActionName = "another-custom-action";
    Action action1 = Action2StringConverter.getActionByName(unknownActionName);
    Action action2 = Action2StringConverter.getActionByName(unknownActionName);

    assertSame(action1, action2, "Should return the same instance for same unknown action name");
  }

  @Test
  public void testGetNullAction() {
    Action action = Action2StringConverter.getActionByName(null);
    assertNull(action, "Should return null for null input");
  }

  @Test
  public void testGetEmptyAction() {
    Action action = Action2StringConverter.getActionByName("");
    assertNull(action, "Should return null for empty string");
  }

  @Test
  public void testRegisterCustomAction() {
    Action customAction =
        new Action(
            new org.apache.amoro.TableFormat[] {org.apache.amoro.TableFormat.PAIMON},
            50,
            "custom-action");

    Action2StringConverter.registerCustomAction(customAction);
    Action retrieved = Action2StringConverter.getActionByName("custom-action");

    assertSame(customAction, retrieved, "Should retrieve the same custom action");
  }

  @Test
  public void testGetRegisteredActions() {
    Action[] actions = Action2StringConverter.getRegisteredActions();
    assertTrue(actions.length > 0, "Should have registered actions");

    boolean hasOptimizingMinor = false;
    for (Action action : actions) {
      if ("optimizing-minor".equals(action.getName())) {
        hasOptimizingMinor = true;
        break;
      }
    }
    assertTrue(hasOptimizingMinor, "Should include optimizing-minor action");
  }
}
