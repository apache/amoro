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

import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.table.ArcticTable;
import org.apache.hadoop.fs.FileStatus;
import org.apache.iceberg.io.OutputFile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class RecoverableArcticFileIOTest extends TableTestBase {
  private RecoverableArcticFileIO recoverableArcticFileIO;
  private ArcticFileIO arcticFileIO;
  TableTrashManager trashManager;
  private String file1;
  private String file2;
  private String file3;

  public RecoverableArcticFileIOTest() {
    super(TableFormat.MIXED_ICEBERG, true, true);
  }

  @Before
  public void before() {
    ArcticTable arcticTable = getArcticTable();
    trashManager = TableTrashManagers.build(arcticTable.id(), arcticTable.location(),
        arcticTable.properties(), arcticTable.io());
    recoverableArcticFileIO = new RecoverableArcticFileIO(arcticTable.io(), trashManager);
    arcticFileIO = arcticTable.io();

    file1 = getArcticTable().location() + "/base/test/test1/test1.parquet";
    file2 = getArcticTable().location() + "/base/test/test2/test2.parquet";
    file3 = getArcticTable().location() + "/base/test/test2.parquet";
  }

  @Test
  public void exists() {
    createFile(file1);
    Assert.assertTrue(recoverableArcticFileIO.exists(file1));
    Assert.assertFalse(recoverableArcticFileIO.exists(file2));
  }

  @Test
  public void mkdirs() {
    String dir = getArcticTable().location() + "location";
    recoverableArcticFileIO.mkdirs(dir);
    arcticFileIO.exists(dir);
  }

  @Test
  public void rename() {
    String newLocation = getArcticTable().location() + "/base/test/test4.parquet";
    createFile(file1);
    recoverableArcticFileIO.rename(file1, newLocation);
    Assert.assertFalse(arcticFileIO.exists(file1));
    Assert.assertTrue(arcticFileIO.exists(newLocation));
  }

  @Test
  public void deleteDirectoryRecursively() {
    createFile(file1);
    createFile(file2);
    createFile(file3);
    String dir = getArcticTable().location() + "/base/test";
    recoverableArcticFileIO.deleteDirectoryRecursively(dir);
    Assert.assertFalse(arcticFileIO.exists(dir));
  }

  @Test
  public void list() {
    createFile(file1);
    createFile(file2);
    createFile(file3);
    List<FileStatus> list = recoverableArcticFileIO.list(getArcticTable().location() + "/base/test");
    Assert.assertEquals(3, list.size());
  }

  @Test
  public void isDirectory() {
    createFile(file1);
    Assert.assertFalse(recoverableArcticFileIO.isDirectory(file1));
    Assert.assertTrue(recoverableArcticFileIO.isDirectory(getArcticTable().location()));
  }

  @Test
  public void isEmptyDirectory() {
    String dir = getArcticTable().location() + "location";
    arcticFileIO.mkdirs(dir);
    Assert.assertTrue(recoverableArcticFileIO.isEmptyDirectory(dir));
    Assert.assertFalse(recoverableArcticFileIO.isEmptyDirectory(getArcticTable().location()));
  }

  @Test
  public void deleteFile() {
    createFile(file1);
    recoverableArcticFileIO.deleteFile(file1);
    Assert.assertFalse(arcticFileIO.exists(file1));
    Assert.assertTrue(trashManager.fileExistInTrash(file1));
  }

  @Test
  public void deleteInputFile() {
    createFile(file1);
    recoverableArcticFileIO.deleteFile(recoverableArcticFileIO.newInputFile(file1));
    Assert.assertFalse(arcticFileIO.exists(file1));
    Assert.assertTrue(trashManager.fileExistInTrash(file1));
  }

  @Test
  public void deleteOutputFile() {
    createFile(file1);
    recoverableArcticFileIO.deleteFile(recoverableArcticFileIO.newOutputFile(file1));
    Assert.assertFalse(arcticFileIO.exists(file1));
    Assert.assertTrue(trashManager.fileExistInTrash(file1));
  }

  private void createFile(String path) {
    OutputFile baseOrphanDataFile = arcticFileIO.newOutputFile(path);
    baseOrphanDataFile.createOrOverwrite();
  }
}