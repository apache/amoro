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

package org.apache.amoro.hive.io.writer;

import org.apache.amoro.data.FileNameRules;
import org.apache.amoro.hive.utils.HiveTableUtil;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.io.writer.OutputFileFactory;
import org.apache.amoro.io.writer.TaskWriterKey;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.encryption.EncryptedOutputFile;
import org.apache.iceberg.encryption.EncryptionManager;
import org.apache.iceberg.io.OutputFile;

/**
 *
 *
 * <pre>
 * For adapt hive table with partitions the dir construct is :
 *    ${table_location}
 *            -| change
 *            -| base
 *            -| hive
 *                 -| ${partition_name1}
 *                 -| ${partition_name2}
 *                            -| ${timestamp}_{txid}
 *
 * For adapt hive table without partitions the dir construct is :
 *    ${table_location}
 *            -| change
 *            -| base
 *            -| hive
 *                  -| ${timestamp}_{txid}
 * txId of unkeyed table is random long.
 * </pre>
 */
public class AdaptHiveOutputFileFactory implements OutputFileFactory {

  private final String hiveLocation;
  private final String hiveSubDirectory;
  private final PartitionSpec partitionSpec;
  private final AuthenticatedFileIO io;
  private final EncryptionManager encryptionManager;
  private final FileNameRules fileNameGenerator;
  private final boolean hiveConsistentWrite;

  public AdaptHiveOutputFileFactory(
      String hiveLocation,
      PartitionSpec partitionSpec,
      FileFormat format,
      AuthenticatedFileIO io,
      EncryptionManager encryptionManager,
      int partitionId,
      long taskId,
      Long transactionId,
      boolean hiveConsistentWrite) {
    this(
        hiveLocation,
        partitionSpec,
        format,
        io,
        encryptionManager,
        partitionId,
        taskId,
        transactionId,
        null,
        hiveConsistentWrite);
  }

  public AdaptHiveOutputFileFactory(
      String hiveLocation,
      PartitionSpec partitionSpec,
      FileFormat format,
      AuthenticatedFileIO io,
      EncryptionManager encryptionManager,
      int partitionId,
      long taskId,
      Long transactionId,
      String hiveSubDirectory,
      boolean hiveConsistentWrite) {
    this.hiveLocation = hiveLocation;
    this.partitionSpec = partitionSpec;
    this.io = io;
    this.encryptionManager = encryptionManager;
    if (hiveSubDirectory == null) {
      this.hiveSubDirectory =
          transactionId != null
              ? HiveTableUtil.newHiveSubdirectory(transactionId)
              : HiveTableUtil.newHiveSubdirectory();
    } else {
      this.hiveSubDirectory = hiveSubDirectory;
    }
    this.fileNameGenerator = new FileNameRules(format, partitionId, taskId, transactionId);
    this.hiveConsistentWrite = hiveConsistentWrite;
  }

  private String generateFilename(TaskWriterKey key) {
    String filename = fileNameGenerator.fileName(key);
    if (hiveConsistentWrite) {
      filename = "." + filename;
    }
    return filename;
  }

  private String fileLocation(StructLike partitionData, String fileName) {
    return String.format(
        "%s/%s",
        HiveTableUtil.newHiveDataLocation(
            hiveLocation, partitionSpec, partitionData, hiveSubDirectory),
        fileName);
  }

  public EncryptedOutputFile newOutputFile(TaskWriterKey key) {
    String fileLocation = fileLocation(key.getPartitionKey(), generateFilename(key));
    OutputFile outputFile = io.newOutputFile(fileLocation);
    return encryptionManager.encrypt(outputFile);
  }
}
