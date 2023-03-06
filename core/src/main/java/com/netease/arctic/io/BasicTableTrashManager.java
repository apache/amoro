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

package com.netease.arctic.io;

import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.utils.TableFileUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.hadoop.HadoopFileIO;
import org.apache.iceberg.hadoop.Util;
import org.apache.iceberg.relocated.com.google.common.annotations.VisibleForTesting;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Basic implementation of TableTrashManager.
 */
class BasicTableTrashManager implements TableTrashManager {
  private static final Logger LOG = LoggerFactory.getLogger(BasicTableTrashManager.class);
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
  private final TableIdentifier tableIdentifier;
  private final ArcticFileIO arcticFileIO;
  private final String tableRootLocation;
  private final String trashLocation;

  BasicTableTrashManager(TableIdentifier tableIdentifier, ArcticFileIO arcticFileIO,
                         String tableRootLocation, String trashLocation) {
    this.tableIdentifier = tableIdentifier;
    this.arcticFileIO = arcticFileIO;
    this.tableRootLocation = tableRootLocation;
    this.trashLocation = trashLocation;
  }

  /**
   * Generate file location in trash
   *
   * @param tableRootLocation - table location
   * @param fileLocation      - file location
   * @param trashLocation     - trash location
   * @param deleteTime        - time the file deleted
   * @return file location in trash
   */
  public static String generateFileLocationInTrash(String tableRootLocation, String fileLocation, String trashLocation,
                                                   long deleteTime) {
    String relativeFileLocation = getRelativeFileLocation(tableRootLocation, fileLocation);
    return trashLocation + "/" + formatDate(deleteTime) + "/" + relativeFileLocation;
  }

  private static String getRelativeFileLocation(String tableRootLocation, String fileLocation) {
    if (!tableRootLocation.endsWith("/")) {
      tableRootLocation = tableRootLocation + "/";
    }
    Preconditions.checkArgument(fileLocation.startsWith(tableRootLocation),
        String.format("file %s is not in table location %s", fileLocation, tableRootLocation));
    return fileLocation.replaceFirst(tableRootLocation, "");
  }

  private static String formatDate(long time) {
    LocalDate localDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).toLocalDate();
    return localDate.format(DATE_FORMATTER);
  }

  private static LocalDate parseDate(String formattedDate) {
    return LocalDate.parse(formattedDate, DATE_FORMATTER);
  }

  @Override
  public TableIdentifier tableId() {
    return tableIdentifier;
  }

  @Override
  public void moveFileToTrash(String path) {
    try {
      Preconditions.checkArgument(!arcticFileIO.isDirectory(path), "should not move a directory to trash " + path);
      String targetFileLocation =
          generateFileLocationInTrash(this.tableRootLocation, path, this.trashLocation, System.currentTimeMillis());
      String targetFileDir = TableFileUtils.getFileDir(targetFileLocation);
      if (!arcticFileIO.exists(targetFileDir)) {
        arcticFileIO.mkdirs(targetFileDir);
      }
      arcticFileIO.rename(path, targetFileLocation);
    } catch (Exception e) {
      LOG.error("{} failed to move file to trash, {}", tableIdentifier, path, e);
      throw e;
    }
  }

  @Override
  public boolean fileExistInTrash(String path) {
    return findFileFromTrash(path) != null;
  }

  @Override
  public boolean restoreFileFromTrash(String path) {
    String fileFromTrash = findFileFromTrash(path);
    if (fileFromTrash == null) {
      return false;
    }
    try {
      arcticFileIO.rename(fileFromTrash, path);
      return true;
    } catch (Exception e) {
      LOG.info("failed to restore file, {}", path, e);
      return false;
    }
  }

  @Override
  public void cleanFiles(LocalDate expirationDate) {
    List<FileStatus> datePaths = arcticFileIO.list(this.trashLocation);
    if (datePaths.isEmpty()) {
      return;
    }
    LOG.info("{} start clean files with expiration date {}", tableIdentifier, expirationDate);
    for (FileStatus datePath : datePaths) {
      String dateName = TableFileUtils.getFileName(datePath.getPath().toString());
      LocalDate localDate;
      try {
        localDate = parseDate(dateName);
      } catch (Exception e) {
        LOG.warn("{} failed to parse path to date {}", tableIdentifier, datePath.getPath().toString());
        continue;
      }
      if (localDate.compareTo(expirationDate) < 0) {
        deleteRecursive(datePath.getPath().toString());
        LOG.info("{} delete files in trash for date {} success, {}", tableIdentifier, localDate,
            datePath.getPath().toString());
      } else {
        LOG.info("{} should not delete files in trash for date {},  {}", tableIdentifier, localDate,
            datePath.getPath().toString());
      }
    }
  }

  @VisibleForTesting
  void deleteRecursive(String path) {
    arcticFileIO.doAs(() -> {
      Path toDelete = new Path(path);
      FileSystem fs = Util.getFs(toDelete, ((HadoopFileIO) arcticFileIO).getConf());

      try {
        fs.delete(toDelete, true);
      } catch (IOException e) {
        throw new UncheckedIOException("Fail to delete file: " + path, e);
      }
      return null;
    });
  }

  private String findFileFromTrash(String path) {
    if (!arcticFileIO.exists(this.trashLocation)) {
      return null;
    }
    List<FileStatus> datePaths = arcticFileIO.list(this.trashLocation);
    String relativeLocation = getRelativeFileLocation(this.tableRootLocation, path);
    for (FileStatus datePath : datePaths) {
      String fullLocation = datePath.getPath().toString() + "/" + relativeLocation;
      if (arcticFileIO.exists(fullLocation)) {
        if (arcticFileIO.isDirectory(fullLocation)) {
          throw new IllegalArgumentException("can't restore a directory from trash " + fullLocation);
        }
        return fullLocation;
      }
    }
    return null;
  }
}
