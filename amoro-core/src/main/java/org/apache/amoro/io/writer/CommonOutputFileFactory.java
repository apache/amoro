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

package org.apache.amoro.io.writer;

import org.apache.amoro.data.DataFileType;
import org.apache.amoro.data.DataTreeNode;
import org.apache.amoro.data.FileNameRules;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.encryption.EncryptedOutputFile;
import org.apache.iceberg.encryption.EncryptionManager;
import org.apache.iceberg.io.OutputFile;

/**
 * Factory responsible for generating data file names for change and base location
 *
 * <p>File name
 * pattern:${tree_node_id}-${file_type}-${transaction_id}-${partition_id}-${task_id}-{operation_id}-{count}
 *
 * <ul>
 *   <li>tree_node_id: id of {@link DataTreeNode} the file belong
 *   <li>file_type: short name of file's {@link DataFileType}
 *   <li>transaction_id: id of transaction the file added
 *   <li>partition_id: id of partitioned data in parallel engine like spark & flink
 *   <li>task_id: id of write task within partition
 *   <li>operation_id: a random id to avoid duplicated file name
 *   <li>count: auto increment count within writer
 * </ul>
 */
public class CommonOutputFileFactory implements OutputFileFactory {
  private final String baseLocation;
  private final PartitionSpec partitionSpec;
  private final AuthenticatedFileIO io;
  private final EncryptionManager encryptionManager;
  private final FileNameRules fileNameGenerator;

  public CommonOutputFileFactory(
      String baseLocation,
      PartitionSpec partitionSpec,
      FileFormat format,
      AuthenticatedFileIO io,
      EncryptionManager encryptionManager,
      int partitionId,
      long taskId,
      Long transactionId) {
    this.baseLocation = baseLocation;
    this.partitionSpec = partitionSpec;
    this.io = io;
    this.encryptionManager = encryptionManager;
    this.fileNameGenerator = new FileNameRules(format, partitionId, taskId, transactionId);
  }

  private String generateFilename(TaskWriterKey key) {
    return fileNameGenerator.fileName(key);
  }

  private String fileLocation(StructLike partitionData, String fileName) {
    if (partitionSpec.isUnpartitioned()) {
      return String.format("%s/%s/%s", baseLocation, "data", fileName);
    } else {
      return String.format(
          "%s/%s/%s/%s",
          baseLocation, "data", partitionSpec.partitionToPath(partitionData), fileName);
    }
  }

  public EncryptedOutputFile newOutputFile(TaskWriterKey key) {
    String fileLocation = fileLocation(key.getPartitionKey(), generateFilename(key));
    OutputFile outputFile = io.newOutputFile(fileLocation);
    return encryptionManager.encrypt(outputFile);
  }
}
