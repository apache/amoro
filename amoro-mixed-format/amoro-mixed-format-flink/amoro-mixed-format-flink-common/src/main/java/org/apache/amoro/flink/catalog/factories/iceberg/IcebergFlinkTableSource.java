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
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.connector.ProviderContext;
import org.apache.flink.table.connector.source.DataStreamScanProvider;
import org.apache.flink.table.data.RowData;
import org.apache.iceberg.Schema;
import org.apache.iceberg.flink.FlinkConfigOptions;
import org.apache.iceberg.flink.TableLoader;
import org.apache.iceberg.flink.krb.IcebergKerberosWrapper;
import org.apache.iceberg.flink.source.FlinkSource;
import org.apache.iceberg.flink.source.IcebergTableSource;

import java.util.Map;

public class IcebergFlinkTableSource extends IcebergTableSource {
  private final TableMetaStore tableMetaStore;

  public IcebergFlinkTableSource(
      TableLoader loader,
      TableSchema schema,
      Map<String, String> properties,
      ReadableConfig readableConfig,
      TableMetaStore tableMetaStore) {
    super(loader, schema, properties, readableConfig, tableMetaStore);
    this.tableMetaStore = tableMetaStore;
  }

  @Override
  public ScanRuntimeProvider getScanRuntimeProvider(ScanContext runtimeProviderContext) {
    return new DataStreamScanProvider() {
      @Override
      public DataStream<RowData> produceDataStream(
          ProviderContext providerContext, StreamExecutionEnvironment execEnv) {
        if (readableConfig.get(FlinkConfigOptions.TABLE_EXEC_ICEBERG_USE_FLIP27_SOURCE)) {
          if (tableMetaStore == null) {
            return createFLIP27Stream(execEnv);
          }
          return tableMetaStore.doAs(() -> createFLIP27StreamWithKrb(execEnv));
        } else {
          if (tableMetaStore == null) {
            return createDataStream(execEnv);
          }
          return tableMetaStore.doAs(() -> createDataStreamWithKrb(execEnv));
        }
      }

      @Override
      public boolean isBounded() {
        return FlinkSource.isBounded(properties);
      }
    };
  }

  private DataStream<RowData> createDataStreamWithKrb(StreamExecutionEnvironment execEnv) {
    DataStream<RowData> origin = createDataStream(execEnv);
    Schema icebergSchema = loader.loadTable().schema();
    IcebergKerberosWrapper wrapper =
        new IcebergKerberosWrapper(execEnv, tableMetaStore, icebergSchema);
    return wrapper.sourceWithKrb(origin);
  }

  private DataStream<RowData> createFLIP27StreamWithKrb(StreamExecutionEnvironment execEnv) {
    DataStream<RowData> origin = createFLIP27Stream(execEnv);
    Schema icebergSchema = loader.loadTable().schema();
    IcebergKerberosWrapper wrapper =
        new IcebergKerberosWrapper(execEnv, tableMetaStore, icebergSchema);
    return wrapper.sourceWithKrb(origin);
  }
}
