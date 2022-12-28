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
import com.netease.arctic.AmsTestBase;
import com.netease.arctic.TableTestHelpers;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.ams.api.properties.TableFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Map;

import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_AMS;
import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_HADOOP;

public abstract class CatalogTestBase extends AmsTestBase {

  protected static final String TEST_CATALOG_NAME = TableTestHelpers.TEST_CATALOG_NAME;

  private final TableFormat testFormat;

  private ArcticCatalog catalog;

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  public CatalogTestBase(TableFormat testFormat) {
    this.testFormat = testFormat;
  }

  @Before
  public void setupCatalog() throws IOException {
    Map<String, String> properties = Maps.newHashMap();
    properties.put(CatalogMetaProperties.KEY_WAREHOUSE, temp.newFolder().getPath());
    CatalogMeta catalogMeta = CatalogTestHelpers.buildCatalogMeta(TEST_CATALOG_NAME, getCatalogType(),
        properties, testFormat);
    getAmsHandler().createCatalog(catalogMeta);
  }

  private String getCatalogType() {
    switch (testFormat) {
      case ICEBERG:
        return CATALOG_TYPE_HADOOP;
      case MIXED_ICEBERG:
        return CATALOG_TYPE_AMS;
      case MIXED_HIVE:
      default:
        throw new UnsupportedOperationException("Unsupported table format:" + testFormat);
    }
  }

  @After
  public void dropCatalog() {
    getAmsHandler().dropCatalog(TEST_CATALOG_NAME);
  }

  protected ArcticCatalog getCatalog() {
    if (catalog == null) {
      catalog = CatalogLoader.load(getCatalogUrl());
    }
    return catalog;
  }

  protected String getCatalogUrl() {
    return AMS.getServerUrl() + "/" + TEST_CATALOG_NAME;
  }
}
