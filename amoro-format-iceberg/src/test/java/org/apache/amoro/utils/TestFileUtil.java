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
import org.apache.amoro.io.PathInfo;
import org.apache.amoro.io.SupportsFileSystemOperations;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.iceberg.Files;
import org.apache.iceberg.io.FileInfo;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.io.OutputFile;
import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

public class TestFileUtil {

  @Test
  public void getFileName() {
    String fileName =
        TableFileUtil.getFileName(
            "hdfs://easyops-sloth/user/warehouse/animal_partition_two/base/"
                + "opt_mon=202109/opt_day=26/00000-0-3-1-37128f07-0845-43d8-905b-bd69b4ca351c-0000000001.parquet");
    Assert.assertEquals(
        "00000-0-3-1-37128f07-0845-43d8-905b-bd69b4ca351c-0000000001.parquet", fileName);
  }

  @Test
  public void getFileDir() {
    String fileDir =
        TableFileUtil.getFileDir(
            "hdfs://easyops-sloth/user/warehouse/animal_partition_two/base/"
                + "opt_mon=202109/opt_day=26/00000-0-3-1-37128f07-0845-43d8-905b-bd69b4ca351c-0000000001.parquet");
    Assert.assertEquals(
        "hdfs://easyops-sloth/user/warehouse/animal_partition_two/base/opt_mon=202109/opt_day=26",
        fileDir);
  }

  @Test
  public void testGetUriPath() {
    Assert.assertEquals("/a/b/c", TableFileUtil.getUriPath("hdfs://xxxxx/a/b/c"));
    Assert.assertEquals("/a/b/c", TableFileUtil.getUriPath("hdfs://localhost:8888/a/b/c"));
    Assert.assertEquals("/a/b/c", TableFileUtil.getUriPath("file://xxxxx/a/b/c"));
    Assert.assertEquals("/a/b/c", TableFileUtil.getUriPath("/a/b/c"));
    Assert.assertEquals("/a/b/c", TableFileUtil.getUriPath("hdfs:/a/b/c"));
    Assert.assertEquals("a/b/c", TableFileUtil.getUriPath("a/b/c"));
  }

  private static final TemporaryFolder temp = new TemporaryFolder();

  static class LocalAuthenticatedFileIO
      implements AuthenticatedFileIO, SupportsFileSystemOperations {

    @Override
    public <T> T doAs(Callable<T> callable) {
      try {
        return callable.call();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public InputFile newInputFile(String path) {
      return Files.localInput(path);
    }

    @Override
    public OutputFile newOutputFile(String path) {
      return Files.localOutput(path);
    }

    @Override
    public void deleteFile(String path) {
      if (!(new File(path)).delete()) {
        throw new UncheckedIOException(new IOException("Failed to delete file: " + path));
      }
    }

    @Override
    public void makeDirectories(String path) {
      try {
        temp.newFolder(path);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean isDirectory(String location) {
      return new File(location).isDirectory();
    }

    @Override
    public boolean isEmptyDirectory(String location) {
      return Objects.requireNonNull(new File(location).listFiles()).length == 0;
    }

    @Override
    public void rename(String oldPath, String newPath) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<PathInfo> listDirectory(String location) {
      File dir = new File(location);
      File[] files = dir.listFiles();
      Iterator<PathInfo> it =
          Stream.of(files)
              .map(
                  file ->
                      new PathInfo(
                          file.getPath(),
                          file.getTotalSpace(),
                          System.currentTimeMillis(),
                          file.isDirectory()))
              .iterator();
      return () -> it;
    }

    @Override
    public Iterable<FileInfo> listPrefix(String prefix) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void deletePrefix(String prefix) {
      File folder = new File(prefix);
      File[] files = folder.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.isDirectory()) {
            deletePrefix(file.getAbsolutePath());
          } else {
            file.delete();
          }
        }
      }
      folder.delete();
    }
  }

  @Test
  public void testDeleteEmptyDirectory() throws IOException {
    temp.create();
    String dataLocation = temp.newFolder("data").getAbsolutePath();
    String metadataLocation = temp.newFolder("metadata").getAbsolutePath();
    temp.newFile("metadata/metadata.json");

    File emptyPartition1 = temp.newFolder("data/partition1");
    File emptyPartition2 = temp.newFolder("data/partition2");
    File emptySubPartition = temp.newFolder("data/partition2/sub-partition1");
    File partition3 = temp.newFolder("data/partition3");
    File file = temp.newFile("data/partition3/data-file-1");

    Set<String> exclude = Sets.newHashSet(file.getAbsolutePath());
    try (LocalAuthenticatedFileIO io = new LocalAuthenticatedFileIO()) {
      Lists.newArrayList(
              emptyPartition1.getAbsolutePath(),
              emptyPartition2.getAbsolutePath(),
              emptySubPartition.getAbsolutePath(),
              partition3.getAbsolutePath(),
              metadataLocation)
          .forEach(f -> TableFileUtil.deleteEmptyDirectory(io, f, exclude));
      Assert.assertFalse(io.exists(emptyPartition1.getAbsolutePath()));
      Assert.assertFalse(io.exists(emptyPartition2.getAbsolutePath()));
      Assert.assertTrue(io.exists(dataLocation));
      Assert.assertTrue(io.exists(metadataLocation));
    }
  }
}
