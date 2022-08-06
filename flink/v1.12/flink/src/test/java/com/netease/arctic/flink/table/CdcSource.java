/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.flink.table;

import com.netease.arctic.flink.DynamicTableSourceTestBase;
import com.netease.arctic.flink.util.DataUtil;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkOutput;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.TimestampData;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;

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

  @Override
  public DynamicTableSource copy() {
    return new CdcSource();
  }
}
