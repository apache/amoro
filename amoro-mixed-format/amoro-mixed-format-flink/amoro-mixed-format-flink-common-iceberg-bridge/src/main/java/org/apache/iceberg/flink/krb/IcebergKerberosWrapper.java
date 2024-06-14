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

package org.apache.iceberg.flink.krb;

import org.apache.amoro.table.TableMetaStore;
import org.apache.flink.api.common.io.InputFormat;
import org.apache.flink.api.dag.Transformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.InputFormatSourceFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.operators.OneInputStreamOperatorFactory;
import org.apache.flink.streaming.api.operators.StreamSource;
import org.apache.flink.streaming.api.transformations.LegacySourceTransformation;
import org.apache.flink.streaming.api.transformations.OneInputTransformation;
import org.apache.flink.table.data.RowData;
import org.apache.iceberg.Schema;
import org.apache.iceberg.flink.krb.interceptor.ProxyFactory;
import org.apache.iceberg.flink.source.FlinkInputFormat;
import org.apache.iceberg.flink.source.StreamingMonitorFunction;
import org.apache.iceberg.flink.util.IcebergClassUtil;
import org.apache.iceberg.flink.util.ProxyUtil;

/** A wrapper class for {@link FlinkInputFormat} and {@link StreamingMonitorFunction} to support */
public class IcebergKerberosWrapper {
  private final StreamExecutionEnvironment env;
  private final TableMetaStore tableMetaStore;
  private final Schema schema;

  public IcebergKerberosWrapper(
      StreamExecutionEnvironment env, TableMetaStore tableMetaStore, Schema schema) {
    this.env = env;
    this.tableMetaStore = tableMetaStore;
    this.schema = schema;
  }

  public DataStream<RowData> sourceWithKrb(DataStream<RowData> ds) {
    IcebergClassUtil.clean(env);
    Transformation<RowData> origin = ds.getTransformation();
    if (origin instanceof OneInputTransformation) {
      OneInputTransformation<RowData, RowData> tf =
          (OneInputTransformation<RowData, RowData>) ds.getTransformation();
      OneInputStreamOperatorFactory op = (OneInputStreamOperatorFactory) tf.getOperatorFactory();
      ProxyFactory<FlinkInputFormat> inputFormatProxyFactory =
          IcebergClassUtil.getInputFormatProxyFactory(op, tableMetaStore, schema);

      if (tf.getInputs().isEmpty()) {
        return env.addSource(
                new UnkeyedInputFormatSourceFunction(inputFormatProxyFactory, tf.getOutputType()))
            .setParallelism(tf.getParallelism());
      }

      LegacySourceTransformation tfSource = (LegacySourceTransformation) tf.getInputs().get(0);
      StreamSource source = tfSource.getOperator();
      SourceFunction function = IcebergClassUtil.getSourceFunction(source);

      SourceFunction functionProxy = (SourceFunction) ProxyUtil.getProxy(function, tableMetaStore);
      return env.addSource(functionProxy, tfSource.getName(), tfSource.getOutputType())
          .transform(
              tf.getName(),
              tf.getOutputType(),
              new UnkeyedInputFormatOperatorFactory(inputFormatProxyFactory));
    }

    LegacySourceTransformation tfSource = (LegacySourceTransformation) origin;
    StreamSource source = tfSource.getOperator();
    InputFormatSourceFunction function =
        (InputFormatSourceFunction) IcebergClassUtil.getSourceFunction(source);

    InputFormat inputFormatProxy =
        (InputFormat) ProxyUtil.getProxy(function.getFormat(), tableMetaStore);
    return env.createInput(inputFormatProxy, tfSource.getOutputType())
        .setParallelism(origin.getParallelism());
  }
}
