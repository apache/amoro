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

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.shade.guava32.com.google.common.collect.Streams;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.iceberg.io.OutputFile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class TestRecoverableAuthenticatedFileIO extends TableTestBase {
  private RecoverableHadoopFileIO recoverableHadoopFileIO;
  private AuthenticatedFileIO authenticatedFileIO;
  TableTrashManager trashManager;
  private String file1;
  private String file2;
  private String file3;

  public TestRecoverableAuthenticatedFileIO() {
    super(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, true));
  }

  @Before
  public void before() {
    MixedTable mixedTable = getMixedTable();
    trashManager =
        TableTrashManagers.build(
            mixedTable.id(),
            mixedTable.location(),
            mixedTable.properties(),
            (AuthenticatedHadoopFileIO) mixedTable.io());
    recoverableHadoopFileIO =
        new RecoverableHadoopFileIO(
            getTableMetaStore(), trashManager, TableProperties.TABLE_TRASH_FILE_PATTERN_DEFAULT);
    authenticatedFileIO = mixedTable.io();

    file1 = getMixedTable().location() + "/base/test/test1/test1.parquet";
    file2 = getMixedTable().location() + "/base/test/test2/test2.parquet";
    file3 = getMixedTable().location() + "/base/test/test2.parquet";
  }

  @Test
  public void exists() throws IOException {
    createFile(file1);
    Assert.assertTrue(recoverableHadoopFileIO.exists(file1));
    Assert.assertFalse(recoverableHadoopFileIO.exists(file2));
  }

  @Test
  public void rename() throws IOException {
    String newLocation = getMixedTable().location() + "/base/test/test4.parquet";
    createFile(file1);
    recoverableHadoopFileIO.rename(file1, newLocation);
    Assert.assertFalse(authenticatedFileIO.exists(file1));
    Assert.assertTrue(authenticatedFileIO.exists(newLocation));
  }

  @Test
  public void deleteDirectoryRecursively() throws IOException {
    createFile(file1);
    createFile(file2);
    createFile(file3);
    String dir = getMixedTable().location() + "/base/test";
    recoverableHadoopFileIO.deletePrefix(dir);
    Assert.assertFalse(authenticatedFileIO.exists(dir));
  }

  @Test
  public void list() throws IOException {
    createFile(file1);
    createFile(file2);
    createFile(file3);
    Iterable<PathInfo> items =
        recoverableHadoopFileIO.listDirectory(getMixedTable().location() + "/base/test");
    Assert.assertEquals(3L, Streams.stream(items).count());
  }

  @Test
  public void isDirectory() throws IOException {
    createFile(file1);
    Assert.assertFalse(recoverableHadoopFileIO.isDirectory(file1));
    Assert.assertTrue(recoverableHadoopFileIO.isDirectory(getMixedTable().location()));
  }

  @Test
  public void isEmptyDirectory() {
    String dir = getMixedTable().location() + "/location";
    authenticatedFileIO.asFileSystemIO().makeDirectories(dir);
    Assert.assertTrue(recoverableHadoopFileIO.isEmptyDirectory(dir));
    Assert.assertFalse(recoverableHadoopFileIO.isEmptyDirectory(getMixedTable().location()));
  }

  @Test
  public void deleteFile() throws IOException {
    createFile(file1);
    recoverableHadoopFileIO.deleteFile(file1);
    Assert.assertFalse(authenticatedFileIO.exists(file1));
    Assert.assertTrue(trashManager.fileExistInTrash(file1));
  }

  @Test
  public void deleteInputFile() throws IOException {
    createFile(file1);
    recoverableHadoopFileIO.deleteFile(recoverableHadoopFileIO.newInputFile(file1));
    Assert.assertFalse(authenticatedFileIO.exists(file1));
    Assert.assertTrue(trashManager.fileExistInTrash(file1));
  }

  @Test
  public void deleteOutputFile() throws IOException {
    createFile(file1);
    recoverableHadoopFileIO.deleteFile(recoverableHadoopFileIO.newOutputFile(file1));
    Assert.assertFalse(authenticatedFileIO.exists(file1));
    Assert.assertTrue(trashManager.fileExistInTrash(file1));
  }

  @Test
  public void trashFilePattern() {
    Assert.assertTrue(recoverableHadoopFileIO.matchTrashFilePattern(file1));
    Assert.assertTrue(recoverableHadoopFileIO.matchTrashFilePattern(file2));
    Assert.assertTrue(recoverableHadoopFileIO.matchTrashFilePattern(file3));
    Assert.assertTrue(
        recoverableHadoopFileIO.matchTrashFilePattern(
            getMixedTable().location() + "/metadata/version-hint.text"));
    Assert.assertTrue(
        recoverableHadoopFileIO.matchTrashFilePattern(
            getMixedTable().location() + "/metadata/v2.metadata.json"));
    Assert.assertTrue(
        recoverableHadoopFileIO.matchTrashFilePattern(
            getMixedTable().location()
                + "/metadata/snap-1515213806302741636-1-85fc817e-941d-4e9a-ab41-2dbf7687bfcd.avro"));
    Assert.assertTrue(
        recoverableHadoopFileIO.matchTrashFilePattern(
            getMixedTable().location() + "/metadata/3ce7600d-4853-45d0-8533-84c12a611916-m0.avro"));

    Assert.assertFalse(
        recoverableHadoopFileIO.matchTrashFilePattern(
            getMixedTable().location() + "/metadata/3ce7600d-4853-45d0-8533-84c12a611916.avro"));
  }

  private void createFile(String path) throws IOException {
    OutputFile baseOrphanDataFile = authenticatedFileIO.newOutputFile(path);
    baseOrphanDataFile.createOrOverwrite().close();
  }
}
