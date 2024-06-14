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

package org.apache.iceberg.flink.util;

import org.apache.amoro.table.TableMetaStore;
import org.apache.flink.api.common.operators.MailboxExecutor;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.operators.AbstractUdfStreamOperator;
import org.apache.flink.streaming.api.operators.OneInputStreamOperatorFactory;
import org.apache.flink.streaming.runtime.tasks.ProcessingTimeService;
import org.apache.iceberg.Schema;
import org.apache.iceberg.encryption.EncryptionManager;
import org.apache.iceberg.flink.TableLoader;
import org.apache.iceberg.flink.krb.interceptor.ProxyFactory;
import org.apache.iceberg.flink.source.FlinkInputFormat;
import org.apache.iceberg.flink.source.ScanContext;
import org.apache.iceberg.flink.source.StreamingReaderOperator;
import org.apache.iceberg.io.FileIO;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/** Utility class for Iceberg, fetching Iceberg source function or inputFormat. */
public class IcebergClassUtil {
  private IcebergClassUtil() {}

  public static StreamingReaderOperator newStreamingReaderOperator(
      FlinkInputFormat format, ProcessingTimeService timeService, MailboxExecutor mailboxExecutor) {
    try {
      Constructor<StreamingReaderOperator> constructor =
          StreamingReaderOperator.class.getDeclaredConstructor(
              FlinkInputFormat.class, ProcessingTimeService.class, MailboxExecutor.class);
      constructor.setAccessible(true);
      return constructor.newInstance(format, timeService, mailboxExecutor);
    } catch (IllegalAccessException
        | NoSuchMethodException
        | InvocationTargetException
        | InstantiationException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("UnnecessaryParentheses")
  public static FlinkInputFormat getInputFormat(OneInputStreamOperatorFactory operatorFactory) {
    try {
      Class<?>[] classes = StreamingReaderOperator.class.getDeclaredClasses();
      Class<?> clazz = null;
      for (Class<?> c : classes) {
        if ("OperatorFactory".equals(c.getSimpleName())) {
          clazz = c;
          break;
        }
      }
      Field field = clazz.getDeclaredField("format");
      field.setAccessible(true);
      return (FlinkInputFormat) (field.get(operatorFactory));
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

  public static ProxyFactory<FlinkInputFormat> getInputFormatProxyFactory(
      OneInputStreamOperatorFactory operatorFactory,
      TableMetaStore tableMetaStore,
      Schema tableSchema) {
    FlinkInputFormat inputFormat = getInputFormat(operatorFactory);
    TableLoader tableLoader =
        ReflectionUtil.getField(FlinkInputFormat.class, inputFormat, "tableLoader");
    FileIO io = ReflectionUtil.getField(FlinkInputFormat.class, inputFormat, "io");
    EncryptionManager encryption =
        ReflectionUtil.getField(FlinkInputFormat.class, inputFormat, "encryption");
    Object context = ReflectionUtil.getField(FlinkInputFormat.class, inputFormat, "context");
    TableMetaStore tableMetaStore1 =
        ReflectionUtil.getField(FlinkInputFormat.class, inputFormat, "tableMetaStore");

    return ProxyUtil.getProxyFactory(
        FlinkInputFormat.class,
        tableMetaStore,
        new Class[] {
          TableLoader.class,
          Schema.class,
          FileIO.class,
          EncryptionManager.class,
          ScanContext.class,
          TableMetaStore.class
        },
        new Object[] {tableLoader, tableSchema, io, encryption, context, tableMetaStore1});
  }

  @SuppressWarnings("UnnecessaryParentheses")
  public static SourceFunction getSourceFunction(AbstractUdfStreamOperator source) {
    try {
      Field field = AbstractUdfStreamOperator.class.getDeclaredField("userFunction");
      field.setAccessible(true);
      return (SourceFunction) (field.get(source));
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("UnnecessaryParentheses")
  public static void clean(StreamExecutionEnvironment env) {
    try {
      Field field = StreamExecutionEnvironment.class.getDeclaredField("transformations");
      field.setAccessible(true);
      ((List<?>) (field.get(env))).clear();
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }
}
