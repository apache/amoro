package com.netease.arctic.spark.test.suites.sql;


import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.spark.test.SparkTableTestBase;
import com.netease.arctic.spark.test.extensions.EnableCatalogSelect;
import org.apache.iceberg.types.Types;
import org.apache.spark.sql.AnalysisException;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true)
public class TestAlterTableColumnSQL extends SparkTableTestBase {

  public static Stream<Arguments> testAddColumn() {
    return Stream.of(
        Arguments.of(TableFormat.MIXED_HIVE),
        Arguments.of(TableFormat.MIXED_ICEBERG)
    );
  }

  @DisplayName("Test `add column`")
  @ParameterizedTest
  @MethodSource()
  public void testAddColumn(TableFormat format) {
    String sqlText = "CREATE TABLE " + target() + " ( \n" +
        "id bigint, data string, ts string, PRIMARY KEY(id)) using " +
        provider(format)  + " PARTITIONED BY (ts)";
    sql(sqlText);
    sql("ALTER TABLE " +
            target().database + "." + target().table +
            " ADD COLUMN point struct<x: double NOT NULL, y: double NOT NULL>");

    Types.StructType expectedSchema = Types.StructType.of(
        Types.NestedField.required(1, "id", Types.LongType.get()),
        Types.NestedField.optional(2, "data", Types.StringType.get()),
        Types.NestedField.optional(3, "ts", Types.StringType.get()),
        Types.NestedField.optional(4, "point", Types.StructType.of(
            Types.NestedField.required(5, "x", Types.DoubleType.get()),
            Types.NestedField.required(6, "y", Types.DoubleType.get())
        )));
    if (format == TableFormat.MIXED_HIVE) {
      expectedSchema = Types.StructType.of(
          Types.NestedField.required(1, "id", Types.LongType.get()),
          Types.NestedField.optional(2, "data", Types.StringType.get()),
          Types.NestedField.optional(4, "point", Types.StructType.of(
              Types.NestedField.required(5, "x", Types.DoubleType.get()),
              Types.NestedField.required(6, "y", Types.DoubleType.get())
          )),
          Types.NestedField.optional(3, "ts", Types.StringType.get()));
    }

    Assertions.assertEquals(expectedSchema, loadTable().schema().asStruct(), "Schema should match expected");
  }

  public static Stream<Arguments> testDropColumn() {
    return Stream.of(
        Arguments.of(TableFormat.MIXED_ICEBERG, " DROP NOT NULL"),
        Arguments.of(TableFormat.MIXED_ICEBERG)
    );
  }

  @DisplayName("Test `drop column`")
  @ParameterizedTest
  @MethodSource()
  public void testDropColumn(TableFormat format) {
    // TODO: Test coverage is not enough.
    // 1. format, keyed/unkeyed, partitioned/un-partitioned , exception tests.

    String sqlText = "CREATE TABLE " + target() + " ( \n" +
        "id bigint, data string, ts string, PRIMARY KEY(id)) using " +
        provider(format)  + " PARTITIONED BY (ts)";
    sql(sqlText);
    sql("ALTER TABLE " +
        target().database + "." + target().table +
        " DROP COLUMN data");

    Types.StructType expectedSchema = Types.StructType.of(
        Types.NestedField.required(1, "id", Types.LongType.get()),
        Types.NestedField.optional(3, "ts", Types.StringType.get()));

    Assertions.assertEquals(expectedSchema, loadTable().schema().asStruct(), "Schema should match expected");
  }

  /**
   * TODO: are arguments could be simplify?
   * TODO:  Test coverage is not enough.
   */
  public static Stream<Arguments> testAlterColumn() {
    return Stream.of(
        Arguments.of(" id COMMENT 'Record id'",
            Types.StructType.of(
            Types.NestedField.required(1, "id", Types.LongType.get(), "Record id"),
                Types.NestedField.optional(2, "data", Types.StringType.get()),
                Types.NestedField.optional(3, "ts", Types.TimestampType.withZone()),
                Types.NestedField.optional(4, "count", Types.IntegerType.get()))),
        Arguments.of(" count TYPE bigint",
            Types.StructType.of(
                Types.NestedField.required(1, "id", Types.LongType.get()),
                Types.NestedField.optional(2, "data", Types.StringType.get()),
                Types.NestedField.optional(3, "ts", Types.TimestampType.withZone()),
                Types.NestedField.optional(4, "count", Types.LongType.get()))),
        Arguments.of(" data DROP NOT NULL",
            Types.StructType.of(
                Types.NestedField.required(1, "id", Types.LongType.get()),
                Types.NestedField.optional(2, "data", Types.StringType.get()),
                Types.NestedField.optional(3, "ts", Types.TimestampType.withZone()),
                Types.NestedField.optional(4, "count", Types.IntegerType.get()))),
        Arguments.of(" data SET NOT NULL", null),
        Arguments.of(" count AFTER id",
            Types.StructType.of(
                Types.NestedField.required(1, "id", Types.LongType.get()),
                Types.NestedField.optional(4, "count", Types.IntegerType.get()),
                Types.NestedField.optional(2, "data", Types.StringType.get()),
                Types.NestedField.optional(3, "ts", Types.TimestampType.withZone())))
    );
  }
  @DisplayName("Test `alter column`")
  @ParameterizedTest
  @MethodSource()
  @EnableCatalogSelect.SelectCatalog(use = INTERNAL_CATALOG)
  public void testAlterColumn(String alterText, Types.StructType expectedSchema) {
    // TODO: Why doesn't run test on mixed-hive ?
    String sqlText = "CREATE TABLE " + target() + " ( \n" +
        "id bigint, data string, ts timestamp, count int, PRIMARY KEY(id)) using " +
        provider(TableFormat.MIXED_ICEBERG)  + " PARTITIONED BY (ts)";
    sql(sqlText);
    if (expectedSchema != null) {
      sql("ALTER TABLE " +
          target().database + "." + target().table +
          " ALTER COLUMN " + alterText);
      Assertions.assertEquals(expectedSchema, loadTable().schema().asStruct(), "Schema should match expected");
    } else {
      Assert.assertThrows(
          AnalysisException.class,
          () -> sql("ALTER TABLE " +
              target().database + "." + target().table +
              " ALTER COLUMN " + alterText));
    }
  }

  public static Stream<Arguments> testAlterTableProperties() {
    return Stream.of(
        Arguments.of(TableFormat.MIXED_ICEBERG, " SET TBLPROPERTIES ('test.props' = 'val')", "val"),
        Arguments.of(TableFormat.MIXED_HIVE, " SET TBLPROPERTIES ('test.props' = 'val')", "val"),
        Arguments.of(TableFormat.MIXED_ICEBERG, " UNSET TBLPROPERTIES ('test.props')", null),
        Arguments.of(TableFormat.MIXED_HIVE, " UNSET TBLPROPERTIES ('test.props')", null)
    );
  }


  @DisplayName("Test `alter table properties`")
  @ParameterizedTest
  @MethodSource()
  public void testAlterTableProperties(TableFormat format, String alterText, String expectedProperties) {
    String sqlText = "CREATE TABLE " + target() + " ( \n" +
        "id bigint, data string, ts string, PRIMARY KEY(id)) using " +
        provider(format)  + " PARTITIONED BY (ts)";
    sql(sqlText);
    sql("ALTER TABLE " +
        target().database + "." + target().table + alterText);
    Assertions.assertEquals(expectedProperties, loadTable().properties().get("test.props"));
  }

  // TODO: lack unset properties tests.
  // TODO: or move the alter table ... properties to the other test class?
}
