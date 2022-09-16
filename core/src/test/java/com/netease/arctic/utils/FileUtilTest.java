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

package com.netease.arctic.utils;

import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.DefaultKeyedFile;
import org.junit.Assert;
import org.junit.Test;

public class FileUtilTest {

  @Test
  public void getFileName() {
    String fileName = FileUtil.getFileName("hdfs://easyops-sloth/user/warehouse/animal_partition_two/base/" +
        "opt_mon=202109/opt_day=26/00000-0-3-1-37128f07-0845-43d8-905b-bd69b4ca351c-0000000001.parquet");
    Assert.assertEquals("00000-0-3-1-37128f07-0845-43d8-905b-bd69b4ca351c-0000000001.parquet", fileName);
  }

  @Test
  public void getFileDir() {
    String fileDir = FileUtil.getFileDir("hdfs://easyops-sloth/user/warehouse/animal_partition_two/base/" +
        "opt_mon=202109/opt_day=26/00000-0-3-1-37128f07-0845-43d8-905b-bd69b4ca351c-0000000001.parquet");
    Assert.assertEquals("hdfs://easyops-sloth/user/warehouse/animal_partition_two/base/opt_mon=202109/opt_day=26", fileDir);
  }

  @Test
  public void testGetKeyedFileMetaFromFileName() {
    String fileName =
        "hdfs://easyops-sloth/user/warehouse/animal_partition_two/base/5-I-2-00000-941953957-0000000001.parquet";
    DefaultKeyedFile.FileMeta fileMeta = FileUtil.parseFileMetaFromFileName(fileName);
    Assert.assertEquals(DataFileType.INSERT_FILE, fileMeta.type());
    Assert.assertEquals(DataTreeNode.of(3,1), fileMeta.node());
    Assert.assertEquals(2, fileMeta.transactionId());

    Assert.assertEquals(DataFileType.INSERT_FILE, FileUtil.parseFileTypeFromFileName(fileName));
    Assert.assertEquals(DataTreeNode.of(3,1), FileUtil.parseFileNodeFromFileName(fileName));
    Assert.assertEquals(2, FileUtil.parseFileTidFromFileName(fileName));
  }

  @Test
  public void testGetUnKeyedFileMetaFromFileName() {
    String fileName =
        "hdfs://easyops-sloth/user/warehouse/animal_partition_two/base/1-I-0-00000-941953957-37128f07-0845-43d8-905b-bd69b4ca351c-0000000001.parquet";
    DefaultKeyedFile.FileMeta fileMeta = FileUtil.parseFileMetaFromFileName(fileName);
    Assert.assertEquals(DataFileType.INSERT_FILE, fileMeta.type());
    Assert.assertEquals(DataTreeNode.of(0,0), fileMeta.node());
    Assert.assertEquals(0, fileMeta.transactionId());

    Assert.assertEquals(DataFileType.INSERT_FILE, FileUtil.parseFileTypeFromFileName(fileName));
    Assert.assertEquals(DataTreeNode.of(0,0), FileUtil.parseFileNodeFromFileName(fileName));
    Assert.assertEquals(0, FileUtil.parseFileTidFromFileName(fileName));
  }

}