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

package org.apache.amoro.spark.test.suites.catalog;

import org.apache.amoro.TableFormat;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.spark.table.MixedSparkTable;
import org.apache.amoro.spark.test.MixedTableTestBase;
import org.apache.amoro.spark.test.SparkTestContext;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.apache.spark.sql.catalyst.analysis.NoSuchTableException;
import org.apache.spark.sql.connector.catalog.Table;
import org.apache.spark.sql.connector.catalog.TableCatalog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TestSessionCatalog extends MixedTableTestBase {

  public static final Schema SCHEMA =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.required(2, "data", Types.StringType.get()),
          Types.NestedField.required(3, "pt", Types.StringType.get()));
  public static final PrimaryKeySpec PK_SPEC =
      PrimaryKeySpec.builderFor(SCHEMA).addColumn("id").build();
  public static final PartitionSpec PT_SPEC =
      PartitionSpec.builderFor(SCHEMA).identity("pt").build();

  @Override
  protected Map<String, String> sparkSessionConfig() {
    return ImmutableMap.of(
        "spark.sql.catalog.spark_catalog",
        SparkTestContext.SESSION_CATALOG_IMPL,
        "spark.sql.catalog.spark_catalog.url",
        CONTEXT.amsCatalogUrl(TableFormat.MIXED_ICEBERG));
  }

  @Test
  public void testLoadTables() throws NoSuchTableException {
    createTarget(SCHEMA, builder -> builder.withPrimaryKeySpec(PK_SPEC).withPartitionSpec(PT_SPEC));

    TableCatalog sessionCatalog =
        (TableCatalog) spark().sessionState().catalogManager().catalog(SPARK_SESSION_CATALOG);

    Table table = sessionCatalog.loadTable(target().toSparkIdentifier());
    Assertions.assertTrue(table instanceof MixedSparkTable);
  }
}
