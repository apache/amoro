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

import com.netease.arctic.hive.HiveTableTestBase;
import com.netease.arctic.hive.utils.HiveSchemaUtil;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.types.Types;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.netease.arctic.hive.HiveTableProperties.ARCTIC_TABLE_FLAG;
import static com.netease.arctic.hive.HiveTableProperties.ARCTIC_TABLE_ROOT_LOCATION;

public class TestHiveSchemaUpdate extends HiveTableTestBase {

  @Test
  public void testHiveParameterFromArctic() throws TException {
    String database = testHiveTable.id().getDatabase();
    String table = testHiveTable.id().getTableName();
    Map<String,String> tableParameter =  hms.getClient()
            .getTable(database,table).getParameters();
    Assert.assertTrue(tableParameter.containsKey(ARCTIC_TABLE_ROOT_LOCATION));
    Assert.assertTrue(tableParameter.get(ARCTIC_TABLE_ROOT_LOCATION).endsWith(table));
    Assert.assertTrue(tableParameter.containsKey(ARCTIC_TABLE_FLAG));

    Map<String,String> keyedTableParameter =  hms.getClient()
            .getTable(testKeyedHiveTable.id().getDatabase(),testKeyedHiveTable.id().getTableName()).getParameters();
    Assert.assertTrue(keyedTableParameter.containsKey(ARCTIC_TABLE_ROOT_LOCATION));
    Assert.assertTrue(keyedTableParameter.get(ARCTIC_TABLE_ROOT_LOCATION)
            .endsWith(testKeyedHiveTable.id().getTableName()));
    Assert.assertTrue(keyedTableParameter.containsKey(ARCTIC_TABLE_FLAG));

  }

  @Test
  public void testKeyedAdd() throws TException {
    String testAddCol = "testAdd";
    String testDoc = "test Doc";
    testKeyedHiveTable.updateSchema().addColumn(testAddCol, Types.IntegerType.get(), testDoc).commit();
    List<FieldSchema> fieldSchemas = hms.getClient().getFields(HIVE_DB_NAME, "test_pk_hive_table");
    boolean isExpect = false;
    for (FieldSchema fieldSchema : fieldSchemas) {
      if (fieldSchema.getName().equalsIgnoreCase(testAddCol) && fieldSchema.getComment().equalsIgnoreCase(testDoc) &&
          fieldSchema.getType().equals("int")) {
        isExpect = true;
      }
    }
    Assert.assertTrue(isExpect);
    Assert.assertTrue(compareSchema(testKeyedHiveTable.changeTable().schema(), testKeyedHiveTable.spec(), fieldSchemas));
    Assert.assertTrue(compareSchema(testKeyedHiveTable.schema(), testKeyedHiveTable.spec(), fieldSchemas));
  }

  @Test
  public void testKeyedUpdate() throws TException {
    String testUpdateCol = "testUpdate";
    String testDoc = "test Doc";
    testKeyedHiveTable.updateSchema().addColumn(testUpdateCol, Types.FloatType.get(), "init doc").commit();
    testKeyedHiveTable.updateSchema().updateColumn(testUpdateCol, Types.DoubleType.get(), testDoc).commit();
    List<FieldSchema> fieldSchemas = hms.getClient().getFields(HIVE_DB_NAME, "test_pk_hive_table");
    boolean isExpect = false;
    for (FieldSchema fieldSchema : fieldSchemas) {
      if (fieldSchema.getName().equalsIgnoreCase(testUpdateCol) && fieldSchema.getComment().equalsIgnoreCase(testDoc) &&
          fieldSchema.getType().equals("double")) {
        isExpect = true;
      }
    }
    Assert.assertTrue(isExpect);
    Assert.assertTrue(compareSchema(testKeyedHiveTable.changeTable().schema(), testKeyedHiveTable.spec(), fieldSchemas));
    Assert.assertTrue(compareSchema(testKeyedHiveTable.schema(), testKeyedHiveTable.spec(), fieldSchemas));
  }

  @Test
  public void testUnKeyedAdd() throws TException {
    String testAddCol = "testAdd";
    String testDoc = "test Doc";
    testHiveTable.updateSchema().addColumn(testAddCol, Types.IntegerType.get(), testDoc).commit();
    List<FieldSchema> fieldSchemas = hms.getClient().getFields(HIVE_DB_NAME, "test_hive_table");
    boolean isExpect = false;
    for (FieldSchema fieldSchema : fieldSchemas) {
      if (fieldSchema.getName().equalsIgnoreCase(testAddCol) && fieldSchema.getComment().equalsIgnoreCase(testDoc) &&
          fieldSchema.getType().equals("int")) {
        isExpect = true;
      }
    }
    Assert.assertTrue(isExpect);
    Assert.assertTrue(compareSchema(testHiveTable.schema(), testHiveTable.spec(), fieldSchemas));
  }

  @Test
  public void testUnKeyedUpdate() throws TException {
    String testUpdateCol = "testUpdate";
    String testDoc = "test Doc";
    testHiveTable.updateSchema().addColumn(testUpdateCol, Types.FloatType.get(), "init doc").commit();
    testHiveTable.updateSchema().updateColumn(testUpdateCol, Types.DoubleType.get(), testDoc).commit();
    List<FieldSchema> fieldSchemas = hms.getClient().getFields(HIVE_DB_NAME, "test_hive_table");
    boolean isExpect = false;
    for (FieldSchema fieldSchema : fieldSchemas) {
      if (fieldSchema.getName().equalsIgnoreCase(testUpdateCol) && fieldSchema.getComment().equalsIgnoreCase(testDoc) &&
          fieldSchema.getType().equals("double")) {
        isExpect = true;
      }
    }
    Assert.assertTrue(isExpect);
    Assert.assertTrue(compareSchema(testHiveTable.schema(), testHiveTable.spec(), fieldSchemas));
  }

  @Test
  public void testUnKeyedTransactionAdd() throws TException {
    String testAddCol1 = "testAdd1";
    String testAddCol2 = "testAdd2";
    String testDoc = "test Doc";
    UnkeyedTable table = testHiveTable;
    Transaction transaction = table.newTransaction();
    transaction.updateSchema().
        addColumn(testAddCol1, Types.FloatType.get(), "init doc").
        addColumn(testAddCol2, Types.DoubleType.get(), testDoc).commit();
    int exceptedNumberOfFields = table.schema().columns().size() + 2;
    testHiveTable.refresh();
    Assert.assertFalse("table schema should not added",
        table.schema().columns().size() == exceptedNumberOfFields);
    transaction.commitTransaction();
    Assert.assertTrue("table schema should added",
        table.schema().columns().size() == exceptedNumberOfFields);
    List<FieldSchema> fieldSchemas = hms.getClient().getFields(HIVE_DB_NAME, "test_hive_table");
    Assert.assertTrue(compareSchema(testHiveTable.schema(), testHiveTable.spec(), fieldSchemas));
  }

  boolean compareSchema(Schema schema, PartitionSpec spec, List<FieldSchema> hiveSchema) {
    List<FieldSchema> convertFields = HiveSchemaUtil.hiveTableFields(schema, spec);
    convertFields.forEach(fieldSchema -> {
      fieldSchema.setName(fieldSchema.getName().toLowerCase());
    });
    if (convertFields.size() != hiveSchema.size()) {
      return false;
    }
    for (FieldSchema fieldSchema : hiveSchema) {
      if (!convertFields.contains(fieldSchema)) {
        return false;
      }
    }
    return true;
  }
}
