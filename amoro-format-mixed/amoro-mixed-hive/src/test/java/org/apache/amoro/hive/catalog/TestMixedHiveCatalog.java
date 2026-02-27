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

package org.apache.amoro.hive.catalog;

import static org.apache.amoro.properties.HiveTableProperties.MIXED_TABLE_FLAG;
import static org.apache.amoro.properties.HiveTableProperties.MIXED_TABLE_ROOT_LOCATION;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.catalog.TestMixedCatalog;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Table;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Map;

@RunWith(JUnit4.class)
public class TestMixedHiveCatalog extends TestMixedCatalog {

  private static final PartitionSpec IDENTIFY_SPEC =
      PartitionSpec.builderFor(BasicTableTestHelper.TABLE_SCHEMA).identity("op_time").build();

  @ClassRule public static TestHMS TEST_HMS = new TestHMS();

  public TestMixedHiveCatalog() {
    super(new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()));
  }

  @Override
  protected String expectCatalogImpl() {
    return MixedHiveCatalog.class.getName();
  }

  @Override
  protected PartitionSpec getCreateTableSpec() {
    return IDENTIFY_SPEC;
  }

  private void validateMixedHiveTableProperties(TableIdentifier tableIdentifier) throws TException {
    String dbName = tableIdentifier.getDatabase();
    String tbl = tableIdentifier.getTableName();
    Map<String, String> tableParameter =
        TEST_HMS.getHiveClient().getTable(dbName, tbl).getParameters();

    Assert.assertTrue(tableParameter.containsKey(MIXED_TABLE_ROOT_LOCATION));
    Assert.assertTrue(tableParameter.get(MIXED_TABLE_ROOT_LOCATION).endsWith(tbl));
    Assert.assertTrue(tableParameter.containsKey(MIXED_TABLE_FLAG));
  }

  @Override
  protected void validateCreatedTable(MixedTable table, boolean withKey) throws Exception {
    super.validateCreatedTable(table, withKey);
    validateMixedHiveTableProperties(table.id());
  }

  @Override
  protected void assertIcebergTableStore(
      Table tableStore, boolean isBaseStore, boolean isKeyedTable) {
    // mixed-hive does not check the table store
  }
}
