package com.netease.arctic.spark.test.suites.sql;

import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.spark.test.SparkTableTestBase;
import com.netease.arctic.spark.test.extensions.EnableCatalogSelect;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

// TODO: @jinsilei
@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true)
public class TestDropPartitionSQL extends SparkTableTestBase {

  public static Stream<Arguments> testDropPartition() {
    return Stream.of(
        Arguments.of(TableFormat.MIXED_HIVE, ", PRIMARY KEY(id)"),
        Arguments.of(TableFormat.MIXED_HIVE, ""),
        Arguments.of(TableFormat.MIXED_ICEBERG, ", PRIMARY KEY(id)"),
        Arguments.of(TableFormat.MIXED_ICEBERG, "")
    );
  }

  @DisplayName("Test `test drop partiton`")
  @ParameterizedTest
  @MethodSource
  public void testDropPartition(TableFormat format, String primaryKeyDDL) {
    String sqlText = "CREATE TABLE " + target() + " ( \n" +
        "id int, data string, day string " + primaryKeyDDL + " ) using " +
        provider(format)  + " PARTITIONED BY (day)";
    sql(sqlText);
    sql("insert into " +
        target().database + "." + target().table +
        " values (1, 'a', 'a'), (2, 'b', 'b'), (3, 'c', 'c')");
    sql("describe " + target().database + "." + target().table);
    sql("alter table " + target().database + "." + target().table + " drop if exists partition (day='c')");
    Dataset<Row> sql = sql("select * from " +
        target().database + "." + target().table);
    Assertions.assertEquals(2, sql.collectAsList().size());
  }
}
