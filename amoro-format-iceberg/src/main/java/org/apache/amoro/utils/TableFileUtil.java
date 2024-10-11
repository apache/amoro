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

package org.apache.amoro.utils;

import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.io.BulkDeletionFailureException;
import org.apache.iceberg.util.Tasks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class TableFileUtil {
  private static final Logger LOG = LoggerFactory.getLogger(TableFileUtil.class);
  private static final String POS_DELETE_FILE_IDENTIFIER = "delete";

  /**
   * Parse file name form file path
   *
   * @param filePath file path
   * @return file name parsed from file path
   */
  public static String getFileName(String filePath) {
    int lastSlash = filePath.lastIndexOf('/');
    return filePath.substring(lastSlash + 1);
  }

  /**
   * Parse file directory path from file path
   *
   * @param filePath file path
   * @return file directory path parsed from file path
   */
  public static String getFileDir(String filePath) {
    int lastSlash = filePath.lastIndexOf('/');
    return filePath.substring(0, lastSlash);
  }

  /**
   * Try to recursiveDelete the empty directory
   *
   * @param io mixed-format file io
   * @param directoryPath directory location
   * @param exclude the directory will not be deleted
   */
  public static void deleteEmptyDirectory(
      AuthenticatedFileIO io, String directoryPath, Set<String> exclude) {
    if (!io.exists(directoryPath)) {
      LOG.debug("The target directory {} does not exist or has been deleted", directoryPath);
      return;
    }
    String parent = new Path(directoryPath).getParent().toString();
    if (!io.asFileSystemIO().isDirectory(directoryPath)
        || exclude.contains(directoryPath)
        || exclude.contains(parent)) {
      return;
    }

    LOG.debug("current path {} and parent path {} not in exclude.", directoryPath, parent);
    if (io.asFileSystemIO().isEmptyDirectory(directoryPath)) {
      io.asFileSystemIO().deletePrefix(directoryPath);
      LOG.debug("success delete empty directory {}", directoryPath);
      deleteEmptyDirectory(io, parent, exclude);
    }
  }

  /**
   * Helper to delete files. Bulk deletion is used if possible.
   *
   * @param io mixed-format file io
   * @param files files to delete
   * @param workPool executor pool. Only applicable for non-bulk FileIO
   * @return deleted file count
   */
  public static int deleteFiles(
      AuthenticatedFileIO io, Set<String> files, ExecutorService workPool) {
    if (files == null || files.isEmpty()) {
      return 0;
    }

    AtomicInteger failedFileCnt = new AtomicInteger(0);
    if (io.supportBulkOperations()) {
      try {
        io.asBulkFileIO().deleteFiles(files);
      } catch (BulkDeletionFailureException e) {
        failedFileCnt.set(e.numberFailedObjects());
        LOG.warn("Failed to bulk delete {} files", e.numberFailedObjects(), e);
      } catch (RuntimeException e) {
        failedFileCnt.set(files.size());
        LOG.warn("Failed to bulk delete files", e);
      }
    } else {
      if (workPool != null) {
        Tasks.foreach(files)
            .executeWith(workPool)
            .noRetry()
            .suppressFailureWhenFinished()
            .onFailure(
                (file, exc) -> {
                  failedFileCnt.addAndGet(1);
                  LOG.warn("Failed to delete file {}", file, exc);
                })
            .run(io::deleteFile);
      } else {
        files.forEach(
            f -> {
              try {
                io.deleteFile(f);
              } catch (RuntimeException e) {
                failedFileCnt.addAndGet(1);
                LOG.warn("Failed to delete file {}", f, e);
              }
            });
      }
    }

    return files.size() - failedFileCnt.get();
  }

  /**
   * Helper to delete files sequentially
   *
   * @param io mixed-format file io
   * @param files to deleted files
   * @return deleted file count
   */
  public static int deleteFiles(AuthenticatedFileIO io, Set<String> files) {
    return deleteFiles(io, files, null);
  }

  /**
   * Helper to delete files in parallel
   *
   * @param io mixed-format file io
   * @param files to deleted files
   * @param workPool executor pool
   * @return deleted file count
   */
  public static int parallelDeleteFiles(
      AuthenticatedFileIO io, Set<String> files, ExecutorService workPool) {
    return deleteFiles(io, files, workPool);
  }

  /**
   * Get the file path after move file to target directory
   *
   * @param newDirectory target directory
   * @param filePath file
   * @return new file path
   */
  public static String getNewFilePath(String newDirectory, String filePath) {
    return newDirectory + File.separator + getFileName(filePath);
  }

  /**
   * remove Uniform Resource Identifier (URI) in file path
   *
   * @param path file path with Uniform Resource Identifier (URI)
   * @return file path without Uniform Resource Identifier (URI)
   */
  public static String getUriPath(String path) {
    return URI.create(path).getPath();
  }

  /**
   * get the parent uri path for given path
   *
   * @param path - path to get parent path
   * @return the parent path
   */
  public static String getParent(String path) {
    Path p = new Path(path);
    return p.getParent().toString();
  }

  public static String optimizingPosDeleteFileName(String dataFileName, String suffix) {
    return String.format("%s-%s-%s", dataFileName, POS_DELETE_FILE_IDENTIFIER, suffix);
  }

  public static boolean isOptimizingPosDeleteFile(String dataFilePath, String posDeleteFilePath) {
    return getFileName(posDeleteFilePath)
        .startsWith(String.format("%s-%s", getFileName(dataFilePath), POS_DELETE_FILE_IDENTIFIER));
  }
}
