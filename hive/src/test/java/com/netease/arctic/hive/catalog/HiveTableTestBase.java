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

package com.netease.arctic.hive.catalog;

import com.google.common.collect.Maps;
import com.netease.arctic.TableTestHelpers;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.catalog.CatalogTestHelpers;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.table.PrimaryKeySpec;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.junit.ClassRule;

import java.util.Map;

public abstract class HiveTableTestBase extends TableTestBase {

  @ClassRule
  public static TestHMS TEST_HMS = new TestHMS();

  public HiveTableTestBase(Schema tableSchema, PrimaryKeySpec primaryKeySpec,
                           PartitionSpec partitionSpec,
                           Map<String, String> tableProperties) {
    super(TableFormat.MIXED_HIVE, tableSchema, primaryKeySpec, partitionSpec, tableProperties);
  }

  public HiveTableTestBase(boolean keyedTable, boolean partitionedTable,
                           Map<String, String> tableProperties) {
    this(TableTestHelpers.TABLE_SCHEMA,
        keyedTable ? TableTestHelpers.PRIMARY_KEY_SPEC : PrimaryKeySpec.noPrimaryKey(),
        partitionedTable ? TableTestHelpers.IDENTIFY_SPEC : PartitionSpec.unpartitioned(),
        tableProperties);
  }

  public HiveTableTestBase(boolean keyedTable, boolean partitionedTable) {
    this(keyedTable, partitionedTable, Maps.newHashMap());
  }

  @Override
  protected CatalogMeta buildCatalogMeta() {
    Map<String, String> properties = Maps.newHashMap();
    return CatalogTestHelpers.buildHiveCatalogMeta(TEST_CATALOG_NAME,
        properties, TEST_HMS.getHiveConf());
  }

}
