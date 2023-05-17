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

package com.netease.arctic.server.optimizing;

import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.iceberg.StructProjection;
import com.netease.arctic.io.writer.GenericChangeTaskWriter;
import com.netease.arctic.io.writer.GenericTaskWriters;
import com.netease.arctic.io.writer.RecordWithAction;
import com.netease.arctic.table.ArcticTable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.relocated.com.google.common.collect.Collections2;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.util.StructLikeMap;

public class TableDataView {

  private ArcticTable arcticTable;

  private Schema schema;

  private int schemaSize;

  private Schema primary;

  private int primaryUpperBound;

  private StructLikeMap<Record> view;

  private RandomRecordGenerator generator;

  public TableDataView(
      ArcticTable arcticTable,
      Schema primary,
      int partitionCount,
      int primaryUpperBound) {
    this.arcticTable = arcticTable;
    this.schema = arcticTable.schema();
    this.schemaSize = schema.columns().size();
    this.primary = primary;
    this.primaryUpperBound = primaryUpperBound;
    this.view = StructLikeMap.create(primary.asStruct());
    this.generator = new RandomRecordGenerator(arcticTable.schema(), arcticTable.spec(), primary, partitionCount);
  }

  public WriteResult upsert(int count) throws IOException {
    Random random = new Random();
    int[] ids = new int[count];
    for (int i = 0; i < count; i++) {
      ids[i] = random.nextInt(primaryUpperBound);
    }
    List<Record> scatter = generator.scatter(ids);
    List<RecordWithAction> upsert = new ArrayList<>();
    for (Record record: scatter) {
      upsert.add(new RecordWithAction(record, ChangeAction.DELETE));
      upsert.add(new RecordWithAction(record, ChangeAction.INSERT));
    }
    writeView(upsert);
    WriteResult writer = writeFile(upsert);
    upsertCommit(writer);
    return writer;
  }

  public int getSize() {
    return view.size();
  }

  public List<Record> notInIntersection(List<Record> records) {
    if ((view.size() == 0 && CollectionUtils.isEmpty(records))) {
      return Collections.emptyList();
    }

    List<Record> notInView = new ArrayList<>();
    List<Record> intersection = new ArrayList<>();
    for (Record record: records) {
      Record viewRecord = view.get(record);
      if (equRecord(viewRecord, record)) {
        intersection.add(record);
      } else {
        notInView.add(record);
      }
    }

    if (intersection.size() == view.size()) {
      return notInView;
    }
    return null;

    // for (Record intersectionRecord: intersection) {
    //
    // }
  }

  private boolean equRecord(Record r1, Record r2) {
    if (r2.size() < schemaSize) {
      return false;
    }
    for (int i = 0; i < schemaSize; i++) {
      boolean equals = r1.get(i).equals(r2.get(i));
      if (!equals) {
        return false;
      }
    }
    return true;
  }

  private void upsertCommit(WriteResult writeResult) {
    AppendFiles appendFiles = arcticTable.asKeyedTable().changeTable().newAppend();
    for (DataFile dataFile: writeResult.dataFiles()) {
      appendFiles.appendFile(dataFile);
    }
    appendFiles.commit();
  }

  private void writeView(List<RecordWithAction> records) {
    for (RecordWithAction record: records) {
      ChangeAction action = record.getAction();
      if (action == ChangeAction.DELETE || action == ChangeAction.UPDATE_BEFORE) {
        view.remove(record);
      } else {
        if (view.containsKey(record)) {
          throw new RuntimeException("You write duplicate pk");
        }
        view.put(record, record);
      }
    }
  }

  private WriteResult writeFile(List<RecordWithAction> records) throws IOException {
    GenericChangeTaskWriter writer = GenericTaskWriters.builderFor(arcticTable.asKeyedTable())
        .withTransactionId(0L)
        .buildChangeWriter();
    try {
      for (Record record: records) {
        writer.write(record);
      }
    }finally {
      writer.close();
    }
    return writer.complete();
  }
}
