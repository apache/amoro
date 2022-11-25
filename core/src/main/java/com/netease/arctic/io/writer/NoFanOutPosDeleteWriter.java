/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.io.writer;

import com.netease.arctic.io.ArcticFileIO;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.io.FileAppenderFactory;

import java.io.Closeable;
import java.io.IOException;

public class NoFanOutPosDeleteWriter<T> implements Closeable {

  public NoFanOutPosDeleteWriter(
      FileAppenderFactory<T> appenderFactory,
      OutputFileFactory fileFactory,
      ArcticFileIO io,
      FileFormat format,
      long mask, long index,
      StructLike partitionKey,
      long recordsNumThreshold) {

  }

  public NoFanOutPosDeleteWriter(FileAppenderFactory<T> appenderFactory,
      OutputFileFactory fileFactory,
      ArcticFileIO io,
      FileFormat format,
      long mask, long index,
      StructLike partitionKey) {
  }

  public NoFanOutPosDeleteWriter(FileAppenderFactory<T> appenderFactory,
      OutputFileFactory fileFactory,
      ArcticFileIO io,
      FileFormat format,
      StructLike partitionKey) {
  }

  public void delete(CharSequence path, long pos) {

  }

  public void delete(CharSequence path, long pos, T row) {

  }

  @Override
  public void close() throws IOException {

  }
}
