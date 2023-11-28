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

package com.netease.arctic.spark;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.spark.test.extensions.EnableCatalogSelect;
import com.netease.arctic.spark.test.unified.UnifiedCatalogTestSuites;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true, unifiedCatalog = true)
public class TestUnifiedCatalog extends UnifiedCatalogTestSuites {

  @ParameterizedTest
  @MethodSource
  public void testTableFormats(TableFormat format, boolean sessionCatalog) {
    super.testTableFormats(format, sessionCatalog);
  }
}