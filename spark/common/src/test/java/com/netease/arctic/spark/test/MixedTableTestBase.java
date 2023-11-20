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

package com.netease.arctic.spark.test;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Test base for all mixed-format tests.
 */
public class MixedTableTestBase extends SparkTestBase {
  public static final String MIXED_ICEBERG_CATALOG = SparkTestContext.SparkCatalogNames.MIXED_ICEBERG;

  protected static final TableFormat MIXED_HIVE = TableFormat.MIXED_HIVE;
  protected static final TableFormat MIXED_ICEBERG = TableFormat.MIXED_ICEBERG;
  protected static final TableFormat ICEBERG = TableFormat.ICEBERG;
  protected static final PartitionSpec unpartitioned = PartitionSpec.unpartitioned();
  protected static final PrimaryKeySpec noPrimaryKey = PrimaryKeySpec.noPrimaryKey();

  private ArcticCatalog mixedCatalog = null;
  private String currentSparkCatalog = null;

  protected ArcticCatalog catalog() {
    boolean reInitMixedCatalog = mixedCatalog == null || (!currentCatalog.equals(currentSparkCatalog));
    if (reInitMixedCatalog) {
      String amsCatalogName = sparkCatalogToAMSCatalog(currentCatalog);
      mixedCatalog = CatalogLoader.load(context.ams.getServerUrl() + "/" + amsCatalogName);
      this.currentSparkCatalog = currentCatalog;
    }
    return mixedCatalog;
  }

  public ArcticTable loadTable() {
    return catalog().loadTable(target().toArcticIdentifier());
  }

  public String provider(TableFormat format) {
    Preconditions.checkArgument(format == TableFormat.MIXED_HIVE || format == TableFormat.MIXED_ICEBERG);
    return "arctic";
  }


  public ArcticTable createArcticSource(Schema schema, Consumer<TableBuilder> consumer) {
    TestIdentifier identifier = TestIdentifier.ofDataLake(
        currentCatalog, catalog().name(), database(), sourceTable, true);
    TableBuilder builder = catalog().newTableBuilder(identifier.toArcticIdentifier(), schema);
    consumer.accept(builder);
    ArcticTable source = builder.create();
    this.source = identifier;
    return source;
  }

  public ArcticTable createTarget(Schema schema, Consumer<TableBuilder> consumer) {
    TestIdentifier identifier = target();
    TableBuilder builder = catalog().newTableBuilder(identifier.toArcticIdentifier(), schema);
    consumer.accept(builder);
    return builder.create();
  }

  protected boolean tableExists() {
    return catalog().tableExists(target().toArcticIdentifier());
  }

  @AfterEach
  public void cleanUpSource() {
    if (source == null) {
      return;
    }
    if (TestIdentifier.SOURCE_TYPE_ARCTIC.equalsIgnoreCase(source.sourceType)) {
      catalog().dropTable(source.toArcticIdentifier(), true);
    } else if (TestIdentifier.SOURCE_TYPE_HIVE.equalsIgnoreCase(source.sourceType)) {
      context.dropHiveTable(source.database, source.table);
    } else if (TestIdentifier.SOURCE_TYPE_VIEW.equalsIgnoreCase(source.sourceType)) {
      spark().sessionState().catalog().dropTempView(source.table);
    }
  }

  public void assertTableDesc(List<Row> rows, List<String> primaryKeys, List<String> partitionKey) {
    boolean partitionBlock = false;
    boolean primaryKeysBlock = false;
    List<String> descPartitionKey = Lists.newArrayList();
    List<String> descPrimaryKeys = Lists.newArrayList();
    List<Object[]> rs =
        rows.stream()
            .map(
                row ->
                    IntStream.range(0, row.size())
                        .mapToObj(pos -> row.isNullAt(pos) ? null : row.get(pos))
                        .toArray(Object[]::new))
            .collect(Collectors.toList());
    for (Object[] row : rs) {
      if (StringUtils.equalsIgnoreCase("# Partitioning", row[0].toString())) {
        partitionBlock = true;
      } else if (StringUtils.startsWith(row[0].toString(), "Part ") && partitionBlock) {
        descPartitionKey.add(row[1].toString());
      }
      if (StringUtils.equalsIgnoreCase("# Primary keys", row[0].toString())) {
        primaryKeysBlock = true;
      } else if (StringUtils.startsWith(row[0].toString(), "# ") && primaryKeysBlock) {
        primaryKeysBlock = false;
      } else if (primaryKeysBlock) {
        descPrimaryKeys.add(row[0].toString());
      }
    }
    Assertions.assertArrayEquals(
        partitionKey.stream().sorted().distinct().toArray(),
        descPartitionKey.stream().sorted().distinct().toArray());

    Assertions.assertEquals(primaryKeys.size(), descPrimaryKeys.size());
    Assertions.assertArrayEquals(
        primaryKeys.stream().sorted().distinct().toArray(),
        descPrimaryKeys.stream().sorted().distinct().toArray());
  }
}
