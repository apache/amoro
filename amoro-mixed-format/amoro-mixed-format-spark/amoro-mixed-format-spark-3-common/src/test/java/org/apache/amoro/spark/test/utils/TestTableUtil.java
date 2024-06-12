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

package org.apache.amoro.spark.test.utils;

import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.hive.io.reader.AdaptHiveGenericKeyedDataReader;
import org.apache.amoro.hive.io.reader.AdaptHiveGenericUnkeyedDataReader;
import org.apache.amoro.hive.table.SupportHive;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.io.reader.GenericUnkeyedDataReader;
import org.apache.amoro.io.writer.GenericTaskWriters;
import org.apache.amoro.scan.CombinedScanTask;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MetadataColumns;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.iceberg.types.Types;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.StructType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TestTableUtil {

  private static Object[] recordToObjects(Record record) {
    Object[] values = new Object[record.size()];
    for (int i = 0; i < values.length; i++) {
      Object v = record.get(i);
      if (v instanceof LocalDateTime) {
        Timestamp ts =
            Timestamp.valueOf(((LocalDateTime) v).atZone(ZoneOffset.UTC).toLocalDateTime());
        Timestamp tsUTC = Timestamp.valueOf((LocalDateTime) v);
        values[i] = ts;
        continue;
      } else if (v instanceof OffsetDateTime) {
        v = new Timestamp(((OffsetDateTime) v).toInstant().toEpochMilli());
      }
      values[i] = v;
    }
    return values;
  }

  public static Row recordToRow(Record record) {
    Object[] values = recordToObjects(record);
    return RowFactory.create(values);
  }

  public static InternalRow recordToInternalRow(Schema schema, Record record) {
    StructType structType = SparkSchemaUtil.convert(schema);
    Row row = recordToRow(record);
    return RowEncoder.apply(structType).createSerializer().apply(row);
  }

  public static Record rowToRecord(Row row, Types.StructType type) {
    Record record = GenericRecord.create(type);
    for (int i = 0; i < type.fields().size(); i++) {
      Object v = row.get(i);
      Types.NestedField field = type.fields().get(i);
      if (field.type().equals(Types.TimestampType.withZone())) {
        Preconditions.checkArgument(v instanceof Timestamp);
        Object offsetDateTime =
            ((Timestamp) v).toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        record.set(i, offsetDateTime);
        continue;
      } else if (field.type().equals(Types.TimestampType.withoutZone())) {
        Preconditions.checkArgument(v instanceof Timestamp);
        Object localDatetime = ((Timestamp) v).toLocalDateTime();
        record.set(i, localDatetime);
        continue;
      }
      record.set(i, v);
    }
    return record;
  }

  public static Schema toSchemaWithPrimaryKey(Schema schema, PrimaryKeySpec keySpec) {
    if (!keySpec.primaryKeyExisted()) {
      return schema;
    }
    Set<String> pks = Sets.newHashSet(keySpec.fieldNames());
    List<Types.NestedField> fields =
        schema.columns().stream()
            .map(
                f -> {
                  if (pks.contains(f.name())) {
                    return f.asRequired();
                  } else {
                    return f;
                  }
                })
            .collect(Collectors.toList());
    return new Schema(fields);
  }

  public static Schema timestampToWithoutZone(Schema schema) {
    List<Types.NestedField> fields =
        schema.columns().stream()
            .map(
                f -> {
                  if (f.type().equals(Types.TimestampType.withZone())) {
                    return Types.NestedField.of(
                        f.fieldId(),
                        f.isOptional(),
                        f.name(),
                        Types.TimestampType.withoutZone(),
                        f.doc());
                  } else {
                    return f;
                  }
                })
            .collect(Collectors.toList());
    return new Schema(fields);
  }

  public static TableFiles files(MixedTable table) {
    table.refresh();
    if (table.isUnkeyedTable()) {
      Pair<Set<DataFile>, Set<DeleteFile>> fileStatistic = icebergFiles(table.asUnkeyedTable());
      return new TableFiles(fileStatistic.getLeft(), fileStatistic.getRight());
    }

    return keyedFiles(table.asKeyedTable());
  }

  public static Pair<Set<DataFile>, Set<DeleteFile>> icebergFiles(Table table) {
    Set<DataFile> baseDataFiles = Sets.newHashSet();
    Set<DeleteFile> baseDeleteFiles = Sets.newHashSet();

    try (CloseableIterable<FileScanTask> it = table.newScan().planFiles()) {
      it.forEach(
          f -> {
            baseDataFiles.add(f.file());
            baseDeleteFiles.addAll(f.deletes());
          });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return ImmutablePair.of(baseDataFiles, baseDeleteFiles);
  }

  public static TableFiles keyedFiles(KeyedTable table) {
    Set<DataFile> baseDataFiles = Sets.newHashSet();
    Set<DeleteFile> baseDeleteFiles = Sets.newHashSet();
    Set<DataFile> insertFiles = Sets.newHashSet();
    Set<DataFile> deleteFiles = Sets.newHashSet();

    try (CloseableIterable<CombinedScanTask> it = table.newScan().planTasks()) {
      it.forEach(
          cst ->
              cst.tasks()
                  .forEach(
                      t -> {
                        t.baseTasks()
                            .forEach(
                                fileTask -> {
                                  baseDataFiles.add(fileTask.file());
                                  baseDeleteFiles.addAll(fileTask.deletes());
                                });
                        t.insertTasks().forEach(fileTask -> insertFiles.add(fileTask.file()));
                        t.mixedEquityDeletes()
                            .forEach(fileTask -> deleteFiles.add(fileTask.file()));
                      }));
      return new TableFiles(baseDataFiles, baseDeleteFiles, insertFiles, deleteFiles);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static List<Record> tableRecords(MixedTable table) {
    return tableRecords(table, Expressions.alwaysTrue());
  }

  public static List<Record> tableRecords(MixedTable table, Expression expression) {
    List<Record> records;
    table.refresh();
    if (table.isKeyedTable()) {
      if (table instanceof SupportHive) {
        records = readKeyedTable(table.asKeyedTable(), expression);
      } else {
        records = MixedDataTestHelpers.readKeyedTable(table.asKeyedTable(), expression);
      }
    } else {
      records = unkeyedTableRecords(table.asUnkeyedTable(), expression);
    }

    return records.stream()
        .map(
            r -> {
              if (r.struct().fields().size() == table.schema().columns().size()) {
                return r;
              }
              GenericRecord record = GenericRecord.create(table.schema());
              for (int i = 0; i < table.schema().columns().size(); i++) {
                record.set(i, r.get(i));
              }
              return record;
            })
        .collect(Collectors.toList());
  }

  public static List<Record> unkeyedTableRecords(UnkeyedTable table, Expression expression) {
    AdaptHiveGenericUnkeyedDataReader reader =
        new AdaptHiveGenericUnkeyedDataReader(
            table.io(),
            table.schema(),
            table.schema(),
            null,
            true,
            IdentityPartitionConverters::convertConstant,
            false);
    List<Record> result = Lists.newArrayList();
    try (CloseableIterable<org.apache.iceberg.CombinedScanTask> combinedScanTasks =
        table.newScan().filter(expression).planTasks()) {
      combinedScanTasks.forEach(
          combinedTask ->
              combinedTask
                  .tasks()
                  .forEach(
                      scTask -> {
                        try (CloseableIterator<Record> records =
                            reader.readData(scTask).iterator()) {
                          while (records.hasNext()) {
                            result.add(records.next());
                          }
                        } catch (IOException e) {
                          throw new UncheckedIOException(e);
                        }
                      }));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return result;
  }

  public static List<Record> readKeyedTable(KeyedTable keyedTable, Expression expression) {
    AdaptHiveGenericKeyedDataReader reader =
        new AdaptHiveGenericKeyedDataReader(
            keyedTable.io(),
            keyedTable.schema(),
            keyedTable.schema(),
            keyedTable.primaryKeySpec(),
            null,
            true,
            IdentityPartitionConverters::convertConstant);
    List<Record> result = Lists.newArrayList();
    try (CloseableIterable<CombinedScanTask> combinedScanTasks =
        keyedTable.newScan().filter(expression).planTasks()) {
      combinedScanTasks.forEach(
          combinedTask ->
              combinedTask
                  .tasks()
                  .forEach(
                      scTask -> {
                        try (CloseableIterator<Record> records = reader.readData(scTask)) {
                          while (records.hasNext()) {
                            result.add(records.next());
                          }
                        } catch (IOException e) {
                          throw new UncheckedIOException(e);
                        }
                      }));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return result;
  }

  public static List<DataFile> writeToBase(MixedTable table, List<Record> data) {
    TaskWriter<Record> baseWriter = null;
    UnkeyedTable baseTable = null;
    if (table.isKeyedTable()) {
      baseWriter =
          GenericTaskWriters.builderFor(table.asKeyedTable())
              .withTransactionId(
                  table.asKeyedTable().beginTransaction(System.currentTimeMillis() + ""))
              .buildBaseWriter();
      baseTable = table.asKeyedTable().baseTable();
    } else {
      baseWriter = GenericTaskWriters.builderFor(table.asUnkeyedTable()).buildBaseWriter();
      baseTable = table.asUnkeyedTable();
    }
    return writeToBase(baseTable, baseWriter, data);
  }

  public static List<DataFile> writeToBase(
      UnkeyedTable table, TaskWriter<Record> writer, List<Record> data) {
    try {
      data.forEach(
          row -> {
            try {
              writer.write(row);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });
      WriteResult result = writer.complete();
      AppendFiles appendFiles = table.newAppend();
      Arrays.stream(result.dataFiles()).forEach(appendFiles::appendFile);
      appendFiles.commit();
      return Lists.newArrayList(result.dataFiles());
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        writer.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static List<DataFile> writeToChange(
      KeyedTable table, List<Record> rows, ChangeAction action) {
    try (TaskWriter<Record> writer =
        GenericTaskWriters.builderFor(table).withChangeAction(action).buildChangeWriter()) {
      rows.forEach(
          row -> {
            try {
              writer.write(row);
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          });
      AppendFiles appendFiles = table.changeTable().newAppend();
      WriteResult result = writer.complete();
      Arrays.stream(result.dataFiles()).forEach(appendFiles::appendFile);
      appendFiles.commit();
      return Lists.newArrayList(result.dataFiles());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static List<Record> changeRecordsWithAction(KeyedTable keyedTable) {
    List<Types.NestedField> columns = Lists.newArrayList(keyedTable.schema().columns());
    columns.add(MetadataColumns.CHANGE_ACTION_FIELD);
    Schema expectSchema = new Schema(columns);

    GenericUnkeyedDataReader reader =
        new GenericUnkeyedDataReader(
            keyedTable.io(),
            keyedTable.schema(),
            expectSchema,
            null,
            true,
            IdentityPartitionConverters::convertConstant,
            false);
    List<Record> result = Lists.newArrayList();
    try (CloseableIterable<org.apache.iceberg.CombinedScanTask> combinedScanTasks =
        keyedTable.changeTable().newScan().planTasks()) {
      combinedScanTasks.forEach(
          combinedTask ->
              combinedTask
                  .tasks()
                  .forEach(
                      scTask -> {
                        try (CloseableIterator<Record> records =
                            reader.readData(scTask).iterator()) {
                          while (records.hasNext()) {
                            result.add(records.next());
                          }
                        } catch (IOException e) {
                          throw new UncheckedIOException(e);
                        }
                      }));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return result;
  }

  public static Record extendMetadataValue(
      Record record, Types.NestedField metaColumn, Object value) {
    List<Types.NestedField> columns = Lists.newArrayList(record.struct().fields());
    columns.add(metaColumn);
    Schema expectSchema = new Schema(columns);
    Record r = GenericRecord.create(expectSchema);
    for (int i = 0; i < columns.size() - 1; i++) {
      r.set(i, record.get(i));
    }
    r.set(columns.size() - 1, value);
    return r;
  }
}
