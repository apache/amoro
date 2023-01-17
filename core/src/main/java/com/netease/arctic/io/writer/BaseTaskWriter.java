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

package com.netease.arctic.io.writer;

import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.PrimaryKeyData;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.table.PrimaryKeySpec;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.io.DataWriter;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.util.Tasks;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

/**
 * Abstract implementation of writer for {@link com.netease.arctic.table.BaseTable}.
 * @param <T> to indicate the record data type.
 */
public abstract class BaseTaskWriter<T> implements TaskWriter<T> {
  private final FileFormat format;
  private final FileAppenderFactory<T> appenderFactory;
  private final OutputFileFactory outputFileFactory;
  private final ArcticFileIO io;
  private final long targetFileSize;
  private final long mask;

  private final PartitionKey partitionKey;
  private final PrimaryKeyData primaryKey;

  private final Map<TaskWriterKey, TaskDataWriter> dataWriterMap = Maps.newHashMap();
  private final List<DataFile> completedFiles = Lists.newArrayList();

  protected BaseTaskWriter(FileFormat format, FileAppenderFactory<T> appenderFactory,
                           OutputFileFactory outputFileFactory, ArcticFileIO io, long targetFileSize, long mask,
                           Schema schema, PartitionSpec spec, PrimaryKeySpec primaryKeySpec) {
    this.format = format;
    this.appenderFactory = appenderFactory;
    this.outputFileFactory = outputFileFactory;
    this.io = io;
    this.targetFileSize = targetFileSize;
    this.mask = mask;
    this.partitionKey = new PartitionKey(spec, schema);
    this.primaryKey = primaryKeySpec == null ? null : new PrimaryKeyData(primaryKeySpec, schema);
  }

  @Override
  public void write(T row) throws IOException {
    TaskWriterKey writerKey = buildWriterKey(row);
    TaskDataWriter writer;
    if (!dataWriterMap.containsKey(writerKey)) {
      TaskWriterKey key = new TaskWriterKey(partitionKey.copy(), writerKey.getTreeNode(), writerKey.getFileType());
      writer = new TaskDataWriter(key);
      dataWriterMap.put(key, writer);
    } else {
      writer = dataWriterMap.get(writerKey);
    }
    write(writer, row);

    if (shouldRollToNewFile(writer)) {
      writer.close();
      dataWriterMap.remove(writerKey);
    }
  }

  protected void write(TaskDataWriter writer, T row) throws IOException {
    writer.write(row);
  }

  protected TaskWriterKey buildWriterKey(T row) {
    StructLike structLike = asStructLike(row);
    partitionKey.partition(structLike);
    DataTreeNode node;
    if (primaryKey != null) {
      primaryKey.primaryKey(structLike);
      node = primaryKey.treeNode(mask);
    } else {
      node = DataTreeNode.ROOT;
    }
    return new TaskWriterKey(partitionKey, node, DataFileType.BASE_FILE);
  }

  private boolean shouldRollToNewFile(TaskDataWriter dataWriter) {
    // TODO: ORC file now not support target file size before closed
    return !format.equals(FileFormat.ORC) && dataWriter.length() >= targetFileSize;
  }

  @Override
  public void abort() throws IOException {
    close();

    // clean up files created by this writer
    Tasks.foreach(completedFiles)
        .throwFailureWhenFinished()
        .noRetry()
        .run(file -> io.deleteFile(file.path().toString()));
  }

  @Override
  public WriteResult complete() throws IOException {
    close();
    List<DataFile> files = Lists.newArrayList(completedFiles);
    completedFiles.clear();
    return WriteResult.builder().addDataFiles(files.toArray(new DataFile[]{})).build();
  }

  @Override
  public void close() throws IOException {
    for (TaskDataWriter dataWriter : dataWriterMap.values()) {
      dataWriter.close();
    }
    dataWriterMap.clear();
  }

  /**
   * Wrap the data as a {@link StructLike}.
   */
  protected abstract StructLike asStructLike(T data);

  protected class TaskDataWriter {
    private final DataWriter<T> dataWriter;
    private long currentRows = 0;

    protected TaskDataWriter(TaskWriterKey writerKey) {
      this.dataWriter = io.doAs(() -> appenderFactory.newDataWriter(
          outputFileFactory.newOutputFile(writerKey), format, writerKey.getPartitionKey()));
    }

    protected void write(T record) {
      dataWriter.write(record);
      currentRows++;
    }

    protected void close() {
      io.doAs(() -> {
        dataWriter.close();
        return null;
      });
      if (currentRows > 0) {
        completedFiles.add(dataWriter.toDataFile());
      } else {
        try {
          io.deleteFile(dataWriter.toDataFile().path().toString());
        } catch (UncheckedIOException e) {
          // the file may not have been created, and it isn't worth failing the job to clean up, skip deleting
        }
      }
    }

    protected long length() {
      return dataWriter.length();
    }
  }
}
