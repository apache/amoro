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

import static org.apache.amoro.io.BasicTableTrashManager.generateFileLocationInTrash;
import static org.apache.amoro.io.BasicTableTrashManager.getRelativeFileLocation;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.utils.TableFileUtil;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.OutputFile;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RunWith(Parameterized.class)
public class TestBasicTableTrashManager extends TableTestBase {

  public TestBasicTableTrashManager(boolean keyedTable, boolean partitionedTable) {
    super(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(keyedTable, partitionedTable));
  }

  @Parameterized.Parameters(name = "keyedTable = {0}, partitionedTable = {1}")
  public static Object[][] parameters() {
    return new Object[][] {
      {true, true},
      {true, false},
      {false, true},
      {false, false}
    };
  }

  @Rule public TemporaryFolder tempTrashLocation = new TemporaryFolder();

  @Test
  public void testGenerateFileLocationInTrash() {
    String relativeFileLocation = getRelativeFileLocation("/tmp/table", "/tmp/table/change/file1");
    Assert.assertEquals("change/file1", relativeFileLocation);
    relativeFileLocation = getRelativeFileLocation("/tmp/table/", "/tmp/table/change/file1");
    Assert.assertEquals("change/file1", relativeFileLocation);
    relativeFileLocation =
        getRelativeFileLocation(
            "hdfs://hz11-trino-arctic-0.jd.163.org:8020/user/warehouse/",
            "/user/warehouse/change/file1");
    Assert.assertEquals("change/file1", relativeFileLocation);

    LocalDateTime localDateTime = LocalDateTime.of(2023, 2, 2, 1, 1);
    long toEpochMilli = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    String locationInTrash =
        generateFileLocationInTrash("change/file1", "/tmp/table/.trash", toEpochMilli);
    Assert.assertEquals("/tmp/table/.trash/20230202/change/file1", locationInTrash);
  }

  @Test
  public void testDeleteAndRestore() throws IOException {
    String tableRootLocation = getMixedTable().location();
    TableTrashManager tableTrashManager = build();
    String trashLocation = tableTrashManager.getTrashLocation();

    String relativeFilePath = "base/test/test1.parquet";
    String path =
        createFile(getMixedTable().io(), fullLocation(tableRootLocation, relativeFilePath));

    Assert.assertFalse(tableTrashManager.fileExistInTrash(path));
    Assert.assertFalse(tableTrashManager.restoreFileFromTrash(path));

    long now = System.currentTimeMillis();
    tableTrashManager.moveFileToTrash(path);
    String fileLocationInTrash = generateFileLocationInTrash(relativeFilePath, trashLocation, now);

    Assert.assertFalse(getMixedTable().io().exists(path));
    Assert.assertTrue(getMixedTable().io().exists(fileLocationInTrash));

    Assert.assertTrue(tableTrashManager.fileExistInTrash(path));
    Assert.assertTrue(tableTrashManager.restoreFileFromTrash(path));

    Assert.assertTrue(getMixedTable().io().exists(path));
    Assert.assertFalse(getMixedTable().io().exists(fileLocationInTrash));
  }

  @Test
  public void testMoveAndOverwrite() throws IOException {
    String tableRootLocation = getMixedTable().location();
    TableTrashManager tableTrashManager = build();

    String relativeFilePath = "base/test/test1.parquet";
    String path =
        createFile(getMixedTable().io(), fullLocation(tableRootLocation, relativeFilePath));

    tableTrashManager.moveFileToTrash(path);
    Assert.assertTrue(tableTrashManager.fileExistInTrash(path));
    createFile(getMixedTable().io(), fullLocation(tableRootLocation, relativeFilePath));
    tableTrashManager.moveFileToTrash(path);
    Assert.assertTrue(tableTrashManager.fileExistInTrash(path));
  }

  @Test
  public void testDeleteDirectory() throws IOException {
    String tableRootLocation = getMixedTable().location();
    TableTrashManager tableTrashManager = build();
    String trashLocation = tableTrashManager.getTrashLocation();
    String relativeFilePath = "base/test/test1.parquet";
    String path =
        createFile(getMixedTable().io(), fullLocation(tableRootLocation, relativeFilePath));

    String directory = TableFileUtil.getFileDir(path);
    long now = System.currentTimeMillis();
    IllegalArgumentException illegalArgumentException =
        Assert.assertThrows(
            "should not successfully move a directory to trash",
            IllegalArgumentException.class,
            () -> tableTrashManager.moveFileToTrash(directory));
    Assert.assertTrue(illegalArgumentException.getMessage().contains("directory"));
    String relativeDirectory = getRelativeFileLocation(tableRootLocation, directory);
    String directoryLocationInTrash =
        generateFileLocationInTrash(relativeDirectory, trashLocation, now);

    Assert.assertTrue(getMixedTable().io().exists(directory));
    Assert.assertFalse(getMixedTable().io().exists(directoryLocationInTrash));
  }

  @Test
  public void testRestoreDirectory() throws IOException {
    String tableRootLocation = getMixedTable().location();
    TableTrashManager tableTrashManager = build();
    String trashLocation = tableTrashManager.getTrashLocation();
    String relativeFilePath = "base/test/test1.parquet";
    String path =
        createFile(getMixedTable().io(), fullLocation(tableRootLocation, relativeFilePath));

    long now = System.currentTimeMillis();
    tableTrashManager.moveFileToTrash(path);
    String fileLocationInTrash = generateFileLocationInTrash(relativeFilePath, trashLocation, now);

    Assert.assertFalse(getMixedTable().io().exists(path));
    Assert.assertTrue(getMixedTable().io().exists(fileLocationInTrash));

    Assert.assertFalse(getMixedTable().io().exists(path));
    Assert.assertTrue(getMixedTable().io().exists(fileLocationInTrash));
  }

  @Test
  public void testCleanFiles() throws IOException {
    String tableRootLocation = getMixedTable().location();
    BasicTableTrashManager tableTrashManager = ((BasicTableTrashManager) build());
    String trashLocation = tableTrashManager.getTrashLocation();
    String file1 = fullLocation(tableRootLocation, "base/test/test1.parquet");
    String file2 = fullLocation(tableRootLocation, "base/test/test2.parquet");
    String file3 = fullLocation(tableRootLocation, "base/test3/test3.parquet");
    String file4 = fullLocation(tableRootLocation, "base/test/test4.parquet");
    String file5 = fullLocation(tableRootLocation, "base/test/test5.parquet");
    String illegalFile = trashLocation + "/000/base/test/test6.parquet";
    long day1 =
        LocalDateTime.of(2023, 2, 20, 1, 1)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli();
    long day2 =
        LocalDateTime.of(2023, 2, 21, 1, 1)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli();
    long day3 =
        LocalDateTime.of(2023, 2, 22, 1, 1)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli();

    String file1Day1 =
        generateFileLocationInTrash(
            getRelativeFileLocation(tableRootLocation, file1), trashLocation, day1);
    String file2Day1 =
        generateFileLocationInTrash(
            getRelativeFileLocation(tableRootLocation, file2), trashLocation, day1);
    String file3Day1 =
        generateFileLocationInTrash(
            getRelativeFileLocation(tableRootLocation, file3), trashLocation, day1);
    String file4Day2 =
        generateFileLocationInTrash(
            getRelativeFileLocation(tableRootLocation, file4), trashLocation, day2);
    String file5Day3 =
        generateFileLocationInTrash(
            getRelativeFileLocation(tableRootLocation, file5), trashLocation, day3);
    createFile(getMixedTable().io(), file1Day1);
    createFile(getMixedTable().io(), file2Day1);
    createFile(getMixedTable().io(), file3Day1);
    createFile(getMixedTable().io(), file4Day2);
    createFile(getMixedTable().io(), file5Day3);
    createFile(getMixedTable().io(), illegalFile);

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

    Assert.assertFalse(getMixedTable().io().exists(file1Day1));
    Assert.assertFalse(getMixedTable().io().exists(file2Day1));
    Assert.assertFalse(getMixedTable().io().exists(file3Day1));
    Assert.assertFalse(getMixedTable().io().exists(file4Day2));
    Assert.assertTrue(getMixedTable().io().exists(file5Day3));
    Assert.assertTrue(getMixedTable().io().exists(illegalFile));
  }

  @Test
  public void testDeleteTrashLocation() throws IOException {
    String tableRootLocation = getMixedTable().location();
    String customTrashLocation = tempTrashLocation.newFolder().getPath().replace('\\', '/');

    getMixedTable()
        .updateProperties()
        .set(TableProperties.TABLE_TRASH_CUSTOM_ROOT_LOCATION, customTrashLocation)
        .commit();
    String file1 = fullLocation(tableRootLocation, "base/test/test1.parquet");
    createFile(getMixedTable().io(), file1);
    TableTrashManager tableTrashManager = build();
    tableTrashManager.moveFileToTrash(file1);
    Assert.assertTrue(tableTrashManager.fileExistInTrash(file1));
    String trashParentLocation =
        TableTrashManagers.getTrashParentLocation(getMixedTable().id(), customTrashLocation);
    getMixedFormatCatalog().dropTable(getMixedTable().id(), true);
    Assert.assertFalse(getMixedTable().io().exists(trashParentLocation));
    Assert.assertFalse(tableTrashManager.fileExistInTrash(file1));
  }

  private TableTrashManager build() {
    MixedTable table = getMixedTable();
    Assert.assertTrue(table.io() instanceof AuthenticatedHadoopFileIO);
    return TableTrashManagers.build(
        table.id(),
        getMixedTable().location(),
        table.properties(),
        (AuthenticatedHadoopFileIO) table.io());
  }

  private String createFile(FileIO io, String path) throws IOException {
    OutputFile baseOrphanDataFile = io.newOutputFile(path);
    baseOrphanDataFile.createOrOverwrite().close();
    return path;
  }

  private String fullLocation(String dir, String relativeLocation) {
    if (!dir.endsWith("/")) {
      dir = dir + "/";
    }
    return dir + relativeLocation;
  }
}
