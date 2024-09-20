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

package org.apache.amoro.catalog;

import org.apache.amoro.TableFormat;
import org.apache.amoro.TestAms;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.mixed.BasicMixedIcebergCatalog;
import org.apache.amoro.mixed.CatalogLoader;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Map;

public class TestCatalogLoader {

  private static final String TEST_CATALOG_NAME = "test";
  @ClassRule public static TestAms TEST_AMS = new TestAms();

  @Test
  public void testLoadMixedIcebergCatalog() {
    Map<String, String> properties = Maps.newHashMap();
    properties.put(CatalogMetaProperties.KEY_WAREHOUSE, "/temp");
    CatalogMeta catalogMeta =
        CatalogTestHelpers.buildCatalogMeta(
            TEST_CATALOG_NAME,
            CatalogMetaProperties.CATALOG_TYPE_HADOOP,
            properties,
            TableFormat.MIXED_ICEBERG);
    TEST_AMS.getAmsHandler().createCatalog(catalogMeta);
    MixedFormatCatalog loadCatalog = CatalogLoader.load(getCatalogUrl(TEST_CATALOG_NAME));
    Assert.assertEquals(TEST_CATALOG_NAME, loadCatalog.name());
    Assert.assertEquals(BasicMixedIcebergCatalog.class.getName(), loadCatalog.getClass().getName());
    TEST_AMS.getAmsHandler().dropCatalog(TEST_CATALOG_NAME);
  }

  @Test
  public void testLoadNotExistedCatalog() {
    Assert.assertThrows(
        "catalog not found, please check catalog name",
        IllegalArgumentException.class,
        () -> CatalogLoader.load(getCatalogUrl(TEST_CATALOG_NAME)));
  }

  private String getCatalogUrl(String catalogName) {
    return TEST_AMS.getServerUrl() + "/" + catalogName;
  }
}
