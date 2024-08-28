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

import org.apache.amoro.table.TableIdentifier;

import java.util.Map;

public interface AmoroTable<T> {

  /** Returns the {@link TableIdentifier} of this table */
  TableIdentifier id();

  /** Returns the name of this table */
  default String name() {
    return id().toString();
  }

  /** Returns the {@link TableFormat} of this table */
  TableFormat format();

  /** Returns the properties of this table */
  Map<String, String> properties();

  /** Returns the original of this table */
  T originalTable();

  /** Returns the current snapshot of this table */
  TableSnapshot currentSnapshot();
}
