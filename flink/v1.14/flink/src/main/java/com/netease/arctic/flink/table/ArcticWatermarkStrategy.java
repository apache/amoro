package com.netease.arctic.flink.table;

import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkGeneratorSupplier;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.table.data.RowData;

import java.io.Serializable;
import java.util.TimeZone;

public class ArcticWatermarkStrategy implements WatermarkStrategy<RowData>, Serializable {
  public static final long serialVersionUID = 1L;
  private final TimeZone timeZone;

  public ArcticWatermarkStrategy(TimeZone timeZone) {
    this.timeZone = timeZone;
  }

  @Override
  public WatermarkGenerator<RowData> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
    return new ArcticWatermarkGenerator(timeZone);
  }
}