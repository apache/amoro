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

package org.apache.amoro.utils;

import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.table.TableProperties;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.rest.RESTCatalog;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestMixedFormatCatalogUtil {
  /**
   * when log-store flag is on , fill up with default related props and other user-defined prop
   * should be keep
   */
  @Test
  public void testMergeCatalogPropertiesToTable() {
    Map<String, String> expected = new HashMap<>();
    expected.put("log-store.enabled", "true");
    expected.put("log-store.address", "168.0.0.1:9092");
    expected.put("log-store.type", "kafka");
    expected.put("other.prop", "10");
    expected.put("log-store.consistency.guarantee.enable", "true");

    Map<String, String> userDefined = new HashMap<>();
    userDefined.put("log-store.enabled", "true");
    userDefined.put("other.prop", "10");

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.log-store.enabled", "false");
    catalogProperties.put("table.log-store.address", "168.0.0.1:9092");
    catalogProperties.put("table.log-store.type", "kafka");
    catalogProperties.put("table.log-store.consistency.guarantee.enable", "true");
    catalogProperties.put("ams.address", "127.0.0.1");

    Map<String, String> result =
        MixedFormatCatalogUtil.mergeCatalogPropertiesToTable(userDefined, catalogProperties);
    Assert.assertEquals(expected, result);
  }

  /** when log-store flag is off, remove all related props */
  @Test
  public void testMergeCatalogPropertiesToTable1() {
    Map<String, String> expected = new HashMap<>();
    expected.put("log-store.enabled", "false");

    Map<String, String> userDefined = new HashMap<>();
    userDefined.put("log-store.enabled", "false");

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.log-store.enabled", "false");
    catalogProperties.put("table.log-store.address", "168.0.0.1:9092");
    catalogProperties.put("table.log-store.type", "kafka");
    catalogProperties.put("table.log-store.consistency.guarantee.enable", "true");
    catalogProperties.put("ams.address", "127.0.0.1");

    Map<String, String> result =
        MixedFormatCatalogUtil.mergeCatalogPropertiesToTable(userDefined, catalogProperties);
    Assert.assertEquals(expected, result);
  }

  /** user-defined prop should not be overwritten by default props */
  @Test
  public void testMergeCatalogPropertiesToTable2() {
    Map<String, String> expected = new HashMap<>();
    expected.put("log-store.enabled", "true");
    expected.put("log-store.address", "168.0.0.1:9092");
    expected.put("log-store.type", "pulsar");
    expected.put("log-store.consistency.guarantee.enable", "true");

    Map<String, String> userDefined = new HashMap<>();
    userDefined.put("log-store.enabled", "true");
    userDefined.put("log-store.type", "pulsar");

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.log-store.enabled", "false");
    catalogProperties.put("table.log-store.address", "168.0.0.1:9092");
    catalogProperties.put("table.log-store.type", "kafka");
    catalogProperties.put("table.log-store.consistency.guarantee.enable", "true");
    catalogProperties.put("ams.address", "127.0.0.1");

    Map<String, String> result =
        MixedFormatCatalogUtil.mergeCatalogPropertiesToTable(userDefined, catalogProperties);
    Assert.assertEquals(expected, result);
  }

  /** Other user-defined prop should not lose */
  @Test
  public void testMergeCatalogPropertiesToTable3() {
    Map<String, String> expected = new HashMap<>();
    expected.put("log-store.enabled", "true");
    expected.put("log-store.address", "168.0.0.1:9092");
    expected.put("log-store.type", "kafka");
    expected.put("log-store.consistency.guarantee.enable", "true");
    expected.put("table.other-props", "foo");

    Map<String, String> userDefined = new HashMap<>();
    userDefined.put("log-store.enabled", "true");
    userDefined.put("log-store.type", "kafka");
    userDefined.put("table.other-props", "foo");

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.log-store.enabled", "false");
    catalogProperties.put("table.log-store.address", "168.0.0.1:9092");
    catalogProperties.put("table.log-store.type", "kafka");
    catalogProperties.put("table.log-store.consistency.guarantee.enable", "true");
    catalogProperties.put("ams.address", "127.0.0.1");

    Map<String, String> result =
        MixedFormatCatalogUtil.mergeCatalogPropertiesToTable(userDefined, catalogProperties);
    Assert.assertEquals(expected, result);
  }

  /**
   * user-defined and default catalog 'self-optimizing.enabled' are both switched on, keep all
   * related props
   */
  @Test
  public void testMergeCatalogPropertiesToTable4() {
    Map<String, String> expected = new HashMap<>();
    expected.put("log-store.enabled", "true");
    expected.put("log-store.address", "168.0.0.1:9092");
    expected.put("log-store.type", "kafka");
    expected.put("log-store.consistency.guarantee.enable", "true");
    expected.put("self-optimizing.enabled", "true");
    expected.put("self-optimizing.quota", "0.2"); // should not overwritten by default
    expected.put("self-optimizing.group", "mygroup"); // inherit from default prop
    expected.put("table.other-props", "foo");

    Map<String, String> userDefined = new HashMap<>();
    userDefined.put("log-store.enabled", "true");
    userDefined.put("self-optimizing.enabled", "true");
    userDefined.put("self-optimizing.quota", "0.2");
    userDefined.put("table.other-props", "foo");

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.log-store.enabled", "false");
    catalogProperties.put("table.log-store.address", "168.0.0.1:9092");
    catalogProperties.put("table.log-store.type", "kafka");
    catalogProperties.put("table.log-store.consistency.guarantee.enable", "true");
    catalogProperties.put("table.self-optimizing.enabled", "false");
    catalogProperties.put("table.self-optimizing.quota", "0.1");
    catalogProperties.put("table.self-optimizing.group", "mygroup");
    catalogProperties.put("ams.address", "127.0.0.1");

    Map<String, String> result =
        MixedFormatCatalogUtil.mergeCatalogPropertiesToTable(userDefined, catalogProperties);
    Assert.assertEquals(expected, result);
  }

  /**
   * user-defined and default catalog prop 'self-optimizing.enabled' are both switched off, remove
   * optimizer related props from default catalog while keep user-defined related prop and
   * 'self-optimizing.enabled' itself.
   */
  @Test
  public void testMergeCatalogPropertiesToTable5() {
    Map<String, String> expected = new HashMap<>();
    expected.put("log-store.enabled", "true");
    expected.put("log-store.address", "168.0.0.1:9092");
    expected.put("log-store.type", "kafka");
    expected.put("log-store.consistency.guarantee.enable", "true");
    expected.put("self-optimizing.enabled", "false");
    // user-defined related prop should be kept
    expected.put("self-optimizing.quota", "0.2");
    expected.put("table.other-props", "foo");

    Map<String, String> userDefined = new HashMap<>();
    userDefined.put("log-store.enabled", "true");
    userDefined.put("self-optimizing.enabled", "false");
    userDefined.put("self-optimizing.quota", "0.2");
    userDefined.put("table.other-props", "foo");

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.log-store.enabled", "false");
    catalogProperties.put("table.log-store.address", "168.0.0.1:9092");
    catalogProperties.put("table.log-store.type", "kafka");
    catalogProperties.put("table.log-store.consistency.guarantee.enable", "true");
    catalogProperties.put("table.self-optimizing.enabled", "false");
    catalogProperties.put("table.self-optimizing.quota", "0.1");
    catalogProperties.put("table.self-optimizing.group", "mygroup");
    catalogProperties.put("ams.address", "127.0.0.1");

    Map<String, String> result =
        MixedFormatCatalogUtil.mergeCatalogPropertiesToTable(userDefined, catalogProperties);
    Assert.assertEquals(expected, result);
  }

  /**
   * when optimized flag is off in catalog props and no user-defined value, remove optimizer related
   * props but 'self-optimizing.enabled' itself.
   */
  @Test
  public void testMergeCatalogPropertiesToTable6() {
    Map<String, String> expected = new HashMap<>();
    expected.put("log-store.enabled", "true");
    expected.put("log-store.address", "168.0.0.1:9092");
    expected.put("log-store.type", "kafka");
    expected.put("log-store.consistency.guarantee.enable", "true");
    expected.put("self-optimizing.enabled", "false");
    expected.put("table.other-props", "foo");

    Map<String, String> userDefined = new HashMap<>();
    userDefined.put("log-store.enabled", "true");
    userDefined.put("table.other-props", "foo");

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.log-store.enabled", "false");
    catalogProperties.put("table.log-store.address", "168.0.0.1:9092");
    catalogProperties.put("table.log-store.type", "kafka");
    catalogProperties.put("table.log-store.consistency.guarantee.enable", "true");
    catalogProperties.put("table.self-optimizing.enabled", "false");
    catalogProperties.put("table.self-optimizing.quota", "0.1");
    catalogProperties.put("table.self-optimizing.group", "mygroup");
    catalogProperties.put("ams.address", "127.0.0.1");

    Map<String, String> result =
        MixedFormatCatalogUtil.mergeCatalogPropertiesToTable(userDefined, catalogProperties);
    Assert.assertEquals(expected, result);
  }

  /**
   * user-defined 'self-optimizing.enabled' is switched on, overwrite behavior of default catalog
   * props
   */
  @Test
  public void testMergeCatalogPropertiesToTable7() {
    Map<String, String> expected = new HashMap<>();
    expected.put("self-optimizing.enabled", "true");
    expected.put("self-optimizing.quota", "0.1");
    expected.put("self-optimizing.group", "mygroup");
    expected.put("table.other-props", "foo");

    Map<String, String> userDefined = new HashMap<>();
    userDefined.put("self-optimizing.enabled", "true");
    userDefined.put("table.other-props", "foo");

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.log-store.enabled", "false");
    catalogProperties.put("table.log-store.address", "168.0.0.1:9092");
    catalogProperties.put("table.log-store.type", "kafka");
    catalogProperties.put("table.log-store.consistency.guarantee.enable", "true");
    catalogProperties.put("table.self-optimizing.enabled", "false");
    catalogProperties.put("table.self-optimizing.quota", "0.1");
    catalogProperties.put("table.self-optimizing.group", "mygroup");
    catalogProperties.put("ams.address", "127.0.0.1");

    Map<String, String> result =
        MixedFormatCatalogUtil.mergeCatalogPropertiesToTable(userDefined, catalogProperties);
    Assert.assertEquals(expected, result);
  }

  @Test
  public void testWithIcebergCatalogInitializeProperties() {
    Map<String, String> props;
    final String name = "test";
    final String typeHadoop = "hadoop";
    final String typeCustom = "custom";
    final String typeAms = CatalogMetaProperties.CATALOG_TYPE_AMS;
    final String type = "type";
    final String keyWarehouse = CatalogProperties.WAREHOUSE_LOCATION;
    final String path = "hdfs://test-cluster/warehouse";
    final String restImpl = RESTCatalog.class.getName();

    // hive catalog
    props =
        MixedFormatCatalogUtil.withIcebergCatalogInitializeProperties(
            name, typeHadoop, ImmutableMap.of(keyWarehouse, path));
    Assert.assertEquals(typeHadoop, props.get(type));

    // custom
    props =
        MixedFormatCatalogUtil.withIcebergCatalogInitializeProperties(
            name,
            typeCustom,
            ImmutableMap.of(keyWarehouse, path, CatalogProperties.CATALOG_IMPL, restImpl));
    Assert.assertFalse(props.containsKey(type));
    // custom args check
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> {
          MixedFormatCatalogUtil.withIcebergCatalogInitializeProperties(
              name, typeCustom, ImmutableMap.of(keyWarehouse, path));
        });

    // ams
    props =
        MixedFormatCatalogUtil.withIcebergCatalogInitializeProperties(
            name, typeAms, ImmutableMap.of(keyWarehouse, path));
    Assert.assertEquals(name, props.get(keyWarehouse));
    Assert.assertFalse(props.containsKey(type));
    Assert.assertEquals(restImpl, props.get(CatalogProperties.CATALOG_IMPL));
  }

  /** test merge writable properties basic case */
  @Test
  public void testMergeDefaultWritableProperties1() {
    Map<String, String> expected = new HashMap<>();
    expected.put("log-store.enabled", "true");
    expected.put("log-store.topic", "topic1");
    expected.put("log-store.address", "168.0.0.1:9092");
    expected.put("log-store.type", "kafka");
    expected.put("log-store.consistency.guarantee.enable", "true");
    expected.put("write.upsert.enable", "true");
    expected.put("write.parquet.compression-codec", "true");

    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put("log-store.enabled", "true");
    tableProperties.put("log-store.topic", "topic1");

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.log-store.enabled", "false");
    catalogProperties.put("table.log-store.address", "168.0.0.1:9092");
    catalogProperties.put("table.log-store.type", "kafka");
    catalogProperties.put("table.log-store.consistency.guarantee.enable", "true");
    catalogProperties.put("table.self-optimizing.enabled", "false");
    catalogProperties.put("table.optimize.quota", "0.1");
    catalogProperties.put("table.write.upsert.enable", "true");
    catalogProperties.put("table.write.parquet.compression-codec", "true");

    Map<String, String> result =
        MixedFormatCatalogUtil.mergePersistedCatalogPropertiesToTable(
            tableProperties, catalogProperties);
    Assert.assertEquals(expected, result);
  }

  /** test handling log-store related properties */
  @Test
  public void testMergeDefaultWritableProperties2() {
    Map<String, String> expected = new HashMap<>();
    expected.put("log-store.enabled", "false");

    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put("log-store.enabled", "false");

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.log-store.enabled", "false");
    catalogProperties.put("table.log-store.address", "168.0.0.1:9092");
    catalogProperties.put("table.log-store.type", "kafka");
    catalogProperties.put("table.self-optimizing.enabled", "false");
    catalogProperties.put("table.optimize.quota", "0.1");

    Map<String, String> result =
        MixedFormatCatalogUtil.mergePersistedCatalogPropertiesToTable(
            tableProperties, catalogProperties);
    Assert.assertEquals(expected, result);
  }

  /** test merge additional properties */
  @Test
  public void testMergeDefaultWritableProperties3() {
    Map<String, String> expected = new HashMap<>();
    expected.put("log-store.enabled", "true");
    expected.put("log-store.topic", "topic1");
    expected.put("log-store.type", "kafka");

    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put("log-store.enabled", "true");
    tableProperties.put("log-store.topic", "topic1");

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.log-store.enabled", "false");
    catalogProperties.put("table.log-store.address", "168.0.0.1:9092"); // would not merge
    catalogProperties.put("table.log-store.type", "kafka");
    catalogProperties.put("table.self-optimizing.enabled", "false");
    catalogProperties.put("table.optimize.quota", "0.1");

    // add additional props
    catalogProperties.put(
        CatalogMetaProperties.TABLE_NON_PERSISTED_PROPERTIES_ADDITIONAL,
        TableProperties.LOG_STORE_ADDRESS);
    Map<String, String> result =
        MixedFormatCatalogUtil.mergePersistedCatalogPropertiesToTable(
            tableProperties, catalogProperties);

    Assert.assertEquals(expected, result);
  }

  /** test merge additional props */
  @Test
  public void testMergeDefaultWritableProperties4() {
    Map<String, String> expected = new HashMap<>();
    expected.put("log-store.enabled", "true");
    expected.put("log-store.name", "cluster1");
    expected.put("log-store.topic", "topic1");

    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put("log-store.enabled", "true");
    tableProperties.put("log-store.topic", "topic1");

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.log-store.enabled", "false");
    catalogProperties.put("table.log-store.name", "cluster1");
    catalogProperties.put("table.log-store.address", "168.0.0.1:9092"); // would not merge
    catalogProperties.put("table.log-store.type", "kafka"); // would not merge

    // add additional prefix
    catalogProperties.put(
        CatalogMetaProperties.TABLE_NON_PERSISTED_PROPERTIES_ADDITIONAL,
        "log-store.address;log-store.type");
    Map<String, String> result =
        MixedFormatCatalogUtil.mergePersistedCatalogPropertiesToTable(
            tableProperties, catalogProperties);
    Assert.assertEquals(expected, result);
  }

  /** test merge additional prefix */
  @Test
  public void testMergeDefaultWritableProperties5() {
    Map<String, String> expected = new HashMap<>();
    expected.put("log-store.enabled", "true");
    expected.put("log-store.topic", "topic1");

    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put("log-store.enabled", "true");
    tableProperties.put("log-store.topic", "topic1");

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.log-store.enabled", "false");
    catalogProperties.put("table.log-store.address", "168.0.0.1:9092"); // would not merge
    catalogProperties.put("table.log-store.type", "kafka");
    catalogProperties.put("table.change.data.ttl.minutes", "10080");

    // add additional prefix
    catalogProperties.put(
        CatalogMetaProperties.TABLE_NON_PERSISTED_PROPERTIES_ADDITIONAL, "log-store.;change.data.");
    Map<String, String> result =
        MixedFormatCatalogUtil.mergePersistedCatalogPropertiesToTable(
            tableProperties, catalogProperties);

    Assert.assertEquals(expected, result);
  }

  /** test merge excluded props */
  @Test
  public void testMergeDefaultWritableProperties6() {
    Map<String, String> expected = new HashMap<>();
    expected.put("self-optimizing.enabled", "false");
    expected.put("optimize.quota", "0.1");

    Map<String, String> tableProperties = new HashMap<>();

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.self-optimizing.enabled", "false");
    catalogProperties.put("table.optimize.quota", "0.1");
    catalogProperties.put("table.clean-orphan-file.min-existing-time-minutes", "0.1");

    // add excluded props
    catalogProperties.put(
        CatalogMetaProperties.TABLE_NON_PERSISTED_PROPERTIES_EXCLUDED,
        "self-optimizing.enabled;optimize.quota");
    Map<String, String> result =
        MixedFormatCatalogUtil.mergePersistedCatalogPropertiesToTable(
            tableProperties, catalogProperties);
    Assert.assertEquals(expected, result);
  }

  /** test merge excluded prefix */
  @Test
  public void testMergeDefaultWritableProperties7() {
    Map<String, String> expected = new HashMap<>();
    expected.put("self-optimizing.enabled", "false");
    expected.put("self-optimizing.execute.num-retries", "3");
    expected.put("optimize.quota", "0.1");

    Map<String, String> tableProperties = new HashMap<>();

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.self-optimizing.enabled", "false");
    catalogProperties.put("table.self-optimizing.execute.num-retries", "3");
    catalogProperties.put("table.optimize.quota", "0.1");
    catalogProperties.put("table.clean-orphan-file.min-existing-time-minutes", "0.1");

    // add excluded prefix
    catalogProperties.put(
        CatalogMetaProperties.TABLE_NON_PERSISTED_PROPERTIES_EXCLUDED,
        "self-optimizing.;optimize.");
    Map<String, String> result =
        MixedFormatCatalogUtil.mergePersistedCatalogPropertiesToTable(
            tableProperties, catalogProperties);
    Assert.assertEquals(expected, result);
  }

  /** test merge excluded prefix */
  @Test
  public void testMergeDefaultWritableProperties8() {
    Map<String, String> expected = new HashMap<>();
    expected.put("snapshot.base.keep.minutes", "720");

    Map<String, String> tableProperties = new HashMap<>();

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.snapshot.base.keep.minutes", "720");
    catalogProperties.put("table.self-optimizing.execute.num-retries", "3");
    catalogProperties.put("table.optimize.quota", "0.1");
    catalogProperties.put("table.clean-orphan-file.min-existing-time-minutes", "0.1");

    // add excluded key
    catalogProperties.put(
        CatalogMetaProperties.TABLE_NON_PERSISTED_PROPERTIES_EXCLUDED,
        "snapshot.base.keep.minutes");
    Map<String, String> result =
        MixedFormatCatalogUtil.mergePersistedCatalogPropertiesToTable(
            tableProperties, catalogProperties);
    Assert.assertEquals(expected, result);
  }

  /** test conflicts between excluded and additional config prop which is a default not-writable */
  @Test
  public void testMergeDefaultWritableProperties9() {
    Map<String, String> expected = new HashMap<>();

    Map<String, String> tableProperties = new HashMap<>();

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.write.upsert.enabled", "false");

    catalogProperties.put(
        CatalogMetaProperties.TABLE_NON_PERSISTED_PROPERTIES_EXCLUDED, "write.upsert.enabled");
    catalogProperties.put(
        CatalogMetaProperties.TABLE_NON_PERSISTED_PROPERTIES_ADDITIONAL, "write.upsert.enabled");
    Map<String, String> result =
        MixedFormatCatalogUtil.mergePersistedCatalogPropertiesToTable(
            tableProperties, catalogProperties);
    Assert.assertEquals(expected, result);
  }

  /** test conflicts between excluded and additional config prop which is a default not-writable */
  @Test
  public void testMergeDefaultWritableProperties10() {
    Map<String, String> expected = new HashMap<>();
    expected.put("tag.auto-create.trigger.max-delay.minutes", "60");

    Map<String, String> tableProperties = new HashMap<>();

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.tag.auto-create.trigger.max-delay.minutes", "60");
    catalogProperties.put("table.tag.auto-create.trigger.offset.minutes", "1");

    catalogProperties.put(
        CatalogMetaProperties.TABLE_NON_PERSISTED_PROPERTIES_EXCLUDED,
        "tag.auto-create.trigger.max-delay.minutes");
    catalogProperties.put(
        CatalogMetaProperties.TABLE_NON_PERSISTED_PROPERTIES_ADDITIONAL, "tag.auto-create.");
    Map<String, String> result =
        MixedFormatCatalogUtil.mergePersistedCatalogPropertiesToTable(
            tableProperties, catalogProperties);
    Assert.assertEquals(expected, result);
  }

  /** test default non-persisted mixed-hive properties */
  @Test
  public void testMergeDefaultWritableProperties11() {
    Map<String, String> expected = new HashMap<>();

    Map<String, String> tableProperties = new HashMap<>();

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put("table.base.hive.auto-sync-data-write", "false");

    Map<String, String> result =
        MixedFormatCatalogUtil.mergePersistedCatalogPropertiesToTable(
            tableProperties, catalogProperties);
    Assert.assertEquals(expected, result);
  }
}
