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

package org.apache.amoro.formats.iceberg.utils;

import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.io.AuthenticatedFileIOAdapter;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.iceberg.exceptions.NotFoundException;
import org.apache.iceberg.inmemory.InMemoryFileIO;
import org.apache.iceberg.io.BulkDeletionFailureException;
import org.apache.iceberg.io.SupportsBulkOperations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TestRollingFileCleaner {

  static class MockBulkFileIO extends InMemoryFileIO implements SupportsBulkOperations {
    private final Set<String> physicallyDeletedFiles = Sets.newConcurrentHashSet();

    @Override
    public void deleteFiles(Iterable<String> pathsToDelete) throws BulkDeletionFailureException {
      List<String> toDelete = Lists.newArrayList(pathsToDelete);
      try {
        // Simulate network latency during remote bulk deletion
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      for (String path : toDelete) {
        physicallyDeletedFiles.add(path);
        try {
          deleteFile(path);
        } catch (NotFoundException ignored) {
          // S3 bulk delete ignores already deleted / non-existent keys
        }
      }
    }
  }

  @Test
  void testCleanFiles() {
    InMemoryFileIO io = new InMemoryFileIO();
    AuthenticatedFileIO fileIO = new AuthenticatedFileIOAdapter(io);
    RollingFileCleaner fileCleaner = new RollingFileCleaner(fileIO, Sets.newHashSet());
    // generate some files
    Set<String> expiredFiles = Sets.newHashSet();
    for (int i = 0; i < 5050; i++) {
      String filePath = "file://bucket/warehouse/date=2025-01-01/file_" + i + ".txt";
      io.addFile(filePath, ("file_content" + i).getBytes());
      expiredFiles.add(filePath);
      fileCleaner.addFile(filePath);
    }

    Assertions.assertEquals(expiredFiles.size(), fileCleaner.fileCount());

    Assertions.assertEquals(5000, fileCleaner.cleanedFileCount());
    fileCleaner.clear();
    Assertions.assertEquals(5050, fileCleaner.cleanedFileCount());
  }

  @Test
  void testConcurrentCleanFiles() throws Exception {
    MockBulkFileIO io = new MockBulkFileIO();
    AuthenticatedFileIO fileIO = new AuthenticatedFileIOAdapter(io);
    RollingFileCleaner fileCleaner = new RollingFileCleaner(fileIO, Sets.newHashSet());

    int threadCount = 10;
    int filesPerThread = 300;
    int totalFiles = threadCount * filesPerThread;

    Set<String> allFiles = Sets.newConcurrentHashSet();
    for (int t = 0; t < threadCount; t++) {
      for (int i = 0; i < filesPerThread; i++) {
        String filePath =
            "file://bucket/warehouse/date=2025-01-01/thread_" + t + "_file_" + i + ".txt";
        io.addFile(filePath, ("content_" + t + "_" + i).getBytes());
        allFiles.add(filePath);
      }
    }

    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    List<Future<?>> futures = new ArrayList<>();
    for (int t = 0; t < threadCount; t++) {
      final int threadId = t;
      futures.add(
          executor.submit(
              () -> {
                for (int i = 0; i < filesPerThread; i++) {
                  String filePath =
                      "file://bucket/warehouse/date=2025-01-01/thread_"
                          + threadId
                          + "_file_"
                          + i
                          + ".txt";
                  fileCleaner.addFile(filePath);
                }
              }));
    }

    for (Future<?> future : futures) {
      future.get(30, TimeUnit.SECONDS);
    }
    executor.shutdown();

    Assertions.assertEquals(totalFiles, fileCleaner.fileCount());

    fileCleaner.clear();

    Assertions.assertEquals(totalFiles, fileCleaner.cleanedFileCount());
    Assertions.assertEquals(totalFiles, io.physicallyDeletedFiles.size());
    for (String file : allFiles) {
      Assertions.assertFalse(io.fileExists(file), "File was leaked: " + file);
    }
  }
}
