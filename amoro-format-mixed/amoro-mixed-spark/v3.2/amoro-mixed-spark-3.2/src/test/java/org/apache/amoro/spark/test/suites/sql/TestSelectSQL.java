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

package org.apache.amoro.spark.test.suites.sql;

import org.apache.amoro.TableFormat;
import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.spark.test.MixedTableTestBase;
import org.apache.amoro.spark.test.extensions.EnableCatalogSelect;
import org.apache.amoro.spark.test.utils.DataComparator;
import org.apache.amoro.spark.test.utils.RecordGenerator;
import org.apache.amoro.spark.test.utils.TestTable;
import org.apache.amoro.spark.test.utils.TestTableUtil;
import org.apache.amoro.spark.test.utils.TestTables;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MetadataColumns;
import org.apache.amoro.table.TableProperties;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true)
public class TestSelectSQL extends MixedTableTestBase {

  public static Stream<Arguments> testKeyedTableQuery() {
    List<TestTable> tests =
        Lists.newArrayList(
            TestTables.MixedIceberg.PK_PT,
            TestTables.MixedIceberg.PK_NO_PT,
            TestTables.MixedHive.PK_PT,
            TestTables.MixedHive.PK_NO_PT);
    return tests.stream()
        .map(t -> Arguments.of(t.format, t))
        .flatMap(
            e -> {
              List parquet = Lists.newArrayList(e.get());
              parquet.add(FileFormat.PARQUET);
              List orc = Lists.newArrayList(e.get());
              orc.add(FileFormat.ORC);
              return Stream.of(Arguments.of(parquet.toArray()), Arguments.of(orc.toArray()));
            });
  }

  @ParameterizedTest
  @MethodSource
  public void testKeyedTableQuery(TableFormat format, TestTable table, FileFormat fileFormat) {
    createTarget(
        table.schema,
        builder ->
            builder
                .withPrimaryKeySpec(table.keySpec)
                .withProperty(TableProperties.CHANGE_FILE_FORMAT, fileFormat.name())
                .withProperty(TableProperties.BASE_FILE_FORMAT, fileFormat.name()));

    KeyedTable tbl = loadTable().asKeyedTable();
    RecordGenerator dataGen = table.newDateGen();

    List<Record> base = dataGen.records(10);
    TestTableUtil.writeToBase(tbl, base);
    LinkedList<Record> expects = Lists.newLinkedList(base);

    // insert some record in change
    List<Record> changeInsert = dataGen.records(5);

    // insert some delete in change(delete base records)
    List<Record> changeDelete = Lists.newArrayList();
    IntStream.range(0, 3).boxed().forEach(i -> changeDelete.add(expects.pollFirst()));

    // insert some delete in change(delete change records)
    expects.addAll(changeInsert);

    IntStream.range(0, 2).boxed().forEach(i -> changeDelete.add(expects.pollLast()));

    // insert some delete in change(delete non exists records)
    changeDelete.addAll(dataGen.records(3));

    TestTableUtil.writeToChange(tbl.asKeyedTable(), changeInsert, ChangeAction.INSERT);
    TestTableUtil.writeToChange(tbl.asKeyedTable(), changeDelete, ChangeAction.DELETE);
    // reload table;
    LinkedList<Record> expectChange = Lists.newLinkedList(changeInsert);
    expectChange.addAll(changeDelete);

    // Assert MOR
    Dataset<Row> ds = sql("SELECT * FROM " + target() + " ORDER BY id");
    List<Record> actual =
        ds.collectAsList().stream()
            .map(r -> TestTableUtil.rowToRecord(r, table.schema.asStruct()))
            .collect(Collectors.toList());
    expects.sort(Comparator.comparing(r -> r.get(0, Integer.class)));

    DataComparator.build(expects, actual).assertRecordsEqual();

    ds = sql("SELECT * FROM " + target() + ".change" + " ORDER BY ID");
    List<Row> changeActual = ds.collectAsList();
    Assertions.assertEquals(expectChange.size(), changeActual.size());

    Schema changeSchema = MetadataColumns.appendChangeStoreMetadataColumns(table.schema);
    changeActual.stream()
        .map(r -> TestTableUtil.rowToRecord(r, changeSchema.asStruct()))
        .forEach(
            r -> {
              Assertions.assertNotNull(r.getField(MetadataColumns.CHANGE_ACTION_NAME));
              Assertions.assertTrue(
                  ((Long) r.getField(MetadataColumns.TRANSACTION_ID_FILED_NAME)) > 0);
            });
  }
}
