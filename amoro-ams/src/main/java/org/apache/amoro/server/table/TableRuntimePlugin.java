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

import org.apache.amoro.AmoroTable;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.table.TableRuntimeFactory;
import org.apache.amoro.table.TableRuntimeStore;

import javax.annotation.Nullable;

import java.util.List;

public interface TableRuntimePlugin {

  boolean accept(ServerTableIdentifier tableIdentifier);

  void initialize(List<TableRuntime> tableRuntimes);

  void onTableCreated(@Nullable AmoroTable<?> amoroTable, TableRuntime tableRuntime);

  void onTableDropped(TableRuntime tableRuntime);

  void dispose();

  default TableRuntime createTableRuntime(
      TableRuntimeFactory.Creator creator, TableRuntimeStore store) {
    return creator.create(store);
  }
}
