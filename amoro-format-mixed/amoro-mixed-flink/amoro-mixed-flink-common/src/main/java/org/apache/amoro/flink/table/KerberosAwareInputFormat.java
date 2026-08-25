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

package org.apache.amoro.flink.table;

import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.flink.api.common.io.InputFormat;
import org.apache.flink.api.common.io.RichInputFormat;
import org.apache.flink.api.common.io.statistics.BaseStatistics;
import org.apache.flink.core.io.InputSplit;
import org.apache.flink.core.io.InputSplitAssigner;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * A concrete {@link InputFormat} wrapper that runs delegate calls inside {@link
 * AuthenticatedFileIO#doAs(Callable)} without using JDK dynamic proxies.
 */
public class KerberosAwareInputFormat<OT, T extends InputSplit> extends RichInputFormat<OT, T> {
  private static final long serialVersionUID = 1L;

  private final InputFormat<OT, T> delegate;
  private final AuthenticatedFileIO authenticatedFileIO;

  public KerberosAwareInputFormat(
      InputFormat<OT, T> delegate, AuthenticatedFileIO authenticatedFileIO) {
    this.delegate = delegate;
    this.authenticatedFileIO = authenticatedFileIO;
  }

  @Override
  public void configure(org.apache.flink.configuration.Configuration parameters) {
    authenticatedFileIO.doAs(
        () -> {
          delegate.configure(parameters);
          return null;
        });
  }

  @Override
  public BaseStatistics getStatistics(BaseStatistics cachedStatistics) throws IOException {
    try {
      return authenticatedFileIO.doAs(() -> delegate.getStatistics(cachedStatistics));
    } catch (RuntimeException e) {
      throw unwrapIOException(e);
    }
  }

  @Override
  public T[] createInputSplits(int minNumSplits) throws IOException {
    try {
      return authenticatedFileIO.doAs(() -> delegate.createInputSplits(minNumSplits));
    } catch (RuntimeException e) {
      throw unwrapIOException(e);
    }
  }

  @Override
  public InputSplitAssigner getInputSplitAssigner(T[] inputSplits) {
    return authenticatedFileIO.doAs(() -> delegate.getInputSplitAssigner(inputSplits));
  }

  @Override
  public void open(T split) throws IOException {
    try {
      authenticatedFileIO.doAs(
          () -> {
            delegate.open(split);
            return null;
          });
    } catch (RuntimeException e) {
      throw unwrapIOException(e);
    }
  }

  @Override
  public boolean reachedEnd() throws IOException {
    try {
      return authenticatedFileIO.doAs(delegate::reachedEnd);
    } catch (RuntimeException e) {
      throw unwrapIOException(e);
    }
  }

  @Override
  public OT nextRecord(OT reuse) throws IOException {
    try {
      return authenticatedFileIO.doAs(() -> delegate.nextRecord(reuse));
    } catch (RuntimeException e) {
      throw unwrapIOException(e);
    }
  }

  @Override
  public void close() throws IOException {
    try {
      authenticatedFileIO.doAs(
          () -> {
            delegate.close();
            return null;
          });
    } catch (RuntimeException e) {
      throw unwrapIOException(e);
    }
  }

  @Override
  public void openInputFormat() throws IOException {
    if (!(delegate instanceof RichInputFormat)) {
      return;
    }
    RichInputFormat<OT, T> richInputFormat = (RichInputFormat<OT, T>) delegate;
    richInputFormat.setRuntimeContext(getRuntimeContext());
    try {
      authenticatedFileIO.doAs(
          () -> {
            richInputFormat.openInputFormat();
            return null;
          });
    } catch (RuntimeException e) {
      throw unwrapIOException(e);
    }
  }

  @Override
  public void closeInputFormat() throws IOException {
    if (!(delegate instanceof RichInputFormat)) {
      return;
    }
    RichInputFormat<OT, T> richInputFormat = (RichInputFormat<OT, T>) delegate;
    try {
      authenticatedFileIO.doAs(
          () -> {
            richInputFormat.closeInputFormat();
            return null;
          });
    } catch (RuntimeException e) {
      throw unwrapIOException(e);
    }
  }

  private IOException unwrapIOException(RuntimeException exception) {
    Throwable cause = exception.getCause();
    if (cause instanceof IOException) {
      return (IOException) cause;
    }
    throw exception;
  }
}
