package com.netease.arctic.spark.test.helper;

import com.netease.arctic.hive.io.reader.AdaptHiveGenericArcticDataReader;
import com.netease.arctic.hive.io.reader.GenericAdaptHiveIcebergDataReader;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.io.DataTestHelpers;
import com.netease.arctic.io.reader.GenericArcticDataReader;
import com.netease.arctic.scan.CombinedScanTask;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.IcebergGenerics;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.types.Types;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TestTableHelper {

  public static Row recordToRow(Record record) {
    Object[] values = new Object[record.size()];
    for (int i = 0; i < values.length; i++) {
      Object v = record.get(i);
      if (v instanceof LocalDateTime) {
        Timestamp ts = Timestamp.valueOf(((LocalDateTime) v).atZone(ZoneOffset.UTC).toLocalDateTime());
        Timestamp tsUTC = Timestamp.valueOf((LocalDateTime) v);
        values[i] = ts;
        continue;
      } else if (v instanceof OffsetDateTime) {
        v = new Timestamp(((OffsetDateTime) v).toInstant().toEpochMilli());
      }
      values[i] = v;
    }
    return RowFactory.create(values);
  }

  public static Schema toSchemaWithPrimaryKey(Schema schema, PrimaryKeySpec keySpec) {
    if (!keySpec.primaryKeyExisted()) {
      return schema;
    }
    Set<String> pks = Sets.newHashSet(keySpec.fieldNames());
    List<Types.NestedField> fields = schema.columns().stream().map(
        f -> {
          if (pks.contains(f.name())) {
            return f.asRequired();
          } else {
            return f;
          }
        }
    ).collect(Collectors.toList());
    return new Schema(fields);
  }

  public static Schema timestampToWithoutZone(Schema schema) {
    List<Types.NestedField> fields = schema.columns().stream().map(
        f -> {
          if (f.type().equals(Types.TimestampType.withZone())) {
            return Types.NestedField.of(f.fieldId(), f.isOptional(), f.name(),
                Types.TimestampType.withoutZone(), f.doc());
          } else {
            return f;
          }
        }
    ).collect(Collectors.toList());
    return new Schema(fields);
  }


  public static TableFiles files(ArcticTable table) {
    if (table.isUnkeyedTable()) {
      Pair<Set<DataFile>, Set<DeleteFile>> fStatistic = icebergFiles(table.asUnkeyedTable());
      return new TableFiles(fStatistic.getLeft(), fStatistic.getRight());
    }

    return keyedFiles(table.asKeyedTable());
  }

  public static Pair<Set<DataFile>, Set<DeleteFile>> icebergFiles(Table table) {
    Set<DataFile> baseDataFiles = Sets.newHashSet();
    Set<DeleteFile> baseDeleteFiles = Sets.newHashSet();

    try (CloseableIterable<FileScanTask> it = table.newScan().planFiles()) {
      it.forEach(f -> {
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
      it.forEach(cst -> cst.tasks().forEach(
          t -> {
            t.baseTasks().forEach(fTask -> {
              baseDataFiles.add(fTask.file());
              baseDeleteFiles.addAll(fTask.deletes());
            });
            t.insertTasks().forEach(fTask -> insertFiles.add(fTask.file()));
            t.arcticEquityDeletes().forEach(fTask -> deleteFiles.add(fTask.file()));
          }
      ));
      return new TableFiles(baseDataFiles, baseDeleteFiles, insertFiles, deleteFiles);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static List<Record> tableRecords(ArcticTable table) {
    return tableRecords(table, Expressions.alwaysTrue());
  }

  public static List<Record> tableRecords(ArcticTable table, Expression expression) {
    if (table.isKeyedTable()) {
      if (table instanceof SupportHive) {
        return readKeyedTable(table.asKeyedTable(), expression);
      } else {
        return DataTestHelpers.readKeyedTable(table.asKeyedTable(), expression);
      }
    }
//    CloseableIterable<Record> it = IcebergGenerics.read(table.asUnkeyedTable())
//        .where(expression)
//        .build();
//    List<Record> records = Lists.newArrayList();
//    it.forEach(records::add);
    return unkeyedTableRecords(table.asUnkeyedTable(), expression);
  }

  public static List<Record> unkeyedTableRecords(UnkeyedTable table, Expression expression) {
    GenericAdaptHiveIcebergDataReader reader = new GenericAdaptHiveIcebergDataReader(
        table.io(),
        table.schema(),
        table.schema(),
        null,
        true,
        IdentityPartitionConverters::convertConstant, false
    );
    List<Record> result = Lists.newArrayList();
    try (CloseableIterable<org.apache.iceberg.CombinedScanTask> combinedScanTasks = table.newScan().filter(expression).planTasks()) {
      combinedScanTasks.forEach(combinedTask -> combinedTask.tasks().forEach(scTask -> {
        try (CloseableIterator<Record> records = reader.readData(scTask).iterator()) {
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
    AdaptHiveGenericArcticDataReader reader = new AdaptHiveGenericArcticDataReader(
        keyedTable.io(),
        keyedTable.schema(),
        keyedTable.schema(),
        keyedTable.primaryKeySpec(),
        null,
        true,
        IdentityPartitionConverters::convertConstant
    );
    List<Record> result = Lists.newArrayList();
    try (CloseableIterable<CombinedScanTask> combinedScanTasks = keyedTable.newScan().filter(expression).planTasks()) {
      combinedScanTasks.forEach(combinedTask -> combinedTask.tasks().forEach(scTask -> {
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
}
