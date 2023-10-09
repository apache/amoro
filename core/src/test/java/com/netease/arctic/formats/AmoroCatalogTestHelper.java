/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.formats;

import com.netease.arctic.AmoroCatalog;
import com.netease.arctic.ams.api.CatalogMeta;
import org.apache.hadoop.conf.Configuration;

public interface AmoroCatalogTestHelper<T> {
  String DEFAULT_TABLE_OPTION_KEY = "amoro.test.key";
  String DEFAULT_TABLE_OPTION_VALUE = "amoro.test.value";
  String DEFAULT_SCHEMA_ID_NAME = "id";
  String DEFAULT_SCHEMA_NAME_NAME = "name";
  String DEFAULT_SCHEMA_AGE_NAME = "age";

  void initWarehouse(String warehouseLocation);

  void initHiveConf(Configuration hiveConf);

  CatalogMeta getCatalogMeta();

  AmoroCatalog amoroCatalog();

  T originalCatalog();

  String catalogName();

  void clean();

  void createTable(String db, String tableName)
      throws Exception;
}
