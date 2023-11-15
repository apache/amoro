package com.netease.arctic.spark.test.suites.catalog;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.spark.test.MixedTableTestBase;
import com.netease.arctic.spark.test.extensions.EnableCatalogSelect;
import com.netease.arctic.table.ArcticTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@Disabled
@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true)
public class TestMixedCatalog extends MixedTableTestBase {

  public static Stream<Arguments> testTableFormats() {
    return Stream.of(Arguments.of(MIXED_HIVE), Arguments.of(MIXED_ICEBERG), Arguments.of(ICEBERG));
  }

  @ParameterizedTest
  @MethodSource
  public void testTableFormats(TableFormat format) {
    String sqlText =
        "CREATE TABLE "
            + target()
            + " ( "
            + "id int, "
            + "data string, "
            + "pt string"
            + ") USING "
            + provider(format)
            + " PARTITIONED BY (pt) ";

    sql(sqlText);
    tableExists();
    ArcticTable table = loadTable();
    Assertions.assertEquals(format, table.format());

    sqlText =
        "INSERT INTO "
            + target()
            + " VALUES "
            + "(1, 'a', '2020-01-01'), (2, 'b', '2020-01-02'), (3, 'c', '2020-01-03')";
    sql(sqlText);

    sqlText = "SELECT * FROM " + target();
    long count = sql(sqlText).count();
    Assertions.assertEquals(3, count);
  }
}
