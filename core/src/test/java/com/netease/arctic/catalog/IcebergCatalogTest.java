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

package com.netease.arctic.catalog;

import com.google.common.collect.Maps;
import com.netease.arctic.TableTestHelpers;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.table.ArcticTable;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE;
import static org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE_HADOOP;

public class IcebergCatalogTest extends CatalogTestBase {

  public IcebergCatalogTest() {
    super(TableFormat.ICEBERG);
  }

  @Test
  public void testLoadIcebergTable() throws TException {
    ArcticCatalog icebergCatalog = getCatalog();
    icebergCatalog.createDatabase("db2");
    CatalogMeta catalogMeta = getAmsHandler().getCatalog(TEST_CATALOG_NAME);
    Map<String, String> catalogProperties = Maps.newHashMap(catalogMeta.getCatalogProperties());
    catalogProperties.put(ICEBERG_CATALOG_TYPE, ICEBERG_CATALOG_TYPE_HADOOP);
    Catalog nativeIcebergTable = org.apache.iceberg.CatalogUtil.buildIcebergCatalog(TEST_CATALOG_NAME,
        catalogProperties, new Configuration());
    nativeIcebergTable.createTable(TableIdentifier.of("db2", "tb1"), TableTestHelpers.TABLE_SCHEMA);
    ArcticTable table = icebergCatalog.loadTable(
        com.netease.arctic.table.TableIdentifier.of(TEST_CATALOG_NAME, "db2", "tb1"));
    Assert.assertTrue(table instanceof BaseIcebergCatalog.BaseIcebergTable);
    Assert.assertEquals(true, table.isUnkeyedTable());
    Assert.assertEquals(TableTestHelpers.TABLE_SCHEMA.asStruct(), table.schema().asStruct());
    nativeIcebergTable.dropTable(TableIdentifier.of("db2", "tb1"), true);
    icebergCatalog.dropDatabase("db2");
  }
}
