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

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.table.StateKey;
import org.apache.amoro.table.TableRuntimeFactory;
import org.apache.amoro.table.TableRuntimeStore;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultTableRuntimeFactory implements TableRuntimeFactory {
  @Override
  public void open(Map<String, String> properties) {}

  @Override
  public void close() {}

  @Override
  public String name() {
    return "default";
  }

  @Override
  public Optional<TableRuntimeCreator> accept(
      ServerTableIdentifier tableIdentifier, Map<String, String> tableProperties) {
    if (tableIdentifier
        .getFormat()
        .in(TableFormat.MIXED_ICEBERG, TableFormat.MIXED_HIVE, TableFormat.ICEBERG)) {
      return Optional.of(new TableRuntimeCreatorImpl());
    }
    return Optional.empty();
  }

  private static class TableRuntimeCreatorImpl implements TableRuntimeFactory.TableRuntimeCreator {
    @Override
    public List<StateKey<?>> requiredStateKeys() {
      return DefaultTableRuntime.REQUIRED_STATES;
    }

    @Override
    public TableRuntime create(TableRuntimeStore store) {
      return new DefaultTableRuntime(store);
    }
  }
}
