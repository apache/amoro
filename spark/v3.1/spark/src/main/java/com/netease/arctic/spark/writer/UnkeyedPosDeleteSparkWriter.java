package com.netease.arctic.spark.writer;

import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.io.writer.OutputFileFactory;
import com.netease.arctic.io.writer.SortedPosDeleteWriter;
import com.netease.arctic.spark.SparkInternalRowCastWrapper;
import com.netease.arctic.spark.SparkInternalRowWrapper;
import com.netease.arctic.spark.io.ArcticSparkBaseTaskWriter;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.spark.sql.catalyst.InternalRow;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.apache.spark.sql.types.DataTypes.IntegerType;
import static org.apache.spark.sql.types.DataTypes.StringType;

public class UnkeyedPosDeleteSparkWriter<T> implements TaskWriter<T> {

  private static final long DEFAULT_RECORDS_NUM_THRESHOLD = Long.MAX_VALUE;
  private final List<DeleteFile> completedDeleteFiles = Lists.newArrayList();
  private final List<DataFile> completedDataFiles = Lists.newArrayList();

  private final FileAppenderFactory<InternalRow> appenderFactory;
  private final OutputFileFactory fileFactory;
  private final FileFormat format;
  private final long recordsNumThreshold;
  private final Schema schema;
  private final ArcticTable table;
  private WriteResult writeResult;

  private int records = 0;

  public UnkeyedPosDeleteSparkWriter(ArcticTable table,
                                     FileAppenderFactory<InternalRow> appenderFactory,
                                     OutputFileFactory fileFactory,
                                     FileFormat format,
                                     long recordsNumThreshold, Schema schema) {
    this.table = table;
    this.appenderFactory = appenderFactory;
    this.fileFactory = fileFactory;
    this.format = format;
    this.recordsNumThreshold = recordsNumThreshold;
    this.schema = schema;
  }

  public UnkeyedPosDeleteSparkWriter(ArcticTable table, FileAppenderFactory<InternalRow> appenderFactory,
                               OutputFileFactory fileFactory,
                               FileFormat format,
                               Schema schema) {
    this(table, appenderFactory, fileFactory, format, DEFAULT_RECORDS_NUM_THRESHOLD, schema);
  }

  @Override
  public void write(T row) throws IOException {
    SparkInternalRowCastWrapper internalRow = (SparkInternalRowCastWrapper) row;
    StructLike structLike = new SparkInternalRowWrapper(SparkSchemaUtil.convert(schema)).wrap(internalRow.getRow());
    PartitionKey partitionKey = new PartitionKey(table.spec(), schema);
    partitionKey.partition(structLike);
    SortedPosDeleteWriter<InternalRow> writer = new SortedPosDeleteWriter<>(appenderFactory,
        fileFactory,
        format, partitionKey);
    if (internalRow.getChangeAction() == ChangeAction.DELETE) {
      int numFields = internalRow.getRow().numFields();
      Object file = internalRow.getRow().get(numFields - 2, StringType);
      Object pos = internalRow.getRow().get(numFields - 1, IntegerType);
      writer.delete(file.toString(), Long.parseLong(pos.toString()), null);
      completedDeleteFiles.addAll(writer.complete());
    } else {
      long fileSizeBytes = PropertyUtil.propertyAsLong(table.properties(), TableProperties.WRITE_TARGET_FILE_SIZE_BYTES,
          TableProperties.WRITE_TARGET_FILE_SIZE_BYTES_DEFAULT);
      long mask = PropertyUtil.propertyAsLong(table.properties(), TableProperties.BASE_FILE_INDEX_HASH_BUCKET,
          TableProperties.BASE_FILE_INDEX_HASH_BUCKET_DEFAULT) - 1;
      ArcticSparkBaseTaskWriter arcticSparkBaseTaskWriter = new ArcticSparkBaseTaskWriter(format, appenderFactory,
          fileFactory,
          table.io(), fileSizeBytes, mask, schema, table.spec(), null);
      arcticSparkBaseTaskWriter.write(internalRow.getRow());
      completedDataFiles.addAll(Arrays.asList(arcticSparkBaseTaskWriter.complete().dataFiles()));
    }
  }

  @Override
  public void abort() throws IOException {

  }

  @Override
  public WriteResult complete() throws IOException {
    close();

    return WriteResult.builder()
        .addDeleteFiles(completedDeleteFiles)
        .addDataFiles(completedDataFiles).build();
  }

  @Override
  public void close() throws IOException {
  }

  private static class PosRow<R> {
    private final long pos;
    private final InternalRow row;

    static <R> UnkeyedPosDeleteSparkWriter.PosRow<InternalRow> of(long pos, InternalRow row) {
      return new UnkeyedPosDeleteSparkWriter.PosRow<>(pos, row);
    }

    private PosRow(long pos, InternalRow row) {
      this.pos = pos;
      this.row = row;
    }

    long pos() {
      return pos;
    }

    InternalRow row() {
      return row;
    }
  }
}
