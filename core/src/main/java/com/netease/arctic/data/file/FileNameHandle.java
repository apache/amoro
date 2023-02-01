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

package com.netease.arctic.data.file;

import com.netease.arctic.ams.api.Constants;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.DefaultKeyedFile;
import com.netease.arctic.io.writer.TaskWriterKey;
import com.netease.arctic.utils.IdGenerator;
import com.netease.arctic.utils.TableFileUtils;
import org.apache.iceberg.FileFormat;

import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileNameHandle {

  private static final String KEYED_FILE_NAME_PATTERN_STRING = "(\\d+)-(\\w+)-(\\d+)-(\\d+)-(\\d+)-.*";
  private static final Pattern KEYED_FILE_NAME_PATTERN = Pattern.compile(KEYED_FILE_NAME_PATTERN_STRING);

  private static final String FORMAT = "%d-%s-%d-%05d-%d-%s-%05d";

  private FileFormat fileFormat;
  private final int partitionId;
  private final long taskId;
  private final long transactionId;

  // uuid avoid duplicated file name
  private final String operationId = IdGenerator.randomId() + "";
  private final AtomicLong fileCount = new AtomicLong(0);

  public FileNameHandle(
      FileFormat fileFormat,
      int partitionId,
      Long taskId,
      Long transactionId) {
    this.fileFormat = fileFormat;
    this.partitionId = partitionId;
    this.taskId = taskId;
    this.transactionId = transactionId == null ? 0 : transactionId;
  }

  public String fileName(TaskWriterKey key) {
    return fileFormat.addExtension(
        String.format(FORMAT, key.getTreeNode().getId(), key.getFileType().shortName(),
            transactionId, partitionId, taskId, operationId, fileCount.incrementAndGet()));
  }

  /**
   * Flink write transactionId as 0.
   * if we get transactionId from path is 0, we set transactionId as iceberg sequenceNumber.
   *
   * @param path           file path
   * @param sequenceNumber iceberg sequenceNumber
   * @return fileMeta
   * @throws IllegalArgumentException if is not arctic format
   */
  public static DefaultKeyedFile.FileMeta parseChange(String path, Long sequenceNumber) {
    String fileName = TableFileUtils.getFileName(path);
    Matcher matcher = KEYED_FILE_NAME_PATTERN.matcher(fileName);
    if (matcher.matches()) {
      DataFileType type;
      long transactionId;
      long nodeId = Long.parseLong(matcher.group(1));
      type = DataFileType.ofShortName(matcher.group(2));
      transactionId = Long.parseLong(matcher.group(3));
      transactionId = transactionId == 0 ? sequenceNumber : transactionId;
      DataTreeNode node = DataTreeNode.ofId(nodeId);
      return new DefaultKeyedFile.FileMeta(transactionId, type, node);
    } else {
      throw new IllegalArgumentException("change path format is not illegal " + path);
    }
  }

  /**
   * Parse FileMeta for BaseStore.
   * Path writen by hive can not be pared by arctic format, we set it to be FileMeta.DEFAULT_BASE_FILE_META
   *
   * @param path - path
   * @return fileMeta
   */
  public static DefaultKeyedFile.FileMeta parseBase(String path) {
    String fileName = TableFileUtils.getFileName(path);
    Matcher matcher = KEYED_FILE_NAME_PATTERN.matcher(fileName);
    if (matcher.matches()) {
      long nodeId = Long.parseLong(matcher.group(1));
      DataFileType type = DataFileType.ofShortName(matcher.group(2));
      if (type == DataFileType.INSERT_FILE) {
        type = DataFileType.BASE_FILE;
      }
      long transactionId = Long.parseLong(matcher.group(3));
      DataTreeNode node = DataTreeNode.ofId(nodeId);
      return new DefaultKeyedFile.FileMeta(transactionId, type, node);
    } else {
      return DefaultKeyedFile.FileMeta.DEFAULT_BASE_FILE_META;
    }
  }

  /**
   * Parse file type for ChangeStore.
   *
   * @param path - path
   * @return file type
   * @throws IllegalArgumentException if is not arctic format
   */
  public static DataFileType parseFileTypeForChange(String path) {
    String fileName = TableFileUtils.getFileName(path);
    Matcher matcher = KEYED_FILE_NAME_PATTERN.matcher(fileName);
    if (matcher.matches()) {
      return DataFileType.ofShortName(matcher.group(2));
    } else {
      throw new IllegalArgumentException("change path format is not illegal " + path);
    }
  }

  /**
   * Parse file type for BaseStore.
   *
   * @param path         - path
   * @param defaultValue - return this value if is not arctic format
   * @return file type
   */
  public static DataFileType parseFileTypeForBase(String path, DataFileType defaultValue) {
    String fileName = TableFileUtils.getFileName(path);
    Matcher matcher = KEYED_FILE_NAME_PATTERN.matcher(fileName);
    if (matcher.matches()) {
      DataFileType type;
      type = DataFileType.ofShortName(matcher.group(2));
      if (type == DataFileType.INSERT_FILE) {
        type = DataFileType.BASE_FILE;
      }
      return type;
    } else {
      return defaultValue;
    }
  }

  /**
   * Parse file type.
   *
   * @param path         - path
   * @param tableType    - table type, base/change
   * @param defaultValue - return this value if is not arctic format
   * @return file type
   */
  public static DataFileType parseFileType(String path, String tableType, DataFileType defaultValue) {
    if (Constants.INNER_TABLE_CHANGE.equals(tableType)) {
      return parseFileTypeForChange(path);
    } else if (Constants.INNER_TABLE_BASE.equals(tableType)) {
      return parseFileTypeForBase(path, defaultValue);
    } else {
      throw new IllegalArgumentException("unknown tableType " + tableType);
    }
  }

  /**
   * Parse keyed file node id from file name, return node(0,0) if path is not arctic format.
   *
   * @param path path
   * @return node id
   */
  public static DataTreeNode parseFileNodeFromFileName(String path) {
    path = TableFileUtils.getFileName(path);
    Matcher matcher = KEYED_FILE_NAME_PATTERN.matcher(path);
    if (matcher.matches()) {
      long nodeId = Long.parseLong(matcher.group(1));
      return DataTreeNode.ofId(nodeId);
    } else {
      return DataTreeNode.ROOT;
    }
  }

  /**
   * Parse transaction id from file name, return 0 if path is not arctic format.
   *
   * @param path path
   * @return transactionId
   */
  public static long parseTransactionId(String path) {
    String fileName = TableFileUtils.getFileName(path);
    Matcher matcher = KEYED_FILE_NAME_PATTERN.matcher(fileName);
    if (matcher.matches()) {
      return Long.parseLong(matcher.group(3));
    } else {
      return 0L;
    }
  }
}
