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

import com.netease.arctic.TransactionSequence;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.utils.TransactionUtil;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.encryption.EncryptedOutputFile;
import org.apache.iceberg.encryption.EncryptionManager;
import org.apache.iceberg.io.OutputFile;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Factory responsible for generating data file names for change and base location
 * <p>
 * File name pattern:
 * <p>
 * ${tree_node_id}-${file_type}-${transaction_sequence}-${partition_id}-${task_id}-${operation_id}-{count}
 * <ul>
 *   <li>tree_node_id: id of {@link com.netease.arctic.data.DataTreeNode} the file belong</li>
 *   <li>file_type: short name of file's {@link com.netease.arctic.data.DataFileType} </li>
 *   <li>transaction_sequence: sequence of transaction the file added, 0 for unknown</li>
 *   <li>partition_id: id of partitioned data in parallel engine like spark & flink </li>
 *   <li>task_id: id of write task within partition</li>
 *   <li>operation_id: id of operation, uuid as default</li>
 *   <li>count: auto increment count within writer </li>
 * </ul>
 */
public class CommonOutputFileFactory implements OutputFileFactory {
  private final String baseLocation;
  private final PartitionSpec partitionSpec;
  private final FileFormat format;
  private final ArcticFileIO io;
  private final EncryptionManager encryptionManager;
  private final int partitionId;
  private final long taskId;
  private final String operationId;

  private final String formattedTransactionSequence;
  private final AtomicLong fileCount = new AtomicLong(0);

  public CommonOutputFileFactory(String baseLocation, PartitionSpec partitionSpec,
                                 FileFormat format, ArcticFileIO io, EncryptionManager encryptionManager,
                                 int partitionId, long taskId, TransactionSequence transactionSequence) {
    this(baseLocation, partitionSpec, format, io, encryptionManager, partitionId, taskId, transactionSequence, null);
  }

  public CommonOutputFileFactory(String baseLocation, PartitionSpec partitionSpec,
                           FileFormat format, ArcticFileIO io, EncryptionManager encryptionManager,
                           int partitionId, long taskId, TransactionSequence transactionSequence, String operationId) {
    this.baseLocation = baseLocation;
    this.partitionSpec = partitionSpec;
    this.format = format;
    this.io = io;
    this.encryptionManager = encryptionManager;
    this.partitionId = partitionId;
    this.taskId = taskId;
    this.formattedTransactionSequence = TransactionUtil.formatTransactionSequence(transactionSequence);
    this.operationId = operationId == null ? UUID.randomUUID().toString() : operationId;
  }

  private String generateFilename(TaskWriterKey key) {
    return format.addExtension(
        String.format("%d-%s-%s-%05d-%d-%s-%05d", key.getTreeNode().getId(), key.getFileType().shortName(),
            formattedTransactionSequence, partitionId, taskId, operationId, fileCount.incrementAndGet()));
  }

  private String fileLocation(StructLike partitionData, String fileName) {
    if (partitionSpec.isUnpartitioned()) {
      return String.format("%s/%s/%s", baseLocation, "data", fileName);
    } else {
      return String.format("%s/%s/%s/%s", baseLocation, "data", partitionSpec.partitionToPath(partitionData), fileName);
    }
  }

  public EncryptedOutputFile newOutputFile(TaskWriterKey key) {
    String fileLocation = fileLocation(key.getPartitionKey(), generateFilename(key));
    OutputFile outputFile = io.newOutputFile(fileLocation);
    return encryptionManager.encrypt(outputFile);
  }
}
