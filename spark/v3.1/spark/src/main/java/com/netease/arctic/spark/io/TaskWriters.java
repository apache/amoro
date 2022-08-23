package com.netease.arctic.spark.io;

import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.hive.write.AdaptHiveOutputFileFactory;
import com.netease.arctic.io.writer.CommonOutputFileFactory;
import com.netease.arctic.io.writer.OutputFileFactory;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.encryption.EncryptionManager;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.types.StructType;

import java.util.Locale;


public class TaskWriters {
  private ArcticTable table;
  private Long transactionId;
  private int partitionId = 0;
  private long taskId = 0;
  private StructType dsSchema;

  private boolean isHiveTable;
  private FileFormat fileFormat;
  private long fileSize;
  private long mask;

  protected TaskWriters(ArcticTable table) {
    this.table = table;
    this.isHiveTable = table instanceof SupportHive;

    this.fileFormat = FileFormat.valueOf((table.properties().getOrDefault(
        TableProperties.BASE_FILE_FORMAT,
        TableProperties.BASE_FILE_FORMAT_DEFAULT).toUpperCase(Locale.ENGLISH)));
    this.fileSize = PropertyUtil.propertyAsLong(table.properties(), TableProperties.WRITE_TARGET_FILE_SIZE_BYTES,
        TableProperties.WRITE_TARGET_FILE_SIZE_BYTES_DEFAULT);
    this.mask = PropertyUtil.propertyAsLong(table.properties(), TableProperties.BASE_FILE_INDEX_HASH_BUCKET,
        TableProperties.BASE_FILE_INDEX_HASH_BUCKET_DEFAULT) - 1;
  }

  public static TaskWriters of(ArcticTable table) {
    return new TaskWriters(table);
  }

  public TaskWriters withTransactionId(long transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public TaskWriters withPartitionId(int partitionId) {
    this.partitionId = partitionId;
    return this;
  }

  public TaskWriters withTaskId(long taskId) {
    this.taskId = taskId;
    return this;
  }

  public TaskWriters withDataSourceSchema(StructType dsSchema) {
    this.dsSchema = dsSchema;
    return this;
  }

  public TaskWriter<InternalRow> newBaseWriter(boolean isOverwrite) {
    preconditions();

    String baseLocation;
    EncryptionManager encryptionManager;
    Schema schema;
    PrimaryKeySpec primaryKeySpec = null;
    Table icebergTable;

    if (table.isKeyedTable()) {
      KeyedTable keyedTable = table.asKeyedTable();
      baseLocation = keyedTable.baseLocation();
      encryptionManager = keyedTable.baseTable().encryption();
      schema = keyedTable.baseTable().schema();
      primaryKeySpec = keyedTable.primaryKeySpec();
      icebergTable = keyedTable.baseTable();
    } else {
      UnkeyedTable table = this.table.asUnkeyedTable();
      baseLocation = table.location();
      encryptionManager = table.encryption();
      schema = table.schema();
      icebergTable = table;
    }

    FileAppenderFactory<InternalRow> appenderFactory = InternalRowFileAppenderFactory
        .builderFor(icebergTable, schema, dsSchema)
        .writeHive(isHiveTable)
        .build();

    OutputFileFactory outputFileFactory = null;
    if (isHiveTable && isOverwrite) {
      outputFileFactory = new AdaptHiveOutputFileFactory(
          ((SupportHive) table).hiveLocation(), table.spec(), fileFormat, table.io(),
          encryptionManager, partitionId, taskId, transactionId);
    } else {
      outputFileFactory = new CommonOutputFileFactory(
          baseLocation, table.spec(), fileFormat, table.io(),
          encryptionManager, partitionId, taskId, transactionId);
    }

    return new ArcticSparkBaseTaskWriter(fileFormat, appenderFactory,
        outputFileFactory,
        table.io(), fileSize, mask, schema, table.spec(), primaryKeySpec);
  }

  private void preconditions() {
    Preconditions.checkState(transactionId != null, "Transaction id is not set");
    Preconditions.checkState(partitionId >= 0, "Partition id is not set");
    Preconditions.checkState(taskId >= 0, "Task id is not set");
    Preconditions.checkState(dsSchema != null, "Data source schema is not set");
  }
}
