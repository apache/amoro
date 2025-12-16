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

package org.apache.amoro.formats;

import org.apache.amoro.AmoroCatalog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

public abstract class AmoroCatalogTestBase {

  @TempDir Path tempDir;

  protected AmoroCatalogTestHelper<?> catalogTestHelper;

  protected AmoroCatalog amoroCatalog;

  protected Object originalCatalog;

  public AmoroCatalogTestBase() {
    this.catalogTestHelper = null;
  }

  public AmoroCatalogTestBase(AmoroCatalogTestHelper<?> catalogTestHelper) {
    this.catalogTestHelper = catalogTestHelper;
  }

  protected AmoroCatalogTestHelper<?> createHelper() {
    return null;
  }

  @BeforeEach
  protected void setupCatalog() throws IOException {
    if (catalogTestHelper == null) {
      catalogTestHelper = createHelper();
      if (catalogTestHelper == null) {
        throw new IllegalStateException(
            "catalogTestHelper must be set either via constructor or createHelper() method");
      }
    }
    String path = tempDir.toFile().getAbsolutePath();
    catalogTestHelper.initWarehouse(path);
    this.amoroCatalog = catalogTestHelper.amoroCatalog();
    this.originalCatalog = catalogTestHelper.originalCatalog();
  }

  @AfterEach
  void cleanCatalog() {
    if (catalogTestHelper != null) {
      catalogTestHelper.clean();
    }
  }
}
