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

package com.netease.arctic.spark.writer;

import com.netease.arctic.hive.table.HiveLocationKind;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.hive.utils.HiveTableUtil;
import com.netease.arctic.hive.write.AdaptHiveOperateToTableRelation;
import com.netease.arctic.hive.write.AdaptHiveOutputFileFactory;
import com.netease.arctic.io.writer.CommonOutputFileFactory;
import com.netease.arctic.io.writer.OutputFileFactory;
import com.netease.arctic.io.writer.SortedPosDeleteWriter;
import com.netease.arctic.io.writer.TaskWriterBuilder;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BaseLocationKind;
import com.netease.arctic.table.ChangeLocationKind;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.LocationKind;
import com.netease.arctic.table.WriteOperationKind;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.encryption.EncryptionManager;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.types.StructType;

import java.util.Locale;

public class ArcticSparkTaskWriterBuilder implements TaskWriterBuilder<InternalRow> {

  private final ArcticTable table;
  private Long transactionId;
  private int partitionId = 0;
  private long taskId = 0;
  private StructType dsSchema;

  private ArcticSparkTaskWriterBuilder(ArcticTable table) {
    this.table = table;
  }

  public ArcticSparkTaskWriterBuilder withTransactionId(long transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public ArcticSparkTaskWriterBuilder withPartitionId(int partitionId) {
    this.partitionId = partitionId;
    return this;
  }

  public ArcticSparkTaskWriterBuilder withTaskId(long taskId) {
    this.taskId = taskId;
    return this;
  }

  public ArcticSparkTaskWriterBuilder withDataSourceSchema(StructType dsSchema) {
    this.dsSchema = dsSchema;
    return this;
  }

  @Override
  public TaskWriter<InternalRow> buildWriter(WriteOperationKind writeOperationKind) {
    LocationKind locationKind = AdaptHiveOperateToTableRelation.INSTANT.getLocationKindsFromOperateKind(
        table,
        writeOperationKind);
    return buildWriter(locationKind);
  }

  @Override
  public TaskWriter<InternalRow> buildWriter(LocationKind locationKind) {
    if (locationKind == ChangeLocationKind.INSTANT) {
      return buildChangeWriter();
    } else if (locationKind == BaseLocationKind.INSTANT || locationKind == HiveLocationKind.INSTANT) {
      return buildBaseWriter(locationKind);
    } else {
      throw new IllegalArgumentException("Not support Location Kind:" + locationKind);
    }
  }

  public SortedPosDeleteWriter<InternalRow> buildBasePosDeleteWriter(long mask, long index, StructLike partitionKey) {
    throw new UnsupportedOperationException("UnSupport position delete on spark");
  }

  private ArcticSparkBaseTaskWriter buildBaseWriter(LocationKind locationKind) {
    Preconditions.checkNotNull(transactionId);
    FileFormat fileFormat = FileFormat.valueOf((table.properties().getOrDefault(
        TableProperties.BASE_FILE_FORMAT,
        TableProperties.BASE_FILE_FORMAT_DEFAULT).toUpperCase(Locale.ENGLISH)));
    long fileSizeBytes = PropertyUtil.propertyAsLong(table.properties(), TableProperties.WRITE_TARGET_FILE_SIZE_BYTES,
        TableProperties.WRITE_TARGET_FILE_SIZE_BYTES_DEFAULT);
    long mask = PropertyUtil.propertyAsLong(table.properties(), TableProperties.BASE_FILE_INDEX_HASH_BUCKET,
        TableProperties.BASE_FILE_INDEX_HASH_BUCKET_DEFAULT) - 1;

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

    OutputFileFactory outputFileFactory = locationKind == HiveLocationKind.INSTANT ?
        new AdaptHiveOutputFileFactory(((SupportHive) table).hiveLocation(), table.spec(), fileFormat, table.io(),
            encryptionManager, partitionId, taskId, transactionId) :
        new CommonOutputFileFactory(baseLocation, table.spec(), fileFormat, table.io(),
            encryptionManager, partitionId, taskId, transactionId);
    FileAppenderFactory<InternalRow> appenderFactory = HiveTableUtil.isHive(table) ?
        ArcticSparkInternalRowAppenderFactory.builderFor(icebergTable, schema, dsSchema)
            .writeHive(true)
            .build() :
        ArcticSparkInternalRowAppenderFactory.builderFor(icebergTable, schema, dsSchema)
            .writeHive(false)
            .build();
    new GenericAppenderFactory(schema, table.spec());
    return new ArcticSparkBaseTaskWriter(fileFormat, appenderFactory,
        outputFileFactory,
        table.io(), fileSizeBytes, mask, schema, table.spec(), primaryKeySpec);
  }

  private TaskWriter<InternalRow> buildChangeWriter() {
    throw new UnsupportedOperationException("Spark UnSupport change write");
  }

  public static ArcticSparkTaskWriterBuilder buildFor(ArcticTable table) {
    return new ArcticSparkTaskWriterBuilder(table);
  }
}
