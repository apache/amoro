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

package org.apache.amoro.table;

import org.apache.hadoop.conf.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TableMetaStoreTest {

  @Test
  public void testTableMetaStoreWithDifferentConfigurations() {
    Configuration config1 = new Configuration();
    config1.set("test.key1", "value1");
    TableMetaStore metaStore1 = TableMetaStore.builder().withConfiguration(config1).build();

    Configuration config2 = new Configuration();
    config2.set("test.key2", "value2");
    TableMetaStore metaStore2 = TableMetaStore.builder().withConfiguration(config2).build();

    Configuration retrievedConfig1 = metaStore1.getConfiguration();
    Configuration retrievedConfig2 = metaStore2.getConfiguration();

    Assertions.assertEquals("value1", retrievedConfig1.get("test.key1"));
    Assertions.assertNull(retrievedConfig1.get("test.key2"));

    Assertions.assertEquals("value2", retrievedConfig2.get("test.key2"));
    Assertions.assertNull(retrievedConfig2.get("test.key1"));
  }

  @Test
  public void testTableMetaStoreEquals() {
    Configuration config1 = new Configuration();
    TableMetaStore metaStore1 = TableMetaStore.builder().withConfiguration(config1).build();

    Configuration config2 = new Configuration();
    TableMetaStore metaStore2 = TableMetaStore.builder().withConfiguration(config2).build();

    Assertions.assertEquals(metaStore1, metaStore2);
    Assertions.assertEquals(metaStore1.hashCode(), metaStore2.hashCode());
  }

  @Test
  public void testTableMetaStoreWithConfigurationDoesNotUseCache() throws Exception {
    Configuration config1 = new Configuration();
    config1.set("test.cache.key1", "cache_value1");
    TableMetaStore metaStore1 = TableMetaStore.builder().withConfiguration(config1).build();

    Configuration config2 = new Configuration();
    config2.set("test.cache.key2", "cache_value2");
    TableMetaStore metaStore2 = TableMetaStore.builder().withConfiguration(config2).build();

    Configuration retrievedConfig1 = metaStore1.getConfiguration();
    Configuration retrievedConfig2 = metaStore2.getConfiguration();

    Assertions.assertEquals("cache_value1", retrievedConfig1.get("test.cache.key1"));
    Assertions.assertNull(retrievedConfig1.get("test.cache.key2"));

    Assertions.assertEquals("cache_value2", retrievedConfig2.get("test.cache.key2"));
    Assertions.assertNull(retrievedConfig2.get("test.cache.key1"));
  }
}
