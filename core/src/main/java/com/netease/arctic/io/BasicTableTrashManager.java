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

import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.TableFileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Basic implementation of TableTrashManager.
 */
public class BasicTableTrashManager implements TableTrashManager {
  private static final Logger LOG = LoggerFactory.getLogger(BasicTableTrashManager.class);
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
  public static String DEFAULT_TRASH_DIR = ".trash";
  private final TableIdentifier tableIdentifier;
  private final ArcticFileIO arcticFileIO;
  private final String tableRootLocation;
  private final String trashLocation;

  public static BasicTableTrashManager of(ArcticTable table) {
    Map<String, String> properties = table.properties();
    String customTrashRootLocation = properties.get(TableProperties.TABLE_TRASH_CUSTOM_ROOT_LOCATION);
    String trashLocation = getTrashLocation(table.id(), table.location(), customTrashRootLocation);
    return new BasicTableTrashManager(table.id(), table.io(), table.location(), trashLocation);
  }

  public BasicTableTrashManager(TableIdentifier tableIdentifier, ArcticFileIO arcticFileIO,
                                String tableRootLocation, String trashLocation) {
    this.tableIdentifier = tableIdentifier;
    this.arcticFileIO = arcticFileIO;
    this.tableRootLocation = tableRootLocation;
    this.trashLocation = trashLocation;
  }

  /**
   * Get trash location.
   *
   * @param tableIdentifier         - table identifier
   * @param tableLocation           - table root location
   * @param customTrashRootLocation - from the table property table-trash.custom-root-location
   * @return trash location
   */
  public static String getTrashLocation(TableIdentifier tableIdentifier, String tableLocation,
                                        String customTrashRootLocation) {
    String trashLocation;
    if (StringUtils.isBlank(customTrashRootLocation)) {
      trashLocation = tableLocation + "/" + DEFAULT_TRASH_DIR;
    } else {
      if (!customTrashRootLocation.endsWith("/")) {
        customTrashRootLocation = customTrashRootLocation + "/";
      }
      trashLocation = customTrashRootLocation + tableIdentifier.getDatabase() +
          "/" + tableIdentifier.getTableName() +
          "/" + DEFAULT_TRASH_DIR;
    }
    return trashLocation;
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
    LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
    return localDateTime.format(DATE_FORMATTER);
  }

  private static LocalDateTime parseDate(String formattedDate) {
    return LocalDateTime.parse(formattedDate, DATE_FORMATTER);
  }

  @Override
  public TableIdentifier tableId() {
    return tableIdentifier;
  }

  @Override
  public boolean moveFileToTrash(String path) {
    try {
      String targetFileLocation =
          generateFileLocationInTrash(this.tableRootLocation, path, this.trashLocation, System.currentTimeMillis());
      String targetFileDir = TableFileUtils.getFileDir(targetFileLocation);
      if (!arcticFileIO.exists(targetFileDir)) {
        arcticFileIO.mkdirs(targetFileDir);
      }
      arcticFileIO.rename(path, targetFileLocation);
    } catch (Exception e) {
      LOG.error("failed to move file to trash, {}", path, e);
      return false;
    }
    return true;
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
    // TODO
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
        return fullLocation;
      }
    }
    return null;
  }
}
