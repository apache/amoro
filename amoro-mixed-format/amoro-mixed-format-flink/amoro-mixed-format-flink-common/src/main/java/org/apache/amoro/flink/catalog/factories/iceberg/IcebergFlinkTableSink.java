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

package org.apache.amoro.flink.catalog.factories.iceberg;

import org.apache.amoro.table.TableMetaStore;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.api.constraints.UniqueConstraint;
import org.apache.flink.table.connector.sink.DataStreamSinkProvider;
import org.apache.flink.table.data.RowData;
import org.apache.flink.util.Preconditions;
import org.apache.iceberg.flink.IcebergTableSink;
import org.apache.iceberg.flink.TableLoader;
import org.apache.iceberg.flink.sink.FlinkSink;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;

public class IcebergFlinkTableSink extends IcebergTableSink {

  private final TableMetaStore tableMetaStore;

  public IcebergFlinkTableSink(
      TableLoader tableLoader,
      TableSchema tableSchema,
      ReadableConfig readableConfig,
      Map<String, String> writeProps,
      TableMetaStore tableMetaStore) {
    super(tableLoader, tableSchema, readableConfig, writeProps);
    this.tableMetaStore = tableMetaStore;
  }

  @Override
  public SinkRuntimeProvider getSinkRuntimeProvider(Context context) {
    Preconditions.checkState(
        !overwrite || context.isBounded(),
        "Unbounded data stream doesn't support overwrite operation.");

    List<String> equalityColumns =
        tableSchema.getPrimaryKey().map(UniqueConstraint::getColumns).orElseGet(ImmutableList::of);

    return new DataStreamSinkProvider() {
      @Override
      public DataStreamSink<?> consumeDataStream(DataStream<RowData> dataStream) {
        return FlinkSink.forRowData(dataStream)
            .tableLoader(tableLoader)
            .tableSchema(tableSchema)
            .equalityFieldColumns(equalityColumns)
            .overwrite(overwrite)
            .setAll(writeProps)
            .flinkConf(readableConfig)
            .tableMetaStore(tableMetaStore)
            .append();
      }
    };
  }
}
