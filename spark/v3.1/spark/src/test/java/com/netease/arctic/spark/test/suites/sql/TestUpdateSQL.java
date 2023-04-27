package com.netease.arctic.spark.test.suites.sql;

import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.spark.test.SparkTableTestBase;
import com.netease.arctic.spark.test.extensions.EnableCatalogSelect;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

// TODO: @jinsilei
@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true)
public class TestUpdateSQL extends SparkTableTestBase {

  public static Stream<Arguments> testUpdate() {
    return Stream.of(
        Arguments.of(TableFormat.MIXED_HIVE, ", PRIMARY KEY(id)", " where id = 3"),
        Arguments.of(TableFormat.MIXED_HIVE, ", PRIMARY KEY(id)", " where day = 'c'"),
        Arguments.of(TableFormat.MIXED_HIVE, ", PRIMARY KEY(id)", ""),
        Arguments.of(TableFormat.MIXED_HIVE, "", " where id = 3"),
        Arguments.of(TableFormat.MIXED_HIVE, "", ""),
        Arguments.of(TableFormat.MIXED_ICEBERG, ", PRIMARY KEY(id)", " where id = 3"),
        Arguments.of(TableFormat.MIXED_ICEBERG, ", PRIMARY KEY(id)", " where day = 'c'"),
        Arguments.of(TableFormat.MIXED_ICEBERG, ", PRIMARY KEY(id)", ""),
        Arguments.of(TableFormat.MIXED_ICEBERG, "", " where id = 3"),
        Arguments.of(TableFormat.MIXED_ICEBERG, "", "")
    );
  }

  @DisplayName("Test `test update table`")
  @ParameterizedTest
  @MethodSource
  public void testUpdate(TableFormat format, String primaryKeyDDL, String filter) {
    String sqlText = "CREATE TABLE " + target() + " ( \n" +
        "id int, data string, day string " + primaryKeyDDL + " ) using " +
        provider(format)  + " PARTITIONED BY (day)";
    sql(sqlText);
    sql("insert into " +
        target().database + "." + target().table +
        " values (1, 'a', 'a'), (2, 'b', 'b'), (3, 'c', 'c')");

    sql("update " + target().database + "." + target().table + " set data = 'd'" + filter);

    Dataset<Row> sql = sql("select id, data from " +
        target().database + "." + target().table + filter);

    if (filter.isEmpty()) {
      Assertions.assertEquals(3, sql.collectAsList().size());
      Assertions.assertEquals("d", sql.collectAsList().get(0).get(1));
    } else {
      Assertions.assertEquals(1, sql.collectAsList().size());
      Assertions.assertEquals("d", sql.collectAsList().get(0).get(1));
    }
  }

  public static Stream<Arguments> testUpdatePartitionField() {
    return Stream.of(
        Arguments.of(TableFormat.MIXED_HIVE, ", PRIMARY KEY(id)", " where id = 3"),
        Arguments.of(TableFormat.MIXED_HIVE, "", " where id = 3"),
        Arguments.of(TableFormat.MIXED_ICEBERG, ", PRIMARY KEY(id)", " where id = 3"),
        Arguments.of(TableFormat.MIXED_ICEBERG, "", " where id = 3")
    );
  }

  @DisplayName("Test `test update partition field`")
  @ParameterizedTest
  @MethodSource
  public void testUpdatePartitionField(TableFormat format, String primaryKeyDDL, String filter) {
    String sqlText = "CREATE TABLE " + target() + " ( \n" +
        "id int, data string, day string " + primaryKeyDDL + " ) using " +
        provider(format)  + " PARTITIONED BY (day)";
    sql(sqlText);
    sql("insert into " +
        target().database + "." + target().table +
        " values (1, 'a', 'a'), (2, 'b', 'b'), (3, 'c', 'c')");

    sql("update " + target().database + "." + target().table + " set day = 'd'" + filter);

    Dataset<Row> sql = sql("select id, day from " +
        target().database + "." + target().table + filter);

    Assertions.assertEquals(1, sql.collectAsList().size());
    Assertions.assertEquals("d", sql.collectAsList().get(0).get(1));
  }

  public static Stream<Arguments> testUpdatePrimaryField() {
    return Stream.of(
        Arguments.of(TableFormat.MIXED_HIVE, ", PRIMARY KEY(id)", " where data = 'c'"),
        Arguments.of(TableFormat.MIXED_ICEBERG, ", PRIMARY KEY(id)", " where data = 'c'")
    );
  }

  @DisplayName("Test `test update primary field`")
  @ParameterizedTest
  @MethodSource
  public void testUpdatePrimaryField(TableFormat format, String primaryKeyDDL, String filter) {
    String sqlText = "CREATE TABLE " + target() + " ( \n" +
        "id int, data string, day string " + primaryKeyDDL + " ) using " +
        provider(format)  + " PARTITIONED BY (day)";
    sql(sqlText);
    sql("insert into " +
        target().database + "." + target().table +
        " values (1, 'a', 'a'), (2, 'b', 'b'), (3, 'c', 'c')");

    Assert.assertThrows(UnsupportedOperationException.class, () ->
    sql("update " + target().database + "." + target().table + " set id = 1" + filter));

  }
}
