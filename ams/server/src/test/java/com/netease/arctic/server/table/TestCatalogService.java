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

package com.netease.arctic.server.table;

import com.google.common.collect.Maps;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.server.exception.AlreadyExistsException;
import com.netease.arctic.server.exception.IllegalMetadataException;
import com.netease.arctic.server.exception.ObjectNotExistsException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestCatalogService extends TableServiceTestBase {

  @Test
  public void testCreateAndDropCatalog() {
    CatalogTestHelper catalogTestHelper = new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG,
        Collections.singletonMap("k", "v"));
    CatalogMeta catalogMeta = catalogTestHelper.buildCatalogMeta("/tmp");
    // test create catalog
    tableService().createCatalog(catalogMeta);

    // test create duplicate catalog
    Assert.assertThrows(AlreadyExistsException.class, () -> tableService().createCatalog(catalogMeta));

    // test get catalog
    CatalogMeta readCatalogMeta = tableService().getCatalogMeta(catalogMeta.getCatalogName());
    Assert.assertEquals(catalogMeta, readCatalogMeta);

    // test get catalog list
    List<CatalogMeta> catalogMetas = tableService().listCatalogMetas();
    // include a default catalog
    Assert.assertEquals(1, catalogMetas.size());
    Assert.assertEquals(catalogMeta, catalogMetas.stream().filter(meta ->
            meta.getCatalogName().equals(catalogMeta.getCatalogName()))
        .findAny().orElseThrow(() -> new IllegalStateException("Cannot find expect catalog")));

    // test catalogExist
    Assert.assertTrue(tableService().catalogExist(catalogMeta.getCatalogName()));

    // test drop catalog
    tableService().dropCatalog(catalogMeta.getCatalogName());

    // test drop not existed catalog
    Assert.assertThrows(ObjectNotExistsException.class, () ->
        tableService().getCatalogMeta(catalogMeta.getCatalogName()));

    Assert.assertFalse(tableService().catalogExist(catalogMeta.getCatalogName()));
  }

  @Test
  public void testUpdateCatalog() {
    Map<String, String> catalogProperties = Maps.newHashMap();
    catalogProperties.put("k1", "v1");
    catalogProperties.put("k2", "v2");
    CatalogTestHelper catalogTestHelper = new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG, catalogProperties);
    CatalogMeta catalogMeta = catalogTestHelper.buildCatalogMeta("/tmp");
    tableService().createCatalog(catalogMeta);

    CatalogMeta updateCatalogMeta = new CatalogMeta(catalogMeta);
    updateCatalogMeta.getCatalogProperties().remove("k1");
    updateCatalogMeta.getCatalogProperties().put("k2", "V2");
    updateCatalogMeta.getCatalogProperties().put("k3", "v3");
    tableService().updateCatalog(updateCatalogMeta);
    CatalogMeta getCatalogMeta = tableService().getCatalogMeta(catalogMeta.getCatalogName());
    Assert.assertEquals("V2", getCatalogMeta.getCatalogProperties().get("k2"));
    Assert.assertEquals("v3", getCatalogMeta.getCatalogProperties().get("k3"));
    Assert.assertEquals(updateCatalogMeta, tableService().getCatalogMeta(catalogMeta.getCatalogName()));

    // test update catalog type
    final CatalogMeta updateCatalogMeta2 = new CatalogMeta(updateCatalogMeta);
    updateCatalogMeta2.setCatalogType(CatalogMetaProperties.CATALOG_TYPE_HIVE);
    Assert.assertThrows(IllegalMetadataException.class, () -> tableService().updateCatalog(updateCatalogMeta2));

    // test update unknown catalog
    tableService().dropCatalog(catalogMeta.getCatalogName());
    Assert.assertThrows(ObjectNotExistsException.class, () -> tableService().updateCatalog(catalogMeta));
  }

  @Test
  public void testDropCatalogWithDatabase() {
    CatalogTestHelper catalogTestHelper = new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG);
    CatalogMeta catalogMeta = catalogTestHelper.buildCatalogMeta("/tmp");
    tableService().createCatalog(catalogMeta);

    tableService().createDatabase(catalogMeta.getCatalogName(), "test_db");
    Assert.assertThrows(IllegalMetadataException.class, () -> tableService().dropCatalog(catalogMeta.getCatalogName()));
    tableService().dropDatabase(catalogMeta.getCatalogName(), "test_db");
    tableService().dropCatalog(catalogMeta.getCatalogName());
  }

}
