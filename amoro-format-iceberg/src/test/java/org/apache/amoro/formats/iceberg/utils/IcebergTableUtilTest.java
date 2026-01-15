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

package org.apache.amoro.formats.iceberg.utils;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.data.FileHelpers;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.types.Types;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class IcebergTableUtilTest extends TableTestBase {

  public static final Schema TABLE_SCHEMA =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.required(2, "data", Types.StringType.get()));
  public static final Schema EQUALITY_DELETE_ROW_SCHEMA = TABLE_SCHEMA.select("id");

  public static final PrimaryKeySpec PRIMARY_KEY_SPEC =
      PrimaryKeySpec.builderFor(TABLE_SCHEMA).addColumn("id").build();

  public static final PartitionSpec SPEC_0 =
      PartitionSpec.builderFor(TABLE_SCHEMA).bucket("id", 5, "id_bucket_5").build();

  public static final PartitionSpec SPEC_1 =
      PartitionSpec.builderFor(TABLE_SCHEMA).bucket("id", 10, "id_bucket_10").build();

  public static final Map<String, String> TABLE_PROPERTIES = Maps.newHashMapWithExpectedSize(1);

  static {
    TABLE_PROPERTIES.put(TableProperties.FORMAT_VERSION, "2");
  }

  public IcebergTableUtilTest() {
    super(
        new BasicCatalogTestHelper(TableFormat.ICEBERG),
        new BasicTableTestHelper(TABLE_SCHEMA, PRIMARY_KEY_SPEC, SPEC_0, TABLE_PROPERTIES));
  }

  @Test
  public void getDanglingDeleteFiles() throws IOException {
    Table table =
        getIcebergCatalog()
            .loadTable(
                TableIdentifier.of(TableTestHelper.TEST_DB_NAME, TableTestHelper.TEST_TABLE_NAME));
    GenericRecord record0 = GenericRecord.create(TABLE_SCHEMA);
    record0.set(0, 0);
    record0.set(1, "spec=0");
    OutputFileFactory outputFileFactory0 =
        OutputFileFactory.builderFor(table, 0, 1).format(FileFormat.PARQUET).build();
    PartitionKey partitionKey0 = new PartitionKey(SPEC_0, TABLE_SCHEMA);
    partitionKey0.partition(record0);
    DataFile dataFile0 =
        FileHelpers.writeDataFile(
            table,
            outputFileFactory0.newOutputFile(partitionKey0).encryptingOutputFile(),
            partitionKey0,
            Collections.singletonList(record0));
    DeleteFile deleteFile0 =
        FileHelpers.writeDeleteFile(
            table,
            outputFileFactory0.newOutputFile(partitionKey0).encryptingOutputFile(),
            partitionKey0,
            Collections.singletonList(record0),
            EQUALITY_DELETE_ROW_SCHEMA);
    RowDelta rowDelta0 = table.newRowDelta();
    rowDelta0.addRows(dataFile0);
    rowDelta0.addDeletes(deleteFile0);
    rowDelta0.commit();

    table
        .updateSpec()
        .removeField("id_bucket_5")
        .addField("id_bucket_10", Expressions.bucket("id", 10))
        .commit();

    GenericRecord record1 = GenericRecord.create(TABLE_SCHEMA);
    record1.set(0, 5);
    record1.set(1, "spec=1");
    OutputFileFactory outputFileFactory1 =
        OutputFileFactory.builderFor(table, 1, 2).format(FileFormat.PARQUET).build();
    PartitionKey partitionKey1 = new PartitionKey(SPEC_1, TABLE_SCHEMA);
    partitionKey1.partition(record1);
    DataFile dataFile1 =
        FileHelpers.writeDataFile(
            table,
            outputFileFactory1.newOutputFile(partitionKey1).encryptingOutputFile(),
            partitionKey1,
            Collections.singletonList(record1));
    DeleteFile deleteFile1 =
        FileHelpers.writeDeleteFile(
            table,
            outputFileFactory1.newOutputFile(partitionKey1).encryptingOutputFile(),
            partitionKey1,
            Collections.singletonList(record1),
            EQUALITY_DELETE_ROW_SCHEMA);
    RowDelta rowDelta1 = table.newRowDelta();
    rowDelta1.addRows(dataFile1);
    rowDelta1.addDeletes(deleteFile1);
    rowDelta1.commit();

    Set<DeleteFile> deleteFileSet = IcebergTableUtil.getDanglingDeleteFiles(table);
    for (DeleteFile deleteFile : deleteFileSet) {
      StructLike partition = deleteFile.partition();
      Assert.assertNotNull(partition.get(0, Integer.class));
    }
  }
}
