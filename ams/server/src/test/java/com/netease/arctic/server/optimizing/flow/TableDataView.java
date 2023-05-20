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
import com.google.common.collect.Lists;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.io.writer.GenericChangeTaskWriter;
import com.netease.arctic.io.writer.GenericTaskWriters;
import com.netease.arctic.io.writer.RecordWithAction;
import com.netease.arctic.table.ArcticTable;
import java.time.OffsetDateTime;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.BaseTaskWriter;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.ArrayUtil;
import org.apache.iceberg.util.StructLikeMap;
import org.apache.iceberg.util.StructLikeSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.netease.arctic.table.TableProperties.WRITE_TARGET_FILE_SIZE_BYTES;

public class TableDataView {

  private final Random random = new Random();

  private final ArcticTable arcticTable;

  private final Schema schema;

  private final Schema primary;

  private final int schemaSize;

  private final int primaryUpperBound;

  private final long targetFileSize;

  private final StructLikeMap<Record> view;

  private final List<RecordWithAction> changeLog = new ArrayList();

  private final RandomRecordGenerator generator;

  public TableDataView(
      ArcticTable arcticTable,
      Schema primary,
      int partitionCount,
      int primaryUpperBound,
      long targetFileSize) {
    this.arcticTable = arcticTable;
    this.schema = arcticTable.schema();
    this.primary = primary;
    this.schemaSize = schema.columns().size();
    this.primaryUpperBound = primaryUpperBound;

    this.targetFileSize = targetFileSize;
    if (arcticTable.format() != TableFormat.ICEBERG) {
      arcticTable.updateProperties().set(WRITE_TARGET_FILE_SIZE_BYTES, targetFileSize + "");
    }

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
    return custom(data);
  }

  public WriteResult custom(List<PKWithAction> data) throws IOException {
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
      if (viewRecord == null) {
        notInView.add(record);
      }
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
      Object o1 = r1.get(i);
      Object o2 = r2.get(i);
      boolean equals;
      if (o1 instanceof OffsetDateTime) {
        equals = ((OffsetDateTime) o1).isEqual((OffsetDateTime)o2);
      } else {
        equals = o1.equals(o2);
      }
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
      changeLog.add(record);
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
      return writeUnKeyedTable(records);
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
    GenericTaskDeltaWriter deltaWriter = createTaskWriter(
        primary.columns().stream().map(Types.NestedField::fieldId).collect(Collectors.toList()),
        schema,
        arcticTable.asUnkeyedTable(),
        FileFormat.PARQUET,
        OutputFileFactory.builderFor(arcticTable.asUnkeyedTable(),
            1,
            1).format(FileFormat.PARQUET).build()
    );
    for (RecordWithAction record: records) {
      if (record.getAction() == ChangeAction.DELETE || record.getAction() == ChangeAction.UPDATE_BEFORE) {
        deltaWriter.delete(record);
      } else {
        deltaWriter.write(record);
      }
    }
    return deltaWriter.complete();
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

    protected final boolean alreadyExists(Record record) {
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

  private GenericTaskDeltaWriter createTaskWriter(
      List<Integer> equalityFieldIds,
      Schema eqDeleteRowSchema,
      Table table,
      FileFormat format,
      OutputFileFactory fileFactory) {
    FileAppenderFactory<Record> appenderFactory =
        new GenericAppenderFactory(
            table.schema(),
            table.spec(),
            ArrayUtil.toIntArray(equalityFieldIds),
            eqDeleteRowSchema,
            null);

    List<String> columns = Lists.newArrayList();
    for (Integer fieldId : equalityFieldIds) {
      columns.add(table.schema().findField(fieldId).name());
    }
    Schema deleteSchema = table.schema().select(columns);

    PartitionKey partitionKey = new PartitionKey(table.spec(), schema);

    return new GenericTaskDeltaWriter(
        table.schema(),
        deleteSchema,
        table.spec(),
        format,
        appenderFactory,
        fileFactory,
        table.io(),
        partitionKey,
        targetFileSize);
  }

  private static class GenericTaskDeltaWriter extends BaseTaskWriter<Record> {
    private final GenericEqualityDeltaWriter deltaWriter;

    private GenericTaskDeltaWriter(
        Schema schema,
        Schema deleteSchema,
        PartitionSpec spec,
        FileFormat format,
        FileAppenderFactory<Record> appenderFactory,
        OutputFileFactory fileFactory,
        FileIO io,
        PartitionKey partitionKey,
        long targetFileSize) {
      super(spec, format, appenderFactory, fileFactory, io, targetFileSize);
      this.deltaWriter = new GenericEqualityDeltaWriter(partitionKey, schema, deleteSchema);
    }

    @Override
    public void write(Record row) throws IOException {
      deltaWriter.write(row);
    }

    public void delete(Record row) throws IOException {
      deltaWriter.delete(row);
    }

    // The caller of this function is responsible for passing in a record with only the key fields
    public void deleteKey(Record key) throws IOException {
      deltaWriter.deleteKey(key);
    }

    @Override
    public void close() throws IOException {
      deltaWriter.close();
    }

    private class GenericEqualityDeltaWriter extends BaseEqualityDeltaWriter {
      private GenericEqualityDeltaWriter(
          PartitionKey partition, Schema schema, Schema eqDeleteSchema) {
        super(partition, schema, eqDeleteSchema);
      }

      @Override
      protected StructLike asStructLike(Record row) {
        return row;
      }

      @Override
      protected StructLike asStructLikeKey(Record data) {
        return data;
      }
    }
  }
}
