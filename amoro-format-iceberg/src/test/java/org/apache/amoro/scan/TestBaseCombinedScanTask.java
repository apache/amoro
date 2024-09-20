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

package org.apache.amoro.scan;

import org.apache.amoro.IcebergFileEntry;
import org.apache.amoro.data.DefaultKeyedFile;
import org.apache.amoro.utils.ManifestEntryFields;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.PartitionSpec;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class TestBaseCombinedScanTask {

  @Test
  public void testToString() {
    IcebergFileEntry entry =
        new IcebergFileEntry(
            1L,
            2L,
            ManifestEntryFields.Status.ADDED,
            DataFiles.builder(PartitionSpec.unpartitioned())
                .withPath("/tmp/1-I-2-0-0-9009257362994691056-2.parquet")
                .withFileSizeInBytes(10L)
                .withRecordCount(100L)
                .build());
    DefaultKeyedFile keyedFile = DefaultKeyedFile.parseChange((DataFile) entry.getFile());
    BasicMixedFileScanTask task =
        new BasicMixedFileScanTask(keyedFile, null, PartitionSpec.unpartitioned(), null);

    BaseCombinedScanTask baseCombinedScanTask =
        new BaseCombinedScanTask(new NodeFileScanTask(Collections.singletonList(task)));
    String expected =
        "BaseCombinedScanTask{\n"
            + "tasks=NodeFileScanTask{\n"
            + "\tbaseTasks=[], \n"
            + "\tinsertTasks=[DefaultKeyedFile{\n"
            + "\t\tfile=/tmp/1-I-2-0-0-9009257362994691056-2.parquet, \n"
            + "\t\ttype=I, \n"
            + "\t\tmask=0, \n"
            + "\t\tindex=0, \n"
            + "\t\ttransactionId=2, \n"
            + "\t\tfileSizeInBytes=10, \n"
            + "\t\trecordCount=100}], \n"
            + "\tdeleteFiles=[]}}";
    Assert.assertEquals(expected, baseCombinedScanTask.toString());
  }
}
