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

package org.apache.amoro.flink.write;

import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.types.logical.RowType;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.MetricsConfig;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.avro.Avro;
import org.apache.iceberg.deletes.EqualityDeleteWriter;
import org.apache.iceberg.deletes.PositionDeleteWriter;
import org.apache.iceberg.encryption.EncryptedOutputFile;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.flink.data.AdaptHiveFlinkParquetWriters;
import org.apache.iceberg.flink.data.FlinkAvroWriter;
import org.apache.iceberg.flink.data.FlinkOrcWriter;
import org.apache.iceberg.io.DataWriter;
import org.apache.iceberg.io.DeleteSchemaUtil;
import org.apache.iceberg.io.FileAppender;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.orc.ORC;
import org.apache.iceberg.parquet.AdaptHiveParquet;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.Map;

public class AdaptHiveFlinkAppenderFactory implements FileAppenderFactory<RowData>, Serializable {
  private final Schema schema;
  private final RowType flinkSchema;
  private final Map<String, String> props;
  private final PartitionSpec spec;
  private final int[] equalityFieldIds;
  private final Schema eqDeleteRowSchema;
  private final Schema posDeleteRowSchema;

  private RowType eqDeleteFlinkSchema = null;
  private RowType posDeleteFlinkSchema = null;

  public AdaptHiveFlinkAppenderFactory(
      Schema schema, RowType flinkSchema, Map<String, String> props, PartitionSpec spec) {
    this(schema, flinkSchema, props, spec, null, null, null);
  }

  public AdaptHiveFlinkAppenderFactory(
      Schema schema,
      RowType flinkSchema,
      Map<String, String> props,
      PartitionSpec spec,
      int[] equalityFieldIds,
      Schema eqDeleteRowSchema,
      Schema posDeleteRowSchema) {
    this.schema = schema;
    this.flinkSchema = flinkSchema;
    this.props = props;
    this.spec = spec;
    this.equalityFieldIds = equalityFieldIds;
    this.eqDeleteRowSchema = eqDeleteRowSchema;
    this.posDeleteRowSchema = posDeleteRowSchema;
  }

  private RowType lazyEqDeleteFlinkSchema() {
    if (eqDeleteFlinkSchema == null) {
      Preconditions.checkNotNull(eqDeleteRowSchema, "Equality delete row schema shouldn't be null");
      this.eqDeleteFlinkSchema = FlinkSchemaUtil.convert(eqDeleteRowSchema);
    }
    return eqDeleteFlinkSchema;
  }

  private RowType lazyPosDeleteFlinkSchema() {
    if (posDeleteFlinkSchema == null) {
      Preconditions.checkNotNull(posDeleteRowSchema, "Pos-delete row schema shouldn't be null");
      this.posDeleteFlinkSchema = FlinkSchemaUtil.convert(posDeleteRowSchema);
    }
    return this.posDeleteFlinkSchema;
  }

  @Override
  public FileAppender<RowData> newAppender(OutputFile outputFile, FileFormat format) {
    MetricsConfig metricsConfig = MetricsConfig.fromProperties(props);
    try {
      switch (format) {
        case AVRO:
          return Avro.write(outputFile)
              .createWriterFunc(ignore -> new FlinkAvroWriter(flinkSchema))
              .setAll(props)
              .schema(schema)
              .metricsConfig(metricsConfig)
              .overwrite()
              .build();

        case ORC:
          return ORC.write(outputFile)
              .createWriterFunc(
                  (schema, typDesc) -> FlinkOrcWriter.buildWriter(flinkSchema, schema))
              .setAll(props)
              .metricsConfig(metricsConfig)
              .schema(schema)
              .overwrite()
              .build();

        case PARQUET:
          return AdaptHiveParquet.write(outputFile)
              .createWriterFunc(
                  msgType -> AdaptHiveFlinkParquetWriters.buildWriter(flinkSchema, msgType))
              .setAll(props)
              .metricsConfig(metricsConfig)
              .schema(schema)
              .overwrite()
              .build();

        default:
          throw new UnsupportedOperationException("Cannot write unknown file format: " + format);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public DataWriter<RowData> newDataWriter(
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
  public EqualityDeleteWriter<RowData> newEqDeleteWriter(
      EncryptedOutputFile outputFile, FileFormat format, StructLike partition) {
    Preconditions.checkState(
        equalityFieldIds != null && equalityFieldIds.length > 0,
        "Equality field ids shouldn't be null or empty when creating equality-delete writer");
    Preconditions.checkNotNull(
        eqDeleteRowSchema,
        "Equality delete row schema shouldn't be null when creating equality-delete writer");

    MetricsConfig metricsConfig = MetricsConfig.fromProperties(props);
    try {
      switch (format) {
        case AVRO:
          return Avro.writeDeletes(outputFile.encryptingOutputFile())
              .createWriterFunc(ignore -> new FlinkAvroWriter(lazyEqDeleteFlinkSchema()))
              .withPartition(partition)
              .overwrite()
              .setAll(props)
              .rowSchema(eqDeleteRowSchema)
              .withSpec(spec)
              .withKeyMetadata(outputFile.keyMetadata())
              .equalityFieldIds(equalityFieldIds)
              .buildEqualityWriter();

        case PARQUET:
          return AdaptHiveParquet.writeDeletes(outputFile.encryptingOutputFile())
              .createWriterFunc(
                  msgType ->
                      AdaptHiveFlinkParquetWriters.buildWriter(lazyEqDeleteFlinkSchema(), msgType))
              .withPartition(partition)
              .overwrite()
              .setAll(props)
              .metricsConfig(metricsConfig)
              .rowSchema(eqDeleteRowSchema)
              .withSpec(spec)
              .withKeyMetadata(outputFile.keyMetadata())
              .equalityFieldIds(equalityFieldIds)
              .buildEqualityWriter();

        case ORC:
          return ORC.writeDeletes(outputFile.encryptingOutputFile())
              .createWriterFunc(
                  (schema, typDesc) ->
                      FlinkOrcWriter.buildWriter(lazyEqDeleteFlinkSchema(), schema))
              .withPartition(partition)
              .overwrite()
              .setAll(props)
              .metricsConfig(metricsConfig)
              .rowSchema(eqDeleteRowSchema)
              .withSpec(spec)
              .withKeyMetadata(outputFile.keyMetadata())
              .equalityFieldIds(equalityFieldIds)
              .buildEqualityWriter();

        default:
          throw new UnsupportedOperationException(
              "Cannot write equality-deletes for unsupported file format: " + format);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public PositionDeleteWriter<RowData> newPosDeleteWriter(
      EncryptedOutputFile outputFile, FileFormat format, StructLike partition) {
    MetricsConfig metricsConfig = MetricsConfig.fromProperties(props);
    try {
      switch (format) {
        case AVRO:
          return Avro.writeDeletes(outputFile.encryptingOutputFile())
              .createWriterFunc(ignore -> new FlinkAvroWriter(lazyPosDeleteFlinkSchema()))
              .withPartition(partition)
              .overwrite()
              .setAll(props)
              .rowSchema(posDeleteRowSchema)
              .withSpec(spec)
              .withKeyMetadata(outputFile.keyMetadata())
              .buildPositionWriter();

        case PARQUET:
          RowType flinkPosDeleteSchema =
              FlinkSchemaUtil.convert(DeleteSchemaUtil.posDeleteSchema(posDeleteRowSchema));
          return AdaptHiveParquet.writeDeletes(outputFile.encryptingOutputFile())
              .createWriterFunc(
                  msgType ->
                      AdaptHiveFlinkParquetWriters.buildWriter(flinkPosDeleteSchema, msgType))
              .withPartition(partition)
              .overwrite()
              .setAll(props)
              .metricsConfig(metricsConfig)
              .rowSchema(posDeleteRowSchema)
              .withSpec(spec)
              .withKeyMetadata(outputFile.keyMetadata())
              .transformPaths(path -> StringData.fromString(path.toString()))
              .buildPositionWriter();

        case ORC:
          RowType orcPosDeleteSchema =
              FlinkSchemaUtil.convert(DeleteSchemaUtil.posDeleteSchema(posDeleteRowSchema));
          return ORC.writeDeletes(outputFile.encryptingOutputFile())
              .createWriterFunc(
                  (schema, typDesc) -> FlinkOrcWriter.buildWriter(orcPosDeleteSchema, schema))
              .withPartition(partition)
              .overwrite()
              .setAll(props)
              .metricsConfig(metricsConfig)
              .rowSchema(posDeleteRowSchema)
              .withSpec(spec)
              .withKeyMetadata(outputFile.keyMetadata())
              .buildPositionWriter();

        default:
          throw new UnsupportedOperationException(
              "Cannot write pos-deletes for unsupported file format: " + format);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
