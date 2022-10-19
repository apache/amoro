/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.server.service;

import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.server.AmsTestBase;
import com.netease.arctic.ams.server.ArcticMetaStore;
import com.netease.arctic.ams.server.service.impl.CatalogMetadataService;
import com.netease.arctic.ams.server.service.impl.DDLTracerService;
import com.netease.arctic.ams.server.service.impl.TableMetricsStatisticService;
import com.netease.arctic.ams.server.utils.JDBCSqlSessionFactoryProvider;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import java.util.ArrayList;
import java.util.List;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.junit.BeforeClass;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static com.netease.arctic.ams.server.AmsTestBase.AMS_TEST_CATALOG_NAME;
import static com.netease.arctic.ams.server.AmsTestBase.AMS_TEST_DB_NAME;

@PowerMockIgnore({"javax.management.*"})
@PrepareForTest({
    ServiceContainer.class,
    JDBCSqlSessionFactoryProvider.class,
    TableMetricsStatisticService.class,
    ArcticMetaStore.class,
    CatalogMetadataService.class
})
public class TestTableMetricsStatisticService {
  private static final String commitTableName = "tblDDlCommit";
  private static TableIdentifier testIdentifier;
  private static ArcticTable testTable;


  private static TableMetricsStatisticService service = ServiceContainer.getTableMetricsStatisticService();

  private static Schema schema;

  @BeforeClass
  public static void before() {
    schema = new Schema(
        Types.NestedField.required(1, "id", Types.IntegerType.get()),
        Types.NestedField.required(2, "data", Types.StringType.get())
    );
    PrimaryKeySpec PRIMARY_KEY_SPEC = PrimaryKeySpec.builderFor(schema)
        .addColumn("id").build();

    testIdentifier = new TableIdentifier();
    testIdentifier.catalog = AMS_TEST_CATALOG_NAME;
    testIdentifier.database = AMS_TEST_DB_NAME;
    testIdentifier.tableName = commitTableName;
    testTable = AmsTestBase.catalog.newTableBuilder(
        com.netease.arctic.table.TableIdentifier.of(testIdentifier),
        schema).withPrimaryKeySpec(PRIMARY_KEY_SPEC).create();
  }
}
