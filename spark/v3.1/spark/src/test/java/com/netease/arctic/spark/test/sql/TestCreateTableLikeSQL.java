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

package com.netease.arctic.spark.test.sql;

import com.netease.arctic.spark.SparkSQLProperties;
import com.netease.arctic.spark.test.Asserts;
import com.netease.arctic.spark.test.helper.TestTableHelper;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.utils.CollectionHelper;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.netease.arctic.spark.test.helper.TestTableHelper.IcebergSchema;
import static com.netease.arctic.spark.test.helper.TestTableHelper.PtSpec;
import static com.netease.arctic.spark.test.helper.TestTableHelper.hivePartition;
import static com.netease.arctic.spark.test.helper.TestTableHelper.hiveSchema;
import static com.netease.arctic.spark.test.helper.TestTableHelper.hiveTable;

@SessionCatalog(usingArcticSessionCatalog = true)
public class TestCreateTableLikeSQL extends SparkTableTestBase {
  //1. 源端表是 Arctic 表 (主键，分区)
  //2. 源端表非 Arctic 表 (分区)

  //3. using arctic 测试 (Session catalog, 非Session catalog)

  public static Stream<Arguments> testCreateTableLikeHiveTable() {
    return Stream.of(
        Arguments.of(hiveTable(hiveSchema, hivePartition).build(),
            IcebergSchema.NO_PK_WITHOUT_ZONE, PtSpec.hive_pt_spec
        ),
        Arguments.of(hiveTable(hiveSchema, null).withProperties("k1", "v1").build(),
            IcebergSchema.NO_PK_NO_PT_WITHOUT_ZONE, PartitionSpec.unpartitioned()
        )
    );
  }

  @DisplayName("Test SQL: CREATE TABLE LIKE hive table")
  @ParameterizedTest
  @MethodSource
  public void testCreateTableLikeHiveTable(
      Table hiveTable,
      Schema expectSchema, PartitionSpec expectPtSpec
  ) {
    test().inSparkCatalog(SESSION_CATALOG)
        .withHiveTable(hiveTable)
        .execute(context -> {
          String sqlText = "CREATE TABLE " + context.databaseAndTable +
              " LIKE " + context.sourceDatabaseAndTable + " USING arctic";
          sql(sqlText);
          ArcticTable table = context.loadTable();
          Asserts.assertType(expectSchema.asStruct(), table.schema().asStruct());
          Asserts.assertPartition(expectPtSpec, table.spec());
          // CREATE TABLE LIKE do not copy properties.
          Assertions.assertFalse(table.properties().containsKey("k1"));
        });
  }

  public static Stream<Arguments> testCreateTableLikeDataLakeTable() {
    return Stream.of(
        Arguments.of(
            SESSION_CATALOG, IcebergSchema.PK_WITHOUT_ZONE, PtSpec.hive_pt_spec,
            TestTableHelper.PkSpec.pk_include_pt
        ),
        Arguments.of(
            SESSION_CATALOG, IcebergSchema.NO_PK_WITHOUT_ZONE, PtSpec.hive_pt_spec,
            PrimaryKeySpec.noPrimaryKey()
        ),
        Arguments.of(
            HIVE_CATALOG, IcebergSchema.PK_WITHOUT_ZONE, PtSpec.hive_pt_spec,
            TestTableHelper.PkSpec.pk_include_pt
        ),
        Arguments.of(
            HIVE_CATALOG, IcebergSchema.PK_WITHOUT_ZONE, PartitionSpec.unpartitioned(),
            TestTableHelper.PkSpec.pk_include_pt
        ),
        Arguments.of(
            INTERNAL_CATALOG, IcebergSchema.PK_WITHOUT_ZONE, PtSpec.hive_pt_spec,
            TestTableHelper.PkSpec.pk_include_pt
        ),
        Arguments.of(
            INTERNAL_CATALOG, IcebergSchema.NO_PK_WITHOUT_ZONE, PtSpec.hive_pt_spec,
            PrimaryKeySpec.noPrimaryKey()
        ),
        Arguments.of(
            INTERNAL_CATALOG, IcebergSchema.PK_WITHOUT_ZONE, PartitionSpec.unpartitioned(),
            TestTableHelper.PkSpec.pk_include_pt
        )
    );
  }

  @DisplayName("Test SQL: CREATE TABLE LIKE data-lake table")
  @ParameterizedTest
  @MethodSource
  public void testCreateTableLikeDataLakeTable(
      String catalog, Schema schema, PartitionSpec partitionSpec, PrimaryKeySpec primaryKeySpec
  ) {
    test().inSparkCatalog(catalog)
        .cleanSourceTable()
        .execute(context -> {
          ArcticTable expect = context.arcticCatalog.newTableBuilder(context.sourceTableIdentifier, schema)
              .withPartitionSpec(partitionSpec)
              .withPrimaryKeySpec(primaryKeySpec)
              .withProperties(CollectionHelper.asMap("k1", "v1"))
              .create();

          sql("SET `" + SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES + "`="
              + true);
          String sqlText = "CREATE TABLE " + context.databaseAndTable +
              " LIKE " + context.sourceDatabaseAndTable + " USING arctic";
          sql(sqlText);

          ArcticTable table = context.loadTable();
          Asserts.assertType(expect.schema().asStruct(), table.schema().asStruct());
          Asserts.assertPartition(expect.spec(), table.spec());
          Assertions.assertEquals(expect.isKeyedTable(), table.isKeyedTable());
          Assertions.assertFalse(table.properties().containsKey("k1"));
          if (expect.isKeyedTable()) {
            Asserts.assertPrimaryKey(expect.asKeyedTable().primaryKeySpec(), table.asKeyedTable().primaryKeySpec());
          }
        });
  }

  public static Stream<Arguments> testCreateTableLikeTimestampZone() {
    return Stream.of(
        Arguments.of(
            INTERNAL_CATALOG, IcebergSchema.PK_WITHOUT_ZONE, false,
            Types.TimestampType.withZone()
        ),
        Arguments.of(
            INTERNAL_CATALOG, IcebergSchema.PK_WITHOUT_ZONE, true,
            Types.TimestampType.withoutZone()
        )
    );
  }

  @DisplayName("TestSQL: CREATE TABLE LIKE handle timestamp type in new table.")
  @ParameterizedTest
  @MethodSource
  public void testCreateTableLikeTimestampZone(
      String catalog, Schema schema, boolean newTableTimestampWithoutZone,
      Type expectTimestampType
  ) {
    test().inSparkCatalog(catalog)
        .cleanSourceTable()
        .execute(context -> {
          ArcticTable expect = context.arcticCatalog.newTableBuilder(context.sourceTableIdentifier, schema)
              .withProperties(CollectionHelper.asMap("k1", "v1"))
              .create();

          sql("SET `" + SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES + "`="
              + newTableTimestampWithoutZone);
          sql("CREATE TABLE " + context.databaseAndTable + " LIKE " +
              context.sourceDatabaseAndTable + " USING arctic");

          ArcticTable table = context.loadTable();
          Types.NestedField tsField = table.schema().findField("ts");
          Asserts.assertType(expectTimestampType, tsField.type());
        });
  }

  public static Stream<Arguments> testCreateTableWithoutProviderInSessionCatalog() {
    return Stream.of(
        Arguments.of(
            "", false
        ),
        Arguments.of(
            "USING arctic", true
        )
    );
  }

  @DisplayName("TestSQL: CREATE TABLE LIKE without USING ARCTIC")
  @ParameterizedTest(name = "{index} provider = {0} ")
  @MethodSource
  public void testCreateTableWithoutProviderInSessionCatalog(
      String provider, boolean expectCreate
  ) {
    Table hiveTable = hiveTable(hiveSchema, hivePartition).build();
    test().inSparkCatalog(SESSION_CATALOG)
        .withHiveTable(hiveTable)
        .execute(context -> {
          sql("CREATE TABLE " + context.databaseAndTable + " LIKE " +
              context.sourceDatabaseAndTable + " " + provider);
          Assertions.assertEquals(expectCreate, context.tableExists());
          sql("DROP TABLE IF EXISTS " + context.databaseAndTable);
        });
  }
}
