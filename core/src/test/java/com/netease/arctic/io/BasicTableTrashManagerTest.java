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
import java.time.LocalDate;
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
    tableTrashManager.moveFileToTrash(path);
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
    IllegalArgumentException illegalArgumentException =
        Assert.assertThrows("should not successfully move a directory to trash",
            IllegalArgumentException.class, () -> tableTrashManager.moveFileToTrash(directory));
    Assert.assertTrue(illegalArgumentException.getMessage().contains("directory"));
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
    tableTrashManager.moveFileToTrash(path);
    String fileLocationInTrash = BasicTableTrashManager.generateFileLocationInTrash(
        getArcticTable().location(), path, trashLocation, now);

    Assert.assertFalse(getArcticTable().io().exists(path));
    Assert.assertTrue(getArcticTable().io().exists(fileLocationInTrash));

    IllegalArgumentException illegalArgumentException =
        Assert.assertThrows("should not successfully check a directory in trash",
            IllegalArgumentException.class, () -> tableTrashManager.fileExistInTrash(directory));
    Assert.assertTrue(illegalArgumentException.getMessage().contains("directory"));

    illegalArgumentException =
        Assert.assertThrows("should not successfully restore a directory in trash",
            IllegalArgumentException.class, () -> tableTrashManager.restoreFileFromTrash(directory));
    Assert.assertTrue(illegalArgumentException.getMessage().contains("directory"));

    Assert.assertFalse(getArcticTable().io().exists(path));
    Assert.assertTrue(getArcticTable().io().exists(fileLocationInTrash));
  }

  @Test
  public void testCleanFiles() {
    BasicTableTrashManager tableTrashManager = ((BasicTableTrashManager) TableTrashManagers.of(getArcticTable()));
    String trashLocation =
        TableTrashManagers.getTrashLocation(getArcticTable().id(), getArcticTable().location(), null);
    String file1 = getArcticTable().location() + File.separator + "base/test/test1.parquet";
    String file2 = getArcticTable().location() + File.separator + "base/test/test2.parquet";
    String file3 = getArcticTable().location() + File.separator + "base/test3/test3.parquet";
    String file4 = getArcticTable().location() + File.separator + "base/test/test4.parquet";
    String file5 = getArcticTable().location() + File.separator + "base/test/test5.parquet";
    String illegalFile = trashLocation + File.separator + "/000/base/test/test6.parquet";
    long day1 = LocalDateTime.of(2023, 2, 20, 1, 1)
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    long day2 = LocalDateTime.of(2023, 2, 21, 1, 1)
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    long day3 = LocalDateTime.of(2023, 2, 22, 1, 1)
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

    String file1Day1 =
        BasicTableTrashManager.generateFileLocationInTrash(getArcticTable().location(), file1, trashLocation, day1);
    String file2Day1 =
        BasicTableTrashManager.generateFileLocationInTrash(getArcticTable().location(), file2, trashLocation, day1);
    String file3Day1 =
        BasicTableTrashManager.generateFileLocationInTrash(getArcticTable().location(), file3, trashLocation, day1);
    String file4Day2 =
        BasicTableTrashManager.generateFileLocationInTrash(getArcticTable().location(), file4, trashLocation, day2);
    String file5Day3 =
        BasicTableTrashManager.generateFileLocationInTrash(getArcticTable().location(), file5, trashLocation, day3);
    createFile(getArcticTable().io(), file1Day1);
    createFile(getArcticTable().io(), file2Day1);
    createFile(getArcticTable().io(), file3Day1);
    createFile(getArcticTable().io(), file4Day2);
    createFile(getArcticTable().io(), file5Day3);
    createFile(getArcticTable().io(), illegalFile);

    Assert.assertTrue(tableTrashManager.fileExistInTrash(file1));
    Assert.assertTrue(tableTrashManager.fileExistInTrash(file2));
    Assert.assertTrue(tableTrashManager.fileExistInTrash(file3));
    Assert.assertTrue(tableTrashManager.fileExistInTrash(file4));
    Assert.assertTrue(tableTrashManager.fileExistInTrash(file5));
    Assert.assertFalse(tableTrashManager.fileExistInTrash(illegalFile));

    tableTrashManager.cleanFiles(LocalDate.of(2023, 2, 22));

    Assert.assertFalse(tableTrashManager.fileExistInTrash(file1));
    Assert.assertFalse(tableTrashManager.fileExistInTrash(file2));
    Assert.assertFalse(tableTrashManager.fileExistInTrash(file3));
    Assert.assertFalse(tableTrashManager.fileExistInTrash(file4));
    Assert.assertTrue(tableTrashManager.fileExistInTrash(file5));
    Assert.assertFalse(tableTrashManager.fileExistInTrash(illegalFile));

    Assert.assertFalse(getArcticTable().io().exists(file1Day1));
    Assert.assertFalse(getArcticTable().io().exists(file2Day1));
    Assert.assertFalse(getArcticTable().io().exists(file3Day1));
    Assert.assertFalse(getArcticTable().io().exists(file4Day2));
    Assert.assertTrue(getArcticTable().io().exists(file5Day3));
    Assert.assertTrue(getArcticTable().io().exists(illegalFile));
  }

  private String createFile(FileIO io, String path) {
    OutputFile baseOrphanDataFile = io.newOutputFile(path);
    baseOrphanDataFile.createOrOverwrite();
    return path;
  }
}