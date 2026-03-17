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

package org.apache.amoro.flink.util;

import org.apache.amoro.flink.interceptor.ProxyFactory;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.operators.AbstractStreamOperator;
import org.apache.flink.streaming.api.operators.AbstractUdfStreamOperator;
import org.apache.flink.streaming.api.operators.OneInputStreamOperator;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.logical.RowType;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.encryption.EncryptionManager;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.flink.TableLoader;
import org.apache.iceberg.flink.sink.TaskWriterFactory;
import org.apache.iceberg.flink.source.FlinkInputFormat;
import org.apache.iceberg.flink.source.ScanContext;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.util.ThreadPools;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** An util generates Apache Iceberg writer and committer operator w */
public class IcebergClassUtil {
  private static final String ICEBERG_PARTITION_SELECTOR_CLASS =
      "org.apache.iceberg.flink.sink.PartitionKeySelector";
  private static final String ICEBERG_FILE_COMMITTER_CLASS =
      "org.apache.iceberg.flink.sink.IcebergFilesCommitter";
  private static final String ICEBERG_FILE_WRITER_CLASS =
      "org.apache.iceberg.flink.sink.IcebergStreamWriter";

  public static KeySelector<RowData, Object> newPartitionKeySelector(
      PartitionSpec spec, Schema schema, RowType flinkSchema) {
    try {
      Class<?> clazz = forName(ICEBERG_PARTITION_SELECTOR_CLASS);
      Constructor<?> c = clazz.getConstructor(PartitionSpec.class, Schema.class, RowType.class);
      return (KeySelector<RowData, Object>) c.newInstance(spec, schema, flinkSchema);
    } catch (NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException
        | InstantiationException e) {
      throw new RuntimeException(e);
    }
  }

  public static OneInputStreamOperator<WriteResult, Void> newIcebergFilesCommitter(
      TableLoader tableLoader, boolean replacePartitions, String branch, PartitionSpec spec) {
    try {
      Class<?> clazz = forName(ICEBERG_FILE_COMMITTER_CLASS);
      Constructor<?> c =
          clazz.getDeclaredConstructor(
              TableLoader.class,
              boolean.class,
              Map.class,
              Integer.class,
              String.class,
              PartitionSpec.class);
      c.setAccessible(true);
      return (OneInputStreamOperator<WriteResult, Void>)
          c.newInstance(
              tableLoader,
              replacePartitions,
              new HashMap<>(),
              ThreadPools.WORKER_THREAD_POOL_SIZE,
              branch,
              spec);
    } catch (NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException
        | InstantiationException e) {
      throw new RuntimeException(e);
    }
  }

  public static OneInputStreamOperator<WriteResult, Void> newIcebergFilesCommitter(
      TableLoader tableLoader,
      boolean replacePartitions,
      String branch,
      PartitionSpec spec,
      AuthenticatedFileIO authenticatedFileIO) {
    OneInputStreamOperator<WriteResult, Void> obj =
        newIcebergFilesCommitter(tableLoader, replacePartitions, branch, spec);
    return (OneInputStreamOperator) ProxyUtil.getProxy(obj, authenticatedFileIO);
  }

  public static ProxyFactory<AbstractStreamOperator> getIcebergStreamWriterProxyFactory(
      String fullTableName,
      TaskWriterFactory taskWriterFactory,
      AuthenticatedFileIO authenticatedFileIO) {
    Class<?> clazz = forName(ICEBERG_FILE_WRITER_CLASS);
    return (ProxyFactory<AbstractStreamOperator>)
        ProxyUtil.getProxyFactory(
            clazz,
            authenticatedFileIO,
            new Class[] {String.class, TaskWriterFactory.class},
            new Object[] {fullTableName, taskWriterFactory});
  }

  public static ProxyFactory<FlinkInputFormat> getInputFormatProxyFactory(
      TableLoader tableLoader,
      Table table,
      AuthenticatedFileIO authenticatedFileIO,
      Schema tableSchema,
      Schema projectedSchema,
      ReadableConfig flinkConf,
      Map<String, String> properties,
      List<Expression> filters,
      long limit,
      Long startSnapshotId) {
    FileIO io = table.io();
    EncryptionManager encryption = table.encryption();
    ScanContext context =
        buildScanContext(
            table, projectedSchema, flinkConf, properties, filters, limit, startSnapshotId);

    return ProxyUtil.getProxyFactory(
        FlinkInputFormat.class,
        authenticatedFileIO,
        new Class[] {
          TableLoader.class, Schema.class, FileIO.class, EncryptionManager.class, ScanContext.class
        },
        new Object[] {tableLoader, tableSchema, io, encryption, context});
  }

  private static ScanContext buildScanContext(
      Table table,
      Schema projectedSchema,
      ReadableConfig flinkConf,
      Map<String, String> properties,
      List<Expression> filters,
      long limit,
      Long startSnapshotId) {
    ScanContext.Builder contextBuilder =
        ScanContext.builder().resolveConfig(table, properties, flinkConf);
    if (projectedSchema != null) {
      contextBuilder.project(projectedSchema);
    }
    if (filters != null) {
      contextBuilder.filters(filters);
    }
    contextBuilder.limit(limit);
    if (startSnapshotId != null) {
      contextBuilder.startSnapshotId(startSnapshotId);
    }
    return contextBuilder.build();
  }

  private static Class<?> forName(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static SourceFunction getSourceFunction(AbstractUdfStreamOperator source) {
    return (SourceFunction) source.getUserFunction();
  }

  public static void clean(StreamExecutionEnvironment env) {
    env.getTransformations().clear();
  }
}
