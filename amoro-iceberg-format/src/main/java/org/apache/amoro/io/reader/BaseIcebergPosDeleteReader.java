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
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.iceberg.Accessor;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.orc.GenericOrcReader;
import org.apache.iceberg.data.parquet.GenericParquetReaders;
import org.apache.iceberg.encryption.EncryptedFiles;
import org.apache.iceberg.encryption.EncryptedInputFile;
import org.apache.iceberg.encryption.EncryptionManager;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.orc.ORC;
import org.apache.iceberg.parquet.Parquet;

import java.util.List;

/** Reader for positional delete files. */
public class BaseIcebergPosDeleteReader {

  private static final Schema POS_DELETE_SCHEMA =
      new Schema(MetadataColumns.DELETE_FILE_PATH, MetadataColumns.DELETE_FILE_POS);
  private static final Accessor<StructLike> FILENAME_ACCESSOR =
      POS_DELETE_SCHEMA.accessorForField(MetadataColumns.DELETE_FILE_PATH.fieldId());
  private static final Accessor<StructLike> POSITION_ACCESSOR =
      POS_DELETE_SCHEMA.accessorForField(MetadataColumns.DELETE_FILE_POS.fieldId());

  protected final AuthenticatedFileIO fileIO;
  protected final EncryptionManager encryptionManager;
  protected final List<DeleteFile> posDeleteFiles;

  public BaseIcebergPosDeleteReader(
      AuthenticatedFileIO fileIO,
      EncryptionManager encryptionManager,
      List<DeleteFile> posDeleteFiles) {
    this.fileIO = fileIO;
    this.encryptionManager = encryptionManager;
    this.posDeleteFiles = posDeleteFiles;
  }

  public CloseableIterable<Record> readDeletes() {
    List<CloseableIterable<Record>> deletes = Lists.transform(posDeleteFiles, this::readDelete);
    return CloseableIterable.concat(deletes);
  }

  public String readPath(Record record) {
    return (String) FILENAME_ACCESSOR.get(record);
  }

  public Long readPos(Record record) {
    return (Long) POSITION_ACCESSOR.get(record);
  }

  private CloseableIterable<Record> readDelete(DeleteFile deleteFile) {
    EncryptedInputFile encryptedInput =
        EncryptedFiles.encryptedInput(
            fileIO.newInputFile(deleteFile.path().toString()), deleteFile.keyMetadata());
    InputFile input = encryptionManager.decrypt(encryptedInput);

    switch (deleteFile.format()) {
      case PARQUET:
        Parquet.ReadBuilder builder =
            Parquet.read(input)
                .project(POS_DELETE_SCHEMA)
                .reuseContainers()
                .createReaderFunc(
                    fileSchema -> GenericParquetReaders.buildReader(POS_DELETE_SCHEMA, fileSchema));

        return fileIO.doAs(builder::build);
      case ORC:
        return ORC.read(input)
            .project(POS_DELETE_SCHEMA)
            .createReaderFunc(
                fileSchema -> GenericOrcReader.buildReader(POS_DELETE_SCHEMA, fileSchema))
            .build();
      default:
        throw new UnsupportedOperationException(
            String.format(
                "Cannot read deletes, %s is not a supported format: %s",
                deleteFile.format().name(), deleteFile.path()));
    }
  }
}
