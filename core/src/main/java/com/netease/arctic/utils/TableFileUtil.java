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

package com.netease.arctic.utils;

import com.netease.arctic.io.ArcticFileIO;
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
   * @param io arcticTableFileIo
   * @param directoryPath directory location
   * @param exclude the directory will not be deleted
   */
  public static void deleteEmptyDirectory(
      ArcticFileIO io, String directoryPath, Set<String> exclude) {
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
   * @param io arctic file io
   * @param files files to delete
   * @param concurrent controls concurrent deletion. Only applicable for non-bulk FileIO
   * @return deleted file count
   */
  public static int deleteFiles(
      ArcticFileIO io, Set<String> files, boolean concurrent, ExecutorService svc) {
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
      if (concurrent) {
        Tasks.foreach(files)
            .executeWith(svc)
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
   * @param io arctic file io
   * @param files to deleted files
   * @return deleted file count
   */
  public static int deleteFiles(ArcticFileIO io, Set<String> files) {
    return deleteFiles(io, files, false, null);
  }

  /**
   * Helper to delete files in parallel
   *
   * @param io arctic file io
   * @param files to deleted files
   * @return deleted file count
   */
  public static int parallelDeleteFiles(ArcticFileIO io, Set<String> files, ExecutorService svc) {
    return deleteFiles(io, files, true, svc);
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
}
