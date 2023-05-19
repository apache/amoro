package com.netease.arctic.spark.test.suites.sql;


import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.spark.test.SparkTableTestBase;
import com.netease.arctic.spark.test.extensions.EnableCatalogSelect;
import com.netease.arctic.spark.test.helper.DataComparator;
import com.netease.arctic.spark.test.helper.RecordGenerator;
import com.netease.arctic.spark.test.helper.TestTable;
import com.netease.arctic.spark.test.helper.TestTableHelper;
import com.netease.arctic.spark.test.helper.TestTables;
import com.netease.arctic.table.KeyedTable;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
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
public class TestSelectSQL extends SparkTableTestBase {

  public static Stream<Arguments> testKeyedTableQuery() {
    List<TestTable> tests = Lists.newArrayList(
        TestTables.MixedIceberg.PK_PT,
        TestTables.MixedIceberg.PK_NoPT,

        TestTables.MixedHive.PK_PT,
        TestTables.MixedHive.PK_NoPT
    );
    return tests.stream().map(t -> Arguments.of(t.format, t));
  }

  @ParameterizedTest
  @MethodSource
  public void testKeyedTableQuery(
      TableFormat format, TestTable table
  ) {
    createTarget(table.schema, builder ->
        builder.withPrimaryKeySpec(table.keySpec));

    KeyedTable tbl = loadTable().asKeyedTable();
    RecordGenerator dataGen = table.newDateGen();

    List<Record> base = dataGen.records(10);
    TestTableHelper.writeToBase(tbl, base);
    LinkedList<Record> expects = Lists.newLinkedList(base);


    // insert some record in change
    List<Record> changeInsert = dataGen.records(5);


    // insert some delete in change(delete base records)
    List<Record> changeDelete = Lists.newArrayList();
    IntStream.range(0, 3).boxed()
        .forEach(i -> changeDelete.add(expects.pollFirst()));

    // insert some delete in change(delete change records)
    expects.addAll(changeInsert);

    IntStream.range(0, 2).boxed()
        .forEach(i -> changeDelete.add(expects.pollLast()));

    // insert some delete in change(delete non exists records)
    changeDelete.addAll(dataGen.records(3));

    TestTableHelper.writeToChange(tbl.asKeyedTable(), changeInsert, ChangeAction.INSERT);
    TestTableHelper.writeToChange(tbl.asKeyedTable(), changeDelete, ChangeAction.DELETE);
    // reload table;
    LinkedList<Record> expectChange = Lists.newLinkedList(changeInsert);
    expectChange.addAll(changeDelete);


    //Assert MOR
    Dataset<Row> ds = sql("SELECT * FROM " + target() + " ORDER BY id");
    List<Record> actual = ds.collectAsList().stream()
        .map(r -> TestTableHelper.rowToRecord(r, table.schema.asStruct()))
        .collect(Collectors.toList());
    expects.sort(Comparator.comparing(r -> r.get(0, Integer.class)));

    DataComparator.build(expects, actual).assertRecordsEqual();

    ds = sql("SELECT * FROM " + target() + ".change" + " ORDER BY ID");
    List<Row> changeActual = ds.collectAsList();
    Assertions.assertEquals(expectChange.size(), changeActual.size());

  }

}
