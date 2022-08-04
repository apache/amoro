package com.netease.arctic.flink.table;

import com.netease.arctic.flink.DynamicTableSourceTestBase;
import com.netease.arctic.flink.util.DataUtil;
import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkOutput;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.source.SourceFunctionProvider;
import org.apache.flink.table.connector.source.abilities.SupportsWatermarkPushDown;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.TimestampData;
import org.apache.flink.table.planner.factories.TableFactoryHarness;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class CdcSource extends DynamicTableSourceTestBase {

  public static final long serialVersionUID = 1L;
  List<Row> inputs;

  public void init() {
    List<Object[]> data = new LinkedList<>();
    data.add(new Object[]{RowKind.INSERT, 1L, "a", LocalDateTime.now()});
    data.add(new Object[]{RowKind.DELETE, 1L, "b", LocalDateTime.now()});
    data.add(new Object[]{RowKind.DELETE, 2L, "c", LocalDateTime.now()});
    data.add(new Object[]{RowKind.UPDATE_BEFORE, 3L, "d", LocalDateTime.now()});
    data.add(new Object[]{RowKind.UPDATE_AFTER, 4L, "e", LocalDateTime.now()});
    data.add(new Object[]{RowKind.INSERT, 5L, "e", LocalDateTime.now()});

    inputs = DataUtil.toRowList(data);
  }

  public void doRun(WatermarkGenerator<RowData> generator, WatermarkOutput output,
                  SourceFunction.SourceContext<RowData> ctx){
    while (true) {
      for (Row input : inputs) {
        Object[] fields = new Object[input.getArity()];
        for (int i = 0; i < fields.length; i++) {
          fields[i] = input.getField(i);
          if (fields[i] instanceof LocalDateTime) {
            fields[i] = TimestampData.fromLocalDateTime(((LocalDateTime) fields[i]));
          } else if (fields[i] instanceof String) {
            fields[i] = StringData.fromString((String) fields[i]);
          }
        }

        RowData next = GenericRowData.ofKind(input.getKind(), fields);
        generator.onEvent(next, Long.MIN_VALUE, output);
        generator.onPeriodicEmit(output);
        ctx.collect(next);
      }
      try {
        Thread.sleep(3000L);
      } catch (InterruptedException ignored) {
      }
    }
  }

}
