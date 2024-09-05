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

package org.apache.amoro.flink.catalog;

import static org.apache.amoro.flink.catalog.factories.CatalogFactoryOptions.METASTORE_URL;
import static org.apache.amoro.flink.table.descriptors.MixedFormatValidator.TABLE_FORMAT;

import org.apache.amoro.TableFormat;
import org.apache.amoro.TestAms;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.flink.catalog.factories.FlinkUnifiedCatalogFactory;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.catalog.CatalogTable;
import org.apache.flink.table.catalog.Column;
import org.apache.flink.table.catalog.ObjectPath;
import org.apache.flink.table.catalog.ResolvedCatalogTable;
import org.apache.flink.table.catalog.ResolvedSchema;
import org.apache.flink.table.factories.FactoryUtil;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

public class FlinkCatalogContext {

  static final TestHMS TEST_HMS = new TestHMS();
  static final TestAms TEST_AMS = new TestAms();
  static final FlinkUnifiedCatalogFactory FLINK_UNIFIED_CATALOG_FACTORY =
      new FlinkUnifiedCatalogFactory();

  static ResolvedSchema resolvedSchema =
      ResolvedSchema.of(
          Column.physical("name", DataTypes.STRING()), Column.physical("age", DataTypes.INT()));
  static Schema schema = Schema.newBuilder().fromResolvedSchema(resolvedSchema).build();

  ObjectPath objectPath = new ObjectPath("default", "test_hive_from_flink");

  static Stream<Arguments> getFlinkCatalogAndTable() {
    return Stream.of(
        Arguments.of(
            initFlinkCatalog(TableFormat.MIXED_HIVE),
            generateFlinkTable(TableFormat.MIXED_HIVE.toString()),
            TableFormat.MIXED_HIVE),
        Arguments.of(
            initFlinkCatalog(TableFormat.MIXED_ICEBERG),
            generateFlinkTable(TableFormat.MIXED_ICEBERG.toString()),
            TableFormat.MIXED_ICEBERG),
        Arguments.of(
            initFlinkCatalog(TableFormat.ICEBERG),
            generateFlinkTable(TableFormat.ICEBERG.toString()),
            TableFormat.ICEBERG),
        Arguments.of(
            initFlinkCatalog(TableFormat.PAIMON),
            generateFlinkTable(TableFormat.PAIMON.toString()),
            TableFormat.PAIMON));
  }

  static ResolvedCatalogTable generateFlinkTable(String tableFormat) {
    return new ResolvedCatalogTable(
        CatalogTable.of(
            schema,
            "Flink managed table",
            new ArrayList<>(),
            Collections.singletonMap(TABLE_FORMAT.key(), tableFormat)),
        resolvedSchema);
  }

  void initial() throws Exception {
    TEST_HMS.before();
    TEST_AMS.before();
  }

  void close() {
    TEST_AMS.after();
    TEST_HMS.after();
  }

  static FlinkUnifiedCatalog initFlinkCatalog(TableFormat tableFormat) {
    FlinkUnifiedCatalog flinkUnifiedCatalog;
    Map<String, String> factoryOptions = Maps.newHashMap();
    CatalogMeta meta =
        HiveCatalogTestHelper.build(TEST_HMS.getHiveConf(), tableFormat)
            .buildCatalogMeta(TEST_HMS.getWareHouseLocation());
    meta.setCatalogName(tableFormat.name().toLowerCase());

    TEST_AMS.getAmsHandler().dropCatalog(meta.getCatalogName());
    TEST_AMS.getAmsHandler().createCatalog(meta);

    factoryOptions.put(METASTORE_URL.key(), TEST_AMS.getServerUrl() + "/" + meta.getCatalogName());
    final FactoryUtil.DefaultCatalogContext context =
        new FactoryUtil.DefaultCatalogContext(
            "FLINK_" + tableFormat,
            factoryOptions,
            new Configuration(),
            FlinkCatalogContext.class.getClassLoader());
    flinkUnifiedCatalog =
        (FlinkUnifiedCatalog) FLINK_UNIFIED_CATALOG_FACTORY.createCatalog(context);
    flinkUnifiedCatalog.open();
    return flinkUnifiedCatalog;
  }

  HiveMetaStoreClient getHMSClient() {
    return TEST_HMS.getHiveClient();
  }
}
