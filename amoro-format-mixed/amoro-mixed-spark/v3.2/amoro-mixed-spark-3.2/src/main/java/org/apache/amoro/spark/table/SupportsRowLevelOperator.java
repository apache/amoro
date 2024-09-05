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

package org.apache.amoro.spark.table;

import org.apache.spark.sql.connector.catalog.Table;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;

/**
 * A mix-in interface of {@link org.apache.spark.sql.connector.catalog.Table}, to indicate that can
 * handle update or delete by upsert.
 */
public interface SupportsRowLevelOperator extends Table {

  /**
   * Returns support extend columns scan builder
   *
   * @param options
   * @return
   */
  SupportsExtendIdentColumns newUpsertScanBuilder(CaseInsensitiveStringMap options);

  boolean requireAdditionIdentifierColumns();

  /**
   * will table handle insert as upsert
   *
   * @return true if table require insert as upsert
   */
  boolean appendAsUpsert();
}
