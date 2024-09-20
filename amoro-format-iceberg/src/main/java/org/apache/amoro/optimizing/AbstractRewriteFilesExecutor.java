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

import static org.apache.iceberg.TableProperties.DEFAULT_FILE_FORMAT;
import static org.apache.iceberg.TableProperties.DEFAULT_FILE_FORMAT_DEFAULT;
import static org.apache.iceberg.TableProperties.DELETE_DEFAULT_FILE_FORMAT;

import org.apache.amoro.data.DataTreeNode;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.io.writer.SetTreeNode;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.utils.map.StructLikeCollections;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.MetricsModes;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.deletes.PositionDelete;
import org.apache.iceberg.encryption.EncryptionManager;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.io.DeleteWriteResult;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.FileWriter;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * An abstract OptimizingExecutor implementation that rewrites the rewrittenDataFiles in
 * RewriteInput and generates new position delete for rePosDeletedDataFiles.
 */
public abstract class AbstractRewriteFilesExecutor
    implements OptimizingExecutor<RewriteFilesOutput> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractRewriteFilesExecutor.class);

  protected final RewriteFilesInput input;

  protected MixedTable table;

  protected OptimizingDataReader dataReader;

  protected AuthenticatedFileIO io;

  protected StructLikeCollections structLikeCollections;

  public AbstractRewriteFilesExecutor(
      RewriteFilesInput input, MixedTable table, StructLikeCollections structLikeCollections) {
    this.input = input;
    this.table = table;
    this.io = table.io();
    this.structLikeCollections = structLikeCollections;
    dataReader = dataReader();
  }

  protected abstract OptimizingDataReader dataReader();

  protected abstract FileWriter<PositionDelete<Record>, DeleteWriteResult> posWriter();

  protected abstract TaskWriter<Record> dataWriter();

  @Override
  public RewriteFilesOutput execute() {
    LOG.info("Start processing table optimize task: {}", input);

    List<DataFile> dataFiles = new ArrayList<>();
    List<DeleteFile> deleteFiles = new ArrayList<>();

    long startTime = System.currentTimeMillis();
    try {
      if (!ArrayUtils.isEmpty(input.rePosDeletedDataFiles())) {
        deleteFiles = io.doAs(this::equalityToPosition);
      }

      if (!ArrayUtils.isEmpty(input.rewrittenDataFiles())) {
        dataFiles = io.doAs(this::rewriterDataFiles);
      }
    } finally {
      dataReader.close();
    }
    long duration = System.currentTimeMillis() - startTime;

    Map<String, String> summary = resolverSummary(dataFiles, deleteFiles, duration);
    return new RewriteFilesOutput(
        dataFiles.toArray(new DataFile[0]), deleteFiles.toArray(new DeleteFile[0]), summary);
  }

  private List<DeleteFile> equalityToPosition() throws Exception {
    FileWriter<PositionDelete<Record>, DeleteWriteResult> posDeleteWriter = posWriter();

    try (CloseableIterator<Record> iterator = dataReader.readDeletedData().iterator()) {
      PositionDelete<Record> positionDelete = PositionDelete.create();
      while (iterator.hasNext()) {
        Record record = iterator.next();
        String filePath = (String) record.getField(MetadataColumns.FILE_PATH.name());
        Long rowPosition = (Long) record.getField(MetadataColumns.ROW_POSITION.name());
        positionDelete.set(filePath, rowPosition, null);
        if (posDeleteWriter instanceof SetTreeNode) {
          DataTreeNode dataTreeNode =
              DataTreeNode.ofId(
                  (Long) record.getField(org.apache.amoro.table.MetadataColumns.TREE_NODE_NAME));
          ((SetTreeNode) posDeleteWriter).setTreeNode(dataTreeNode);
        }
        posDeleteWriter.write(positionDelete);
      }
    } finally {
      posDeleteWriter.close();
    }

    return posDeleteWriter.result().deleteFiles();
  }

  private List<DataFile> rewriterDataFiles() throws Exception {
    List<DataFile> result = Lists.newArrayList();
    TaskWriter<Record> writer = dataWriter();

    try (CloseableIterator<Record> records = dataReader.readData().iterator()) {
      while (records.hasNext()) {
        Record record = records.next();
        writer.write(record);
      }
    } finally {
      writer.close();
    }

    result.addAll(Arrays.asList(writer.dataFiles()));

    return result;
  }

  protected FileFormat dataFileFormat() {
    String formatAsString =
        table.properties().getOrDefault(DEFAULT_FILE_FORMAT, DEFAULT_FILE_FORMAT_DEFAULT);
    return FileFormat.valueOf(formatAsString.toUpperCase());
  }

  protected FileFormat deleteFileFormat() {
    String deleteFileFormatName =
        table.properties().getOrDefault(DELETE_DEFAULT_FILE_FORMAT, DEFAULT_FILE_FORMAT_DEFAULT);
    return FileFormat.valueOf(deleteFileFormatName.toUpperCase());
  }

  protected FileAppenderFactory<Record> fullMetricAppenderFactory(PartitionSpec spec) {
    GenericAppenderFactory appenderFactory = new GenericAppenderFactory(table.schema(), spec);
    appenderFactory.setAll(table.properties());
    appenderFactory.set(
        org.apache.iceberg.TableProperties.METRICS_MODE_COLUMN_CONF_PREFIX
            + MetadataColumns.DELETE_FILE_PATH.name(),
        MetricsModes.Full.get().toString());
    appenderFactory.set(
        org.apache.iceberg.TableProperties.METRICS_MODE_COLUMN_CONF_PREFIX
            + MetadataColumns.DELETE_FILE_POS.name(),
        MetricsModes.Full.get().toString());
    return appenderFactory;
  }

  protected long targetSize() {
    return PropertyUtil.propertyAsLong(
        table.properties(),
        TableProperties.SELF_OPTIMIZING_TARGET_SIZE,
        TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT);
  }

  protected StructLike partition() {
    ContentFile<?>[] dataFiles = input.allFiles();
    return dataFiles[0].partition();
  }

  protected EncryptionManager encryptionManager() {
    if (table.isKeyedTable()) {
      return table.asKeyedTable().baseTable().encryption();
    } else {
      return table.asUnkeyedTable().encryption();
    }
  }

  private Map<String, String> resolverSummary(
      List<DataFile> dataFiles, List<DeleteFile> deleteFiles, long duration) {
    int dataFileCnt = 0;
    long dataFileTotalSize = 0;
    int eqDeleteFileCnt = 0;
    long eqDeleteFileTotalSize = 0;
    int posDeleteFileCnt = 0;
    long posDeleteFileTotalSize = 0;
    if (dataFiles != null) {
      for (DataFile dataFile : dataFiles) {
        dataFileCnt++;
        dataFileTotalSize += dataFile.fileSizeInBytes();
      }
    }
    if (deleteFiles != null) {
      for (DeleteFile deleteFile : deleteFiles) {
        if (deleteFile.content() == FileContent.EQUALITY_DELETES) {
          eqDeleteFileCnt++;
          eqDeleteFileTotalSize += deleteFile.fileSizeInBytes();
        } else {
          posDeleteFileCnt++;
          posDeleteFileTotalSize += deleteFile.fileSizeInBytes();
        }
      }
    }

    OptimizingTaskSummary summary = new OptimizingTaskSummary();
    summary.setDataFileCnt(dataFileCnt);
    summary.setDataFileTotalSize(dataFileTotalSize);
    summary.setEqDeleteFileCnt(eqDeleteFileCnt);
    summary.setEqDeleteFileTotalSize(eqDeleteFileTotalSize);
    summary.setPosDeleteFileCnt(posDeleteFileCnt);
    summary.setPosDeleteFileTotalSize(posDeleteFileTotalSize);
    summary.setExecuteDuration(duration);

    return summary.getSummary();
  }
}
