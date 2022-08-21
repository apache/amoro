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

package com.netease.arctic.hive.op;

import com.netease.arctic.hive.HiveTableTestBase;
import com.netease.arctic.hive.utils.HiveSchemaUtil;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.iceberg.types.Types;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestHiveSchemaUpdate extends HiveTableTestBase {

  @Test
  public void testKeyedHiveTable() throws TException {
    String testAddCol = "testAdd";
    String testDoc = "test Doc";
    testKeyedHiveTable.updateSchema().addColumn(testAddCol, Types.IntegerType.get(), testDoc).commit();
    List<FieldSchema> fieldSchemas = hms.getClient().getFields(HIVE_DB_NAME, "test_pk_hive_table");
    boolean isAdd = false;
    for (FieldSchema fieldSchema : fieldSchemas) {
      if (fieldSchema.getName().equalsIgnoreCase(testAddCol) && fieldSchema.getComment().equalsIgnoreCase(testDoc) &&
          fieldSchema.getType().equals("int")) {
        isAdd = true;
      }
    }
    Assert.assertTrue(isAdd);
    Assert.assertTrue(HiveSchemaUtil.compareSchema(testKeyedHiveTable.schema(), testKeyedHiveTable.spec(), fieldSchemas));
  }
}
