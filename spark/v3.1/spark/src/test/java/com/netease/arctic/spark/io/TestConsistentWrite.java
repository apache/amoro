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

package com.netease.arctic.spark.io;

import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.hive.HiveTableProperties;
import com.netease.arctic.hive.io.HiveDataTestHelpers;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.spark.SparkTestBase;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableIdentifier;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.iceberg.types.Types;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.types.StructType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.List;

@RunWith(Parameterized.class)
public class TestConsistentWrite extends SparkTestBase {
  private final String catalogName = catalogNameHive;
  private final String database = "db_def";
  private final String tableName = "tbl";
  public static final Schema schema = new Schema(
      Types.NestedField.required(1, "id", Types.IntegerType.get()),
      Types.NestedField.required(2, "ts", Types.LongType.get()),
      Types.NestedField.required(3, "pt", Types.StringType.get())
  );


  private final boolean consistentWriteEnabled ;
  private final PartitionSpec partitionSpec;
  private final PrimaryKeySpec keySpec;

  public TestConsistentWrite(boolean consistentWriteEnabled, PartitionSpec ptSpec, PrimaryKeySpec pkSpec) {
    this.consistentWriteEnabled = consistentWriteEnabled;
    this.partitionSpec = ptSpec;
    this.keySpec = pkSpec;
  }

  @Parameterized.Parameters(name = "{0}, {1}, {2}")
  public static Object[] parameters() {
    final PartitionSpec ptSpec = PartitionSpec.builderFor(schema)
        .identity("pt").build();

    final PrimaryKeySpec pkSpec = PrimaryKeySpec.builderFor(schema)
        .addColumn("id")
        .build();
    return new Object[][] {
        {true, ptSpec, pkSpec},
        {false, ptSpec, pkSpec},
        {true, PartitionSpec.unpartitioned(), pkSpec},
        {false, PartitionSpec.unpartitioned(), pkSpec},
        {true, ptSpec, PrimaryKeySpec.noPrimaryKey()},
        {false, ptSpec, PrimaryKeySpec.noPrimaryKey()}
    };
  }

  ArcticCatalog catalog;
  @Before
  public void before() {
    catalog = catalog(catalogName);
    catalog.createDatabase(database);
  }

  public void after() {
    try{
      catalog.dropTable(TableIdentifier.of(catalogName, database, tableName), true);
    } catch (Exception e) {
      // pass
    }
    catalog.dropDatabase(database);
  }


  @Test
  public void testConsistentWrite() {
    ArcticTable table = catalog.newTableBuilder(
        TableIdentifier.of(catalogName, database, tableName), schema)
        .withProperty(HiveTableProperties.HIVE_CONSISTENT_WRITE_ENABLED, consistentWriteEnabled + "")
        .withPrimaryKeySpec(this.keySpec)
        .withPartitionSpec(this.partitionSpec)
        .create();

    StructType dsSchema = SparkSchemaUtil.convert(schema);
    List<Record> records = Lists.newArrayList(
        newRecord(schema, 1, 0L, "pt1"),
        newRecord(schema, 2, 0L, "pt2"),
        newRecord(schema, 3, 0L, "pt3")
    );
    TaskWriters taskWriters =  TaskWriters.of(table)
        .withOrderedWriter(false)
        .withDataSourceSchema(dsSchema);
    if (keySpec.primaryKeyExisted()) {
      taskWriters = taskWriters.withTransactionId(1L);
    }

    try (TaskWriter<InternalRow> writer = taskWriters.newBaseWriter(true)) {
      records.stream()
          .map(r -> recordToInternalRow(schema, r))
          .forEach(
              i -> {
                try {
                  writer.write(i);
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              });
      WriteResult result = writer.complete();
      DataFile[] dataFiles = result.dataFiles();
      HiveDataTestHelpers.assertWriteConsistentFilesName(
          (SupportHive) table, Lists.newArrayList(dataFiles));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

