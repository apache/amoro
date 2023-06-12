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

package com.netease.arctic.table;

import com.netease.arctic.ams.api.TableFormat;
import org.apache.iceberg.Schema;


/**
 * An interface to abstract table format operations
 */
public interface TableFormatOperations {

  /**
   * Returns the supported {@link TableFormat} type.
   */
  TableFormat format();

  /**
   * Instantiate a builder to build a table which the format is {@link #format()}.
   *
   * @param identifier a table identifier
   * @param schema     a schema
   * @return the builder to build a table
   */
  TableBuilder newTableBuilder(Schema schema, TableIdentifier identifier);


  /**
   * Get an arctic table by table identifier.
   *
   * @param identifier a table identifier
   * @return instance of {@link com.netease.arctic.table.ArcticTable}
   *  implementation referred by {@code tableIdentifier}
   */
  ArcticTable loadTable(TableIdentifier identifier);


  /**
   * Drop a table and delete all data and metadata files.
   *
   * @param tableIdentifier a table identifier
   * @param purge           if true, delete all data and metadata files in the table
   * @return true if the table was dropped, false if the table did not exist
   */
  boolean dropTable(TableIdentifier tableIdentifier, boolean purge);
}
