package com.netease.arctic.spark.writer;

import com.netease.arctic.op.RewritePartitions;
import com.netease.arctic.spark.source.SupportsDynamicOverwrite;
import com.netease.arctic.spark.source.SupportsOverwrite;
import com.netease.arctic.table.KeyedTable;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.apache.iceberg.spark.SparkFilters;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.iceberg.util.Tasks;
import org.apache.spark.TaskContext;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.sources.Filter;
import org.apache.spark.sql.sources.v2.writer.DataSourceWriter;
import org.apache.spark.sql.sources.v2.writer.DataWriter;
import org.apache.spark.sql.sources.v2.writer.DataWriterFactory;
import org.apache.spark.sql.sources.v2.writer.SupportsWriteInternalRow;
import org.apache.spark.sql.sources.v2.writer.WriterCommitMessage;
import org.apache.spark.sql.types.StructType;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import static org.apache.iceberg.TableProperties.COMMIT_MAX_RETRY_WAIT_MS;
import static org.apache.iceberg.TableProperties.COMMIT_MAX_RETRY_WAIT_MS_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_MIN_RETRY_WAIT_MS;
import static org.apache.iceberg.TableProperties.COMMIT_MIN_RETRY_WAIT_MS_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_NUM_RETRIES;
import static org.apache.iceberg.TableProperties.COMMIT_NUM_RETRIES_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_TOTAL_RETRY_TIME_MS;
import static org.apache.iceberg.TableProperties.COMMIT_TOTAL_RETRY_TIME_MS_DEFAULT;

public class ArcticOverwriteWriter implements DataSourceWriter, SupportsWriteInternalRow,
    SupportsOverwrite, SupportsDynamicOverwrite {

  private final KeyedTable table;
  private final StructType dsSchema;
  private final long transactionId;
  private final SaveMode mode;
  private boolean overwriteDynamic = false;
  private boolean overwriteByFilter = false;
  private Expression overwriteExpr = null;


  public ArcticOverwriteWriter(KeyedTable table, StructType dsSchema, SaveMode mode) {
    this.table = table;
    this.dsSchema = dsSchema;
    this.transactionId = table.beginTransaction(null);
    this.mode = mode;
  }

  @Override
  public DataSourceWriter overwriteDynamicPartitions() {
    Preconditions.checkState(!overwriteByFilter, "Cannot overwrite dynamically and by filter: %s", overwriteExpr);
    this.overwriteDynamic = true;
    return this;
  }

  @Override
  public DataSourceWriter overwrite(Filter[] filters) {
    Expression expression = Expressions.alwaysTrue();
    for (Filter filter : filters) {
      Expression converted = SparkFilters.convert(filter);
      Preconditions.checkArgument(converted != null, "Cannot convert filter to Iceberg: %s", filter);
      expression = Expressions.and(expression, converted);
    }
    this.overwriteExpr = expression;
    if (overwriteExpr == Expressions.alwaysTrue()) {
      // use the write option to override truncating the table. use dynamic overwrite instead.
      this.overwriteDynamic = true;
    } else {
      Preconditions.checkState(!overwriteDynamic, "Cannot overwrite dynamically and by filter: %s", overwriteExpr);
      this.overwriteByFilter = true;
    }
    return this;
  }

  @Override
  public DataWriterFactory<InternalRow> createInternalRowWriterFactory() {
    return new WriterFactory(table, dsSchema, transactionId);
  }

  private static class WriterFactory implements DataWriterFactory, Serializable {
    private final KeyedTable table;
    private final StructType dsSchema;
    private final long transactionId;

    WriterFactory(KeyedTable table, StructType dsSchema, long transactionId) {
      this.table = table;
      this.dsSchema = dsSchema;
      this.transactionId = transactionId;
    }

    @Override
    public DataWriter createDataWriter(int partitionId, int attemptNumber) {
      TaskWriter<InternalRow> writer = ArcticSparkTaskWriters.buildFor(table)
          .withTransactionId(transactionId)
          .withPartitionId(partitionId)
          .withTaskId(TaskContext.get().taskAttemptId())
          .withDataSourceSchema(dsSchema)
          .buildBaseWriter();

      return new SparkInternalRowWriter(writer);
    }
  }

  @Override
  public void commit(WriterCommitMessage[] messages) {
    RewritePartitions rewritePartitions = table.newRewritePartitions();
    rewritePartitions.withTransactionId(transactionId);
    for (DataFile file : files(messages)) {
      rewritePartitions.addDataFile(file);
    }
    rewritePartitions.commit();
  }

  @Override
  public void abort(WriterCommitMessage[] messages) {
    Map<String, String> props = table.properties();
    Tasks.foreach(files(messages))
        .retry(PropertyUtil.propertyAsInt(props, COMMIT_NUM_RETRIES, COMMIT_NUM_RETRIES_DEFAULT))
        .exponentialBackoff(
            PropertyUtil.propertyAsInt(props, COMMIT_MIN_RETRY_WAIT_MS, COMMIT_MIN_RETRY_WAIT_MS_DEFAULT),
            PropertyUtil.propertyAsInt(props, COMMIT_MAX_RETRY_WAIT_MS, COMMIT_MAX_RETRY_WAIT_MS_DEFAULT),
            PropertyUtil.propertyAsInt(props, COMMIT_TOTAL_RETRY_TIME_MS, COMMIT_TOTAL_RETRY_TIME_MS_DEFAULT),
            2.0 /* exponential */)
        .throwFailureWhenFinished()
        .run(file -> {
          table.io().deleteFile(file.path().toString());
        });
  }

  public static class TaskCommit implements WriterCommitMessage {
    private final DataFile[] taskFiles;

    TaskCommit(DataFile[] taskFiles) {
      this.taskFiles = taskFiles;
    }

    DataFile[] files() {
      return taskFiles;
    }
  }

  private static Iterable<DataFile> files(WriterCommitMessage[] messages) {
    if (messages.length > 0) {
      return Iterables.concat(Iterables.transform(Arrays.asList(messages), message -> message != null ?
          ImmutableList.copyOf(((TaskCommit) message).files()) :
          ImmutableList.of()));
    }
    return ImmutableList.of();
  }

}
