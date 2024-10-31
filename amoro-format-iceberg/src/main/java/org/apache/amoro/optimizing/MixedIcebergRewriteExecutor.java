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

package org.apache.amoro.optimizing;

import org.apache.amoro.data.PrimaryKeyedFile;
import org.apache.amoro.io.writer.GenericTaskWriters;
import org.apache.amoro.io.writer.MixedTreeNodePosDeleteWriter;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.utils.map.StructLikeCollections;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.deletes.PositionDelete;
import org.apache.iceberg.io.DeleteWriteResult;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.FileWriter;
import org.apache.iceberg.io.TaskWriter;

import java.util.List;

public class MixedIcebergRewriteExecutor extends AbstractRewriteFilesExecutor {

  public MixedIcebergRewriteExecutor(
      RewriteFilesInput input, MixedTable table, StructLikeCollections structLikeCollections, String outputDir) {
    super(input, table, structLikeCollections);
  }

  @Override
  protected OptimizingDataReader dataReader() {
    return new MixedIcebergOptimizingDataReader(table, structLikeCollections, input);
  }

  @Override
  protected FileWriter<PositionDelete<Record>, DeleteWriteResult> posWriter() {
    FileAppenderFactory<Record> appenderFactory = fullMetricAppenderFactory(table.spec());
    return new MixedTreeNodePosDeleteWriter<>(
        appenderFactory,
        deleteFileFormat(),
        partition(),
        io,
        encryptionManager(),
        getTransactionId(input.rePosDeletedDataFilesForMixed()),
        baseLocation(),
        table.spec());
  }

  @Override
  protected TaskWriter<Record> dataWriter() {
    return GenericTaskWriters.builderFor(table)
        .withTransactionId(table.isKeyedTable() ? getTransactionId(input.rewrittenDataFilesForMixed()) : null)
        .withTaskId(0)
        .withTargetFileSize(targetSize())
        .buildBaseWriter();
  }

  private long getTransactionId(List<PrimaryKeyedFile> dataFiles) {
    return dataFiles.stream().mapToLong(PrimaryKeyedFile::transactionId).max().orElse(0L);
  }

  private String baseLocation() {
    if (table.isKeyedTable()) {
      return table.asKeyedTable().baseTable().location();
    } else {
      return table.asUnkeyedTable().location();
    }
  }
}
