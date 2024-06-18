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

package org.apache.amoro.io;

import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.TableMetaStore;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.iceberg.hadoop.HadoopFileIO;
import org.apache.iceberg.hadoop.Util;
import org.apache.iceberg.io.BulkDeletionFailureException;
import org.apache.iceberg.io.FileInfo;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.io.SupportsPrefixOperations;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/** Implementation of {@link AuthenticatedFileIO} for hadoop file system with authentication. */
public class AuthenticatedHadoopFileIO extends HadoopFileIO
    implements AuthenticatedFileIO, SupportsPrefixOperations, SupportsFileSystemOperations {

  private final TableMetaStore tableMetaStore;
  private boolean fileRecycleEnabled;

  AuthenticatedHadoopFileIO(TableMetaStore tableMetaStore) {
    super(tableMetaStore.getConfiguration());
    this.tableMetaStore = tableMetaStore;
  }

  @Override
  public InputFile newInputFile(String path) {
    return tableMetaStore.doAs(() -> super.newInputFile(path));
  }

  @Override
  public InputFile newInputFile(String path, long length) {
    return tableMetaStore.doAs(() -> super.newInputFile(path, length));
  }

  @Override
  public OutputFile newOutputFile(String path) {
    return tableMetaStore.doAs(() -> super.newOutputFile(path));
  }

  @Override
  public void deleteFile(String path) {
    tableMetaStore.doAs(
        () -> {
          Path toDelete = new Path(path);
          FileSystem fs = getFs(toDelete);

          try {
            fs.delete(toDelete, false);
          } catch (IOException e) {
            throw new UncheckedIOException("Fail to delete file: " + path, e);
          }
          return null;
        });
  }

  @Override
  public Iterable<PathInfo> listDirectory(String location) {
    return tableMetaStore.doAs(
        () -> {
          Path path = new Path(location);
          FileSystem fs = getFs(path);
          try {
            FileStatus[] fileStatuses = fs.listStatus(path);
            Iterator<PathInfo> it =
                Stream.of(fileStatuses)
                    .map(
                        status ->
                            new PathInfo(
                                status.getPath().toString(),
                                status.getLen(),
                                status.getModificationTime(),
                                status.isDirectory()))
                    .iterator();
            return () -> it;
          } catch (IOException e) {
            throw new UncheckedIOException("Fail to list files in " + location, e);
          }
        });
  }

  @VisibleForTesting
  public List<FileStatus> listWithoutDoAs(String location) {
    Path path = new Path(location);
    FileSystem fs = getFs(path);
    try {
      FileStatus[] fileStatuses = fs.listStatus(path);
      return Lists.newArrayList(fileStatuses);
    } catch (IOException e) {
      throw new UncheckedIOException("Fail to list files in " + location, e);
    }
  }

  @Override
  public void makeDirectories(String path) {
    tableMetaStore.doAs(
        () -> {
          Path filePath = new Path(path);
          FileSystem fs = getFs(filePath);
          try {
            if (!fs.mkdirs(filePath)) {
              throw new IOException(
                  "Fail to mkdirs: path "
                      + path
                      + " and file system return false,, need to check the hdfs path");
            }
          } catch (IOException e) {
            throw new UncheckedIOException("Fail to mkdirs: path " + path, e);
          }
          return null;
        });
  }

  @Override
  public boolean isDirectory(String location) {
    return tableMetaStore.doAs(
        () -> {
          Path path = new Path(location);
          FileSystem fs = getFs(path);

          try {
            return fs.isDirectory(path);
          } catch (IOException e) {
            throw new UncheckedIOException("Fail to check file directory for %s", e);
          }
        });
  }

  @Override
  public boolean isEmptyDirectory(String location) {
    Preconditions.checkArgument(
        isDirectory(location), "the target location %s is not a directory", location);
    return tableMetaStore.doAs(
        () -> {
          Path path = new Path(location);
          FileSystem fs = getFs(path);
          try {
            RemoteIterator<LocatedFileStatus> fileStatuses = fs.listFiles(path, true);
            return !fileStatuses.hasNext();
          } catch (IOException e) {
            throw new UncheckedIOException("Fail to list files in " + location, e);
          }
        });
  }

  @Override
  public void rename(String src, String dts) {
    tableMetaStore.doAs(
        () -> {
          Path srcPath = new Path(src);
          Path dtsPath = new Path(dts);
          FileSystem fs = getFs(srcPath);
          try {
            if (!fs.rename(srcPath, dtsPath)) {
              throw new IOException(
                  "Fail to rename: from "
                      + src
                      + " to "
                      + dts
                      + " and file system return false, need to check the hdfs path");
            }
          } catch (IOException e) {
            throw new UncheckedIOException("Fail to rename: from " + src + " to " + dts, e);
          }
          return null;
        });
  }

  @Override
  public <T> T doAs(Callable<T> callable) {
    return tableMetaStore.doAs(callable);
  }

  @Override
  public boolean exists(String path) {
    return tableMetaStore.doAs(
        () -> {
          Path filePath = new Path(path);
          FileSystem fs = getFs(filePath);
          try {
            return fs.exists(filePath);
          } catch (IOException e) {
            throw new UncheckedIOException("Fail to check file exist for " + path, e);
          }
        });
  }

  @Override
  public Iterable<FileInfo> listPrefix(String prefix) {
    return tableMetaStore.doAs(() -> super.listPrefix(prefix));
  }

  @Override
  public void deletePrefix(String prefix) {
    tableMetaStore.doAs(
        () -> {
          Path prefixToDelete = new Path(prefix);
          FileSystem fs = getFs(prefixToDelete);
          try {
            return fs.delete(prefixToDelete, true /* recursive */);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
  }

  @Override
  public void deleteFiles(Iterable<String> pathsToDelete) throws BulkDeletionFailureException {
    tableMetaStore.doAs(
        () -> {
          super.deleteFiles(pathsToDelete);
          return null;
        });
  }

  @Override
  public boolean supportPrefixOperations() {
    return true;
  }

  @Override
  public boolean supportBulkOperations() {
    return true;
  }

  @Override
  public boolean supportFileSystemOperations() {
    return true;
  }

  public TableMetaStore getTableMetaStore() {
    return tableMetaStore;
  }

  private FileSystem getFs(Path path) {
    return Util.getFs(path, conf());
  }
}
