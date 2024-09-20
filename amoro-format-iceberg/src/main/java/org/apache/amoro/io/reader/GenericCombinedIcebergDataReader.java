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

package org.apache.amoro.io.reader;

import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.optimizing.OptimizingDataReader;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.scan.CombinedIcebergScanTask;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.utils.map.StructLikeCollections;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.avro.Avro;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.avro.DataReader;
import org.apache.iceberg.data.orc.GenericOrcReader;
import org.apache.iceberg.data.parquet.GenericParquetReaders;
import org.apache.iceberg.encryption.EncryptedFiles;
import org.apache.iceberg.encryption.EncryptedInputFile;
import org.apache.iceberg.encryption.EncryptionManager;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.parquet.Parquet;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.TypeUtil;
import org.apache.iceberg.types.Types;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Read data by {@link CombinedIcebergScanTask} for optimizer of native iceberg. During the
 * execution of readData and readDeleteData, delete data is read only once, thereby improving
 * performance.
 */
public class GenericCombinedIcebergDataReader implements OptimizingDataReader {

  protected final Schema tableSchema;
  protected final String nameMapping;
  protected final boolean caseSensitive;
  protected final AuthenticatedFileIO fileIO;
  protected final EncryptionManager encryptionManager;
  protected final BiFunction<Type, Object, Object> convertConstant;
  protected final boolean reuseContainer;
  protected CombinedDeleteFilter<Record> deleteFilter;

  protected PartitionSpec spec;

  protected RewriteFilesInput input;

  public GenericCombinedIcebergDataReader(
      AuthenticatedFileIO fileIO,
      Schema tableSchema,
      PartitionSpec spec,
      EncryptionManager encryptionManager,
      String nameMapping,
      boolean caseSensitive,
      BiFunction<Type, Object, Object> convertConstant,
      boolean reuseContainer,
      StructLikeCollections structLikeCollections,
      RewriteFilesInput rewriteFilesInput) {
    this.tableSchema = tableSchema;
    this.spec = spec;
    this.encryptionManager = encryptionManager;
    this.nameMapping = nameMapping;
    this.caseSensitive = caseSensitive;
    this.fileIO = fileIO;
    this.convertConstant = convertConstant;
    this.reuseContainer = reuseContainer;
    this.input = rewriteFilesInput;
    this.deleteFilter =
        new GenericDeleteFilter(rewriteFilesInput, tableSchema, structLikeCollections);
  }

  @Override
  public CloseableIterable<Record> readData() {
    if (input.rewrittenDataFiles() == null) {
      return CloseableIterable.empty();
    }
    Schema requireSchema =
        fileProjection(
            tableSchema, tableSchema, deleteFilter.hasPosition(), deleteFilter.deleteIds());

    CloseableIterable<Record> concat =
        CloseableIterable.concat(
            CloseableIterable.transform(
                CloseableIterable.withNoopClose(
                    Arrays.stream(input.rewrittenDataFiles()).collect(Collectors.toList())),
                s -> openFile(s, spec, requireSchema)));

    StructForDelete<Record> structForDelete =
        new StructForDelete<>(requireSchema, deleteFilter.deleteIds());
    CloseableIterable<StructForDelete<Record>> structForDeleteCloseableIterable =
        CloseableIterable.transform(concat, structForDelete::wrap);

    CloseableIterable<Record> iterable =
        CloseableIterable.transform(
            deleteFilter.filter(structForDeleteCloseableIterable), StructForDelete::recover);
    return iterable;
  }

  @Override
  public CloseableIterable<Record> readDeletedData() {
    if (input.rePosDeletedDataFiles() == null) {
      return CloseableIterable.empty();
    }

    Schema schema =
        new Schema(
            MetadataColumns.FILE_PATH,
            MetadataColumns.ROW_POSITION,
            org.apache.amoro.table.MetadataColumns.TREE_NODE_FIELD);
    Schema requireSchema =
        fileProjection(tableSchema, schema, deleteFilter.hasPosition(), deleteFilter.deleteIds());

    CloseableIterable<Record> concat =
        CloseableIterable.concat(
            CloseableIterable.transform(
                CloseableIterable.withNoopClose(
                    Arrays.stream(input.rePosDeletedDataFiles()).collect(Collectors.toList())),
                s -> openFile(s, spec, requireSchema)));

    StructForDelete<Record> structForDelete =
        new StructForDelete<>(requireSchema, deleteFilter.deleteIds());
    CloseableIterable<StructForDelete<Record>> structForDeleteCloseableIterable =
        CloseableIterable.transform(concat, structForDelete::wrap);

    CloseableIterable<Record> iterable =
        CloseableIterable.transform(
            deleteFilter.filterNegate(structForDeleteCloseableIterable), StructForDelete::recover);
    return iterable;
  }

  public void close() {
    deleteFilter.close();
  }

  private CloseableIterable<Record> openFile(
      DataFile dataFile, PartitionSpec spec, Schema require) {
    Map<Integer, ?> idToConstant =
        DataReaderCommon.getIdToConstant(dataFile, require, spec, convertConstant);

    return openFile(dataFile, require, idToConstant);
  }

  private CloseableIterable<Record> openFile(
      DataFile dataFile, Schema fileProjection, Map<Integer, ?> idToConstant) {
    EncryptedInputFile encryptedInput =
        EncryptedFiles.encryptedInput(
            fileIO.newInputFile(dataFile.path().toString()), dataFile.keyMetadata());
    InputFile input = encryptionManager.decrypt(encryptedInput);

    switch (dataFile.format()) {
      case AVRO:
        Avro.ReadBuilder avro =
            Avro.read(input)
                .project(fileProjection)
                .createReaderFunc(
                    avroSchema -> DataReader.create(fileProjection, avroSchema, idToConstant));

        if (reuseContainer) {
          avro.reuseContainers();
        }

        return avro.build();

      case PARQUET:
        Parquet.ReadBuilder parquet =
            Parquet.read(input)
                .project(fileProjection)
                .createReaderFunc(
                    fileSchema ->
                        GenericParquetReaders.buildReader(
                            fileProjection, fileSchema, idToConstant));

        if (reuseContainer) {
          parquet.reuseContainers();
        }

        return parquet.build();

      case ORC:
        Schema projectionWithoutConstantAndMetadataFields =
            TypeUtil.selectNot(
                fileProjection,
                Sets.union(idToConstant.keySet(), MetadataColumns.metadataFieldIds()));
        org.apache.iceberg.orc.ORC.ReadBuilder orc =
            org.apache
                .iceberg
                .orc
                .ORC
                .read(input)
                .project(projectionWithoutConstantAndMetadataFields)
                .createReaderFunc(
                    fileSchema ->
                        GenericOrcReader.buildReader(fileProjection, fileSchema, idToConstant));
        return orc.build();

      default:
        throw new UnsupportedOperationException(
            String.format("Cannot read %s file: %s", dataFile.format().name(), dataFile.path()));
    }
  }

  private static Schema fileProjection(
      Schema tableSchema, Schema requestedSchema, boolean hasPosDelete, Set<Integer> eqDeleteIds) {
    if (!hasPosDelete && eqDeleteIds == null) {
      return requestedSchema;
    }
    List<Integer> requiredEqDeleteIds =
        TypeUtil.select(tableSchema, eqDeleteIds).columns().stream()
            .map(Types.NestedField::fieldId)
            .collect(Collectors.toList());
    Set<Integer> requiredIds = Sets.newLinkedHashSet();
    if (hasPosDelete) {
      requiredIds.add(MetadataColumns.FILE_PATH.fieldId());
      requiredIds.add(MetadataColumns.ROW_POSITION.fieldId());
    }

    requiredIds.addAll(requiredEqDeleteIds);
    requiredIds.add(org.apache.amoro.table.MetadataColumns.TRANSACTION_ID_FILED.fieldId());

    requiredIds.add(MetadataColumns.IS_DELETED.fieldId());

    Set<Integer> missingIds =
        Sets.newLinkedHashSet(
            Sets.difference(requiredIds, TypeUtil.getProjectedIds(requestedSchema)));

    if (missingIds.isEmpty()) {
      return requestedSchema;
    }

    List<Types.NestedField> columns = Lists.newArrayList(requestedSchema.columns());
    for (int fieldId : missingIds) {
      if (fieldId == MetadataColumns.ROW_POSITION.fieldId()
          || fieldId == MetadataColumns.IS_DELETED.fieldId()
          || fieldId == org.apache.amoro.table.MetadataColumns.TRANSACTION_ID_FILED.fieldId()
          || fieldId == MetadataColumns.FILE_PATH.fieldId()) {
        continue; // add _pos and _deleted at the end
      }

      Types.NestedField field = tableSchema.asStruct().field(fieldId);
      Preconditions.checkArgument(field != null, "Cannot find required field for ID %s", fieldId);

      columns.add(field);
    }

    if (missingIds.contains(MetadataColumns.FILE_PATH.fieldId())) {
      columns.add(MetadataColumns.FILE_PATH);
    }

    if (missingIds.contains(MetadataColumns.ROW_POSITION.fieldId())) {
      columns.add(MetadataColumns.ROW_POSITION);
    }

    if (missingIds.contains(
        org.apache.amoro.table.MetadataColumns.TRANSACTION_ID_FILED.fieldId())) {
      columns.add(org.apache.amoro.table.MetadataColumns.TRANSACTION_ID_FILED);
    }

    if (missingIds.contains(MetadataColumns.IS_DELETED.fieldId())) {
      columns.add(MetadataColumns.IS_DELETED);
    }

    return new Schema(columns);
  }

  @VisibleForTesting
  public CombinedDeleteFilter<Record> getDeleteFilter() {
    return deleteFilter;
  }

  protected class GenericDeleteFilter extends CombinedDeleteFilter<Record> {

    public GenericDeleteFilter(
        RewriteFilesInput rewriteFilesInput,
        Schema tableSchema,
        StructLikeCollections structLikeCollections) {
      super(rewriteFilesInput, tableSchema, structLikeCollections);
    }

    @Override
    protected InputFile getInputFile(ContentFile<?> contentFile) {
      EncryptedInputFile encryptedInput =
          EncryptedFiles.encryptedInput(
              fileIO.newInputFile(contentFile.path().toString()), contentFile.keyMetadata());
      return encryptionManager.decrypt(encryptedInput);
    }

    @Override
    protected AuthenticatedFileIO getFileIO() {
      return fileIO;
    }
  }
}
