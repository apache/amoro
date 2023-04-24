/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.spark.test.helper;

import com.netease.arctic.hive.HiveTableProperties;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.utils.CollectionHelper;
import org.apache.hadoop.hive.metastore.TableType;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.SerDeInfo;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.types.Types;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestTableHelper {

  public static final List<FieldSchema> hiveSchema = new ArrayList<>();
  public static final List<FieldSchema> hivePartition = new ArrayList<>();

  static {
    hiveSchema.add(new FieldSchema("id", "int", null));
    hiveSchema.add(new FieldSchema("data", "string", "test comment"));
    hiveSchema.add(new FieldSchema("d", "double", null));
    hiveSchema.add(new FieldSchema("ts_long", "bigint", null));
    hiveSchema.add(new FieldSchema("ts", "timestamp", null));

    hivePartition.add(new FieldSchema("pt", "string", null));
  }

  public static final Types.NestedField id = Types.NestedField.optional(1, "id", Types.IntegerType.get());
  static final Types.NestedField data = Types.NestedField.optional(2, "data",
      Types.StringType.get(), "test comment");
  static final Types.NestedField d = Types.NestedField.optional(3, "d", Types.DoubleType.get());
  static final Types.NestedField ts_long = Types.NestedField.optional(4, "ts_long", Types.LongType.get());
  static final Types.NestedField tsWoZ = Types.NestedField.optional(8, "ts", Types.TimestampType.withoutZone());
  static final Types.NestedField tsWz = Types.NestedField.optional(8, "ts", Types.TimestampType.withZone());
  static final Types.NestedField pt = Types.NestedField.optional(20, "pt", Types.StringType.get());

  public static class IcebergSchema {
    public static final Schema NO_PK_NO_PT_WITHOUT_ZONE = new Schema(id, data, d, ts_long, tsWoZ);
    public static final Schema NO_PK_WITHOUT_ZONE = new Schema(id, data, d, ts_long, tsWoZ, pt);
    public static final Schema NO_PK_WITH_ZONE = new Schema(id, data, d, ts_long, tsWz, pt);
    public static final Schema PK_WITHOUT_ZONE = new Schema(id.asRequired(), data, ts_long, tsWoZ, pt.asRequired());
    public static final Schema PK_WITH_ZONE = new Schema(id.asRequired(), data, ts_long, tsWz, pt.asRequired());
  }

  public static class PtSpec {
    public static final PartitionSpec hive_pt_spec = PartitionSpec.builderFor(IcebergSchema.NO_PK_WITHOUT_ZONE)
        .identity("pt").build();
  }

  public static class PkSpec {
    public static final PrimaryKeySpec pk_include_pt = PrimaryKeySpec.builderFor(IcebergSchema.PK_WITH_ZONE)
        .addColumn("id")
        .addColumn("pt")
        .build();
  }

  public static class DataGen {
    public static final RecordGenerator.Builder builder = RecordGenerator.buildFor(IcebergSchema.PK_WITH_ZONE)
        .withSequencePrimaryKey(PkSpec.pk_include_pt);
  }

  public static HiveTableBuilder hiveTable(List<FieldSchema> hiveSchema, List<FieldSchema> hivePartition) {
    return new HiveTableBuilder(hiveSchema, hivePartition);
  }

  public static Row recordToRow(Record record) {
    Object[] values = new Object[record.size()];
    for (int i = 0; i < values.length; i++) {
      Object v = record.get(i);
      if (v instanceof LocalDateTime) {
        v = new Timestamp(((LocalDateTime) v).atOffset(ZoneOffset.UTC).toInstant().toEpochMilli());
      } else if (v instanceof OffsetDateTime) {
        v = new Timestamp(((OffsetDateTime) v).toInstant().toEpochMilli());
      }
      values[i] = v;
    }
    return RowFactory.create(values);
  }

  public static class HiveTableBuilder {
    Table table;

    public HiveTableBuilder(List<FieldSchema> hiveSchema, List<FieldSchema> hivePartition) {
      long currentTimeMillis = System.currentTimeMillis();
      this.table = new Table(
          "tbl_hive",
          "spark_test_db",
          null,
          (int) currentTimeMillis / 1000,
          (int) currentTimeMillis / 1000,
          Integer.MAX_VALUE,
          null,
          hivePartition,
          new HashMap<>(),
          null,
          null,
          TableType.EXTERNAL_TABLE.toString());
      StorageDescriptor storageDescriptor = new StorageDescriptor();
      storageDescriptor.setInputFormat(HiveTableProperties.PARQUET_INPUT_FORMAT);
      storageDescriptor.setOutputFormat(HiveTableProperties.PARQUET_OUTPUT_FORMAT);
      storageDescriptor.setCols(hiveSchema);
      SerDeInfo serDeInfo = new SerDeInfo();
      serDeInfo.setSerializationLib(HiveTableProperties.PARQUET_ROW_FORMAT_SERDE);
      storageDescriptor.setSerdeInfo(serDeInfo);
      this.table.setSd(storageDescriptor);
    }

    public HiveTableBuilder withProperties(String... properties) {
      this.table.setParameters(CollectionHelper.asMap(properties));
      return this;
    }

    public Table build() {
      return this.table;
    }
  }
}
