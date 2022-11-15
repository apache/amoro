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

package com.netease.arctic.utils;

import com.netease.arctic.ams.api.PartitionFieldData;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.ams.api.properties.MetaTableProperties;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.DefaultKeyedFile;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableIdentifier;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.PartitionField;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.types.Types;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Util class convert types to metastore api structs.
 */
public class ConvertStructUtil {

  /**
   * Convert {@link org.apache.iceberg.ContentFile iceberg file} to {@link com.netease.arctic.ams.api.DataFile}
   *
   * @param dataFile iceberg file
   * @param table arctic table file belong
   * @return ams file
   */
  public static com.netease.arctic.ams.api.DataFile convertToAmsDatafile(
      org.apache.iceberg.ContentFile<?> dataFile,
      ArcticTable table) {
    com.netease.arctic.ams.api.DataFile amsDataFile = new com.netease.arctic.ams.api.DataFile();
    amsDataFile.setFileSize(dataFile.fileSizeInBytes());
    amsDataFile.setPath(dataFile.path().toString());
    amsDataFile.setPartition(partitionFields(table.spec(), dataFile.partition()));
    amsDataFile.setSpecId(table.spec().specId());
    amsDataFile.setRecordCount(dataFile.recordCount());
    Map<Integer, ByteBuffer> upperBounds = dataFile.upperBounds();
    if (upperBounds != null) {
      Map<String, ByteBuffer> amsUpperBounds = new HashMap<>();
      upperBounds.forEach((fieldId, value) -> {
        Types.NestedField field = table.schema().findField(fieldId);
        if (field != null) {
          amsUpperBounds.put(field.name(), value);
        }
      });
      amsDataFile.setUpperBounds(amsUpperBounds);
    }


    /*
    Iceberg file has 3 types(FileContent) : DATA, POSITION_DELETES, EQUALITY_DELETES
    Arctic file has 4 types(DataFileType): BASE_FILE, INSERT_FILE, EQ_DELETE_FILE, POS_DELETE_FILE 
    i.  for iceberg DATA file, arctic keyed table has 3 file type: BASE_FILE, INSERT_FILE, EQ_DELETE_FILE;
        and arctic unkeyed table has 1 file type: BASE_FILE
    ii. for iceberg POSITION_DELETES file, arctic file type is POS_DELETE_FILE
    iii.for iceberg EQUALITY_DELETES file, arctic is unsupported now
     */
    FileContent content = dataFile.content();
    if (content == FileContent.DATA) {
      DefaultKeyedFile.FileMeta fileMeta = FileUtil.parseFileMetaFromFileName(dataFile.path().toString());
      validateArcticFileType(content, dataFile.path().toString(), fileMeta.type());
      amsDataFile.setFileType(fileMeta.type().name());
      amsDataFile.setIndex(fileMeta.node().index());
      amsDataFile.setMask(fileMeta.node().mask());
    } else if (content == FileContent.POSITION_DELETES) {
      DefaultKeyedFile.FileMeta fileMeta = FileUtil.parseFileMetaFromFileName(dataFile.path().toString());
      amsDataFile.setFileType(DataFileType.POS_DELETE_FILE.name());
      if (fileMeta.type() == DataFileType.POS_DELETE_FILE || fileMeta.type() == DataFileType.BASE_FILE) {
        amsDataFile.setIndex(fileMeta.node().index());
        amsDataFile.setMask(fileMeta.node().mask());
      } else {
        throw new IllegalArgumentException(
            "iceberg file content should not be POSITION_DELETES for " + dataFile.path().toString());
      }
    } else {
      throw new UnsupportedOperationException(
          "not support file content now: " + content + ", " + dataFile.path().toString());
    }
    return amsDataFile;
  }

  private static void validateArcticFileType(FileContent content, String path, DataFileType type) {
    switch (type) {
      case BASE_FILE:
      case INSERT_FILE:
      case EQ_DELETE_FILE:
        Preconditions.checkArgument(content == FileContent.DATA,
            "%s, File content should be POSITION_DELETES, but is %s", path, content);
        break;
      case POS_DELETE_FILE:
        Preconditions.checkArgument(content == FileContent.POSITION_DELETES,
            "%s, File content should be POSITION_DELETES, but is %s", path, content);
        break;
      default:
        throw new IllegalArgumentException("Unknown file type: " + type);
    }
  }

  public static List<PartitionFieldData> partitionFields(PartitionSpec partitionSpec, StructLike partitionData) {
    List<PartitionFieldData> partitionFields = Lists.newArrayListWithCapacity(partitionSpec.fields().size());
    Class<?>[] javaClasses = partitionSpec.javaClasses();
    for (int i = 0; i < javaClasses.length; i += 1) {
      PartitionField field = partitionSpec.fields().get(i);
      String valueString = field.transform().toHumanString(get(partitionData, i, javaClasses[i]));
      partitionFields.add(new PartitionFieldData(field.name(), valueString));
    }
    return partitionFields;
  }

  @SuppressWarnings("unchecked")
  private static <T> T get(StructLike data, int pos, Class<?> javaClass) {
    return data.get(pos, (Class<T>) javaClass);
  }

  public static TableMetaBuilder newTableMetaBuilder(TableIdentifier identifier, Schema schema) {
    return new TableMetaBuilder(identifier, schema);
  }

  public static class TableMetaBuilder {
    TableMeta meta = new TableMeta();
    Schema schema;
    Map<String, String> properties = new HashMap<>();
    Map<String, String> locations = new HashMap<>();

    public TableMetaBuilder(TableIdentifier identifier, org.apache.iceberg.Schema schema) {
      meta.setTableIdentifier(identifier.buildTableIdentifier());
      this.schema = schema;
    }

    public TableMetaBuilder withPrimaryKeySpec(PrimaryKeySpec keySpec) {
      if (keySpec == null) {
        return this;
      }
      com.netease.arctic.ams.api.PrimaryKeySpec primaryKeySpec =
          new com.netease.arctic.ams.api.PrimaryKeySpec();
      List<String> fields = keySpec.primaryKeyStruct().fields()
          .stream().map(Types.NestedField::name)
          .collect(Collectors.toList());
      primaryKeySpec.setFields(fields);
      meta.setKeySpec(primaryKeySpec);
      return this;
    }

    public TableMetaBuilder withTableLocation(String location) {
      locations.put(MetaTableProperties.LOCATION_KEY_TABLE, location);
      return this;
    }

    public TableMetaBuilder withBaseLocation(String baseLocation) {
      locations.put(MetaTableProperties.LOCATION_KEY_BASE, baseLocation);
      return this;
    }

    public TableMetaBuilder withChangeLocation(String changeLocation) {
      locations.put(MetaTableProperties.LOCATION_KEY_CHANGE, changeLocation);
      return this;
    }

    public TableMetaBuilder withProperties(Map<String, String> properties) {
      this.properties.putAll(properties);
      return this;
    }

    public TableMetaBuilder withProperty(String key, String value) {
      this.properties.put(key, value);
      return this;
    }

    public TableMeta build() {
      meta.setLocations(locations);
      meta.setProperties(this.properties);
      return meta;
    }
  }
}
