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

import com.netease.arctic.TestAms;
import com.netease.arctic.UnifiedCatalog;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.MockArcticMetastoreServer;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import org.apache.commons.lang3.SystemUtils;
import org.apache.iceberg.catalog.Catalog;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

public abstract class CatalogTestBase {

  @ClassRule public static TestAms TEST_AMS = new TestAms();
  private final CatalogTestHelper testHelper;
  @Rule public TemporaryFolder temp = new TemporaryFolder();
  private UnifiedCatalog unifiedCatalog;
  private ArcticCatalog mixedFormatCatalog;
  private CatalogMeta catalogMeta;
  private Catalog icebergCatalog;

  public CatalogTestBase(CatalogTestHelper testHelper) {
    this.testHelper = testHelper;
  }

  public static MockArcticMetastoreServer.AmsHandler getAmsHandler() {
    return TEST_AMS.getAmsHandler();
  }

  @Before
  public void setupCatalog() throws IOException {
    String baseDir = temp.newFolder().getPath();
    if (!SystemUtils.IS_OS_UNIX) {
      baseDir = "file:/" + temp.newFolder().getPath().replace("\\", "/");
    }
    catalogMeta = testHelper.buildCatalogMeta(baseDir);
    catalogMeta.putToCatalogProperties(CatalogMetaProperties.AMS_URI, TEST_AMS.getServerUrl());
    getAmsHandler().createCatalog(catalogMeta);
  }

  @After
  public void dropCatalog() {
    if (catalogMeta != null) {
      getAmsHandler().dropCatalog(catalogMeta.getCatalogName());
      mixedFormatCatalog = null;
    }
  }

  protected ArcticCatalog getMixedFormatCatalog() {
    if (mixedFormatCatalog == null) {
      mixedFormatCatalog = CatalogLoader.load(getCatalogUrl());
    }
    return mixedFormatCatalog;
  }

  protected void refreshMixedFormatCatalog() {
    this.mixedFormatCatalog = CatalogLoader.load(getCatalogUrl());
  }

  protected String getCatalogUrl() {
    return TEST_AMS.getServerUrl() + "/" + catalogMeta.getCatalogName();
  }

  protected CatalogMeta getCatalogMeta() {
    return catalogMeta;
  }

  protected TableFormat getTestFormat() {
    return testHelper.tableFormat();
  }

  protected Catalog getIcebergCatalog() {
    if (icebergCatalog == null) {
      icebergCatalog = testHelper.buildIcebergCatalog(catalogMeta);
    }
    return icebergCatalog;
  }

  protected UnifiedCatalog getUnifiedCatalog() {
    if (unifiedCatalog == null) {
      unifiedCatalog = testHelper.buildUnifiedCatalog(catalogMeta);
    }
    return unifiedCatalog;
  }
}
