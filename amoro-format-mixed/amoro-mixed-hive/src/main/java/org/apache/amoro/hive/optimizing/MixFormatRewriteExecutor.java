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

package org.apache.amoro.hive.optimizing;

import org.apache.amoro.data.PrimaryKeyedFile;
import org.apache.amoro.hive.io.writer.AdaptHiveGenericTaskWriterBuilder;
import org.apache.amoro.io.writer.MixedTreeNodePosDeleteWriter;
import org.apache.amoro.optimizing.AbstractRewriteFilesExecutor;
import org.apache.amoro.optimizing.OptimizingDataReader;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.WriteOperationKind;
import org.apache.amoro.utils.map.StructLikeCollections;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.deletes.PositionDelete;
import org.apache.iceberg.io.DeleteWriteResult;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.FileWriter;
import org.apache.iceberg.io.TaskWriter;

import java.util.List;

/** OptimizingExecutor form mixed format */
public class MixFormatRewriteExecutor extends AbstractRewriteFilesExecutor {

  private final String outputDir;

  public MixFormatRewriteExecutor(
      RewriteFilesInput input,
      MixedTable table,
      StructLikeCollections structLikeCollections,
      String outputDir) {
    super(input, table, structLikeCollections);
    this.outputDir = outputDir;
  }

  @Override
  protected OptimizingDataReader dataReader() {
    return new MixFormatOptimizingDataReader(table, structLikeCollections, input);
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
    return AdaptHiveGenericTaskWriterBuilder.builderFor(table)
        .withTransactionId(
            table.isKeyedTable() ? getTransactionId(input.rewrittenDataFilesForMixed()) : null)
        .withTaskId(0)
        .withCustomHiveSubdirectory(outputDir)
        .withTargetFileSize(targetSize())
        .buildWriter(
            StringUtils.isBlank(outputDir)
                ? WriteOperationKind.MAJOR_OPTIMIZE
                : WriteOperationKind.FULL_OPTIMIZE);
  }

  public long getTransactionId(List<PrimaryKeyedFile> dataFiles) {
    return dataFiles.stream().mapToLong(PrimaryKeyedFile::transactionId).max().orElse(0L);
  }

  public String baseLocation() {
    if (table.isKeyedTable()) {
      return table.asKeyedTable().baseTable().location();
    } else {
      return table.asUnkeyedTable().location();
    }
  }
}
