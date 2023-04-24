package com.netease.arctic.spark.test.junit5.suites;

import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.spark.SparkSQLProperties;
import com.netease.arctic.spark.test.Asserts;
import com.netease.arctic.spark.test.helper.TestSource;
import com.netease.arctic.spark.test.helper.TestTables;
import com.netease.arctic.spark.test.junit5.SparkTableTestBase;
import com.netease.arctic.spark.test.junit5.extensions.EnableCatalogSelect;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;


// 1. ts handle
// 2. primary key spec/table schema
// 3. data expect
// 4. duplicate check
@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true)
public class TestCreateTableAsSelect extends SparkTableTestBase {


  public static final Schema simpleSourceSchema = TestTables.MixedIceberg.NoPK_PT.schema;
  public static final List<GenericRecord> simpleSourceData = TestTables.MixedIceberg.PK_PT.generator.records(10);


  public static Stream<Arguments> testTimestampZoneHandle() {

    return Stream.of(
        Arguments.of(TableFormat.MIXED_ICEBERG, "PRIMARY KEY(id, pt)", true, Types.TimestampType.withoutZone()),
        Arguments.of(TableFormat.MIXED_ICEBERG, "", false, Types.TimestampType.withZone()),
        Arguments.of(TableFormat.MIXED_HIVE, "PRIMARY KEY(id, pt)", true, Types.TimestampType.withoutZone()),
        Arguments.of(TableFormat.MIXED_HIVE, "", false, Types.TimestampType.withoutZone())
    );
  }

  @ParameterizedTest
  @MethodSource
  public void testTimestampZoneHandle(
      TableFormat format, String primaryKeyDDL, boolean timestampWithoutZone, Types.TimestampType expectType) {
    createViewSource(simpleSourceSchema, simpleSourceData);

    sql("SET `" + SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES
        + "`=" + timestampWithoutZone);

    String sqlText = "CREATE TABLE " + target() + " " + primaryKeyDDL
        + " USING " + provider(format) + " AS SELECT * FROM " + source();
    sql(sqlText);

    ArcticTable table = loadTable();
    Types.NestedField f = table.schema().findField("ts");
    Asserts.assertType(expectType, f.type());
  }
}
