package com.netease.arctic.spark.test.suites.catalog;

import com.netease.arctic.spark.table.ArcticSparkTable;
import com.netease.arctic.spark.test.SparkTableTestBase;
import com.netease.arctic.spark.test.SparkTestContext;
import com.netease.arctic.table.PrimaryKeySpec;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.apache.iceberg.types.Types;
import org.apache.spark.sql.catalyst.analysis.NoSuchTableException;
import org.apache.spark.sql.connector.catalog.Table;
import org.apache.spark.sql.connector.catalog.TableCatalog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TestSessionCatalog extends SparkTableTestBase {

  public static final Schema schema = new Schema(
      Types.NestedField.required(1, "id", Types.IntegerType.get()),
      Types.NestedField.required(2, "data", Types.StringType.get()),
      Types.NestedField.required(3, "pt", Types.StringType.get())
  );
  public static final PrimaryKeySpec pkSpec = PrimaryKeySpec.builderFor(schema).addColumn("id").build();
  public static final PartitionSpec ptSpec = PartitionSpec.builderFor(schema).identity("pt").build();

  @Override
  protected Map<String, String> sparkSessionConfig() {
    return ImmutableMap.of(
        "spark.sql.catalog.spark_catalog", SparkTestContext.SESSION_CATALOG_IMPL,
        "spark.sql.catalog.spark_catalog.url", context.catalogUrl(SparkTestContext.EXTERNAL_MIXED_ICEBERG_HIVE)
    );
  }

  @Test
  public void testLoadTables() throws NoSuchTableException {
    createTarget(schema,
        builder -> builder.withPrimaryKeySpec(pkSpec).withPartitionSpec(ptSpec));

    TableCatalog sessionCatalog = (TableCatalog) spark().sessionState().catalogManager().catalog(SESSION_CATALOG);

    Table table = sessionCatalog.loadTable(target().toSparkIdentifier());
    Assertions.assertTrue(table instanceof ArcticSparkTable);
  }
}
