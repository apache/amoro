package com.netease.arctic.spark.writer;

import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.io.writer.OutputFileFactory;
import com.netease.arctic.io.writer.TaskWriterKey;
import com.netease.arctic.spark.SparkInternalRowCastWrapper;
import com.netease.arctic.spark.io.ArcticSparkBaseTaskWriter;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.deletes.PositionDeleteWriter;
import org.apache.iceberg.encryption.EncryptedOutputFile;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.types.Comparators;
import org.apache.iceberg.util.CharSequenceSet;
import org.apache.iceberg.util.CharSequenceWrapper;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.spark.sql.catalyst.InternalRow;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.apache.spark.sql.types.DataTypes.IntegerType;
import static org.apache.spark.sql.types.DataTypes.StringType;

public class UnkeyedPosDeleteSparkWriter<T> implements TaskWriter<T> {

  private static final long DEFAULT_RECORDS_NUM_THRESHOLD = Long.MAX_VALUE;

  private final Map<CharSequenceWrapper, List<UnkeyedPosDeleteSparkWriter.PosRow<InternalRow>>> posDeletes = Maps.newHashMap();
  private final List<DeleteFile> completedDeleteFiles = Lists.newArrayList();
  private final List<DataFile> completedDataFiles = Lists.newArrayList();
  private final CharSequenceSet referencedDataFiles = CharSequenceSet.empty();
  private final CharSequenceWrapper wrapper = CharSequenceWrapper.wrap(null);

  private final FileAppenderFactory<InternalRow> appenderFactory;
  private final OutputFileFactory fileFactory;
  private final FileFormat format;
  private final TaskWriterKey writerKey;
  private final long recordsNumThreshold;
  private final Schema schema;
  private final ArcticTable table;

  private int records = 0;
  public UnkeyedPosDeleteSparkWriter(ArcticTable table,
                                     FileAppenderFactory<InternalRow> appenderFactory,
                                     OutputFileFactory fileFactory,
                                     FileFormat format,
                                     long mask, long index,
                                     StructLike partitionKey,
                                     long recordsNumThreshold, Schema schema) {
    this.table = table;
    this.appenderFactory = appenderFactory;
    this.fileFactory = fileFactory;
    this.format = format;
    this.writerKey = new TaskWriterKey(partitionKey, DataTreeNode.of(mask, index), DataFileType.POS_DELETE_FILE);
    this.recordsNumThreshold = recordsNumThreshold;
    this.schema = schema;
  }

  public UnkeyedPosDeleteSparkWriter(ArcticTable table, FileAppenderFactory<InternalRow> appenderFactory,
                               OutputFileFactory fileFactory,
                               FileFormat format,
                               long mask, long index,
                               StructLike partitionKey, Schema schema) {
    this(table, appenderFactory, fileFactory, format, mask, index, partitionKey, DEFAULT_RECORDS_NUM_THRESHOLD, schema);
  }

  @Override
  public void write(T row) throws IOException {
    SparkInternalRowCastWrapper internalRow = (SparkInternalRowCastWrapper) row;
    if (internalRow.getChangeAction() == ChangeAction.DELETE) {
      int numFields = internalRow.getRow().numFields();
      Object file = internalRow.getRow().get(numFields - 2, StringType);
      Object pos = internalRow.getRow().get(numFields - 1, IntegerType);
      delete(file.toString(), Long.parseLong(pos.toString()), null);
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
    flushDeletes();
  }

  public void delete(CharSequence path, long pos, InternalRow row) {
    List<UnkeyedPosDeleteSparkWriter.PosRow<InternalRow>> posRows = posDeletes.get(wrapper.set(path));
    if (posRows != null) {
      posRows.add(UnkeyedPosDeleteSparkWriter.PosRow.of(pos, row));
    } else {
      posDeletes.put(CharSequenceWrapper.wrap(path), Lists.newArrayList(UnkeyedPosDeleteSparkWriter.PosRow.of(pos, row)));
    }

    records += 1;

    // TODO Flush buffer based on the policy that checking whether whole heap memory size exceed the threshold.
    if (records >= recordsNumThreshold) {
      flushDeletes();
    }
  }

  private void flushDeletes() {
    if (posDeletes.isEmpty()) {
      return;
    }

    // Create a new output file.
    EncryptedOutputFile outputFile;
    outputFile = fileFactory.newOutputFile(writerKey);

    PositionDeleteWriter<InternalRow> writer = appenderFactory
        .newPosDeleteWriter(outputFile, format, writerKey.getPartitionKey());
    try (PositionDeleteWriter<InternalRow> closeableWriter = writer) {
      // Sort all the paths.
      List<CharSequence> paths = Lists.newArrayListWithCapacity(posDeletes.keySet().size());
      for (CharSequenceWrapper charSequenceWrapper : posDeletes.keySet()) {
        paths.add(charSequenceWrapper.get());
      }
      paths.sort(Comparators.charSequences());

      // Write all the sorted <path, pos, row> triples.
      for (CharSequence path : paths) {
        List<UnkeyedPosDeleteSparkWriter.PosRow<InternalRow>> positions = posDeletes.get(wrapper.set(path));
        positions.sort(Comparator.comparingLong(UnkeyedPosDeleteSparkWriter.PosRow::pos));

        positions.forEach(posRow -> closeableWriter.delete(path, posRow.pos(), posRow.row()));
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to write the sorted path/pos pairs to pos-delete file: " +
          outputFile.encryptingOutputFile().location(), e);
    }

    // Clear the buffered pos-deletions.
    posDeletes.clear();
    records = 0;

    // Add the referenced data files.
    referencedDataFiles.addAll(writer.referencedDataFiles());

    // Add the completed delete files.
    completedDeleteFiles.add(writer.toDeleteFile());
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
