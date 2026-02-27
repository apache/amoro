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

import org.apache.amoro.table.TableRuntimeFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple manager for {@link TableRuntimeFactory} implementations.
 *
 * <p>AMS currently only ships with {@link DefaultTableRuntimeFactory}. This manager wraps a list of
 * factories so that existing wiring code in {@link DefaultTableService} can stay unchanged while
 * {@link TableRuntimeFactory} is no longer an {@code ActivePlugin}.
 */
public class TableRuntimeFactoryManager {

  private final List<TableRuntimeFactory> factories = new ArrayList<>();

  public TableRuntimeFactoryManager() {
    this(Collections.singletonList(new DefaultTableRuntimeFactory()));
  }

  public TableRuntimeFactoryManager(List<TableRuntimeFactory> factories) {
    this.factories.addAll(factories);
  }

  public void initialize() {
    // kept for backward compatibility, no-op for now
  }

  public List<TableRuntimeFactory> installedPlugins() {
    return factories;
  }
}
