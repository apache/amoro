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

package com.netease.arctic.ams.server.utils;

import com.netease.arctic.ams.api.DataFileInfo;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.file.ContentFileWithSequence;
import com.netease.arctic.data.file.WrapFileWithSequenceNumberHelper;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.ManifestEntryFields;
import com.netease.arctic.utils.TableFileUtils;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.FileMetadata;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.MetadataTableType;
import org.apache.iceberg.MetadataTableUtils;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.IcebergGenerics;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Tools for handling the ContentFile which in Iceberg
 */
public class ContentFileUtil {
  private static final Logger LOG = LoggerFactory.getLogger(ContentFileUtil.class);

  public static ContentFileWithSequence<?> buildContentFile(DataFileInfo dataFileInfo,
                                                PartitionSpec partitionSpec,
                                                String fileFormat) {
    ContentFile<?> contentFile;
    if (DataFileType.POS_DELETE_FILE == DataFileType.valueOf(dataFileInfo.getType())) {
      if (partitionSpec.isUnpartitioned()) {
        contentFile = FileMetadata.deleteFileBuilder(partitionSpec)
            .ofPositionDeletes()
            .withPath(dataFileInfo.getPath())
            .withFormat(fileFormat)
            .withFileSizeInBytes(dataFileInfo.getSize())
            .withRecordCount(dataFileInfo.getRecordCount())
            .build();
      } else {
        contentFile = FileMetadata.deleteFileBuilder(partitionSpec)
            .ofPositionDeletes()
            .withPath(dataFileInfo.getPath())
            .withFormat(fileFormat)
            .withFileSizeInBytes(dataFileInfo.getSize())
            .withRecordCount(dataFileInfo.getRecordCount())
            .withPartitionPath(dataFileInfo.getPartition())
            .build();
      }
    } else {
      if (partitionSpec.isUnpartitioned()) {
        contentFile = DataFiles.builder(partitionSpec)
            .withPath(dataFileInfo.getPath())
            .withFormat(fileFormat)
            .withFileSizeInBytes(dataFileInfo.getSize())
            .withRecordCount(dataFileInfo.getRecordCount())
            .build();
      } else {
        contentFile = DataFiles.builder(partitionSpec)
            .withPath(dataFileInfo.getPath())
            .withFormat(fileFormat)
            .withFileSizeInBytes(dataFileInfo.getSize())
            .withRecordCount(dataFileInfo.getRecordCount())
            .withPartitionPath(dataFileInfo.getPartition())
            .build();
      }
    }

    return WrapFileWithSequenceNumberHelper.wrap(contentFile, dataFileInfo.sequence);
  }

  public static Set<String> getAllContentFilePath(UnkeyedTable innerTable) {
    Set<String> validFilesPath = new HashSet<>();

    Table manifestTable =
        MetadataTableUtils.createMetadataTableInstance(((HasTableOperations) innerTable).operations(),
            innerTable.name(), metadataTableName(innerTable.name(), MetadataTableType.ALL_ENTRIES),
            MetadataTableType.ALL_ENTRIES);
    try (CloseableIterable<Record> entries = IcebergGenerics.read(manifestTable).build()) {
      for (Record entry : entries) {
        ManifestEntryFields.Status status =
            ManifestEntryFields.Status.of((int) entry.get(ManifestEntryFields.STATUS.fieldId()));
        if (status == ManifestEntryFields.Status.ADDED || status == ManifestEntryFields.Status.EXISTING) {
          GenericRecord dataFile = (GenericRecord) entry.get(ManifestEntryFields.DATA_FILE_ID);
          String filePath = (String) dataFile.getField(DataFile.FILE_PATH.name());
          validFilesPath.add(TableFileUtils.getUriPath(filePath));
        }
      }
    } catch (IOException e) {
      LOG.error("close manifest file error", e);
    }

    return validFilesPath;
  }

  private static String metadataTableName(String tableName, MetadataTableType type) {
    return tableName + (tableName.contains("/") ? "#" : ".") + type;
  }
}
