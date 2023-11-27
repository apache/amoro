package com.netease.arctic.flink;

import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.TableSchema;
import org.apache.iceberg.Schema;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

public class TestFlinkSchemaUtil {
  @Test
  public void testFlinkSchemaToIcebergSchema() {
    // flinkSchema with physical column,compute column, watermark
    TableSchema flinkSchema =
        TableSchema.builder()
            .field("id", DataTypes.INT().notNull())
            .field("name", DataTypes.STRING())
            .field("ts", DataTypes.TIMESTAMP(6))
            .field("compute_id", DataTypes.INT(), "`id` + 5")
            .field("proc", DataTypes.TIMESTAMP_LTZ(), "PROCTIME()")
            // org.apache.iceberg.flink.TypeToFlinkType will convert Timestamp to Timestamp(6), so
            // we cast datatype manually
            .field("ts3", DataTypes.TIMESTAMP(3), "cast(`ts` as timestamp(3))")
            .watermark("ts3", "`ts3` - INTERVAL '5' SECOND", DataTypes.TIMESTAMP(3))
            .build();

    // get physicalSchema from tableSchema and convert into iceberg Schema
    Schema icebergSchema =
        org.apache.iceberg.flink.FlinkSchemaUtil.convert(
            FlinkSchemaUtil.getPhysicalSchema(flinkSchema));

    Map<String, String> extraOptions = FlinkSchemaUtil.generateExtraOptionsFrom(flinkSchema);

    // Convert iceberg Schema with extraOptions into flink TableSchema
    TableSchema fromIcebergSchema =
        FlinkSchemaUtil.toSchema(icebergSchema, new ArrayList<>(), extraOptions);

    Assert.assertEquals(flinkSchema, fromIcebergSchema);
  }
}
