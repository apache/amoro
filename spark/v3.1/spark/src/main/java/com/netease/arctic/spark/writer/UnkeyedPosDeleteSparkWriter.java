package com.netease.arctic.spark.writer;

import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.io.writer.OutputFileFactory;
import com.netease.arctic.io.writer.SortedPosDeleteWriter;
import com.netease.arctic.io.writer.TaskWriterKey;
import com.netease.arctic.spark.SparkInternalRowWrapper;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.deletes.PositionDeleteWriter;
import org.apache.iceberg.encryption.EncryptedOutputFile;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.iceberg.types.Comparators;
import org.apache.iceberg.util.CharSequenceSet;
import org.apache.iceberg.util.CharSequenceWrapper;
import org.apache.spark.sql.catalyst.InternalRow;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class UnkeyedPosDeleteSparkWriter<T> implements TaskWriter<T> {

  private static final long DEFAULT_RECORDS_NUM_THRESHOLD = Long.MAX_VALUE;

  private final Map<CharSequenceWrapper, List<UnkeyedPosDeleteSparkWriter.PosRow<T>>> posDeletes = Maps.newHashMap();
  private final List<DeleteFile> completedFiles = Lists.newArrayList();
  private final CharSequenceSet referencedDataFiles = CharSequenceSet.empty();
  private final CharSequenceWrapper wrapper = CharSequenceWrapper.wrap(null);

  private final FileAppenderFactory<T> appenderFactory;
  private final OutputFileFactory fileFactory;
  private final FileFormat format;
  private final TaskWriterKey writerKey;
  private final long recordsNumThreshold;
  private final Schema schema;

  private int records = 0;
  public UnkeyedPosDeleteSparkWriter(FileAppenderFactory<T> appenderFactory,
                               OutputFileFactory fileFactory,
                               FileFormat format,
                               long mask, long index,
                               StructLike partitionKey,
                               long recordsNumThreshold, Schema schema) {
    this.appenderFactory = appenderFactory;
    this.fileFactory = fileFactory;
    this.format = format;
    this.writerKey = new TaskWriterKey(partitionKey, DataTreeNode.of(mask, index), DataFileType.POS_DELETE_FILE);
    this.recordsNumThreshold = recordsNumThreshold;
    this.schema = schema;
  }

  public UnkeyedPosDeleteSparkWriter(FileAppenderFactory<T> appenderFactory,
                               OutputFileFactory fileFactory,
                               FileFormat format,
                               long mask, long index,
                               StructLike partitionKey, Schema schema) {
    this(appenderFactory, fileFactory, format, mask, index, partitionKey, DEFAULT_RECORDS_NUM_THRESHOLD, schema);
  }

  @Override
  public void write(T row) throws IOException {
    StructLike structLike = asStructLike(row);
    // TODO: 2022/8/29
  }

  private StructLike asStructLike(T row) {
    return new SparkInternalRowWrapper(SparkSchemaUtil.convert(schema)).wrap((InternalRow) row);
  }

  @Override
  public void abort() throws IOException {

  }

  @Override
  public WriteResult complete() throws IOException {
    return null;
  }

  @Override
  public void close() throws IOException {

  }

  public void delete(CharSequence path, long pos, T row) {
    List<UnkeyedPosDeleteSparkWriter.PosRow<T>> posRows = posDeletes.get(wrapper.set(path));
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

    PositionDeleteWriter<T> writer = appenderFactory
        .newPosDeleteWriter(outputFile, format, writerKey.getPartitionKey());
    try (PositionDeleteWriter<T> closeableWriter = writer) {
      // Sort all the paths.
      List<CharSequence> paths = Lists.newArrayListWithCapacity(posDeletes.keySet().size());
      for (CharSequenceWrapper charSequenceWrapper : posDeletes.keySet()) {
        paths.add(charSequenceWrapper.get());
      }
      paths.sort(Comparators.charSequences());

      // Write all the sorted <path, pos, row> triples.
      for (CharSequence path : paths) {
        List<UnkeyedPosDeleteSparkWriter.PosRow<T>> positions = posDeletes.get(wrapper.set(path));
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
    completedFiles.add(writer.toDeleteFile());
  }

  private static class PosRow<R> {
    private final long pos;
    private final R row;

    static <R> UnkeyedPosDeleteSparkWriter.PosRow<R> of(long pos, R row) {
      return new UnkeyedPosDeleteSparkWriter.PosRow<>(pos, row);
    }

    private PosRow(long pos, R row) {
      this.pos = pos;
      this.row = row;
    }

    long pos() {
      return pos;
    }

    R row() {
      return row;
    }
  }
}
