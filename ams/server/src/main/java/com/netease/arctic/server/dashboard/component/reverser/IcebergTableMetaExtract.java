/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.dashboard.component.reverser;

import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableMetadataParser;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.TypeUtil;
import org.apache.iceberg.types.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TableMetaExtract for iceberg table.
 */
public class IcebergTableMetaExtract implements TableMetaExtract<Table> {

  @Override
  public List<InternalTableMeta> extractTable(Table table) {
    List<TableMetadata.MetadataLogEntry> metadataLogEntries =
        ((HasTableOperations) table).operations().current().previousFiles();

    List<TableMetadata> metadataList = metadataLogEntries.stream()
        .map(entry -> TableMetadataParser.read(table.io(), entry.file()))
        .collect(Collectors.toList());

    metadataList.add(((HasTableOperations) table).operations().current());

    return metadataList.stream()
        .map(metadata -> new InternalTableMeta(
            metadata.lastUpdatedMillis(),
            transform(metadata.schema()),
            metadata.properties()))
        .collect(Collectors.toList());
  }

  private List<InternalSchema> transform(Schema schema) {
    return transform(null, new ArrayList<>(), schema.asStruct());
  }

  private List<InternalSchema> transform(Integer parent, List<String> parentName, Type type) {
    List<InternalSchema> result = new ArrayList<>();
    if (type.isStructType()) {
      for (Types.NestedField field : type.asStructType().fields()) {
        List<String> name = Lists.newArrayList(parentName);
        name.add(field.name());
        Type fieldType = field.type();
        result.add(
            new InternalSchema(
                field.fieldId(),
                parent,
                formatName(name),
                dateTypeToSparkString(fieldType),
                field.doc(),
                field.isRequired()
            ));
        result.addAll(transform(field.fieldId(), name, field.type()));
      }
    } else if (type.isListType()) {
      List<String> name = Lists.newArrayList(parentName);
      name.add("element");
      Type elementType = type.asListType().elementType();
      result.add(
          new InternalSchema(
              type.asListType().elementId(),
              parent,
              formatName(name),
              dateTypeToSparkString(elementType),
              "",
              false
          )
      );
      result.addAll(transform(type.asListType().elementId(), name, type.asListType().elementType()));
    } else if (type.isMapType()) {
      List<String> keyName = Lists.newArrayList(parentName);
      keyName.add("key");
      Type keyType = type.asMapType().keyType();
      int keyId = type.asMapType().keyId();
      result.add(new InternalSchema(
          keyId,
          parent,
          formatName(keyName),
          dateTypeToSparkString(keyType),
          "",
          false
      ));
      result.addAll(transform(keyId, keyName, keyType));

      List<String> valueName = Lists.newArrayList(parentName);
      valueName.add("value");
      Type valueType = type.asMapType().valueType();
      int valueId = type.asMapType().valueId();
      result.add(new InternalSchema(
          valueId,
          parent,
          formatName(valueName),
          dateTypeToSparkString(valueType),
          "",
          false
      ));
      result.addAll(transform(valueId, valueName, valueType));
    }
    return result;
  }

  private String formatName(List<String> names) {
    return String.join(".", names);
  }

  private String dateTypeToSparkString(Type type) {
    return TypeUtil.visit(type, new IcebergTypeToSparkType()).catalogString();
  }
}
