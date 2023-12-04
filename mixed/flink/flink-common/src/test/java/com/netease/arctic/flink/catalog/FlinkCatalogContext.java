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

package com.netease.arctic.flink.catalog;

import static com.netease.arctic.flink.catalog.factories.ArcticCatalogFactoryOptions.METASTORE_URL;
import static com.netease.arctic.flink.table.descriptors.ArcticValidator.TABLE_FORMAT;

import com.netease.arctic.TestAms;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.flink.catalog.factories.FlinkCatalogFactory;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.hive.catalog.HiveCatalogTestHelper;
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
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

public class FlinkCatalogContext {

  static final TestHMS TEST_HMS = new TestHMS();
  static final TestAms TEST_AMS = new TestAms();
  static final FlinkCatalogFactory flinkCatalogFactory = new FlinkCatalogFactory();

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
            TableFormat.MIXED_ICEBERG));
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

  static FlinkCatalog initFlinkCatalog(TableFormat tableFormat) {
    FlinkCatalog flinkCatalog;
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
            "flink_catalog_name",
            factoryOptions,
            new Configuration(),
            FlinkCatalogContext.class.getClassLoader());
    flinkCatalog = (FlinkCatalog) flinkCatalogFactory.createCatalog(context);
    flinkCatalog.open();
    return flinkCatalog;
  }

  HiveMetaStoreClient getHMSClient() {
    return TEST_HMS.getHiveClient();
  }
}
