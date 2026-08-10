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

package org.apache.amoro.formats.paimon;

import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.hadoop.conf.Configuration;
import org.apache.paimon.hive.HiveCatalogOptions;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestPaimonCatalogFactory {

  @Test
  public void testHmsDefaultCatalogIsIncludedInClientPoolCacheKey() {
    Configuration configuration = new Configuration(false);
    configuration.set(PaimonCatalogFactory.HMS_DEFAULT_CATALOG, "tenant@catalog");
    Map<String, String> properties = new HashMap<>();

    PaimonCatalogFactory.configureHiveClientPoolCache(
        CatalogMetaProperties.CATALOG_TYPE_HIVE, properties, configuration);

    Assert.assertEquals(
        PaimonCatalogFactory.HMS_DEFAULT_CATALOG_CACHE_KEY,
        properties.get(HiveCatalogOptions.CLIENT_POOL_CACHE_KEYS.key()));
  }

  @Test
  public void testExistingClientPoolCacheKeysArePreserved() {
    Configuration configuration = new Configuration(false);
    configuration.set(PaimonCatalogFactory.HMS_DEFAULT_CATALOG, "tenant@catalog");
    Map<String, String> properties = new HashMap<>();
    properties.put(HiveCatalogOptions.CLIENT_POOL_CACHE_KEYS.key(), "ugi");

    PaimonCatalogFactory.configureHiveClientPoolCache(
        CatalogMetaProperties.CATALOG_TYPE_HIVE, properties, configuration);
    PaimonCatalogFactory.configureHiveClientPoolCache(
        CatalogMetaProperties.CATALOG_TYPE_HIVE, properties, configuration);

    Assert.assertEquals(
        "ugi," + PaimonCatalogFactory.HMS_DEFAULT_CATALOG_CACHE_KEY,
        properties.get(HiveCatalogOptions.CLIENT_POOL_CACHE_KEYS.key()));
  }

  @Test
  public void testNonHiveCatalogIsUnchanged() {
    Configuration configuration = new Configuration(false);
    configuration.set(PaimonCatalogFactory.HMS_DEFAULT_CATALOG, "tenant@catalog");
    Map<String, String> properties = new HashMap<>();

    PaimonCatalogFactory.configureHiveClientPoolCache(
        CatalogMetaProperties.CATALOG_TYPE_HADOOP, properties, configuration);

    Assert.assertFalse(properties.containsKey(HiveCatalogOptions.CLIENT_POOL_CACHE_KEYS.key()));
  }
}
