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

package com.netease.arctic.server.optimizing.flow;

import com.google.common.base.Objects;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.io.writer.GenericChangeTaskWriter;
import com.netease.arctic.io.writer.GenericTaskWriters;
import com.netease.arctic.io.writer.RecordWithAction;
import com.netease.arctic.table.ArcticTable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.util.StructLikeMap;
import org.apache.iceberg.util.StructLikeSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TableDataView {

  private final Random random = new Random();

  private final ArcticTable arcticTable;

  private final Schema schema;

  private final int schemaSize;

  private final int primaryUpperBound;

  private final StructLikeMap<Record> view;

  private final RandomRecordGenerator generator;

  public TableDataView(
      ArcticTable arcticTable,
      Schema primary,
      int partitionCount,
      int primaryUpperBound) {
    this.arcticTable = arcticTable;
    this.schema = arcticTable.schema();
    this.schemaSize = schema.columns().size();
    this.primaryUpperBound = primaryUpperBound;
    this.view = StructLikeMap.create(primary.asStruct());
    this.generator = new RandomRecordGenerator(arcticTable.schema(), arcticTable.spec(), primary, partitionCount);
  }

  public WriteResult upsert(int count) throws IOException {
    List<Record> scatter = randomRecord(count);
    List<RecordWithAction> upsert = new ArrayList<>();
    for (Record record : scatter) {
      upsert.add(new RecordWithAction(record, ChangeAction.DELETE));
      upsert.add(new RecordWithAction(record, ChangeAction.INSERT));
    }
    return doWrite(upsert);
  }

  public WriteResult cdc(int count) throws IOException {
    List<Record> scatter = randomRecord(count);
    List<RecordWithAction> cdc = new ArrayList<>();
    for (Record record : scatter) {
      if (view.containsKey(record)) {
        if (random.nextBoolean()) {
          //delete
          cdc.add(new RecordWithAction(view.get(record), ChangeAction.DELETE));
        } else {
          // update
          cdc.add(new RecordWithAction(view.get(record), ChangeAction.UPDATE_BEFORE));
          cdc.add(new RecordWithAction(record, ChangeAction.UPDATE_AFTER));
        }
      } else {
        cdc.add(new RecordWithAction(record, ChangeAction.DELETE));
      }
    }
    return doWrite(cdc);
  }

  public WriteResult onlyDelete(int count) throws IOException {
    List<Record> scatter = randomRecord(count);
    List<RecordWithAction> delete =
        scatter.stream().map(s -> new RecordWithAction(s, ChangeAction.DELETE)).collect(Collectors.toList());
    return doWrite(delete);
  }

  public WriteResult custom(CustomData customData) throws IOException {
    customData.accept(view);
    List<PKWithAction> data = customData.data();
    List<RecordWithAction> records = new ArrayList<>();
    for (PKWithAction pkWithAction: data) {
      records.add(new RecordWithAction(generator.randomRecord(pkWithAction.pk), pkWithAction.action));
    }
    return doWrite(records);
  }

  public int getSize() {
    return view.size();
  }

  public MatchResult match(List<Record> records) {
    if ((view.size() == 0 && CollectionUtils.isEmpty(records))) {
      return MatchResult.ok();
    }

    List<Record> notInView = new ArrayList<>();
    List<Record> inViewButDuplicate = new ArrayList<>();
    StructLikeSet intersection = StructLikeSet.create(schema.asStruct());
    for (Record record : records) {
      Record viewRecord = view.get(record);
      if (equRecord(viewRecord, record)) {
        if (intersection.contains(record)) {
          inViewButDuplicate.add(record);
        } else {
          intersection.add(record);
        }
      } else {
        notInView.add(record);
      }
    }

    if (intersection.size() == view.size()) {
      return MatchResult.of(notInView, inViewButDuplicate, null);
    }

    List<Record> missInView = new ArrayList<>();
    for (Record viewRecord : view.values()) {
      if (!intersection.contains(viewRecord)) {
        missInView.add(viewRecord);
      }
    }
    return new MatchResult(notInView, inViewButDuplicate, missInView);
  }

  private WriteResult doWrite(List<RecordWithAction> upsert) throws IOException {
    writeView(upsert);
    WriteResult writeResult = writeFile(upsert);
    upsertCommit(writeResult);
    return writeResult;
  }

  private List<Record> randomRecord(int count) {
    int[] ids = new int[count];
    for (int i = 0; i < count; i++) {
      ids[i] = random.nextInt(primaryUpperBound);
    }
    return generator.scatter(ids);
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
    if (arcticTable.isKeyedTable()) {
      AppendFiles appendFiles = arcticTable.asKeyedTable().changeTable().newAppend();
      for (DataFile dataFile : writeResult.dataFiles()) {
        appendFiles.appendFile(dataFile);
      }
      appendFiles.commit();
    } else {
      RowDelta rowDelta = arcticTable.asUnkeyedTable().newRowDelta();
      for (DataFile dataFile: writeResult.dataFiles()) {
        rowDelta.addRows(dataFile);
      }
      for (DeleteFile deleteFile: writeResult.deleteFiles()) {
        rowDelta.addDeletes(deleteFile);
      }
      rowDelta.commit();
    }
  }

  private void writeView(List<RecordWithAction> records) {
    for (RecordWithAction record : records) {
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
    if (arcticTable.isKeyedTable()) {
      return writeKeyedTable(records);
    } else {
      return null;
    }
  }

  private WriteResult writeKeyedTable(List<RecordWithAction> records) throws IOException {
    GenericChangeTaskWriter writer = GenericTaskWriters.builderFor(arcticTable.asKeyedTable())
        .withTransactionId(0L)
        .buildChangeWriter();
    try {
      for (Record record : records) {
        writer.write(record);
      }
    } finally {
      writer.close();
    }
    return writer.complete();
  }

  private WriteResult writeUnKeyedTable(List<RecordWithAction> records) throws IOException {
    GenericChangeTaskWriter writer = GenericTaskWriters.builderFor(arcticTable.asKeyedTable())
        .withTransactionId(0L)
        .buildChangeWriter();
    try {
      for (Record record : records) {
        writer.write(record);
      }
    } finally {
      writer.close();
    }
    return writer.complete();
  }

  public static class PKWithAction {
    private final int pk;

    private final ChangeAction action;

    public PKWithAction(int pk, ChangeAction action) {
      this.pk = pk;
      this.action = action;
    }
  }

  public abstract static class CustomData {

    private StructLikeMap<Record> view;

    public abstract  List<PKWithAction> data();

    private void accept(StructLikeMap<Record> view) {
      this.view = view;
    }

    protected boolean alreadyExists(Record record) {
      return view.containsKey(record);
    }

  }

  public static class MatchResult {

    private final List<Record> notInView;

    private final List<Record> inViewButDuplicate;

    private final List<Record> missInView;

    private MatchResult(List<Record> notInView, List<Record> inViewButDuplicate, List<Record> missInView) {
      this.notInView = notInView;
      this.inViewButDuplicate = inViewButDuplicate;
      this.missInView = missInView;
    }

    public static MatchResult of(List<Record> notInView, List<Record> inViewButDuplicate, List<Record> missInView) {
      return new MatchResult(notInView, inViewButDuplicate, missInView);
    }

    public static MatchResult ok() {
      return new MatchResult(null, null, null);
    }

    public boolean isOk() {
      return CollectionUtils.isEmpty(notInView) &&
          CollectionUtils.isEmpty(inViewButDuplicate) &&
          CollectionUtils.isEmpty(missInView);
    }

    @Override
    public String toString() {
      return Objects.toStringHelper(this)
          .add("notInView", notInView)
          .add("inViewButDuplicate", inViewButDuplicate)
          .add("missInView", missInView)
          .toString();
    }
  }
}
