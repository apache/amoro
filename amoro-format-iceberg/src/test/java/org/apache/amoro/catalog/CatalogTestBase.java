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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

/**
 * Dual-mode base class supporting both JUnit 4 and JUnit 5 subclasses.
 *
 * <p>JUnit 4 path (used by cross-module {@code @RunWith(Parameterized.class)} subclasses in
 * amoro-format-mixed-hive and amoro-ams that have not yet migrated): the legacy {@link
 * #CatalogTestBase(CatalogTestHelper)} constructor is invoked by the parameterized runner, the
 * {@code @ClassRule} {@link #TEST_AMS} starts the mock AMS, the {@code @Rule} {@link #temp} manages
 * a temporary folder, and the {@code @Before} {@link #setupCatalog()} / {@code @After} {@link
 * #dropCatalog()} fire automatically.
 *
 * <p>JUnit 5 path (used by in-module subclasses migrated as part of #4203): the no-arg constructor
 * is used, {@link #startTestAms()} / {@link #stopTestAms()} drive the mock AMS via
 * {@code @BeforeAll} / {@code @AfterAll}, and subclasses explicitly call {@link
 * #setupCatalog(CatalogTestHelper)} from their {@code @ParameterizedTest} method bodies (or from a
 * {@code @BeforeEach} when the helper is fixed). The {@link #temp} field is initialised manually
 * inside {@link #setupCatalog(CatalogTestHelper)} when null because JUnit 4 {@code @Rule} does not
 * fire under the JUnit Platform engine.
 *
 * <p>This dual plumbing is removed in the closing PR of #4203 once all cross-module subclasses
 * migrate to JUnit 5.
 */
public abstract class CatalogTestBase {

  @ClassRule public static TestAms TEST_AMS = new TestAms();

  @Rule public TemporaryFolder temp = new TemporaryFolder();

  protected CatalogTestHelper testHelper;
  private UnifiedCatalog unifiedCatalog;
  private MixedFormatCatalog mixedFormatCatalog;
  private CatalogMeta catalogMeta;
  private org.apache.iceberg.catalog.Catalog icebergCatalog;
  private boolean tempInitializedByJunit5;

  /** JUnit 4 constructor — kept for cross-module {@code @RunWith(Parameterized.class)} callers. */
  public CatalogTestBase(CatalogTestHelper testHelper) {
    this.testHelper = testHelper;
  }

  /** JUnit 5 constructor — used by in-module migrated subclasses. */
  public CatalogTestBase() {}

  @BeforeAll
  public static void startTestAms() throws Exception {
    TEST_AMS.before();
  }

  @AfterAll
  public static void stopTestAms() {
    TEST_AMS.after();
  }

  public static MockAmoroManagementServer.AmsHandler getAmsHandler() {
    return TEST_AMS.getAmsHandler();
  }

  /** JUnit 4 lifecycle — fires when subclass uses {@code @RunWith(Parameterized)}. */
  @Before
  public void setupCatalog() throws IOException {
    if (testHelper != null) {
      doSetupCatalog();
    }
  }

  /**
   * JUnit 5 entry point — invoke from {@code @ParameterizedTest} method body or
   * {@code @BeforeEach}.
   */
  protected void setupCatalog(CatalogTestHelper helper) throws IOException {
    this.testHelper = helper;
    // JUnit 4 @Rule does not fire under JUnit Platform; manually create the temp folder.
    if (!tempInitializedByJunit5) {
      if (temp == null) {
        temp = new TemporaryFolder();
      }
      try {
        temp.create();
      } catch (IOException e) {
        throw e;
      }
      tempInitializedByJunit5 = true;
    }
    doSetupCatalog();
  }

  private void doSetupCatalog() throws IOException {
    String baseDir = temp.newFolder().getPath();
    if (!SystemUtils.IS_OS_UNIX) {
      baseDir = "file:/" + temp.newFolder().getPath().replace("\\", "/");
    }
    catalogMeta = testHelper.buildCatalogMeta(baseDir);
    catalogMeta.putToCatalogProperties(CatalogMetaProperties.AMS_URI, TEST_AMS.getServerUrl());
    getAmsHandler().createCatalog(catalogMeta);
  }

  @After
  @AfterEach
  public void dropCatalog() {
    if (catalogMeta != null) {
      getAmsHandler().dropCatalog(catalogMeta.getCatalogName());
      mixedFormatCatalog = null;
    }
    if (tempInitializedByJunit5 && temp != null) {
      temp.delete();
      temp = null;
      tempInitializedByJunit5 = false;
    }
  }

  protected MixedFormatCatalog getMixedFormatCatalog() {
    if (mixedFormatCatalog == null) {
      mixedFormatCatalog = CatalogLoader.load(getCatalogUri());
    }
    return mixedFormatCatalog;
  }

  protected void refreshMixedFormatCatalog() {
    this.mixedFormatCatalog = CatalogLoader.load(getCatalogUri());
  }

  protected String getCatalogUri() {
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
