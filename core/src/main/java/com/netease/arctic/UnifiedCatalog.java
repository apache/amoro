/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic;

import java.util.List;

/**
 * UnifiedCatalog is a catalog that can visit tables with all types of formats.
 */
public interface UnifiedCatalog extends AmoroCatalog {

  /**
   * name of this catalog
   */
  String name();

  /**
   * list tables with format
   *
   * @param database given database
   * @return identifier and format list
   */
  List<TableIDWithFormat> listTableMetas(String database);

  /**
   * Refresh catalog meta
   */
  void refresh();
}
