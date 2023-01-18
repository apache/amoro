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

package com.netease.arctic.io;

import com.netease.arctic.TableTestBase;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.data.DefaultKeyedFile;
import com.netease.arctic.io.writer.GenericBaseTaskWriter;
import com.netease.arctic.io.writer.GenericChangeTaskWriter;
import com.netease.arctic.io.writer.GenericTaskWriters;
import com.netease.arctic.io.writer.SortedPosDeleteWriter;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.WriteResult;
import org.junit.Before;

import java.io.IOException;
import java.util.Arrays;

import static com.netease.arctic.io.TestRecords.baseRecords;
import static com.netease.arctic.io.TestRecords.changeDeleteRecords;
import static com.netease.arctic.io.TestRecords.changeInsertRecords;

public class TableTestBaseWithInitData extends TableTestBase {

  protected DataFile dataFileForPositionDelete;

  protected DeleteFile deleteFileOfPositionDelete;

  @Before
  public void initData() throws IOException {

    //write base
    {
      GenericBaseTaskWriter writer = GenericTaskWriters.builderFor(testKeyedTable)
          .withTransactionId(1L).buildBaseWriter();

      for (Record record : baseRecords()) {
        writer.write(record);
      }
      WriteResult result = writer.complete();
      AppendFiles baseAppend = testKeyedTable.baseTable().newAppend();
      dataFileForPositionDelete = Arrays.stream(result.dataFiles())
          .filter(s -> s.path().toString().contains("op_time_day=2022-01-04")).findAny().get();
      Arrays.stream(result.dataFiles()).forEach(baseAppend::appendFile);
      baseAppend.commit();
    }

    // write position delete
    {
      SortedPosDeleteWriter<Record> writer = GenericTaskWriters.builderFor(testKeyedTable)
          .withTransactionId(4L).buildBasePosDeleteWriter(3, 3, dataFileForPositionDelete.partition());
      writer.delete(dataFileForPositionDelete.path().toString(), 0);
      DeleteFile posDeleteFiles = writer.complete().stream().findAny().get();
      this.deleteFileOfPositionDelete = posDeleteFiles;
      testKeyedTable.baseTable().newRowDelta().addDeletes(posDeleteFiles).commit();
    }

    //write change insert
    {
      GenericChangeTaskWriter writer = GenericTaskWriters.builderFor(testKeyedTable)
          .withTransactionId(2L).buildChangeWriter();
      for (Record record : changeInsertRecords()) {
        writer.write(record);
      }
      WriteResult result = writer.complete();
      AppendFiles changeAppend = testKeyedTable.changeTable().newAppend();
      Arrays.stream(result.dataFiles())
              .forEach(changeAppend::appendFile);
      changeAppend.commit();
    }

    //write change delete
    {
      GenericChangeTaskWriter writer = GenericTaskWriters.builderFor(testKeyedTable)
          .withTransactionId(3L).withChangeAction(ChangeAction.DELETE).buildChangeWriter();
      for (Record record : changeDeleteRecords()) {
        writer.write(record);
      }
      WriteResult result = writer.complete();
      AppendFiles changeAppend = testKeyedTable.changeTable().newAppend();
      Arrays.stream(result.dataFiles())
          .forEach(changeAppend::appendFile);
      changeAppend.commit();
    }
  }
}
