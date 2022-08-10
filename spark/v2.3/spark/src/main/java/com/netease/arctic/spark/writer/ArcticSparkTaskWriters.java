package com.netease.arctic.spark.writer;

import com.netease.arctic.io.writer.OutputFileFactory;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.types.StructType;

import java.util.Locale;

public class ArcticSparkTaskWriters {
  public static Builder buildFor(KeyedTable table) {
    return new Builder(table);
  }

  public static class Builder {
    private final KeyedTable table;
    private Long transactionId;
    private int partitionId = 0;
    private long taskId = 0;
    private StructType dsSchema;

    public Builder(KeyedTable table) {
      this.table = table;
    }

    public Builder withTransactionId(long transactionId) {
      this.transactionId = transactionId;
      return this;
    }

    public Builder withPartitionId(int partitionId) {
      this.partitionId = partitionId;
      return this;
    }

    public Builder withTaskId(long taskId) {
      this.taskId = taskId;
      return this;
    }

    public Builder withDataSourceSchema(StructType dsSchema) {
      this.dsSchema = dsSchema;
      return this;
    }

    public TaskWriter<InternalRow> buildBaseWriter() {
      Preconditions.checkNotNull(transactionId);
      FileFormat fileFormat = FileFormat.valueOf((table.properties().getOrDefault(
          TableProperties.BASE_FILE_FORMAT,
          TableProperties.BASE_FILE_FORMAT_DEFAULT).toUpperCase(Locale.ENGLISH)));
      long fileSizeBytes = PropertyUtil.propertyAsLong(table.properties(), TableProperties.WRITE_TARGET_FILE_SIZE_BYTES,
          TableProperties.WRITE_TARGET_FILE_SIZE_BYTES_DEFAULT);
      long mask = PropertyUtil.propertyAsLong(table.properties(), TableProperties.BASE_FILE_INDEX_HASH_BUCKET,
          TableProperties.BASE_FILE_INDEX_HASH_BUCKET_DEFAULT) - 1;
      FileAppenderFactory<InternalRow> appenderFactory =
          ArcticSparkInternalRowAppenderFactory.builderFor(table.baseTable(), table.baseTable().schema(), dsSchema)
              .build();
      OutputFileFactory outputFileFactory = new OutputFileFactory(
          table.baseLocation(), table.spec(), fileFormat, table.io(),
          table.baseTable().encryption(), partitionId, taskId, transactionId);
      return new ArcticSparkBaseTaskWriter(fileFormat, appenderFactory, outputFileFactory, table.io(), fileSizeBytes,
          mask, table.baseTable().schema(), table.spec(), table.primaryKeySpec());
    }
  }
}
