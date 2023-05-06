package com.netease.arctic.spark.test.suites.sql;

import com.netease.arctic.spark.test.SparkTableTestBase;
import com.netease.arctic.spark.test.extensions.EnableCatalogSelect;
import com.netease.arctic.spark.test.helper.TestTable;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true)
public class TestArcticCommandSQL extends SparkTableTestBase {


  public static Stream<Arguments> testMigrate() {
    Types.NestedField[] fields = {
        Types.NestedField.required(1, "id", Types.IntegerType.get()),
        Types.NestedField.required(2, "data", Types.StringType.get()),
        Types.NestedField.required(3, "pt", Types.StringType.get())
    };

    return Stream.of(
        Arguments.arguments(fields, new String[0]),
        Arguments.arguments(fields, new String[]{"pt"})
    );
  }

  @EnableCatalogSelect.SelectCatalog(use = SESSION_CATALOG)
  @ParameterizedTest
  @MethodSource
  public void testMigrate(Types.NestedField[] fields, String[] pt) {

    TestTable source = TestTable.format(MIXED_HIVE, fields).pt(pt).build();
    createHiveSource(source.hiveSchema, source.hivePartitions);
    sql("insert overwrite " + source() + " values " +
        " ( 1, 'aaa', '0001' ),  " +
        " ( 2, 'bbb', '0002' ) ");

    sql("migrate " + source() + " to arctic " + target());
  }
}
