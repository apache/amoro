package com.netease.arctic;

import com.netease.arctic.iceberg.InternalRecordWrapper;
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
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.deletes.EqualityDeleteWriter;
import org.apache.iceberg.deletes.PositionDelete;
import org.apache.iceberg.deletes.PositionDeleteWriter;
import org.apache.iceberg.encryption.EncryptedOutputFile;
import org.apache.iceberg.io.DataWriter;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Multimap;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.ArrayUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IcebergTableTestHelper {

  public static DataFile writeNewDataFile(Table table, List<Record> records, StructLike partitionData)
      throws IOException {
    GenericAppenderFactory appenderFactory = new GenericAppenderFactory(table.schema(), table.spec());
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(table, table.spec().specId(), 1)
            .build();
    EncryptedOutputFile outputFile = outputFileFactory.newOutputFile();

    DataWriter<Record> writer = appenderFactory
        .newDataWriter(outputFile, FileFormat.PARQUET, partitionData);

    for (Record record : records) {
      writer.write(record);
    }
    writer.close();
    return writer.toDataFile();
  }

  public static DeleteFile writeEqDeleteFile(Table table, List<Record> records, StructLike partitionData)
      throws IOException {
    List<Integer> equalityFieldIds = Lists.newArrayList(table.schema().findField("id").fieldId());
    Schema eqDeleteRowSchema = table.schema().select("id");
    GenericAppenderFactory appenderFactory =
        new GenericAppenderFactory(table.schema(), table.spec(),
            ArrayUtil.toIntArray(equalityFieldIds), eqDeleteRowSchema, null);
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(table, table.spec().specId(), 1)
            .build();
    EncryptedOutputFile outputFile = outputFileFactory.newOutputFile(table.spec(), partitionData);

    EqualityDeleteWriter<Record> writer = appenderFactory
        .newEqDeleteWriter(outputFile, FileFormat.PARQUET, partitionData);

    for (Record record : records) {
      writer.write(record);
    }
    writer.close();
    return writer.toDeleteFile();
  }

  public static DeleteFile writePosDeleteFile(
      Table table, Multimap<String, Long> file2Positions,
      StructLike partitionData) throws IOException {
    GenericAppenderFactory appenderFactory =
        new GenericAppenderFactory(table.schema(), table.spec());
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(table, table.spec().specId(), 1)
            .build();
    EncryptedOutputFile outputFile = outputFileFactory.newOutputFile(table.spec(), partitionData);

    PositionDeleteWriter<Record> writer = appenderFactory
        .newPosDeleteWriter(outputFile, FileFormat.PARQUET, partitionData);
    for (Map.Entry<String, Collection<Long>> entry : file2Positions.asMap().entrySet()) {
      String filePath = entry.getKey();
      Collection<Long> positions = entry.getValue();
      for (Long position : positions) {
        PositionDelete<Record> positionDelete = PositionDelete.create();
        positionDelete.set(filePath, position, null);
        writer.write(positionDelete);
      }
    }
    writer.close();
    return writer.toDeleteFile();
  }

  public static void rowDelta(Table table, List<Record> insertRecords, List<Record> deleteRecords,
      StructLike partitionData)
      throws IOException {
    DataFile dataFile = writeNewDataFile(table, insertRecords, partitionData);

    DeleteFile deleteFile = writeEqDeleteFile(table, deleteRecords, partitionData);
    RowDelta rowDelta = table.newRowDelta();
    rowDelta.addRows(dataFile);
    rowDelta.addDeletes(deleteFile);
    rowDelta.validateFromSnapshot(table.currentSnapshot().snapshotId());
    rowDelta.commit();
  }

  public static StructLike partitionData(Schema tableSchema, PartitionSpec spec, Object... partitionValues) {
    GenericRecord record = GenericRecord.create(tableSchema);
    int index = 0;
    Set<Integer> partitionField = Sets.newHashSet();
    spec.fields().forEach(f -> partitionField.add(f.sourceId()));
    List<Types.NestedField> tableFields = tableSchema.columns();
    for (int i = 0; i < tableFields.size(); i++) {
      // String sourceColumnName = tableSchema.findColumnName(i);
      Types.NestedField sourceColumn = tableFields.get(i);
      if (partitionField.contains(sourceColumn.fieldId())) {
        Object partitionVal = partitionValues[index];
        index++;
        record.set(i, partitionVal);
      } else {
        record.set(i, 0);
      }
    }

    PartitionKey pd = new PartitionKey(spec, tableSchema);
    InternalRecordWrapper wrapper = new InternalRecordWrapper(tableSchema.asStruct());
    wrapper = wrapper.wrap(record);
    pd.partition(wrapper);
    return pd;
  }
}
