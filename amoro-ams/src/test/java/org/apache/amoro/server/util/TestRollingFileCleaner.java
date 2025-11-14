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

package org.apache.amoro.server.util;

import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.io.AuthenticatedFileIOAdapter;
import org.apache.amoro.server.utils.RollingFileCleaner;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.iceberg.inmemory.InMemoryFileIO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class TestRollingFileCleaner {

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
}
