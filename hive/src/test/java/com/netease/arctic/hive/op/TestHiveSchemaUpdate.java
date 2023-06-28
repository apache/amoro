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

package com.netease.arctic.hive.op;

import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.hive.catalog.HiveCatalogTestHelper;
import com.netease.arctic.hive.catalog.HiveTableTestHelper;
import com.netease.arctic.hive.table.KeyedHiveTable;
import com.netease.arctic.hive.table.UnkeyedHiveTable;
import com.netease.arctic.hive.utils.HiveSchemaUtil;
import com.netease.arctic.table.TableIdentifier;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.types.Types;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.Map;

import static com.netease.arctic.BasicTableTestHelper.PRIMARY_KEY_SPEC;
import static com.netease.arctic.hive.HiveTableProperties.ARCTIC_TABLE_FLAG;
import static com.netease.arctic.hive.HiveTableProperties.ARCTIC_TABLE_ROOT_LOCATION;

import static com.netease.arctic.hive.HiveTableTestBase.hms;
import static com.netease.arctic.hive.catalog.HiveTableTestHelper.COLUMN_NAME_D;
import static com.netease.arctic.hive.catalog.HiveTableTestHelper.COLUMN_NAME_OP_DAY;
import static com.netease.arctic.hive.catalog.HiveTableTestHelper.COLUMN_NAME_OP_TIME_WITH_ZONE;
import static com.netease.arctic.hive.catalog.HiveTableTestHelper.HIVE_SPEC;
import static com.netease.arctic.hive.catalog.HiveTableTestHelper.HIVE_TABLE_SCHEMA;

@RunWith(Parameterized.class)
public class TestHiveSchemaUpdate extends TableTestBase {

  @ClassRule
  public static TestHMS TEST_HMS = new TestHMS();

  public TestHiveSchemaUpdate(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {{new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
                            new HiveTableTestHelper(true, true)},
                           {new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
                            new HiveTableTestHelper(false, true)}};
  }

  @Test
  public void testHiveParameterFromArctic() throws TException {

    String unkeyedTable = "unkeyed_test_hive_table_tmp";
    String keyedTable = "keyed_test_hive_table_tmp";
    ArcticCatalog hiveCatalog = getCatalog();

    final TableIdentifier unKeyedHiveTableIdentifier =
            TableIdentifier.of(TableTestHelper.TEST_CATALOG_NAME, TableTestHelper.TEST_DB_NAME, unkeyedTable);

    final TableIdentifier keyedHiveTableIdentifier =
            TableIdentifier.of(TableTestHelper.TEST_CATALOG_NAME, TableTestHelper.TEST_DB_NAME, keyedTable);

    UnkeyedHiveTable testUnKeyedTable = (UnkeyedHiveTable) hiveCatalog
            .newTableBuilder(unKeyedHiveTableIdentifier, HIVE_TABLE_SCHEMA)
            .withPartitionSpec(HIVE_SPEC)
            .create().asUnkeyedTable();

    Map<String,String> tableParameter =  TEST_HMS.getHiveClient()
            .getTable(TableTestHelper.TEST_DB_NAME, unkeyedTable).getParameters();
    Assert.assertTrue(tableParameter.containsKey(ARCTIC_TABLE_ROOT_LOCATION));
    Assert.assertTrue(tableParameter.get(ARCTIC_TABLE_ROOT_LOCATION).endsWith(unkeyedTable));
    Assert.assertTrue(tableParameter.containsKey(ARCTIC_TABLE_FLAG));

    hiveCatalog.dropTable(unKeyedHiveTableIdentifier, true);
    TEST_AMS.getAmsHandler().getTableCommitMetas().remove(unKeyedHiveTableIdentifier.buildTableIdentifier());

    KeyedHiveTable testKeyedTable = (KeyedHiveTable) hiveCatalog
            .newTableBuilder(keyedHiveTableIdentifier, HIVE_TABLE_SCHEMA)
            .withPartitionSpec(HIVE_SPEC)
            .withPrimaryKeySpec(PRIMARY_KEY_SPEC)
            .create().asKeyedTable();

    Map<String,String> keyedTableParameter =  TEST_HMS.getHiveClient()
            .getTable(TableTestHelper.TEST_DB_NAME, keyedTable).getParameters();
    Assert.assertTrue(keyedTableParameter.containsKey(ARCTIC_TABLE_ROOT_LOCATION));
    Assert.assertTrue(keyedTableParameter.get(ARCTIC_TABLE_ROOT_LOCATION)
            .endsWith(keyedTable));
    Assert.assertTrue(keyedTableParameter.containsKey(ARCTIC_TABLE_FLAG));

    hiveCatalog.dropTable(keyedHiveTableIdentifier, true);
    TEST_AMS.getAmsHandler().getTableCommitMetas().remove(keyedHiveTableIdentifier.buildTableIdentifier());
  }

  @Test
  public void testAddColumn() throws TException {
    String addColumnName = "test_add";
    String addColumnDoc = "test Doc";
    getArcticTable().updateSchema().addColumn(addColumnName, Types.IntegerType.get(), addColumnDoc).commit();
    Schema expectSchema = new Schema(
        Types.NestedField.required(1, "id", Types.IntegerType.get()),
        Types.NestedField.required(2, "name", Types.StringType.get()),
        Types.NestedField.required(3, "ts", Types.LongType.get()),
        Types.NestedField.required(4, "op_time", Types.TimestampType.withoutZone()),
        Types.NestedField.required(5, COLUMN_NAME_OP_TIME_WITH_ZONE, Types.TimestampType.withZone()),
        Types.NestedField.required(6, COLUMN_NAME_D, Types.DecimalType.of(10, 0)),
        Types.NestedField.optional(8, addColumnName, Types.IntegerType.get(), addColumnDoc),
        Types.NestedField.required(7, COLUMN_NAME_OP_DAY, Types.StringType.get())
    );
    checkTableSchema(expectSchema);
  }

  @Test
  public void testUpdateColumn() throws TException {
    getArcticTable().updateSchema().updateColumn("id", Types.LongType.get(), "update doc").commit();
    Schema expectSchema = new Schema(
        Types.NestedField.required(1, "id", Types.LongType.get(), "update doc"),
        Types.NestedField.required(2, "name", Types.StringType.get()),
        Types.NestedField.required(3, "ts", Types.LongType.get()),
        Types.NestedField.required(4, "op_time", Types.TimestampType.withoutZone()),
        Types.NestedField.required(5, COLUMN_NAME_OP_TIME_WITH_ZONE, Types.TimestampType.withZone()),
        Types.NestedField.required(6, COLUMN_NAME_D, Types.DecimalType.of(10, 0)),
        Types.NestedField.required(7, COLUMN_NAME_OP_DAY, Types.StringType.get())
    );
    checkTableSchema(expectSchema);
  }

  @Test
  public void testAddColumnInTx() throws TException {
    String addColumnName = "test_add";
    String addColumnDoc = "test Doc";
    Transaction transaction = getBaseStore().newTransaction();
    transaction.updateSchema().addColumn(addColumnName, Types.IntegerType.get(), addColumnDoc).commit();
    transaction.commitTransaction();
    Schema expectSchema = new Schema(
        Types.NestedField.required(1, "id", Types.IntegerType.get()),
        Types.NestedField.required(2, "name", Types.StringType.get()),
        Types.NestedField.required(3, "ts", Types.LongType.get()),
        Types.NestedField.required(4, "op_time", Types.TimestampType.withoutZone()),
        Types.NestedField.required(5, COLUMN_NAME_OP_TIME_WITH_ZONE, Types.TimestampType.withZone()),
        Types.NestedField.required(6, COLUMN_NAME_D, Types.DecimalType.of(10, 0)),
        Types.NestedField.optional(8, addColumnName, Types.IntegerType.get(), addColumnDoc),
        Types.NestedField.required(7, COLUMN_NAME_OP_DAY, Types.StringType.get())
    );
    checkTableSchema(expectSchema);
  }

  private void checkTableSchema(Schema expectSchema) throws TException {
    Assert.assertEquals(expectSchema.asStruct(), getArcticTable().schema().asStruct());
    if (isKeyedTable()) {
      Assert.assertEquals(expectSchema.asStruct(), getArcticTable().asKeyedTable().changeTable().schema().asStruct());
      Assert.assertEquals(expectSchema.asStruct(), getArcticTable().asKeyedTable().baseTable().schema().asStruct());
    }
    Table hiveTable = TEST_HMS.getHiveClient().getTable(getArcticTable().id().getDatabase(),
        getArcticTable().id().getTableName());
    Assert.assertEquals(HiveSchemaUtil.hiveTableFields(expectSchema, getArcticTable().spec()),
        hiveTable.getSd().getCols());
  }
}
