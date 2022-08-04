package com.netease.arctic.flink.table;

import com.netease.arctic.flink.util.ArcticUtils;
import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkOutput;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.TimestampData;

import java.io.Serializable;
import java.util.TimeZone;

public class ArcticWatermarkGenerator implements WatermarkGenerator<RowData>, Serializable {
  public static final long serialVersionUID = 1L;
  private boolean generateWatermark = false;
  private long lastTs = System.currentTimeMillis();
  private long watermarkIdle = 60 * 1000;
  private TimeZone timeZone;

  public ArcticWatermarkGenerator(TimeZone timeZone) {
    this.timeZone = timeZone;
  }

  @Override
  public void onEvent(RowData event, long eventTimestamp, WatermarkOutput output) {
    lastTs = System.currentTimeMillis();

    TimestampData ts = event.getTimestamp(3, 3);
    eventTimestamp = ts.getMillisecond();
    output.emitWatermark(new Watermark(eventTimestamp));
    if (eventTimestamp > Long.MIN_VALUE) {
      generateWatermark = true;
    }
  }

  @Override
  public void onPeriodicEmit(WatermarkOutput output) {
    if (generateWatermark) {
      output.emitWatermark(new Watermark(ArcticUtils.getCurrentTimestampData(timeZone).getMillisecond()));
    } else if (System.currentTimeMillis() - lastTs >= watermarkIdle) {
      generateWatermark = true;
    }
  }
}