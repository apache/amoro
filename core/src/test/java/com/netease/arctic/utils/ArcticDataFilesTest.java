package com.netease.arctic.utils;

import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class ArcticDataFilesTest {

  private static final Schema SCHEMA = new Schema(
      Types.NestedField.required(1, "dt", Types.TimestampType.withoutZone())
  );
  private static final PartitionSpec SPEC = PartitionSpec.builderFor(SCHEMA)
      .month("dt").build();

  private static final Integer MONTH = 624;

  @Test
  public void testReadData() {
    Assert.assertEquals(MONTH, ArcticDataFiles.data(SPEC, "dt_month=2022-01").get(0));
  }

  @Test
  public void testReadMonthData() {
    Assert.assertEquals(MONTH, ArcticDataFiles.readMonthData("2022-01"));
  }
}
