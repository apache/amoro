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

package com.netease.arctic.spark;

import com.clearspring.analytics.util.Lists;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.spark.util.RecordGenerator;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableIdentifier;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.iceberg.types.Types;
import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.glassfish.jersey.internal.guava.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TestUpsert extends SparkTestBase {
  private final String database = "db";
  private final String table = "sink_table";
  private final String sourceTable = "source_table";
  private final String initView = "init_view";
  private final TableIdentifier identifier = TableIdentifier.of(catalogNameArctic, database, table);

  private List<GenericRecord> sources;
  private List<GenericRecord> initialize;

  private Schema schema;
  private int newRecordSize = 300;
  private int upsertRecordSize = 200;

  @Before
  public void before() throws AnalysisException {
    sql("use " + catalogNameArctic);
    sql("create database if not exists {0}", database);

    schema = new Schema(
        Types.NestedField.of(1, false, "order_key", Types.IntegerType.get()),
        Types.NestedField.of(2, false, "line_number", Types.IntegerType.get()),
        Types.NestedField.of(3, false, "data", Types.StringType.get()),
        Types.NestedField.of(4, false, "pt", Types.StringType.get())
    );

    ArcticCatalog catalog = catalog(catalogNameArctic);
    PrimaryKeySpec primaryKeySpec = PrimaryKeySpec.builderFor(schema)
        .addColumn("order_key")
        .addColumn("line_number").build();

    PartitionSpec partitionSpec = PartitionSpec.builderFor(schema)
        .identity("pt").build();
    catalog.newTableBuilder(identifier, schema)
        .withPrimaryKeySpec(primaryKeySpec)
        .withPartitionSpec(partitionSpec)
        .withProperties(properties(
            "write.upsert.enabled", "true",
            "base.file-index.hash-bucket", "4"
        )).create();

    RecordGenerator generator = RecordGenerator.buildFor(schema)
        .withSequencePrimaryKey(primaryKeySpec)
        .withRandomDate("pt")
        .build();
    initialize = generator.records(1000);

    Dataset<Row> dataset = spark.createDataFrame(
        initialize.stream().map(SparkTestContext::recordToRow).collect(Collectors.toList()),
        SparkSchemaUtil.convert(schema));
    dataset.createTempView(initView);

    sources = upsertSource(initialize, generator,
        primaryKeySpec, partitionSpec, upsertRecordSize, newRecordSize);
    dataset = spark.createDataFrame(
        sources.stream().map(SparkTestContext::recordToRow).collect(Collectors.toList()),
        SparkSchemaUtil.convert(schema));
    dataset.createTempView(sourceTable);
  }

  @Test
  public void testKeyedTableUpsert() {
    sql("set `spark.sql.arctic.check-source-data-uniqueness.enabled`=`true`");
    sql("set `spark.sql.arctic.optimize-write-enabled`=`true`");

    sql("INSERT OVERWRITE " + database + "." + table + " SELECT * FROM " + initView);
    sql("insert into table " + database + '.' + table + " SELECT * FROM " + sourceTable);

    rows = sql("SELECT * FROM " + database + "." + table);
    Assert.assertEquals(initialize.size() + newRecordSize, rows.size());
  }

  private List<GenericRecord> upsertSource(
      List<GenericRecord> initializeData, RecordGenerator sourceGenerator,
      PrimaryKeySpec keySpec, PartitionSpec partitionSpec,
      int upsertCount, int insertCount) {
    RecordGenerator generator = RecordGenerator.buildFor(schema)
        .withSeed(System.currentTimeMillis())
        .build();

    List<GenericRecord> source = Lists.newArrayList();
    List<GenericRecord> insertSource = sourceGenerator.records(insertCount);
    source.addAll(insertSource);

    List<GenericRecord> upsertSource = sourceGenerator.records(upsertCount);
    Iterator<GenericRecord> it = initializeData.iterator();

    Set<String> sourceCols = Sets.newHashSet();
    sourceCols.addAll(keySpec.fieldNames());
    // partitionSpec.fields().stream()
    //     .map(f -> schema.findField(f.sourceId()))
    //     .map(Types.NestedField::name)
    //     .forEach(sourceCols::add);

    for (GenericRecord r : upsertSource) {
      GenericRecord t = it.next();
      sourceCols.forEach(col -> t.setField(col, r.getField(col)));
    }
    return source;
  }
}
