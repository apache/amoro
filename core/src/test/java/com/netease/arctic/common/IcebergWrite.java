package com.netease.arctic.common;

import com.google.common.collect.Lists;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.io.writer.RecordWithAction;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.InternalRecordWrapper;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.BaseTaskWriter;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.ArrayUtil;

public class IcebergWrite {

    private final Schema primary;

    private final Table table;

    private final Schema schema;

    private final long targetFileSize;

    public IcebergWrite(Schema primary, Table table, long targetFileSize) {
        this.primary = primary;
        this.table = table;
        this.targetFileSize = targetFileSize;
        this.schema = table.schema();
    }

    public WriteResult write(List<RecordWithAction> records) throws IOException {
        Schema eqDeleteSchema = primary == null ? schema : primary;
        GenericTaskDeltaWriter deltaWriter = createTaskWriter(
                eqDeleteSchema
                        .columns()
                        .stream()
                        .map(Types.NestedField::fieldId).collect(Collectors.toList()),
                schema,
                table,
                FileFormat.PARQUET,
                OutputFileFactory.builderFor(
                        table,
                        1,
                        1).format(FileFormat.PARQUET).build()
        );
        for (RecordWithAction record : records) {
            if (record.getAction() == ChangeAction.DELETE || record.getAction() == ChangeAction.UPDATE_BEFORE) {
                deltaWriter.delete(record);
            } else {
                deltaWriter.write(record);
            }
        }
        return deltaWriter.complete();
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

            private final InternalRecordWrapper dataWrapper;
            private final InternalRecordWrapper keyWrapper;

            private GenericEqualityDeltaWriter(
                    PartitionKey partition, Schema schema, Schema eqDeleteSchema) {
                super(partition, schema, eqDeleteSchema);
                this.dataWrapper = new InternalRecordWrapper(schema.asStruct());
                this.keyWrapper = new InternalRecordWrapper(eqDeleteSchema.asStruct());
            }

            @Override
            protected StructLike asStructLike(Record row) {
               return dataWrapper.copyFor(row);
            }

            @Override
            protected StructLike asStructLikeKey(Record data) {
                return keyWrapper.copyFor(data);
            }
        }
    }
}
