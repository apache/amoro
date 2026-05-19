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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestAction {

  /**
   * {@link Action#toString()} must return the action name so that diagnostic logs print a readable
   * name instead of {@code org.apache.amoro.Action@hash} (see AMORO-4223).
   */
  @Test
  public void testToStringReturnsName() {
    Action action = Action.register("expire-snapshots");
    assertEquals("EXPIRE-SNAPSHOTS", action.toString());
    assertEquals(action.getName(), action.toString());
    assertEquals("recover action: EXPIRE-SNAPSHOTS", "recover action: " + action);
  }
}
