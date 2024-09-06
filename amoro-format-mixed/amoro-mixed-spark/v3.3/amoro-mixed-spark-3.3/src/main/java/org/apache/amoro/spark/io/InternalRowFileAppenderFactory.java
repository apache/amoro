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

package org.apache.amoro.spark.io;

import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.MetricsConfig;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.avro.Avro;
import org.apache.iceberg.deletes.EqualityDeleteWriter;
import org.apache.iceberg.deletes.PositionDeleteWriter;
import org.apache.iceberg.encryption.EncryptedOutputFile;
import org.apache.iceberg.io.DataWriter;
import org.apache.iceberg.io.DeleteSchemaUtil;
import org.apache.iceberg.io.FileAppender;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.orc.ORC;
import org.apache.iceberg.parquet.AdaptHiveParquet;
import org.apache.iceberg.parquet.Parquet;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.iceberg.spark.data.AdaptHiveSparkParquetWriters;
import org.apache.iceberg.spark.data.SparkAvroWriter;
import org.apache.iceberg.spark.data.SparkOrcWriter;
import org.apache.iceberg.spark.data.SparkParquetWriters;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.unsafe.types.UTF8String;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * This InternalRowFileAppenderFactory file is forked from
 * org.apache.iceberg.spark.source.SparkAppenderFactory for SparkAppenderFactory is package-private
 * class
 */
public class InternalRowFileAppenderFactory implements FileAppenderFactory<InternalRow> {

  private final Map<String, String> properties;
  private final Schema writeSchema;
  private final StructType dsSchema;
  private final PartitionSpec spec;
  private final int[] equalityFieldIds;
  private final Schema eqDeleteRowSchema;
  private final Schema posDeleteRowSchema;

  private StructType eqDeleteSparkType = null;
  private StructType posDeleteSparkType = null;
  private final boolean writeHive;

  public static InternalRowFileAppenderFactory.Builder builderFor(
      Table table, Schema writeSchema, StructType dsSchema) {
    return new InternalRowFileAppenderFactory.Builder(table, writeSchema, dsSchema);
  }

  protected InternalRowFileAppenderFactory(
      Map<String, String> properties,
      Schema writeSchema,
      StructType dsSchema,
      PartitionSpec spec,
      int[] equalityFieldIds,
      Schema eqDeleteRowSchema,
      Schema posDeleteRowSchema,
      boolean writeHive) {
    this.properties = properties;
    this.writeSchema = writeSchema;
    this.dsSchema = dsSchema;
    this.spec = spec;
    this.equalityFieldIds = equalityFieldIds;
    this.eqDeleteRowSchema = eqDeleteRowSchema;
    this.posDeleteRowSchema = posDeleteRowSchema;
    this.writeHive = writeHive;
  }

  public static class Builder {
    private final Table table;
    private final Schema writeSchema;
    private final StructType dsSchema;
    private PartitionSpec spec;
    private int[] equalityFieldIds;
    private Schema eqDeleteRowSchema;
    private Schema posDeleteRowSchema;
    private boolean writeHive;

    Builder(Table table, Schema writeSchema, StructType dsSchema) {
      this.table = table;
      this.spec = table.spec();
      this.writeSchema = writeSchema;
      this.dsSchema = dsSchema;
    }

    public InternalRowFileAppenderFactory.Builder spec(PartitionSpec newSpec) {
      this.spec = newSpec;
      return this;
    }

    public InternalRowFileAppenderFactory.Builder equalityFieldIds(int[] newEqualityFieldIds) {
      this.equalityFieldIds = newEqualityFieldIds;
      return this;
    }

    public InternalRowFileAppenderFactory.Builder eqDeleteRowSchema(Schema newEqDeleteRowSchema) {
      this.eqDeleteRowSchema = newEqDeleteRowSchema;
      return this;
    }

    public InternalRowFileAppenderFactory.Builder posDelRowSchema(Schema newPosDelRowSchema) {
      this.posDeleteRowSchema = newPosDelRowSchema;
      return this;
    }

    public InternalRowFileAppenderFactory.Builder writeHive(boolean writeHive) {
      this.writeHive = writeHive;
      return this;
    }

    public InternalRowFileAppenderFactory build() {
      Preconditions.checkNotNull(table, "Table must not be null");
      Preconditions.checkNotNull(writeSchema, "Write Schema must not be null");
      Preconditions.checkNotNull(dsSchema, "DS Schema must not be null");
      if (equalityFieldIds != null) {
        Preconditions.checkNotNull(
            eqDeleteRowSchema,
            "Equality Field Ids and Equality Delete Row Schema" + " must be set together");
      }
      if (eqDeleteRowSchema != null) {
        Preconditions.checkNotNull(
            equalityFieldIds,
            "Equality Field Ids and Equality Delete Row Schema" + " must be set together");
      }

      return new InternalRowFileAppenderFactory(
          table.properties(),
          writeSchema,
          dsSchema,
          spec,
          equalityFieldIds,
          eqDeleteRowSchema,
          posDeleteRowSchema,
          writeHive);
    }
  }

  private StructType lazyEqDeleteSparkType() {
    if (eqDeleteSparkType == null) {
      Preconditions.checkNotNull(eqDeleteRowSchema, "Equality delete row schema shouldn't be null");
      this.eqDeleteSparkType = SparkSchemaUtil.convert(eqDeleteRowSchema);
    }
    return eqDeleteSparkType;
  }

  private StructType lazyPosDeleteSparkType() {
    if (posDeleteSparkType == null) {
      Preconditions.checkNotNull(
          posDeleteRowSchema, "Position delete row schema shouldn't be null");
      this.posDeleteSparkType = SparkSchemaUtil.convert(posDeleteRowSchema);
    }
    return posDeleteSparkType;
  }

  // todo control whether need adapt hive parquet
  @Override
  public FileAppender<InternalRow> newAppender(OutputFile file, FileFormat fileFormat) {
    MetricsConfig metricsConfig = MetricsConfig.fromProperties(properties);
    try {
      switch (fileFormat) {
        case PARQUET:
          if (writeHive) {
            return AdaptHiveParquet.write(file)
                .createWriterFunc(
                    msgType -> AdaptHiveSparkParquetWriters.buildWriter(dsSchema, msgType))
                .setAll(properties)
                .metricsConfig(metricsConfig)
                .schema(writeSchema)
                .overwrite()
                .build();
          } else {
            return Parquet.write(file)
                .createWriterFunc(msgType -> SparkParquetWriters.buildWriter(dsSchema, msgType))
                .setAll(properties)
                .metricsConfig(metricsConfig)
                .schema(writeSchema)
                .overwrite()
                .build();
          }

        case AVRO:
          return Avro.write(file)
              .createWriterFunc(ignored -> new SparkAvroWriter(dsSchema))
              .setAll(properties)
              .schema(writeSchema)
              .overwrite()
              .build();

        case ORC:
          return ORC.write(file)
              .createWriterFunc(SparkOrcWriter::new)
              .setAll(properties)
              .metricsConfig(metricsConfig)
              .schema(writeSchema)
              .overwrite()
              .build();

        default:
          throw new UnsupportedOperationException("Cannot write unknown format: " + fileFormat);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public DataWriter<InternalRow> newDataWriter(
      EncryptedOutputFile file, FileFormat format, StructLike partition) {
    return new DataWriter<>(
        newAppender(file.encryptingOutputFile(), format),
        format,
        file.encryptingOutputFile().location(),
        spec,
        partition,
        file.keyMetadata());
  }

  @Override
  public EqualityDeleteWriter<InternalRow> newEqDeleteWriter(
      EncryptedOutputFile file, FileFormat format, StructLike partition) {
    Preconditions.checkState(
        equalityFieldIds != null && equalityFieldIds.length > 0,
        "Equality field ids shouldn't be null or empty when creating equality-delete writer");
    Preconditions.checkNotNull(
        eqDeleteRowSchema,
        "Equality delete row schema shouldn't be null when creating equality-delete writer");

    try {
      switch (format) {
        case PARQUET:
          if (writeHive) {
            return AdaptHiveParquet.writeDeletes(file.encryptingOutputFile())
                .createWriterFunc(
                    msgType ->
                        AdaptHiveSparkParquetWriters.buildWriter(lazyEqDeleteSparkType(), msgType))
                .overwrite()
                .rowSchema(eqDeleteRowSchema)
                .withSpec(spec)
                .withPartition(partition)
                .equalityFieldIds(equalityFieldIds)
                .withKeyMetadata(file.keyMetadata())
                .buildEqualityWriter();
          } else {
            return Parquet.writeDeletes(file.encryptingOutputFile())
                .createWriterFunc(
                    msgType -> SparkParquetWriters.buildWriter(lazyEqDeleteSparkType(), msgType))
                .overwrite()
                .rowSchema(eqDeleteRowSchema)
                .withSpec(spec)
                .withPartition(partition)
                .equalityFieldIds(equalityFieldIds)
                .withKeyMetadata(file.keyMetadata())
                .buildEqualityWriter();
          }
        case AVRO:
          return Avro.writeDeletes(file.encryptingOutputFile())
              .createWriterFunc(ignored -> new SparkAvroWriter(lazyEqDeleteSparkType()))
              .overwrite()
              .rowSchema(eqDeleteRowSchema)
              .withSpec(spec)
              .withPartition(partition)
              .equalityFieldIds(equalityFieldIds)
              .withKeyMetadata(file.keyMetadata())
              .buildEqualityWriter();

        case ORC:
          return ORC.writeDeletes(file.encryptingOutputFile())
              .createWriterFunc(SparkOrcWriter::new)
              .overwrite()
              .rowSchema(eqDeleteRowSchema)
              .withSpec(spec)
              .withPartition(partition)
              .equalityFieldIds(equalityFieldIds)
              .withKeyMetadata(file.keyMetadata())
              .buildEqualityWriter();

        default:
          throw new UnsupportedOperationException(
              "Cannot write equality-deletes for unsupported file format: " + format);
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to create new equality delete writer", e);
    }
  }

  @Override
  public PositionDeleteWriter<InternalRow> newPosDeleteWriter(
      EncryptedOutputFile file, FileFormat format, StructLike partition) {
    try {
      switch (format) {
        case PARQUET:
          StructType sparkPosDeleteSchema =
              SparkSchemaUtil.convert(DeleteSchemaUtil.posDeleteSchema(posDeleteRowSchema));
          if (writeHive) {
            return AdaptHiveParquet.writeDeletes(file.encryptingOutputFile())
                .createWriterFunc(
                    msgType ->
                        AdaptHiveSparkParquetWriters.buildWriter(sparkPosDeleteSchema, msgType))
                .overwrite()
                .rowSchema(posDeleteRowSchema)
                .withSpec(spec)
                .withPartition(partition)
                .withKeyMetadata(file.keyMetadata())
                .transformPaths(path -> UTF8String.fromString(path.toString()))
                .buildPositionWriter();
          } else {
            return Parquet.writeDeletes(file.encryptingOutputFile())
                .createWriterFunc(
                    msgType -> SparkParquetWriters.buildWriter(sparkPosDeleteSchema, msgType))
                .overwrite()
                .rowSchema(posDeleteRowSchema)
                .withSpec(spec)
                .withPartition(partition)
                .withKeyMetadata(file.keyMetadata())
                .transformPaths(path -> UTF8String.fromString(path.toString()))
                .buildPositionWriter();
          }
        case AVRO:
          return Avro.writeDeletes(file.encryptingOutputFile())
              .createWriterFunc(ignored -> new SparkAvroWriter(lazyPosDeleteSparkType()))
              .overwrite()
              .rowSchema(posDeleteRowSchema)
              .withSpec(spec)
              .withPartition(partition)
              .withKeyMetadata(file.keyMetadata())
              .buildPositionWriter();

        case ORC:
          return ORC.writeDeletes(file.encryptingOutputFile())
              .createWriterFunc(SparkOrcWriter::new)
              .overwrite()
              .rowSchema(posDeleteRowSchema)
              .withSpec(spec)
              .withPartition(partition)
              .withKeyMetadata(file.keyMetadata())
              .buildPositionWriter();

        default:
          throw new UnsupportedOperationException(
              "Cannot write pos-deletes for unsupported file format: " + format);
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to create new equality delete writer", e);
    }
  }
}
