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

import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class Action {

  private static final int MAX_NAME_LENGTH = 16;
  private static final Map<String, Action> registeredActions = new ConcurrentHashMap<>();

  private final String name;

  public static Action register(String name) {
    final String regularName = name.trim().toUpperCase(Locale.ROOT);
    return registeredActions.computeIfAbsent(regularName, s -> new Action(regularName));
  }

  public static Action valueOf(String name) {
    final String regularName = name.trim().toUpperCase(Locale.ROOT);
    return registeredActions.get(regularName);
  }

  private Action(String name) {
    Preconditions.checkArgument(
        name.length() <= MAX_NAME_LENGTH,
        "Action name length should be less than " + MAX_NAME_LENGTH);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Action action = (Action) o;
    return Objects.equals(name, action.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
