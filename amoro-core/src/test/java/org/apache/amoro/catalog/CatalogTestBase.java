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

import org.apache.amoro.MockAmoroManagementServer;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TestAms;
import org.apache.amoro.UnifiedCatalog;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.mixed.CatalogLoader;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.commons.lang3.SystemUtils;
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
  private MixedFormatCatalog mixedFormatCatalog;
  private CatalogMeta catalogMeta;
  private org.apache.iceberg.catalog.Catalog icebergCatalog;

  public CatalogTestBase(CatalogTestHelper testHelper) {
    this.testHelper = testHelper;
  }

  public static MockAmoroManagementServer.AmsHandler getAmsHandler() {
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

  protected MixedFormatCatalog getMixedFormatCatalog() {
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

  protected org.apache.iceberg.catalog.Catalog getIcebergCatalog() {
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
