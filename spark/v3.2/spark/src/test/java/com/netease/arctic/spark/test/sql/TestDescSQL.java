package com.netease.arctic.spark.test.sql;

import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.spark.test.SparkTableTestBase;
import com.netease.arctic.spark.test.extensions.EnableCatalogSelect;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true)
public class TestDescSQL extends SparkTableTestBase {

  public static Stream<Arguments> testDescTable() {
    return Stream.of(
        Arguments.of(TableFormat.MIXED_HIVE, ", PRIMARY KEY(id)", ""),
        Arguments.of(TableFormat.MIXED_HIVE, "", ""),
        Arguments.of(TableFormat.MIXED_ICEBERG, ", PRIMARY KEY(id)", ""),
        Arguments.of(TableFormat.MIXED_ICEBERG, "", ""),
        Arguments.of(TableFormat.MIXED_HIVE, ", PRIMARY KEY(id)", " PARTITIONED BY (day)"),
        Arguments.of(TableFormat.MIXED_HIVE, "", " PARTITIONED BY (day)"),
        Arguments.of(TableFormat.MIXED_ICEBERG, ", PRIMARY KEY(id)", " PARTITIONED BY (day)"),
        Arguments.of(TableFormat.MIXED_ICEBERG, "", " PARTITIONED BY (day)")
    );
  }

  @DisplayName("Test `test describe table`")
  @ParameterizedTest
  @MethodSource
  public void testDescTable(TableFormat format, String primaryKeyDDL, String partitionDDL) {
    String sqlText = "CREATE TABLE " + target() + " ( \n" +
        "id int, data string, day string " + primaryKeyDDL + " ) using " +
        provider(format)  + partitionDDL;
    sql(sqlText);
    List<Row> rows = sql("desc " + target().database + "." + target().table).collectAsList();
    List<String> primaryKeys = new ArrayList<>();
    List<String> partitions = new ArrayList<>();
    if (!primaryKeyDDL.isEmpty()) {
      primaryKeys.add("id");
    }
    if (!partitionDDL.isEmpty()) {
      partitions.add("day");
    }
    assertTableDesc(rows, primaryKeys, partitions);

    List<Row> rows2 = sql("desc extended " + target().database + "." + target().table).collectAsList();
    assertTableDesc(rows2, primaryKeys, partitions);
  }
}
