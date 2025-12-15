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
import org.apache.amoro.spark.test.utils.ExpectResultUtil;
import org.apache.amoro.spark.test.utils.RecordGenerator;
import org.apache.amoro.spark.test.utils.TestTableUtil;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true)
public class TestMergeIntoSQL extends MixedTableTestBase {

  private static final Schema schema =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.required(2, "data", Types.StringType.get()),
          Types.NestedField.required(3, "fdata", Types.FloatType.get()),
          Types.NestedField.required(4, "ddata", Types.DoubleType.get()),
          Types.NestedField.required(5, "pt", Types.StringType.get()));

  private static final PrimaryKeySpec pk =
      PrimaryKeySpec.builderFor(schema).addColumn("id").build();

  private static final List<Record> base =
      Lists.newArrayList(
          RecordGenerator.newRecord(schema, 1, "a", 1.1f, 1.1D, "001"),
          RecordGenerator.newRecord(schema, 2, "b", 1.1f, 1.1D, "002"));
  private static final List<Record> change =
      Lists.newArrayList(
          RecordGenerator.newRecord(schema, 3, "c", 1.1f, 1.1D, "001"),
          RecordGenerator.newRecord(schema, 4, "d", 1.1f, 1.1D, "002"));

  private static final List<Record> source =
      Lists.newArrayList(
          RecordGenerator.newRecord(schema, 1, "s1", 1.1f, 1.1D, "001"),
          RecordGenerator.newRecord(schema, 2, "s2", 1.1f, 1.1D, "002"),
          RecordGenerator.newRecord(schema, 5, "s5", 1.1f, 1.1D, "001"),
          RecordGenerator.newRecord(schema, 6, "s6", 1.1f, 1.1D, "003"));

  private final List<Record> target = Lists.newArrayList();

  public void setupTest(PrimaryKeySpec keySpec) {
    MixedTable table = createTarget(schema, builder -> builder.withPrimaryKeySpec(keySpec));
    target.addAll(base);
    target.addAll(change);

    if (table.isKeyedTable()) {
      TestTableUtil.writeToBase(table, base);
      TestTableUtil.writeToChange(table.asKeyedTable(), change, ChangeAction.INSERT);
    } else {
      TestTableUtil.writeToBase(table, target);
    }
  }

  public static Stream<Arguments> args() {
    return Stream.of(
        Arguments.arguments(MIXED_ICEBERG, pk),
        Arguments.arguments(MIXED_ICEBERG, NO_PRIMARY_KEY),
        Arguments.arguments(MIXED_HIVE, pk),
        Arguments.arguments(MIXED_HIVE, NO_PRIMARY_KEY));
  }

  @DisplayName("SQL: MERGE INTO for all actions with condition")
  @ParameterizedTest
  @MethodSource("args")
  public void testAllAction(TableFormat format, PrimaryKeySpec keySpec) {
    setupTest(keySpec);
    createViewSource(schema, source);

    sql(
        "MERGE INTO "
            + target()
            + " AS t USING "
            + source()
            + " AS s ON t.id == s.id "
            + "WHEN MATCHED AND t.id = 1 THEN DELETE "
            + "WHEN MATCHED AND t.id = 2 THEN UPDATE SET * "
            + "WHEN NOT MATCHED AND s.id != 5 THEN INSERT (t.data, t.pt, t.id, t.fdata,t.ddata) values ( s.data, s.pt, 1000,1.1,1.1)");

    List<Record> expects =
        ExpectResultUtil.expectMergeResult(target, source, r -> r.getField("id"))
            .whenMatched((t, s) -> t.getField("id").equals(1), (t, s) -> null)
            .whenMatched((t, s) -> t.getField("id").equals(2), (t, s) -> s)
            .whenNotMatched(
                s -> !s.getField("id").equals(5),
                s -> {
                  s.setField("id", 1000);
                  s.setField("fdata", 1.1f);
                  s.setField("ddata", 1.1D);
                  return s;
                })
            .results();

    MixedTable table = loadTable();
    List<Record> actual = TestTableUtil.tableRecords(table);
    DataComparator.build(expects, actual).ignoreOrder("id").assertRecordsEqual();
  }

  @DisplayName("SQL: MERGE INTO for all actions with condition")
  @ParameterizedTest
  @MethodSource("args")
  public void testSetExactValue(TableFormat format, PrimaryKeySpec keySpec) {
    setupTest(keySpec);
    createViewSource(schema, source);

    sql(
        "MERGE INTO "
            + target()
            + " AS t USING "
            + source()
            + " AS s ON t.id == s.id "
            + "WHEN MATCHED AND t.id = 2 THEN UPDATE SET t.data = 'ccc', t.fdata = 1.1, t.ddata = 1.1");

    List<Record> expects =
        ExpectResultUtil.expectMergeResult(target, source, r -> r.getField("id"))
            .whenMatched(
                (t, s) -> t.getField("id").equals(2),
                (t, s) -> {
                  t.setField("data", "ccc");
                  return t;
                })
            .results();

    MixedTable table = loadTable();
    List<Record> actual = TestTableUtil.tableRecords(table);
    DataComparator.build(expects, actual).ignoreOrder("id").assertRecordsEqual();
  }

  @DisplayName("SQL: MERGE INTO for all actions with target no data")
  @ParameterizedTest
  @MethodSource("args")
  public void testEmptyTarget(TableFormat format, PrimaryKeySpec keySpec) {
    MixedTable table = createTarget(schema, builder -> builder.withPrimaryKeySpec(keySpec));
    createViewSource(schema, source);

    sql(
        "MERGE INTO "
            + target()
            + " AS t USING "
            + source()
            + " AS s ON t.id == s.id "
            + "WHEN MATCHED AND t.id = 1 THEN DELETE "
            + "WHEN MATCHED AND t.id = 2 THEN UPDATE SET * "
            + "WHEN NOT MATCHED THEN INSERT *");

    table.refresh();
    List<Record> expects = Lists.newArrayList(source);
    List<Record> actual = TestTableUtil.tableRecords(table);
    DataComparator.build(expects, actual).ignoreOrder("id").assertRecordsEqual();
  }

  @DisplayName("SQL: MERGE INTO for all actions without condition")
  @ParameterizedTest
  @MethodSource("args")
  public void testActionWithoutCondition(TableFormat format, PrimaryKeySpec keySpec) {
    setupTest(keySpec);
    createViewSource(schema, source);

    sql(
        "MERGE INTO "
            + target()
            + " AS t USING "
            + source()
            + " AS s ON t.id == s.id "
            + "WHEN MATCHED THEN UPDATE SET * "
            + "WHEN NOT MATCHED THEN INSERT *");

    List<Record> expects =
        ExpectResultUtil.expectMergeResult(target, source, r -> r.getField("id"))
            .whenMatched((t, s) -> true, (t, s) -> s)
            .whenNotMatched(s -> true, Function.identity())
            .results();

    MixedTable table = loadTable();
    List<Record> actual = TestTableUtil.tableRecords(table);
    DataComparator.build(expects, actual).ignoreOrder("id").assertRecordsEqual();
  }

  @DisplayName("SQL: MERGE INTO for only delete actions")
  @ParameterizedTest
  @MethodSource("args")
  public void testOnlyDeletes(TableFormat format, PrimaryKeySpec keySpec) {
    setupTest(keySpec);
    createViewSource(schema, source);
    sql(
        "MERGE INTO "
            + target()
            + " AS t USING "
            + source()
            + " AS s ON t.id == s.id "
            + "WHEN MATCHED THEN DELETE ");

    List<Record> expects =
        ExpectResultUtil.expectMergeResult(target, source, r -> r.getField("id"))
            .whenMatched((t, s) -> true, (t, s) -> null)
            .results();

    MixedTable table = loadTable();
    List<Record> actual = TestTableUtil.tableRecords(table);
    DataComparator.build(expects, actual).ignoreOrder("id").assertRecordsEqual();
  }

  @DisplayName("SQL: MERGE INTO for explicit column ")
  @ParameterizedTest
  @MethodSource("args")
  public void testExplicitColumn(TableFormat format, PrimaryKeySpec keySpec) {
    setupTest(keySpec);
    createViewSource(schema, source);

    sql(
        "MERGE INTO "
            + target()
            + " AS t USING "
            + source()
            + " AS s ON t.id == s.id "
            + "WHEN MATCHED THEN UPDATE SET t.id = s.id, t.data = s.pt, t.pt = s.pt "
            + "WHEN NOT MATCHED THEN INSERT (t.data, t.pt, t.id, t.fdata,t.ddata) values ( s.pt, s.pt, s.id,s.fdata,s.ddata) ");

    Function<Record, Record> dataAsPt =
        s -> {
          Record r = s.copy();
          r.setField("data", s.getField("pt"));
          return r;
        };
    List<Record> expects =
        ExpectResultUtil.expectMergeResult(target, source, r -> r.getField("id"))
            .whenMatched((t, s) -> true, (t, s) -> dataAsPt.apply(s))
            .whenNotMatched(s -> true, dataAsPt)
            .results();

    MixedTable table = loadTable();
    List<Record> actual = TestTableUtil.tableRecords(table);
    DataComparator.build(expects, actual).ignoreOrder("id").assertRecordsEqual();
  }

  public static Stream<TableFormat> formatArgs() {
    return Stream.of(MIXED_HIVE, MIXED_ICEBERG);
  }

  @DisplayName("SQL: MERGE INTO failed if join on non primary key")
  @ParameterizedTest
  @MethodSource("formatArgs")
  public void testFailedForNonPrimaryKeyMerge(TableFormat format) {
    setupTest(pk);
    createViewSource(schema, source);

    boolean catched = false;
    try {
      sql(
          "MERGE INTO "
              + target()
              + " AS t USING "
              + source()
              + " AS s ON t.pt == s.id "
              + "WHEN MATCHED THEN UPDATE SET t.id = s.id, t.data = s.pt, t.pt = s.pt "
              + "WHEN NOT MATCHED THEN INSERT (t.data, t.pt, t.id,t.fdata,t.ddata) values ( s.pt, s.data, s.id,s.fdata,s.ddata) ");
    } catch (Exception e) {
      catched = true;
    }
    Assertions.assertTrue(catched);
  }

  @DisplayName("SQL: MERGE INTO failed if source has duplicate join key")
  @ParameterizedTest
  @MethodSource("formatArgs")
  public void testFailedWhenDuplicateJoinKey(TableFormat format) {
    setupTest(pk);
    List<Record> source =
        Lists.newArrayList(
            RecordGenerator.newRecord(schema, 1, "s1", 1.1f, 2.2D, "001"),
            RecordGenerator.newRecord(schema, 1, "s2", 1.1f, 2.2D, "001"));
    createViewSource(schema, source);

    boolean catched = false;
    try {
      sql(
          "MERGE INTO "
              + target()
              + " AS t USING "
              + source()
              + " AS s ON t.id == s.id "
              + "WHEN MATCHED THEN UPDATE SET t.id = s.id, t.data = s.pt, t.pt = s.pt "
              + "WHEN NOT MATCHED THEN INSERT (t.data, t.pt, t.id, t.fdata, t.ddata) values ( s.data, s.pt, s.id, s.fdata, s.ddata) ");
    } catch (Exception e) {
      catched = true;
    }
    Assertions.assertTrue(catched);
  }
}
