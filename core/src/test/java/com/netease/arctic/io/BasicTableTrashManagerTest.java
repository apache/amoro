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
import com.netease.arctic.utils.TableFileUtils;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.OutputFile;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class BasicTableTrashManagerTest extends TableTestBase {
  public BasicTableTrashManagerTest() {
    super(TableFormat.MIXED_ICEBERG, true, true);
  }

  @Test
  public void testGenerateFileLocationInTrash() {
    LocalDateTime localDateTime = LocalDateTime.of(2023, 2, 2, 1, 1);
    long toEpochMilli = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    String locationInTrash = BasicTableTrashManager.generateFileLocationInTrash(
        "/tmp/table", "/tmp/table/change/file1", "/tmp/table/.trash", toEpochMilli);
    Assert.assertEquals("/tmp/table/.trash/20230202/change/file1", locationInTrash);
  }

  @Test
  public void testDeleteAndRestore() {
    TableTrashManager tableTrashManager = TableTrashManagers.of(getArcticTable());
    String trashLocation =
        TableTrashManagers.getTrashLocation(getArcticTable().id(), getArcticTable().location(), null);

    String relativeFilePath = "base/test/test1.parquet";
    String path = createFile(getArcticTable().io(), getArcticTable().location() + File.separator + relativeFilePath);

    Assert.assertFalse(tableTrashManager.fileExistInTrash(path));
    Assert.assertFalse(tableTrashManager.restoreFileFromTrash(path));

    long now = System.currentTimeMillis();
    Assert.assertTrue(tableTrashManager.moveFileToTrash(path));
    String fileLocationInTrash = BasicTableTrashManager.generateFileLocationInTrash(
        getArcticTable().location(), path, trashLocation, now);

    Assert.assertFalse(getArcticTable().io().exists(path));
    Assert.assertTrue(getArcticTable().io().exists(fileLocationInTrash));

    Assert.assertTrue(tableTrashManager.fileExistInTrash(path));
    Assert.assertTrue(tableTrashManager.restoreFileFromTrash(path));

    Assert.assertTrue(getArcticTable().io().exists(path));
    Assert.assertFalse(getArcticTable().io().exists(fileLocationInTrash));
  }

  @Test
  public void testDeleteDirectory() {
    TableTrashManager tableTrashManager = TableTrashManagers.of(getArcticTable());
    String trashLocation =
        TableTrashManagers.getTrashLocation(getArcticTable().id(), getArcticTable().location(), null);
    String relativeFilePath = "base/test/test1.parquet";
    String path = createFile(getArcticTable().io(), getArcticTable().location() + File.separator + relativeFilePath);

    String directory = TableFileUtils.getFileDir(path);
    long now = System.currentTimeMillis();
    Assert.assertFalse(tableTrashManager.moveFileToTrash(directory));
    String directoryLocationInTrash = BasicTableTrashManager.generateFileLocationInTrash(
        getArcticTable().location(), directory, trashLocation, now);

    Assert.assertTrue(getArcticTable().io().exists(directory));
    Assert.assertFalse(getArcticTable().io().exists(directoryLocationInTrash));
  }

  @Test
  public void testRestoreDirectory() {
    TableTrashManager tableTrashManager = TableTrashManagers.of(getArcticTable());
    String trashLocation =
        TableTrashManagers.getTrashLocation(getArcticTable().id(), getArcticTable().location(), null);
    String relativeFilePath = "base/test/test1.parquet";
    String path = createFile(getArcticTable().io(), getArcticTable().location() + File.separator + relativeFilePath);

    String directory = TableFileUtils.getFileDir(path);
    long now = System.currentTimeMillis();
    Assert.assertTrue(tableTrashManager.moveFileToTrash(path));
    String fileLocationInTrash = BasicTableTrashManager.generateFileLocationInTrash(
        getArcticTable().location(), path, trashLocation, now);

    Assert.assertFalse(getArcticTable().io().exists(path));
    Assert.assertTrue(getArcticTable().io().exists(fileLocationInTrash));

    Assert.assertFalse(tableTrashManager.fileExistInTrash(directory));
    Assert.assertFalse(tableTrashManager.restoreFileFromTrash(directory));

    Assert.assertFalse(getArcticTable().io().exists(path));
    Assert.assertTrue(getArcticTable().io().exists(fileLocationInTrash));
  }

  @Test
  public void testDeleteRecursive() {
    BasicTableTrashManager tableTrashManager = ((BasicTableTrashManager) TableTrashManagers.of(getArcticTable()));
    String relativeFilePath = "base/test/test1.parquet";
    String relativeFilePath2 = "base/test2/test2.parquet";
    String path = createFile(getArcticTable().io(), getArcticTable().location() + File.separator + relativeFilePath);
    String path2 = createFile(getArcticTable().io(), getArcticTable().location() + File.separator + relativeFilePath2);
    String testDir = TableFileUtils.getFileDir(path);
    String baseDir = TableFileUtils.getFileDir(testDir);

    Assert.assertTrue(getArcticTable().io().exists(path));
    Assert.assertTrue(getArcticTable().io().exists(path2));

    tableTrashManager.deleteRecursive(baseDir);
    Assert.assertFalse(getArcticTable().io().exists(baseDir));
    Assert.assertFalse(getArcticTable().io().exists(path));
    Assert.assertFalse(getArcticTable().io().exists(path2));
  }

  private String createFile(FileIO io, String path) {
    OutputFile baseOrphanDataFile = io.newOutputFile(path);
    baseOrphanDataFile.createOrOverwrite();
    return path;
  }
}