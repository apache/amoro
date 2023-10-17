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

package com.netease.arctic.server.dashboard.utils;

import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class PropertiesUtilTest {
  
  private static final Map<String, String> ALL_PROPERTIES = Maps.newHashMap();
  private static final Map<String, String> CATALOG_PROPERTIES = Maps.newHashMap();
  private static final Map<String, String> TABLE_PROPERTIES = Maps.newHashMap();
  
  static {
    CATALOG_PROPERTIES.put("catalog1", "catalog1");
    CATALOG_PROPERTIES.put("catalog2", "catalog2");
    TABLE_PROPERTIES.put("table1", "table1");
    TABLE_PROPERTIES.put("table2", "table2");
    ALL_PROPERTIES.putAll(CATALOG_PROPERTIES);
    ALL_PROPERTIES.put("table.table1", "table1");
    ALL_PROPERTIES.put("table.table2", "table2");
  }
  
  @Test
  public void testExtractTableProperties() {
    Map<String, String> tableProperties = PropertiesUtil.extractTableProperties(ALL_PROPERTIES);
    Assert.assertEquals(tableProperties, TABLE_PROPERTIES);
  }
  
  @Test
  public void testExtractCatalogMetaProperties() {
    Map<String, String> catalogMetaProperties = PropertiesUtil.extractCatalogMetaProperties(ALL_PROPERTIES);
    Assert.assertEquals(catalogMetaProperties, CATALOG_PROPERTIES);
  }
  
  @Test
  public void testUnionCatalogProperties() {
    Map<String, String> unionCatalogProperties = PropertiesUtil.unionCatalogProperties(TABLE_PROPERTIES,
        CATALOG_PROPERTIES);
    Assert.assertEquals(unionCatalogProperties, ALL_PROPERTIES);
  }
}