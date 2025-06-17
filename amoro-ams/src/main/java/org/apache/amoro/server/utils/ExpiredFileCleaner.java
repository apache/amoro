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

package org.apache.amoro.server.utils;

import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.io.BulkDeletionFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ExpiredFileCleaner {
  private final Set<String> collectedFiles = Sets.newConcurrentHashSet();
  private final Set<String> excludeFiles;

  private static final int CLEANED_FILE_GROUP_SIZE = 1_000;

  private final AtomicInteger fileCounter = new AtomicInteger(0);
  private final AtomicInteger cleanedFileCounter = new AtomicInteger(0);

  private final AuthenticatedFileIO fileIO;

  private static final Logger LOG = LoggerFactory.getLogger(ExpiredFileCleaner.class);

  public ExpiredFileCleaner(AuthenticatedFileIO fileIO, Set<String> excludeFiles) {
    this.fileIO = fileIO;
    this.excludeFiles =
        excludeFiles != null
            ? Sets.newConcurrentHashSet(excludeFiles)
            : Sets.newConcurrentHashSet();
  }

  public void addFile(String filePath) {
    if (excludeFiles.isEmpty()) {
      collectedFiles.add(filePath);
      int fc = fileCounter.incrementAndGet();

      if (fc % CLEANED_FILE_GROUP_SIZE == 0) {
        doCleanFiles();
      }
    } else {
      String uriPath = URI.create(filePath).getPath();
      String parentPath = new Path(uriPath).getParent().toString();
      if (!excludeFiles.contains(uriPath) && !excludeFiles.contains(parentPath)) {
        collectedFiles.add(filePath);
        int fc = fileCounter.incrementAndGet();

        if (fc % CLEANED_FILE_GROUP_SIZE == 0) {
          doCleanFiles();
        }
      } else {
        LOG.debug("File {} is excluded from cleaning", filePath);
      }
    }
  }

  private void doCleanFiles() {
    if (collectedFiles.isEmpty()) {
      return;
    }

    if (fileIO.supportBulkOperations()) {
      try {
        fileIO.asBulkFileIO().deleteFiles(collectedFiles);
        cleanedFileCounter.addAndGet(collectedFiles.size());
      } catch (BulkDeletionFailureException e) {
        LOG.warn("Failed to delete {} expired files in bulk", e.numberFailedObjects());
      }
    } else {
      for (String filePath : collectedFiles) {
        try {
          fileIO.deleteFile(filePath);
          cleanedFileCounter.incrementAndGet();
        } catch (Exception e) {
          LOG.warn("Failed to delete expired file: {}", filePath, e);
        }
      }
    }

    LOG.debug("Cleaned expired a file group, total files: {}", collectedFiles.size());

    collectedFiles.clear();
  }

  public int fileCount() {
    return fileCounter.get();
  }

  public int cleanedFileCount() {
    return cleanedFileCounter.get();
  }

  public void clear() {
    if (!collectedFiles.isEmpty()) {
      doCleanFiles();
    }

    collectedFiles.clear();
    excludeFiles.clear();
  }
}
