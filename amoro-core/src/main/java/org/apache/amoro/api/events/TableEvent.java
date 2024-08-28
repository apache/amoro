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

package org.apache.amoro.api.events;

/** An event associated with a table */
public interface TableEvent extends Event {

  /**
   * The catalog name that this event source related to.
   *
   * @return Catalog name
   */
  String catalog();

  /**
   * The database name that this event source related to.
   *
   * @return Database name
   */
  String database();

  /**
   * The table name that this event source related to.
   *
   * @return Table name
   */
  String table();

  /**
   * External or Internal Catalog that this event source related to.
   *
   * @return True if event source from an external catalog.
   */
  boolean isExternal();
}
