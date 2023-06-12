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


/**
 * A mixed-in interface to support for mixed-format operations
 */
public interface MixedTableOperations extends TableFormatOperations {

  /**
   * check if this table is a table store and belong to a mixed-format table.
   * @param base a basic format table to check
   * @return true if this table is part of a mixed-format table's table store.
   */
  boolean isMixedTable(ArcticTable base);

  /**
   * load a mixed-format table by a table store
   * @param base - a table store of a mixed-format table
   * @return the total mixed-format table.
   */
  ArcticTable loadMixedTable(ArcticTable base);
}
